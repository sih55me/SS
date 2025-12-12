package cakar.search

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cakar.search.adapter.Adapter
import cakar.search.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private val adap by lazy{ Adapter(this, arrayListOf()) }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.list.apply{
            val s = Point()
            windowManager.defaultDisplay.getSize(s)
            if(!resources.getBoolean(R.bool.tablet)){
                if(s.x > s.y) {
                    this.layoutManager = GridLayoutManager(context, 2)
                }else{
                    this.layoutManager = LinearLayoutManager(context)
                }
            }else{
                this.layoutManager = GridLayoutManager(context, if(s.x < s.y) 2 else 3)
            }
            adapter = adap
        }
        Toast.makeText(this, "Try search to show it!!", Toast.LENGTH_SHORT).show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                adap.flush()
                Search(this@MainActivity).searchProject(
                    query
                ) {
                    adap.setdata(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false

        })
        return super.onCreateOptionsMenu(menu)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        val s  = Point()
        windowManager.defaultDisplay.getSize(s)
        if(!resources.getBoolean(R.bool.tablet)){
            when(newConfig.orientation){
                Configuration.ORIENTATION_LANDSCAPE -> {
                    binding.list.layoutManager = GridLayoutManager(this, 2)
                }

                else -> {
                    binding.list.layoutManager = LinearLayoutManager(this)
                }
            }
        }else{
            binding.list.layoutManager = GridLayoutManager(this, if(s.x < s.y) 2 else 3)
        }

        super.onConfigurationChanged(newConfig)
    }
}