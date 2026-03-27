package cakar.search.ste.f

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import cakar.search.R

class ProjectSettings: PreferenceFragment() {

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(preferenceScreen == null){
            addPreferencesFromResource(R.xml.pref)
        }

    }


}