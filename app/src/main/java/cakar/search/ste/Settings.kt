package cakar.search.ste

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import cakar.search.MainActivity.Companion.handleMainMenu
import cakar.search.R

class Settings: PreferenceActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if(actionBar != null){
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }


    }

    override fun isValidFragment(fragmentName: String?): Boolean {
        return fragmentName?.contains("cakar") == true
    }


    override fun onBuildHeaders(target: List<Header?>?) {
        super.onBuildHeaders(target)
        loadHeadersFromResource(R.xml.prefhed, target)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId == android.R.id.home){
//            finish()
//        }
        return super.onOptionsItemSelected(item)
    }
}