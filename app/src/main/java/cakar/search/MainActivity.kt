package cakar.search


import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import android.window.OnBackInvokedCallback
import cakar.search.adapter.Adapter
import cakar.search.databinding.ActivityMainBinding
import cakar.search.filetype.Project
import cakar.search.ste.Settings


class MainActivity : Activity() {

    private val adap by lazy{ Adapter(this, arrayListOf()) }
    private lateinit var binding: ActivityMainBinding
    val sm by lazy { getSystemService(Context.SEARCH_SERVICE) as SearchManager }

    protected var q = ""

    private var m = 0
    private var t = 0

    private var skipTo = 0

    var preque = ""

    /**OnbackList
     *
     *
     * N:onbackcallback as any for prevent older api issue
     *
     */
    var onBacks: MutableMap<String,Any> = mutableMapOf()



    companion object{

        fun Activity.handleMainMenu(item: MenuItem){
            when(item.itemId){
                R.id.set ->{
                    startActivity(Intent(this, Settings::class.java))
                }
                R.id.emp -> {
                    val id = EditText(this)
                    id.hint = "Enter project id"
                    id.maxLines = 1
                    id.inputType = EditorInfo.TYPE_CLASS_NUMBER
                    AlertDialog.Builder(this).apply {
                        setTitle("Enter project id manually")
                        setView(id)
                        setPositiveButton("Go") { _, _ ->
                            try{

                                InfoProDialog().apply {
                                    arguments = Bundle().also {
                                        it.putInt("id", Integer.valueOf(id.text.toString()))
                                    }
                                }.show(fragmentManager, "ipd")
                            }catch (_:Exception){
                                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            }
                        }
                        setNegativeButton(android.R.string.cancel, null)
                    }.show()
                }
                R.id.emu -> {
                    val id = EditText(this)
                    id.hint = "Enter username"
                    id.maxLines = 1
                    id.isSingleLine = true
                    AlertDialog.Builder(this).apply {
                        setTitle("Enter username to visit")
                        setView(id)
                        setPositiveButton("Visit") { _, _ ->
                            try{
                                val i = Intent(context, PP::class.java)
                                i.putExtra("user", id.text.toString())
                                context.startActivity(i)
                            }catch (_:Exception){
                                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            }
                        }
                        setNegativeButton(android.R.string.cancel, null)
                    }.show()
                }
            }
        }
    }



    fun e(newConfig: Configuration){
        val s  = Point()
        windowManager.defaultDisplay.getSize(s)
        if(!resources.getBoolean(R.bool.tablet)){
            when(newConfig.orientation){
                Configuration.ORIENTATION_LANDSCAPE -> {
                    binding.list.numColumns = 2
                }

                else -> {
                    binding.list.numColumns = 1
                }
            }
        }else{
            binding.list.numColumns = ( if(s.x < s.y) 2 else 3)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Rumah")
        val suggestions = SearchRecentSuggestions(
            this,
            SearchProvider.AUTHORITY, SearchProvider.MODE
        )
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
        e(resources.configuration)
        binding.list.apply{
            val s = Point()
            windowManager.defaultDisplay.getSize(s)
            if(!resources.getBoolean(R.bool.tablet)){
                if(s.x > s.y) {
                    this.numColumns = 2
                }else{
                    this.numColumns = 1
                }
            }else{
                this.numColumns = (if(s.x < s.y) 2 else 3)
            }
            adapter = adap
        }
        val se = Search(this@MainActivity)
        val e = ProgressDialog(this@MainActivity)
        se.onError = {
            if(e.isShowing){
                e.dismiss()
            }
        }
        binding.button.setOnClickListener {

        }
        if(savedInstanceState != null){
            val l = savedInstanceState.getParcelableArrayList<Project>("data")
            q = savedInstanceState.getString("q", "")
            if(l == null){
                Toast.makeText(this, "Try search to show it!!", Toast.LENGTH_SHORT).show()
            }
            l?.forEach {
                adap.setdata(it)
            }
        }else {
            Toast.makeText(this, "Try search to show it!!", Toast.LENGTH_SHORT).show()
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
                        val si = sm.getSearchableInfo(componentName)
                        it.setSearchableInfo(si)
                        it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String): Boolean {
                                fun d(){
                                    val i = Intent(this@MainActivity, Result::class.java)
                                    i.putExtra("q", query)
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

        fun d(m: Int){

        }
        handleMainMenu(item)
        when (item.title) {



        }
        Log.i("menu", "onOptionsItemSelected: ${item.order}")
        return super.onOptionsItemSelected(item)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        e(newConfig)

        super.onConfigurationChanged(newConfig)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("data", adap.data)
        outState.putString("q", q)

    }
}