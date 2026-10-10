package cakar.search.ste

import android.app.FragmentManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.Menu
import android.view.MenuItem
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.annotation.RequiresApi
import cakar.search.MainActivity.Companion.handleMainMenu
import cakar.search.R

class Settings: PreferenceActivity() {



    val ng by lazy { Ngatur(this) }
    val onBackFrag = object : FragmentManager.OnBackStackChangedListener{
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val onBackInvokedCallback : OnBackInvokedCallback= {
            if(fragmentManager.backStackEntryCount >= 1){
                fragmentManager.popBackStack()
            }
            if((fragmentManager.backStackEntryCount == 0)){
                onBackPressed()
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback( onBackInvokedCallback)
            }
        }
        override fun onBackStackChanged() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if(
                    (fragmentManager.backStackEntryCount >= 1) or
                    !hasHeaders()
                    ){
                    onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackInvokedCallback)
                }else{
                    onBackInvokedDispatcher.unregisterOnBackInvokedCallback( onBackInvokedCallback)
                }
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("opt new project preview")!!.setCheckable(true).setChecked(ng.getBoolean("optnpp", false)).setOnMenuItemClickListener {
            ng.edit().putBoolean("optnpp",it.isChecked.not()).apply()
            invalidateOptionsMenu()
            true
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if(actionBar != null){
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        fragmentManager.addOnBackStackChangedListener(onBackFrag)


        onBackFrag.onBackStackChanged()

    }

    override fun onHeaderClick(header: Header?, position: Int) {
        super.onHeaderClick(header, position)
        onBackFrag.onBackStackChanged()
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