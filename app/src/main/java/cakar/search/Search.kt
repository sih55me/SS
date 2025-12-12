package cakar.search

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Search(private val activity : Activity) {
    fun searchProject(
        query:String,
        onGet : ((Project) -> Unit)
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://api.scratch.mit.edu/explore/projects?q=$query&mode=trending&language=en"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(Project(
                        item.getInt("id"),
                        item.getString("title"),
                        item.getString("description"),
                        item.getString("instructions"),
                        item.getJSONObject("author").getString("username"),
                        item.getString("image"),
                    ))
                }
            } catch (e: JSONException) {
                AlertDialog.Builder(activity).setTitle("Something went wrong")
                    .setMessage(e.message).setPositiveButton(android.R.string.ok, null).show()
                Log.e("E URL", e.message ?: "?")
                e.printStackTrace()
            }

        }, {e->
            AlertDialog.Builder(activity).setTitle("Something went wrong")
                .setMessage(e.message).setPositiveButton(android.R.string.ok, null).show()
        })
        requestQueue.add(stringRequest)
    }
    fun getProject(
        id:Int,
        onGet : ((Project) -> Unit)
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://api.scratch.mit.edu/projects/$id"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(Project(
                        item.getInt("id"),
                        item.getString("title"),
                        item.getString("description"),
                        item.getString("instructions"),
                        item.getJSONObject("author").getString("username"),
                        item.getString("image"),
                    ))
                }
            } catch (e: JSONException) {
                AlertDialog.Builder(activity).setTitle("Something went wrong")
                    .setMessage(e.message).setPositiveButton(android.R.string.ok, null).show()
                Log.e("E URL", e.message ?: "?")
                e.printStackTrace()
            }

        }, {e->
            AlertDialog.Builder(activity).setTitle("Something went wrong")
                .setMessage(e.message).setPositiveButton(android.R.string.ok, null).show()
        })
        requestQueue.add(stringRequest)
    }
}