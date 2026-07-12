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
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import android.window.OnBackInvokedCallback
import cakar.search.MainActivity.Companion.handleMainMenu
import cakar.search.MainActivity.SearcT
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


    override fun onBackPressed() {
        super.onBackPressed()
    }
    val st by lazy {
        SearcT(this)
    }


    private var skipTo
        set(value) {
            st.skipTo = value
        }
        get() = st.skipTo






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        query = intent.getStringExtra("query").orEmpty()
        val suggestions = SearchRecentSuggestions(
            this,
            SearchProvider.AUTHORITY, SearchProvider.MODE
        )
        suggestions.saveRecentQuery(query, null)
        l()
        val se = Search(this@Result)
        se.token = intent.getStringExtra("tkn")?:""
        val lolod = object: ActionMode.Callback{
            val p = ProgressBar(this@Result, null, 0, android.R.style.Widget_Holo_ProgressBar_Horizontal)
            override fun onCreateActionMode(
                p0: ActionMode?,
                p1: Menu?
            ): Boolean {
                p.isIndeterminate = true
                if(p.parent ==null){
                    p0?.customView = p
                }

                return true
            }

            override fun onPrepareActionMode(
                p0: ActionMode?,
                p1: Menu?
            )=false

            override fun onActionItemClicked(
                p0: ActionMode?,
                p1: MenuItem?
            )=false

            override fun onDestroyActionMode(p0: ActionMode?) {
                se?.cancelAll()
            }

        }

        val lolero = object: ActionMode.Callback{

            var recr = false

            override fun onCreateActionMode(
                p0: ActionMode?,
                p1: Menu?
            ): Boolean {
                p0?.setTitle("There's a error!")
                p0?.subtitle = "Check your connection and try again"
                p1?.add("Retry")?.setOnMenuItemClickListener {
                    recreate()
                    true
                }
                window?.decorView?.postDelayed({p0?.finish()},1000L)
                return true
            }

            override fun onPrepareActionMode(
                p0: ActionMode?,
                p1: Menu?
            )=false

            override fun onActionItemClicked(
                p0: ActionMode?,
                p1: MenuItem?
            )=false

            override fun onDestroyActionMode(p0: ActionMode?) {

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
        se.onError = {
            startActionMode(lolero)
        }
        skipTo = intent.getIntExtra("skipTo", 0) + se.limitGet
        val mode = intent.getIntExtra("searchBy", 0)
        val isFindTag = query.contains("#")
        fun pP(){
            binding.tabpro.adapter = padap
            if (padap.data.isEmpty()) {
                padap.data.clear()
                val e = startActionMode(lolod)
                val k:(Project)->Unit ={
                    e?.finish()
                    padap.setdata(it)
                }
                if(isFindTag){
                    se.searchProject(
                        query, offset = skipTo - se.limitGet, stype = 1, mode = mode, onGet = k
                    )
                }else{
                    se.searchProject(
                        query, offset = skipTo - se.limitGet, stype = 0, mode=mode,onGet = k
                    )
                }
            }
        }



        fun sP(){
            binding.tabstu.adapter = sadap
            if (sadap.data.isEmpty()) {
                sadap.data.clear()
                val e = startActionMode(lolod)
                val k:(Studia)->Unit ={
                    e?.finish()
                    sadap.setdata(it)
                }
                if(isFindTag){

                    se.searchStudia(
                        query, offset = skipTo - se.limitGet, stype = 1,mode=mode, onGet = k
                    )
                }else {
                    se.searchStudia(
                        query, offset = skipTo - se.limitGet, stype = 0,mode=mode, onGet = k
                    )
                }
            }
        }
        val na = savedInstanceState?.getInt("nav",0)?:0
        if(actionBar != null){
            if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                if(isFindTag){
                    actionBar?.setTitle(query)
                }else{
                    actionBar?.setTitle("Result of..")
                    actionBar?.setSubtitle(query)
                }
            }else{
                actionBar?.setTitle(null)
                actionBar?.setSubtitle(null)
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
        menu.addSubMenu("View").add(0, 'm'.code,0,"Mini mode").setCheckable(true).setChecked(typeList == -1)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem('m'.code)?.setChecked(typeList == -1)
        menu?.setGroupVisible(R.id.needUser, intent.getStringExtra("tkn").orEmpty().isNotEmpty())
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        handleMainMenu(item)
        if(item.itemId == R.id.app_bar_search){
            st.setQueryText(query)
            st.show()
        }
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
        if(item.itemId == R.id.dm) {
            showDialog(
                0x01,
                intent.getBundleExtra("login")
            )
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