package cakar.search

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cakar.search.databinding.ActivityProjectBinding

class ProjectActivity : Activity() {
    private val bin by lazy { ActivityProjectBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bin.root)
        val project = intent.getIntExtra("project", 0)
        bin.root.apply{
            settings.javaScriptEnabled = true
            webChromeClient = WebChromeClient()
            loadUrl("https://turbowarp.org/$project/embed?settings-button")
        }
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        bin.root.destroy()
        super.onDestroy()
    }
}