package cakar.search

import android.app.Activity
import android.util.Log
import android.widget.Toast
import cakar.search.filetype.Komentar
import cakar.search.filetype.Project
import cakar.search.filetype.Studia
import cakar.search.filetype.User
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.Array
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



    var token = ""

    companion object{
        val modes = arrayOf("popular", "trending", "recent")
        val searchType = arrayOf("search","explore")
        val searchWhat = arrayOf("projects","studios")
    }
    val requestQueue = Volley.newRequestQueue(activity.applicationContext)

    var resultF = ""
    var onError :((Double)->Unit) = {

    }

    var reason = ""
    
    var limitGet = 20
    
    fun cancelAll(){
        requestQueue.cancelAll("search")
    }
    

    private fun handleException(e: Exception, id: Double = 0.toDouble()){
        val p = mutableListOf<Any>()

        p.add(e)
        val trace: Array<StackTraceElement?> = e.stackTrace
        for (traceElement in trace) p.add("\tat $traceElement")
        p.addAll(e.suppressed.toList())
        p.add(e.cause?:p)
        reason = ("Broken info \n\n Logs : \n${(e.message?:"??")}\n${p.joinToString(separator = "\n")}}")
        Log.e("E URL", e.message ?: "?")
        e.printStackTrace()
        onError(id)
    }

    private fun Project.o(item: JSONObject): Project {
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
                }
            }catch (_: Exception){
                //ignore
            }
        }
    }
    private fun Studia.q(item: JSONObject): Studia {
        return also {
            try{
                it.uninfo.apply {
                    put("created", item.getJSONObject("history").getString("created"))
                    put("modified", item.getJSONObject("history").getString("modified"))
                    put("comments", item.getJSONObject("stats").getInt("views"))
                    put("loves", item.getJSONObject("stats").getInt("followers"))
                    put("managers", item.getJSONObject("stats").getInt("managers"))
                    put("projects", item.getJSONObject("stats").getInt("projects"))
                    put("visibility", item.getString("visibility"))
                    put("public", item.getBoolean("public"))
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

    private fun makinP(item: JSONObject, fromUsr: String=""): Project {
        return Project(
            item.getInt("id"),
            item.getString("title"),
            item.getString("description"),
            item.getString("instructions"),
            try {
                item.getJSONObject("author").getString("username")
            } catch (_: Exception) {
                fromUsr
            },
            item.getString("image"),
        )
    }

    private fun makinS(item: JSONObject, fromUsr: String=""): Studia {
        return Studia(
            item.getInt("id"),
            item.getString("title"),
            item.getString("description"),
            item.getString("host"),
            try {
                item.getString("host")
            } catch (_: Exception) {
                fromUsr
            },
            item.getString("image"),
        )
    }
    fun searchStudia(
        query:String = "",
        mode:Int = 0,
        stype:Int = 0,
        offset:Int = 0,
        onGet : ((Studia) -> Unit)={}
    ){
        if(mode !in 0..2){
            throw IllegalArgumentException("Mode must be between 0 and 2")
        }
        if(stype !in 0..1){
            throw IllegalArgumentException("Stype must be between 0 and 1")
        }

        val urlT = "https://api.scratch.mit.edu/${searchType[stype]}/${Companion.searchWhat[1]}?q=$query&mode=${modes[mode]}&limit=$limitGet&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    onError(2.0)
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(makinS(item).q(item))
                }
            } catch (e: JSONException) {
                handleException(e, 2.0)
            }

        }, {e->
            handleException(e, 2.0)
        })
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }

    fun searchProject(
        query:String = "",
        mode:Int = 0,
        stype:Int = 0,
        offset:Int = 0,
        onGet : ((Project) -> Unit)={}
    ){
        if(mode !in 0..2){
            throw IllegalArgumentException("Mode must be between 0 and 2")
        }
        if(stype !in 0..1){
            throw IllegalArgumentException("Stype must be between 0 and 1")
        }

        val urlT = "https://api.scratch.mit.edu/${searchType[stype]}/${Companion.searchWhat[0]}?q=$query&mode=${modes[mode]}&limit=$limitGet&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    onError(1.0)
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    onGet.invoke(makinP(item).o(item))
                }
            } catch (e: JSONException) {
                handleException(e,1.0)
            }

        }, {e->
            handleException(e,1.0)
        })
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }

    fun fetchFollowersFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((User) -> Unit)={}
    ){
        val urlT = "https://api.scratch.mit.edu/users/$user/followers?limit=$limitGet&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
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
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }

    fun fetchFollowingFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((User) -> Unit)={}
    ){

        val urlT = "https://api.scratch.mit.edu/users/$user/following?limit=$limitGet&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
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
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }

    fun searchProjectFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((Project) -> Unit)={}
    ){

        var urlT = "https://api.scratch.mit.edu/users/$user/projects?limit=$limitGet&offset=${offset}"
        if(token.isNotEmpty()){
            urlT = "$urlT&x-token=$token"
        }
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
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
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }
    fun searchFavProjectFromUser(
        user:String = "",
        offset:Int = 0,
        onGet : ((Project) -> Unit)={}
    ){

        val urlT = "https://api.scratch.mit.edu/users/$user/favorites?limit=$limitGet&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
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
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }
    fun getProject(
        id:Int,
        onGet : ((Project) -> Unit)
    ){

        var urlT = "https://api.scratch.mit.edu/projects/$id"
        if(token.isNotEmpty()){
            urlT = "$urlT?x-token=$token"
        }
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
            try {
                Log.i("I URL", it)
                val item = JSONObject(it)
                val o = makinP(item)
                o.o(item)
                o.uninfo["project_token"] = item.getString("project_token")
                onGet.invoke(o)
            } catch (e: JSONException) {
                e.printStackTrace()
                reason = ("Looks like the project ${id} is deleted or not posted by author.\n\nLogs : ${e.message}\n\nContent:\n$it" )
                onError(1.5)
            }

        }, {e->
            reason = ("Looks like the project ${id} is deleted or not posted by author.\n\nLogs : ${e.toString()}" )
            val w = StringWriter()
            val s = PrintWriter(w)
            e.printStackTrace(s)
            e.printStackTrace()
            onError(1.5)
        })
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }

    fun getUser(
        id:String,
        onGet : ((User) -> Unit)
    ){

        var urlT = "https://api.scratch.mit.edu/users/$id"
        if(token.isNotEmpty()){
            urlT = "$urlT?x-token=$token"
        }
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
            try {
                Log.i("I URL", it)
                val item = JSONObject(it)
                val o = makeU(item)
                print(item)
                onGet.invoke(o)
            } catch (e: JSONException) {
                handleException(e,-1.0)
                Log.e("E URL", e.message ?: "?")

            }

        }, {e->
            handleException(e,-1.0)
        })
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }


    fun fetchCommentFromProject(
        /**
         * First = username
         *
         * Second = project id
         */
        data:Pair<String,Int>,
        offset:Int = 0,
        onGet : ((Komentar) -> Unit)={}
    ){


        val urlT = "https://api.scratch.mit.edu/users/${data.first}/projects/${data.second}/comments?limit=$limitGet&offset=${offset}"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    onError(1.0)
                    return@StringRequest
                }
                val list = JSONArray(it)
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    val k = Komentar(
                        item.getInt("id"),
                        item.getJSONObject("author").getString("username"),
                        item.getJSONObject("author").getString("image"),
                        item.getString("content"),
                        Pair(item.getString("datetime_created"), item.getString("datetime_modified"))
                    )
                    onGet.invoke(k)
                }
            } catch (e: JSONException) {
                handleException(e,1.0)
            }

        }, {e->
            handleException(e,1.0)
        })
        requestQueue.add(stringRequest.also{it.tag = "search"})
    }


    fun getMessageFromUser(
        user: CharSequence,
        offset:Int = 0,
        onGet : ((JSONArray) -> Unit)={}
    ) {
        var urlT = "https://api.scratch.mit.edu/users/$user/messages?limit=$limitGet&offset=${offset}"
        if(token.isNotEmpty()){
            urlT = "$urlT&x-token=$token"
        }
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }
                onGet(JSONArray(it))

            } catch (e: JSONException) {
                handleException(e)
                e.printStackTrace()
            }

        }, {e->
            handleException(e)
        })
        requestQueue.add(stringRequest.also{it.tag = "search"})

    }



    fun whatHappenBro(
        user: String,
        onGet:(JSONArray) -> Unit={}
    ){
        var urlT = "https://api.scratch.mit.edu/users/$user/following/users/activity?limit=5"
        if(token.isNotEmpty()){
            urlT = "$urlT&x-token=$token"
        }
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            resultF = it
            try {
                Log.i("I URL", it)
                if((it == "{}") or (it == "[]")){
                    Toast.makeText(activity, "No results found", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }
                onGet(JSONArray(it))

            } catch (e: JSONException) {
                handleException(e)
                e.printStackTrace()
            }

        }, {e->
            handleException(e)
        })
        requestQueue.add(stringRequest.also{it.tag = "search"})
        "/users/<username>/following/users/activity"
    }
}