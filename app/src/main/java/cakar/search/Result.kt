package cakar.search

import android.app.ActionBar
import android.app.Activity
import android.app.FragmentTransaction
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import android.window.OnBackInvokedCallback
import cakar.search.MainActivity.Companion.handleMainMenu
import cakar.search.adapter.Adapter
import cakar.search.adapter.SAdapter
import cakar.search.databinding.ResultBinding
import cakar.search.filetype.Project
import cakar.search.filetype.Studia


class Result : Activity() {

    /**

    typelist

    0 = normal

    -1 = mini
     */
    private var typeList = 0

    private val padap by lazy{ Adapter(this, arrayListOf()) }
    private val sadap by lazy{ SAdapter(this, arrayListOf()) }
    private lateinit var binding: ResultBinding




    protected var q = ""

    private var m = 0
    private var t = 0

    private var skipTo = 0

    var s = 0

    var query = ""

    /**OnbackList
     *
     *
     * N:onbackcallback as any for prevent older api issue
     *
     */

    fun l(){

        listOf(binding.tabpro, binding.tabstu).forEach{
            it.apply {
                val s = Point()
                windowManager.defaultDisplay.getSize(s)
                if (!resources.getBoolean(R.bool.tablet)) {
                    if (s.x > s.y) {
                        this.numColumns = 2
                    } else {
                        this.numColumns = 1
                    }
                } else {
                    this.numColumns = (if (s.x < s.y) 2 else 3)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        query = intent.getStringExtra("q").orEmpty()
        if (Intent.ACTION_SEARCH == intent.getAction()) {
            query = intent.getStringExtra(SearchManager.QUERY)?:query
        }
        val suggestions = SearchRecentSuggestions(
            this,
            SearchProvider.AUTHORITY, SearchProvider.MODE
        )
        suggestions.saveRecentQuery(query, null)
        l()
        val se = Search(this@Result)
        val e = ProgressDialog(this@Result)
        se.onError = {
            if(e.isShowing){
                e.dismiss()
            }
        }
        if(savedInstanceState != null){
            padap.data.clear()
            sadap.data.clear()
            savedInstanceState.getParcelableArrayList<Project>("pdata")?.forEach {
                padap.setdata(it)
            }
            savedInstanceState.getParcelableArrayList<Studia>("sdata")?.forEach {
                sadap.setdata(it)
            }
        }
        e.apply{
            isIndeterminate =  true
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setMessage("Searching for $query")
            setOnDismissListener {
                se.cancelAll()
            }
            setCanceledOnTouchOutside(false)
            window!!.setDimAmount(0F)
        }
        skipTo = intent.getIntExtra("skipTo", 0)
        fun pP(){
            binding.tabpro.adapter = padap
            if (padap.data.isEmpty()) {
                padap.data.clear()
                e.show()
                se.searchProject(
                    query, offset = skipTo
                ) {
                    if (e.isShowing) {
                        e.dismiss()
                    }
                    padap.setdata(it)
                }
            }
        }

        fun sP(){
            binding.tabstu.adapter = sadap
            if (sadap.data.isEmpty()) {
                sadap.data.clear()
                e.show()
                se.searchStudia(
                    query, offset = skipTo
                ) {
                    if (e.isShowing) {
                        e.dismiss()
                    }
                    sadap.setdata(it)
                }
            }
        }
        val na = savedInstanceState?.getInt("nav",0)?:0
        if(actionBar != null){
            if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                actionBar?.setTitle("Result of ${query}")
            }else{
                actionBar?.setTitle(null)
            }
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.navigationMode = ActionBar.NAVIGATION_MODE_TABS
            actionBar?.apply {
                val o = object : ActionBar.TabListener{


                    override fun onTabSelected(
                        p0: ActionBar.Tab?,
                        p1: FragmentTransaction?
                    ) {
                        if(p0 == null)return
                        val it = p0.tag
                        if(it == "pro"){
                            binding.tabpro.visibility = View.VISIBLE
                            pP()
                        }else if(it == "stu"){
                            binding.tabstu.visibility = View.VISIBLE
                            sP()
                        }
                    }

                    override fun onTabUnselected(
                        p0: ActionBar.Tab?,
                        p1: FragmentTransaction?
                    ) {
                        if(p0 == null)return
                        val it = p0.tag
                        if(it == "pro"){
                            binding.tabpro.visibility = View.GONE

                        }else if(it == "stu"){
                            binding.tabstu.visibility = View.GONE
                        }
                    }

                    override fun onTabReselected(
                        p0: ActionBar.Tab?,
                        p1: FragmentTransaction?
                    ) {

                    }
                }
                addTab(newTab().setTag("pro").setText("Project").setTabListener(o))
                addTab(newTab().setTag("stu").setText("Studio").setTabListener(o))
                setSelectedNavigationItem(na)

            }
        }






    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu.add(0, 'm'.code,0,"Mini mode").setCheckable(true).setChecked(typeList == -1)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem('m'.code)?.setChecked(typeList == -1)
        menu?.findItem(R.id.app_bar_search)?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
            var onBack = Any()
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    try{
                        onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBack as OnBackInvokedCallback)
                    }catch (_: Exception){}
                }
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    try{
                        onBack = OnBackInvokedCallback{
                            item.collapseActionView()
                        }
                        onBackInvokedDispatcher.registerOnBackInvokedCallback(0,onBack as OnBackInvokedCallback)
                    }catch (_: Exception){}
                }
                item.actionView?.let {
                    if(it is SearchView){
                        Handler(mainLooper).postDelayed({
                            it.setQuery(query, false)
                        },600L)
                        val si = (getSystemService(SEARCH_SERVICE) as SearchManager).getSearchableInfo(componentName)
                        it.setSearchableInfo(si)
                        it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String): Boolean {
                                fun d(){
                                    val i = Intent(this@Result, Result::class.java)
                                    i.putExtra("q", query)
                                    if(this@Result.query == query){
                                        Toast.makeText(this@Result, "Same query, Next page", Toast.LENGTH_SHORT).show()
                                        i.putExtra("skipTo", skipTo + 20)
                                    }
                                    startActivity(i)
                                };d()
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean{
                                it.isSubmitButtonEnabled = !newText.isNullOrEmpty()
                                return true
                            }

                        })
                    }
                }
                return true
            }
        })
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        handleMainMenu(item)
        if(item.itemId == 'm'.code){
            if(typeList == 0){
                typeList = -1
            }else{
                typeList = 0
            }
            item.setChecked(typeList == -1)
            sadap.mini = typeList == -1
            padap.mini = typeList == -1
            setVisible(false)
            Runnable{
                binding.tabstu.adapter = sadap
                binding.tabpro.adapter = padap
                setVisible(true)
            }.let {
                Handler(mainLooper).postDelayed(it,100L)
            }
        }
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        l()

        super.onConfigurationChanged(newConfig)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("pdata", padap.data)
        outState.putParcelableArrayList("sdata", sadap.data)
        outState.putString("q", q)
        outState.putInt("nav", actionBar?.selectedNavigationIndex?:0)

    }
}