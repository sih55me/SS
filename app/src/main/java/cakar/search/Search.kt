package cakar.search

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.Array
import kotlin.Boolean
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.also
import kotlin.apply
import kotlin.arrayOf
import kotlin.synchronized

class Search(private val activity : Activity) {


    companion object{
        val modes = arrayOf("popular", "trending", "recent")
        val searchType = arrayOf("search","explore")
        val searchWhat = arrayOf("projects","studios")
    }

    private fun handleException(e: Exception){
        val p = mutableListOf<Any>()

        synchronized(p) {
            // Print our stack trace
            p.add(e)
            val trace: Array<StackTraceElement?> = e.stackTrace
            for (traceElement in trace) p.add("\tat $traceElement")
            p.addAll(e.suppressed.toList())
            p.add(e.cause?:p)
            AlertDialog.Builder(activity).setTitle("Something went wrong")
                .setMessage("Broken info \n\n Logs : \n${(e.message?:"??")}\n${p.joinToString(separator = "\n")}}").setPositiveButton(android.R.string.ok, null).show()
        }
        Log.e("E URL", e.message ?: "?")
        e.printStackTrace()
    }

    private fun Project.o(item: JSONObject): Project{
        return also {
            try{
                it.uninfo.apply {
                    put("created", item.getJSONObject("history").getString("created"))
                    put("modified", item.getJSONObject("history").getString("modified"))
                    put("shared", item.getJSONObject("history").getString("shared"))
                    put("views", item.getJSONObject("stats").getInt("views"))
                    put("loves", item.getJSONObject("stats").getInt("loves"))
                    put("favorites", item.getJSONObject("stats").getInt("favorites"))
                    put("remixes", item.getJSONObject("stats").getInt("remixes"))
                    put("scratchteam", item.getJSONObject("author").getBoolean("scratchteam"))
                    put("visibility", item.getString("visibility"))
                    put("public", item.getBoolean("public"))
                    put("posted", item.getBoolean("is_published"))
                    val rp = item.getJSONObject("remix").getString("parent").toString()
                    if((rp != "null")){
                        put("remix@p", rp)
                    }
                    val ro = item.getJSONObject("remix").getString("root")
                    if((ro != "null")){
                        put("remix@o", ro)
                    }
                    put("tkn", item.getString("project_token"))
                }
            }catch (_: Exception){
                //ignore
            }
        }
    }


    fun makeU(item: JSONObject): User {
        val o = User(
            item.getInt("id"),
            item.getString("username"),
            item.getJSONObject("profile").getString("bio"),
            item.getJSONObject("profile").getString("status"),
            item.getJSONObject("profile").getJSONObject("images").getString("90x90"),
            item.getJSONObject("profile").getString("country"),
        )
        try{
            o.uninfo.apply {
                put("created", item.getJSONObject("history").getString("joined"))
                put("scratchteam", item.getBoolean("scratchteam").toString())
            }
        }catch (_: Exception){
            //ignore
        }
        return o
    }

    private fun makinP(item: JSONObject, fromUsr: String=""): Project{
        return Project(
            item.getInt("id"),
            item.getString("title"),
            item.getString("description"),
            item.getString("instructions"),
            try{
                item.getJSONObject("author").getString("username")
               }catch (_: Exception){
                   fromUsr
               },
            item.getString("image"),
        )
    }
    fun searchProject(
        query:String = "",
        mode:Int = 0,
        stype:Int = 0,
        offset:Int = 0,
        onGet : ((Project) -> Unit)={}
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        if(mode !in 0..2){
            throw IllegalArgumentException("Mode must be between 0 and 2")
        }
        if(stype !in 0..1){
            throw IllegalArgumentException("Stype must be between 0 and 1")
        }

        val urlT = "https://api.scratch.mit.edu/${searchType[stype]}/${Companion.searchWhat[0]}?q=$query&mode=${modes[mode]}&limit=20&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(makinP(item).o(item))
                }
            } catch (e: JSONException) {
                handleException(e)
            }

        }, {e->
            AlertDialog.Builder(activity).setTitle("Something went wrong")
                .setMessage(e.message).setPositiveButton(android.R.string.ok, null).show()
        })
        requestQueue.add(stringRequest)
    }

    fun fetchFollowersFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((User) -> Unit)={}
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://api.scratch.mit.edu/users/$user/followers?limit=20&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(makeU(item))
                }
            } catch (e: JSONException) {
                handleException(e)
                e.printStackTrace()
            }

        }, {e->
            handleException(e)
        })
        requestQueue.add(stringRequest)
    }

    fun fetchFollowingFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((User) -> Unit)={}
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://api.scratch.mit.edu/users/$user/following?limit=20&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(makeU(item))
                }
            } catch (e: JSONException) {
                handleException(e)
                e.printStackTrace()
            }

        }, {e->
            handleException(e)
        })
        requestQueue.add(stringRequest)
    }

    fun searchProjectFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((Project) -> Unit)={}
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://api.scratch.mit.edu/users/$user/projects?limit=20&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(makinP(item, user).o(item))
                }
            } catch (e: JSONException) {
                handleException(e)
                e.printStackTrace()
            }

        }, {e->
            handleException(e)
        })
        requestQueue.add(stringRequest)
    }
    fun searchFavProjectFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((Project) -> Unit)={}
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://api.scratch.mit.edu/users/$user/favorites?limit=20&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(makinP(item, user).o(item))
                }
            } catch (e: JSONException) {
                handleException(e)
                Log.e("E URL", e.message ?: "?")

            }

        }, {e->
            handleException(e)
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
                val item = JSONObject(it)
                val o = makinP(item)
                o.o(item)
                onGet.invoke(o)
            } catch (e: JSONException) {
                handleException(e)
            }

        }, {e->
            val w = StringWriter()
            val s = PrintWriter(w)
            e.printStackTrace(s)
            AlertDialog.Builder(activity).setTitle("Something went wrong")
                .setMessage("Looks like the project ${id} is deleted or not posted by author.\n\nLogs : ${w.toString()}").setPositiveButton(android.R.string.ok, null).show()
        })
        requestQueue.add(stringRequest)
    }

    fun getUser(
        id:String,
        onGet : ((User) -> Unit)
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://api.scratch.mit.edu/users/$id"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                val item = JSONObject(it)
                val o = makeU(item)
                onGet.invoke(o)
            } catch (e: JSONException) {
                handleException(e)
                Log.e("E URL", e.message ?: "?")

            }

        }, {e->
            handleException(e)
        })
        requestQueue.add(stringRequest)
    }
}