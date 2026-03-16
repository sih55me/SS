package cakar.search

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import cakar.search.MainActivity.Companion.handleMainMenu

class Settings: PreferenceActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(preferenceScreen == null){
            addPreferencesFromResource(R.xml.pref)
        }

        if(actionBar != null){
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        handleMainMenu(item)
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}