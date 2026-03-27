package cakar.search.ste;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

public class Ngatur {
    private Context konteks;

    private SharedPreferences mahaSetel;

    private String[] keys = {"pro_fulldef", "pro_width", "pro_height"};
    public Ngatur(Context konteks){
        this.konteks = konteks;
        mahaSetel = PreferenceManager.getDefaultSharedPreferences(konteks);
    }



    public boolean kill(){
        konteks = null;
        return konteks == null;
    }

    public boolean getBoolean(String s, boolean b) {
        return mahaSetel.getBoolean(s, b);
    }

    public float getFloat(String s, float v) {
        return mahaSetel.getFloat(s, v);
    }

    public long getLong(String s, long l) {
        return mahaSetel.getLong(s, l);
    }

    public int getInt(String s, int i) {
        return mahaSetel.getInt(s, i);
    }


    public Set<String> getStringSet(String s,  Set<String> set) {
        return mahaSetel.getStringSet(s, set);
    }


    public String getString(String s, String s1) {
        return mahaSetel.getString(s, s1);
    }

    public Map<String, ?> getAll() {
        return mahaSetel.getAll();
    }

    public String[] getKeys() {
        return keys;
    }

}
