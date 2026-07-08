package cakar.search


import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.SearchRecentSuggestions
import android.text.util.Linkify
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.SearchView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedCallback
import cakar.search.InfoProDialog.r
import cakar.search.adapter.Adapter
import cakar.search.databinding.ActivityMainBinding
import cakar.search.filetype.Project
import cakar.search.ste.Settings
import coil3.load
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import okhttp3.internal.closeQuietly
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.iterator


class MainActivity : Activity() {

    private val adap by lazy{ Adapter(this, arrayListOf()) }
    private lateinit var binding: ActivityMainBinding


    val con by lazy {
        if(Akun.Client.ins==null){
            Akun.Client(getSharedPreferences("scratch", MODE_PRIVATE))
        }else{
            Akun.Client.I
        }
    }

    val st by lazy {
        SearcT(this)
    }

    val m by lazy {
        Markwon
            .builder(this)
            .usePlugin(CorePlugin.create())
            .usePlugin(TablePlugin.create(this)) // to render tables
            .usePlugin(TaskListPlugin.create(this)) // to render task lists
            .usePlugin(StrikethroughPlugin.create()) // to render strikethrough
            .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
            .build()
    }

    val seaCore by lazy { Search(this) }

    protected var q = ""


    private var t = 0

    private var skipTo = 0

    internal var signedIn = false

    var preque = ""

    /**OnbackList
     *
     *
     * N:onbackcallback as any for prevent older api issue
     *
     */
    var onBacks: MutableMap<String,Any> = mutableMapOf()



    companion object{

        fun Activity.handleMainMenu(item: MenuItem){
            when(item.itemId){
                R.id.set ->{
                    startActivity(Intent(this, Settings::class.java))
                }
                R.id.emp -> {
                    val id = EditText(this)
                    id.hint = "Enter project id"
                    id.maxLines = 1
                    id.inputType = EditorInfo.TYPE_CLASS_NUMBER
                    AlertDialog.Builder(this).apply {
                        setTitle("Enter project id manually")
                        setView(id)
                        setPositiveButton("Go") { _, _ ->
                            try{

                                InfoProDialog().apply {
                                    arguments = Bundle().also {
                                        it.putInt("id", Integer.valueOf(id.text.toString()))
                                        it.putString("tkn", intent.getStringExtra("tkn"))
                                    }
                                }.show(fragmentManager, "ipd")
                            }catch (_:Exception){
                                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            }
                        }
                        setNegativeButton(android.R.string.cancel, null)
                    }.show()
                }
                R.id.emu -> {
                    val id = EditText(this)
                    id.hint = "Enter username"
                    id.maxLines = 1
                    id.isSingleLine = true
                    AlertDialog.Builder(this).apply {
                        setTitle("Enter username to visit")
                        setView(id)
                        setPositiveButton("Visit") { _, _ ->
                            try{
                                val i = Intent(context, PP::class.java)
                                i.putExtra("user", id.text.toString())
                                val b = intent.getBundleExtra("login")?:Bundle.EMPTY
                                i.putExtra("tkn", b.getString("token"))

                                context.startActivity(i)
                            }catch (_:Exception){
                                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            }
                        }
                        setNegativeButton(android.R.string.cancel, null)
                    }.show()
                }
            }
        }


        fun Activity.downloadSmthUri(u: Uri, message: String, onGet:(Long)-> Unit){
            fun nowDownload(f: String): Long{
                val e = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                return e.enqueue(
                    DownloadManager.Request(u)
                        .setDescription(message)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setTitle("SS")
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${f}")
                )
            }
            val id = EditText(this)
            id.hint = "File name"
            id.maxLines = 1
            id.isSingleLine = true
            AlertDialog.Builder(this).apply {
                setTitle("Save to download as [File name]")
                setView(id)
                setPositiveButton("Visit") { _, _ ->
                    try{
                        id.text.toString()?.let{
                            if(it.isNotEmpty()){
                                nowDownload(it)
                            }else{
                                onGet(-2L);
                            }
                        }
                    }catch (_:Exception){
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                        onGet(-2L)
                    }
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show()
        }
    }



    fun e(newConfig: Configuration){
        val s  = Point()
        windowManager.defaultDisplay.getSize(s)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Rumah")
        val suggestions = SearchRecentSuggestions(
            this,
            SearchProvider.AUTHORITY, SearchProvider.MODE
        )
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        e(resources.configuration)

        val se = Search(this@MainActivity)
        val e = ProgressDialog(this@MainActivity)
        se.onError = {
            if(e.isShowing){
                e.dismiss()
            }
        }

        if(savedInstanceState != null){
            val l = savedInstanceState.getParcelableArrayList<Project>("data")
            q = savedInstanceState.getString("q", "")
            if(l == null){
                Toast.makeText(this, "Try search to show it!!", Toast.LENGTH_SHORT).show()
            }
            l?.forEach {
                adap.setdata(it)
            }
        }else {
            Toast.makeText(this, "Try search to show it!!", Toast.LENGTH_SHORT).show()
        }
        binding.usrcard.setOnCreateContextMenuListener { menu, view, info ->
            menu.add("Visit").setEnabled(signedIn).setOnMenuItemClickListener {
                val b = intent.getBundleExtra("login")?:Bundle.EMPTY
                val i = Intent(this, PP::class.java)
                i.putExtra("user", b.getString("username"))
                i.putExtra("tkn", b.getString("token"))

                startActivity(i)
                true
            }
            menu.add("My project").setEnabled(signedIn).setOnMenuItemClickListener {
                val bk = PP.HisProjects()
                val b = intent.getBundleExtra("login")?:Bundle.EMPTY
                bk.u = b.getString("username").orEmpty()
                bk.show(fragmentManager, "flsa")
                true
            }
        }
        binding.usrcard.setOnClickListener {
            it.showContextMenu(it.x,it.y)
        }

    }


    override fun onResume() {
        super.onResume()
        Thread{
            con.getSes().let {
                runOnUiThread{
                    signedIn = it.has("user")
                    if (signedIn) {
                        val u = it.getJSONObject("user")
                        seaCore.token = u.optString("token")
                        intent.putExtra("tkn", u.optString("token"))
                        intent.putExtra("username", u.optString("username"))
                        val ld = Bundle()
                        for(x in u.keys()){
                            val e = u.opt(x)
                            when (e) {
                                is Int -> {
                                    ld.putInt(x, e)
                                }

                                is Boolean -> {
                                    ld.putBoolean(x, e)
                                }

                                else -> {
                                    ld.putString(x, e.toString())
                                }
                            }
                            println(x)
                        }
                        intent.putExtra("login", ld)
                        binding.imageView.load("https:${u.optString("thumbnailUrl")}")
                        binding.usr.setText("Hello, ${u.optString("username")}!")
                    } else {
                        actionBar?.setSubtitle(null)//signed out
                        intent.putExtra("username", "")
                        intent.putExtra("tkn", "")
                        intent.putExtra("login", Bundle.EMPTY)
                        seaCore.token = ""
                        binding.imageView.setImageResource(R.drawable.user)
                        binding.usr.setText("Hello, a ghost...")
                    }
                    invalidateOptionsMenu()
                }
            }
        }.start()

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    class SearcT(private val a: Activity): ActionMode.Callback{

        private val sm =  a.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        private val s = SearchView(a)
        var skipTo = 0
        override fun onCreateActionMode(
            p0: ActionMode?,
            p1: Menu?
        ): Boolean {
            p1?.addSubMenu("Filter by")?.also {
                it.getItem()?.let{ o->
                    o.setIcon(R.drawable.filter)
                    o.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
                val e = arrayOf("Popular", "Trending", "Recent")
                e.forEach { ite->
                    it.add(R.id.amenu,0,e.indexOf(ite),ite).setChecked(a.intent.getIntExtra("searchBy",0) == e.indexOf(ite)).setOnMenuItemClickListener {i->
                        a.intent.putExtra("searchBy", e.indexOf(ite))
                        i.setChecked(a.intent.getIntExtra("searchBy",0) == e.indexOf(ite))
                        true
                    }
                    it.setGroupCheckable(R.id.amenu, true, true)
                }
            }
            fun d(q: String){
                val i = Intent(a, Result::class.java)
                i.putExtra("query", q)
                i.putExtra("skipTo", skipTo)
                i.putExtra("tkn", a.intent.getStringExtra("tkn"))
                i.putExtra("searchBy", a.intent.getIntExtra("searchBy",0))
                a.startActivity(i)
            };
            val si = sm.getSearchableInfo(a.componentName)
            s.isIconifiedByDefault = false
            s.isIconified = false
            s.setSearchableInfo(si)
            s.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    d(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean{
                    s.isSubmitButtonEnabled = !newText.isNullOrEmpty()
                    return true
                }

            })
            p0?.customView = s
            s.setOnSuggestionListener(object: SearchView.OnSuggestionListener{
                override fun onSuggestionClick(p0: Int): Boolean {
                    d(findOutQueryFromSuggest(p0))
                    return true
                }

                override fun onSuggestionSelect(p0: Int): Boolean {
                    val q = findOutQueryFromSuggest(p0)
                    s.setQuery(q,false)
                    return true
                }


                fun findOutQueryFromSuggest(position:Int): String{
                    val adapter = s.getSuggestionsAdapter();
                    if (adapter != null) {
                        val cursor =  adapter.getItem(position) as Cursor;

                        // Get data using the column index
                        val columnIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
                        val suggestionText = cursor.getString(columnIndex).orEmpty();

                        // Update SearchView text and submit query
                        return suggestionText
                    }
                    return ""
                }

            })
            return true
        }

        override fun onPrepareActionMode(
            p0: ActionMode?,
            p1: Menu?
        ) : Boolean{
            return true
        }

        override fun onActionItemClicked(
            p0: ActionMode?,
            p1: MenuItem?
        )=false

        override fun onDestroyActionMode(p0: ActionMode?) {

        }

        fun setQueryText(t: String){
            s.setQuery(t, false)
        }

        fun show(){
            a.startActionMode(this)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.setGroupVisible(R.id.needUser, signedIn)
        return super.onPrepareOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {



        handleMainMenu(item)
        when (item.itemId) {
            R.id.app_bar_search -> st.show()
            R.id.dm -> showDialog(
                0x01,
                intent.getBundleExtra("login")
            )


        }
        Log.i("menu", "onOptionsItemSelected: ${item.order}")
        return super.onOptionsItemSelected(item)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        e(newConfig)

        super.onConfigurationChanged(newConfig)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("data", adap.data)
        outState.putString("q", q)
    }


    override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
        if(id == 0x01){
            return object :Dialog(this, R.style.Theme_C){


                var ms = JSONArray()
                init {
                    setTitle("Inbox")
                    setContentView(android.R.layout.list_content)
                }
                val ad = object :BaseAdapter(){
                    override fun getCount(): Int = ms.length()


                    override fun getItem(p0: Int): JSONObject? = try{
                        ms.getJSONObject(p0)
                    }catch (_: Exception){
                        null
                    }


                    override fun getItemId(p0: Int): Long {
                        return 0L
                    }

                    override fun getView(
                        p0: Int,
                        p1: View?,
                        p2: ViewGroup?
                    ): View? {
                        val view: View
                        val j = getItem(p0)
                        if(p1 == null){
                            view = layoutInflater.inflate(R.layout.message, null)
                        }else{
                            view = p1
                        }
                        if(j != null){
                            val tabret = StringBuilder()
                            val d :Int
                            if(j.get("type") == "loveproject"){
                                tabret.append("love  **${j.get("title")}**")
                                d = R.drawable.love
                            }else if(j.get("type") == "favoriteproject"){
                                tabret.append("save **${j.get("project_title")}** as best project")
                                d = R.drawable.list
                            }

                            else if(j.get("type") == "addcomment"){
                                tabret.append("saying this in ")
                                val e = if(
                                    (j.getInt("comment_type") == 0) or
                                    (j.getInt("comment_type") == 2)
                                    ) {
                                    "**${j.get("comment_obj_title")}**"
                                }else if(j.getInt("comment_type") == 1) {
                                    "your profile"
                                }
                                else ""
                                tabret.append(e)
                                tabret.append("\n```\n${j.get("comment_fragment")}\n```\n")
                                d = R.drawable.chat
                            }
                            else if(j.get("type") == "followuser"){
                                tabret.append("is now __following you!__")
                                d = R.drawable.favcha
                            }else{
                                tabret.append(j.toString())
                                d = R.drawable.info
                            }
                            view.post{
                                view.findViewById<ImageView>(android.R.id.icon).setImageResource(d)
                                view.findViewById<TextView>(android.R.id.text1).setText(j.getString("actor_username"))
                                m.setMarkdown(view.findViewById<TextView>(android.R.id.text2), tabret.toString().trimIndent())
                                val i = Intent(context, PP::class.java)
                                i.putExtra("user", j.getString("actor_username"))
                                view.setOnCreateContextMenuListener { menu, view, info ->
                                    menu.add("Visit user").intent = i
                                    if(j.has("project_id")){
                                        val k  = menu.add("Visit project")
                                        k.setOnMenuItemClickListener {
                                            InfoProDialog().apply {
                                                arguments = Bundle().also {
                                                    it.putInt("id", j.getInt("project_id"))
                                                    it.putString("tkn", intent.getStringExtra("tkn"))
                                                }
                                            }.show(fragmentManager, "ipd")
                                            true
                                        }
                                    }else if(j.has("comment_obj_id") and (j.optInt("comment_type",-1) == 0)){
                                        menu.add("Visit project").intent = Intent(context, ProjectActivity::class.java) .also{i->
                                            i.putExtra("project", j.getInt("comment_obj_id"))
                                        }
                                    }
                                }
                            }
                        }
                        return view
                    }

                }

                val l get():ListView = findViewById(android.R.id.list)

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    l.adapter = ad



                    if(savedInstanceState==null){
                        seaCore.getMessageFromUser(
                            args?.getString("username", "").orEmpty()
                        ) {
                            ms = it
                            ad.notifyDataSetChanged()
                        }
                    }else{
                        savedInstanceState?.getString("k", "[]")?.let{
                            ms = JSONArray(it)
                            ad.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCreateOptionsMenu(menu: Menu): Boolean {
                    menu.add(0,0,2,"Close").setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                        dismissDialog(id)
                        true
                    }
                    menu.add(0,0,1,"Reload").setIcon(R.drawable.refresh).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                        seaCore.getMessageFromUser(
                            args?.getString("usr", "").orEmpty()
                        ) {
                            ms = it
                            ad.notifyDataSetChanged()
                        }
                        true
                    }
                    return super.onCreateOptionsMenu(menu)
                }


                override fun onSaveInstanceState(): Bundle {
                    val d  =super.onSaveInstanceState()
                    d.putString("k", ms.toString())
                    return d
                }
            }
        }
        return super.onCreateDialog(id, args)
    }


    override fun onDestroy() {
        super.onDestroy()
        if(isFinishing){
            con.closeQuietly()
        }
    }




}