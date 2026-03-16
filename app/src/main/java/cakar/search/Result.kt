package cakar.search

import android.app.ActionBar
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ArrayAdapter
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
            window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
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
                    if (e.isShowing and (sadap.data.isNotEmpty())) {
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
                    if (e.isShowing and(padap.data.isNotEmpty())) {
                        e.dismiss()
                    }
                    sadap.setdata(it)
                }
            }
        }

        sP()
        pP()
        if(actionBar != null){
            actionBar?.setTitle("Result of ${query}")
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding.tbh.apply {
            setup()
            addTab(newTabSpec("pro").setIndicator("Project").setContent(R.id.tabpro))
            addTab(newTabSpec("stu").setIndicator("Studio").setContent(R.id.tabstu))
            setOnTabChangedListener {
                if(it == "pro"){
                    pP()
                }else if(it == "stu"){
                    sP()
                }
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
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
                        it.isSubmitButtonEnabled = true
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

    }
}