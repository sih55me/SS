package cakar.search

import android.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.util.Linkify
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import cakar.search.databinding.AkunManagBinding
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.TimeUnit


class Akun: Fragment(){
    val b by lazy { AkunManagBinding.inflate(layoutInflater) }

    val con = Client.I
    
    var loadedT : Thread? = null

    fun getSecretSysId(id: String?, type: String?): Int {
        return getContext().getResources().getIdentifier(id, type, "android")
    }


    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return b.root
    }


    fun ERRORDOTL(
        onD:()-> Unit = {},
        onL:()-> Unit = {}
    )= view?.startActionMode(object: ActionMode.Callback{




            override fun onCreateActionMode(
                a: ActionMode?,
                p1: Menu?
            ): Boolean {
                a?.setTitle("Login failure!!")
                return true
            }

            override fun onPrepareActionMode(
                a: ActionMode?,
                p1: Menu?
            ): Boolean{
                return true
            }

            override fun onActionItemClicked(
                p0: ActionMode?,
                p1: MenuItem?
            )=false

            override fun onDestroyActionMode(p0: ActionMode?) {
                onD()
            }

        })



    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val flipper = b.pager
        val m = Markwon
            .builder(context)
            .usePlugin(CorePlugin.create())
            .usePlugin(TablePlugin.create(context)) // to render tables
            .usePlugin(TaskListPlugin.create(context)) // to render task lists
            .usePlugin(StrikethroughPlugin.create()) // to render strikethrough
            .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
            .build()

        fun loadUsr(){
            Thread{
                con.getSes().let {
                    if (it.has("user")) {
                        val u = it.getJSONObject("user")
                        val tabret = StringBuilder()
                        tabret.append("""
                            |||
                            | :--- | :---: |
                        """.trimIndent())
                        val k = u.keys()
                        var x = 0
                        for(i in k){
                            x=x+1
                            tabret.append("\n")
                            tabret.append("| **$x. $i** | ${u.get(i)} |")
                        }
                        b.root.post{
                            b.root.setTag(R.id.usr, it)
                            b.root.setTag(R.id.title, u.getString("username"))
                            m.setMarkdown(b.mankun.myusr, tabret.toString().trimIndent())
                            b.mankun.menu.menu.findItem(R.id.usr)?.setOnMenuItemClickListener {_->
                                val i = Intent(context, PP::class.java)
                                i.putExtra("user", b.root.getTag(R.id.title).toString())
                                i.putExtra("tkn",u.getString("token"))
                                context.startActivity(i)
                                true
                            }
                        }

                    }
                }
            }.start()
        }

        con.cookieJar.getCookie(
            "scratch.mit.edu",
            "scratchsessionsid"
        )?.value.let{s->
            if (!s.isNullOrEmpty()){
                flipper.displayedChild = 1
                loadUsr()
            }
        }

        val lolod = object: ActionMode.Callback{
            val p = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal)
            override fun onCreateActionMode(
                p0: ActionMode?,
                p1: Menu?
            ): Boolean {
                p.isIndeterminate = true
                if(p.parent ==null){
                    p0?.customView = p
                }
                p1?.add("Cancel")?.setOnMenuItemClickListener {
                    p0?.finish()
                    true
                }
                return true
            }

            override fun onPrepareActionMode(
                p0: ActionMode?,
                p1: Menu?
            )=false

            override fun onActionItemClicked(
                p0: ActionMode?,
                p1: MenuItem?
            )=false

            override fun onDestroyActionMode(p0: ActionMode?) {
                loadedT?.interrupt()
            }

        }
        b.loglay.login.setOnClickListener {itp->
            val o = itp.startActionMode(lolod)
            Toast.makeText(
                activity,
                "Login in process..",
                Toast.LENGTH_LONG
            ).show()
            itp.isEnabled = false
            loadedT = Thread{
                runBlocking(Dispatchers.IO) {
                    con.login(
                        b.loglay.usr.text.toString(),
                        b.loglay.ps.text.toString()
                    )
                }.let {
                    activity.runOnUiThread{
                        o.finish()
                        if (it is LogResult.Success) {
                            flipper.setInAnimation(getContext(), getSecretSysId("activity_open_enter", "anim"));
                            flipper.setOutAnimation(getContext(), getSecretSysId("activity_open_exit", "anim"));
                            Toast.makeText(
                                activity,
                                "Login success",
                                Toast.LENGTH_SHORT
                            ).show()
                            flipper.displayedChild = 1
                            loadUsr()
                        }else if (it is LogResult.Error){
                            view?.startActionMode(object: ActionMode.Callback{
                                var osa = {
                                    itp.isEnabled = true
                                }



                                override fun onCreateActionMode(
                                    a: ActionMode?,
                                    p1: Menu?
                                ): Boolean {
                                    a?.setTitle("Login failure!!")
                                    p1?.add("Retry")!!.setOnMenuItemClickListener {
                                        osa = {
                                            itp.callOnClick()
                                            Unit
                                        }.also { p->
                                            p()
                                        }
                                        true
                                    }
                                    p1?.add("Why?")!!.setOnMenuItemClickListener {_->
                                        Toast.makeText(
                                            activity,
                                            "${it.message}\n${it.code}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        true
                                    }
                                    return true
                                }

                                override fun onPrepareActionMode(
                                    a: ActionMode?,
                                    p1: Menu?
                                ): Boolean{
                                    return true
                                }

                                override fun onActionItemClicked(
                                    p0: ActionMode?,
                                    p1: MenuItem?
                                )=false

                                override fun onDestroyActionMode(p0: ActionMode?) {
                                    osa()
                                }

                            })

                        }
                    }
                }
            }
            loadedT?.start()
        }
        b.mankun.menu.menu.apply {
            add(0,R.id.close,0,"Logout").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                con.cookieJar.clear()
                flipper.setInAnimation(getContext(), getSecretSysId("activity_close_enter", "anim"));
                flipper.setOutAnimation(getContext(), getSecretSysId("activity_close_exit", "anim"));
                flipper.displayedChild = 0
                b.loglay.login.isEnabled = true
                Toast.makeText(
                    activity,
                    "Account deleted",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            add(0,R.id.usr,1,"Vist my public pp")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if(isRemoving){
            loadedT?.interrupt()
        }
    }




    sealed class LogResult {

        data object Success : LogResult()

        data class Error(
            val code: Int,
            val message: String
        ) : LogResult()

    }

    class Client(private val s : SharedPreferences): Closeable {
        init {
            if(ins ==null){
                ins = this
            }else{
                throw IllegalAccessError("only one instance are allow")
            }
        }


        val cookieJar = PersistentCookieJar(s)

        private val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(1, TimeUnit.HOURS)
            .writeTimeout(1, TimeUnit.HOURS)
            .readTimeout(1, TimeUnit.HOURS)
            .build()


        fun getSes(): JSONObject{
            val request = Request.Builder()
                .url("https://scratch.mit.edu/session/")
                .header("User-Agent", USER_AGENT)
                .header("X-Requested-With", "XMLHttpRequest")
                .build();
            try{
                client.newCall(request).execute().use { response ->
                    val s = StringBuilder(response.body?.string()).toString()
                    println("AHS\n" + s)
                    if (s.startsWith("{")) {
                        return JSONObject(s)
                    } else {
                        return JSONObject()
                    }
                }
            }catch (_: Exception){
                return JSONObject()
            }
        }
        fun login(
            username: String,
            password: String
        ): LogResult {

            obtainCsrf()

            val csrf =
                cookieJar.getCookie(
                    "scratch.mit.edu",
                    "scratchcsrftoken"
                )?.value
                    ?: return LogResult.Error(
                        -1,
                        "CSRF token not found"
                    )

            val json = JSONObject()
                .put("username", username)
                .put("password", password)
                .toString()

            val body =
                json.toRequestBody(
                    "application/json".toMediaType()
                )

            val request = Request.Builder()
                .url("https://scratch.mit.edu/login/")
                .post(body)
                .header("X-CSRFToken", csrf)
                .header("Referer", "https://scratch.mit.edu/")
                .header("Origin", "https://scratch.mit.edu")
                .header("User-Agent", USER_AGENT)
                .header(
                    "X-Requested-With",
                    "XMLHttpRequest"
                )
                .build()

            client.newCall(request)
                .execute()
                .use {
                    println("[AkunClient]CodeRes:"+it.code)
                    val s= StringBuilder(it.body?.string().orEmpty())
                    println("[AkunClient]ResBod:\n"+s)


                    return if (it.isSuccessful) {

                        LogResult.Success

                    } else {

                        LogResult.Error(
                            it.code,
                            s.toString()
                        )

                    }

                }

        }

        fun oldLogin(
            u: String, p: String
        ) {
            val cookieManager = cookieJar

            val client = OkHttpClient.Builder()
                .cookieJar(cookieManager)
                .build()



            fun runLogin(k: String){
                val JSON: MediaType = "application/json".toMediaType()

                val strl = JSONObject()
                try {
                    strl.put("useMessages", true)
                    strl.put("username", u)
                    strl.put("password", p)
                } catch (e: JSONException) {
                }
                val json = strl.toString()

                val body: RequestBody = json.toRequestBody(JSON)

                val request = Request.Builder()
                    .url("https://scratch.mit.edu/accounts/login/")
                    .post(body)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("X-CSRFToken", k)
                    .header("Referer", "https://scratch.mit.edu")
                    .header("Origin", "https://scratch.mit.edu")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Accept", "application/json")
                    .build()
                val ca = object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        println("Login return "+response.code)
                        fun t(t:String){
                            println("LSCO =\n\n $t")
                        }
                        val s = StringBuilder(response.body!!.string())
                        println(s)

                        when(response.code){
                            200->{
                                val headers = response.headers

                                for (cookie in headers.values("Set-Cookie")) {
                                    println(cookie)
                                }

                                var key = ""
                                if(s.startsWith("[{")){
                                    try{
                                        val logstr = JSONArray(s.toString()).getJSONObject(0)?: JSONObject()
                                        key = logstr.getString("token")
                                        t("Logined\n${key}")
                                    }catch (e: Exception){
                                        e.printStackTrace()
                                        t("Logined, but unvalue")
                                    }
                                }


                            }
                            401-> t("Tet tot")
                            403->t("Denial")
                            429->t("STOP")
                            else -> t("E:$response.code")
                        }
                    }
                }
                client.newCall(request).enqueue(ca)
            }
            Thread{
                val getRequest = Request.Builder()
                    .url("https://scratch.mit.edu/csrf_token").get()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Android 14) AppleWebKit/537.36 Chrome/137.0.0.0 Mobile Safari/537.36"
                    ).header("X-Requested-With", "XMLHttpRequest")
                    .build();

                client.newCall(getRequest).execute().use {
                    it.headers("Set-Cookie").forEach(::println)
                    val csrfToken = cookieManager.getCookie("scratch.mit.edu", "scratchcsrftoken")?.value.orEmpty()
                    println("ket = "+csrfToken)
                    print(it.body?.string())
                    println(it.code)
                    runLogin(csrfToken)


                    println("Code = ${it.code}")
                    println("URL = ${it.request.url}")
                    println("Redirect = ${it.priorResponse != null}")

                }
            }.also {
                it.name = "getReq"
                it.priority = Thread.MAX_PRIORITY
            }.start()


        }

        private fun obtainCsrf() {

            val request =
                Request.Builder()
                    .url("https://scratch.mit.edu/csrf_token/")
                    .header(
                        "User-Agent",
                        USER_AGENT
                    )
                    .build()

            client.newCall(request)
                .execute()
                .close()

        }

        fun logout() {

            cookieJar.clear()

        }

        override fun close() {
            ins = null
        }

        fun sessionId(): String? {

            return cookieJar
                .getCookie(
                    "scratch.mit.edu",
                    "scratchsessionsid"
                )
                ?.value

        }

        fun csrfToken(): String? {

            return cookieJar
                .getCookie(
                    "scratch.mit.edu",
                    "scratchcsrftoken"
                )
                ?.value

        }

        fun get(
            url: String
        ): String {

            val request =
                Request.Builder()
                    .url(url)
                    .header(
                        "User-Agent",
                        USER_AGENT
                    )
                    .build()

            client.newCall(request)
                .execute()
                .use {

                    return it.body!!.string()

                }

        }

        companion object {

            private const val USER_AGENT = "Mozilla/5.0"

            var ins :Client? = null
                private set

            val I get() = checkNotNull(ins){
                "Please init in the main"
            }

        }

    }
}