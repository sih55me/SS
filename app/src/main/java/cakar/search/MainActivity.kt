package cakar.search

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cakar.search.adapter.Adapter
import cakar.search.databinding.ActivityMainBinding
import androidx.core.view.get

class MainActivity : Activity() {

    private val adap by lazy{ Adapter(this, arrayListOf()) }
    private lateinit var binding: ActivityMainBinding

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

    private var fltershow = false
        set(value) {
            field = value
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (value) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        onBacks["fl"] = object:OnBackAnimationCallback{
                            override fun onBackInvoked() {
                                field = false
                                binding.filterlay.animate().translationX(binding.filterlay.width.toFloat()).withEndAction {
                                    binding.filterlay.visibility = View.GONE
                                    binding.filterlay.apply{
                                        setTranslationX(0F)
                                        setTranslationY(0F)
                                    }
                                }
                                onBackInvokedDispatcher.unregisterOnBackInvokedCallback( onBacks["fl"] as OnBackInvokedCallback)
                            }

                            override fun onBackStarted(backEvent: BackEvent) {
                                binding.filterlay.translationX = 50F
                                binding.filterlay.translationY = 50F
                            }

                            override fun onBackCancelled() {
                                binding.filterlay.animate().translationX(0F).translationY(0F)
                                super.onBackCancelled()
                            }

                        }
                    }else{
                        onBacks["fl"] = OnBackInvokedCallback {
                            onBackInvokedDispatcher.unregisterOnBackInvokedCallback( onBacks["fl"] as OnBackInvokedCallback)
                            binding.filterlay.visibility = View.GONE
                            field = false
                        }
                    }
                    try{
                        onBackInvokedDispatcher.registerOnBackInvokedCallback(0, onBacks["fl"] as OnBackInvokedCallback)
                    }catch (_: Exception){

                    }
                } else{
                    try{
                        onBackInvokedDispatcher.unregisterOnBackInvokedCallback( onBacks["fl"] as OnBackInvokedCallback)
                    }catch (_: Exception){

                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.bd.apply{
            adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, Search.modes)
            onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    Toast.makeText(this@MainActivity, "Select Mode: "+Search.modes[p2], Toast.LENGTH_SHORT).show()
                    m = p2
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }
        }
        binding.searchBar.also{
            it.isSubmitButtonEnabled = true
            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    fun d(){
                        q = query
                        adap.flush()
                        Search(this@MainActivity).searchProject(
                            query,
                            mode = m,
                            stype = t,
                            offset = skipTo
                        ) {
                            adap.setdata(it)
                        }
                    }
                    if(q.isNotEmpty() and adap.data.isNotEmpty() and(query == q)){
                        AlertDialog.Builder(this@MainActivity).setMessage("Next page?").setPositiveButton("Yes"){_,_->
                            skipTo += 20
                            d()
                        }.setNeutralButton("Previous"){_,_->
                            if(skipTo > 0){
                                skipTo -= 20
                            }
                            d()
                        }.setNegativeButton(android.R.string.cancel, null).show()
                    }else{
                        skipTo = 0
                        d()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean = false

            })
        }
        binding.t2.apply{
            adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayOf("Normal", "Tag"))
            onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    Toast.makeText(this@MainActivity, "Select Mode: "+Search.searchType[p2], Toast.LENGTH_SHORT).show()
                    t = p2
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }
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


        binding.clsflt.setOnClickListener {
            binding.filterlay.visibility = View.GONE
            fltershow = false
        }



    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.filter)?.isChecked = fltershow
        return super.onPrepareOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        fun d(m: Int){

        }
        when(item.itemId){
            R.id.emp -> {
                val id = EditText(this)
                id.hint = "Enter project id"
                id.maxLines = 1
                id.inputType = EditorInfo.TYPE_CLASS_NUMBER
                AlertDialog.Builder(this).apply {
                    setTitle("Enter manualy")
                    setView(id)
                    setPositiveButton("Go") { _, _ ->
                        try{
                            val i = Intent(context, ProjectActivity::class.java)
                            i.putExtra("project", Integer.valueOf(id.text.toString()))
                            context.startActivity(i)
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
        when (item.title) {

            "Search Filter" ->{
                fltershow = !fltershow
                item.isChecked = fltershow
                if (fltershow) {
                    binding.filterlay.visibility = View.VISIBLE
                } else {
                    binding.filterlay.visibility = View.GONE
                }
            }

        }
        Log.i("menu", "onOptionsItemSelected: ${item.order}")
        return super.onOptionsItemSelected(item)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
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

        super.onConfigurationChanged(newConfig)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("data", adap.data)
        outState.putString("q", q)

    }
}