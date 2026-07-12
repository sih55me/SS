package cakar.search

import android.app.AlertDialog.Builder
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.util.Linkify
import android.view.ActionMode
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.core.net.toUri
import cakar.search.MainActivity.Companion.downloadSmthUri
import cakar.search.databinding.ProjectviewBinding
import cakar.search.filetype.Komentar
import cakar.search.filetype.Project
import cakar.search.wtbcore.PreviewImgPage
import coil3.load
import coil3.request.crossfade
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.logging.Handler

class InfoProDialog()  : DialogFragment(){
    var itemdata : Project? = null
    private set
    private var isTabReady = false


    val pb by lazy { ProjectviewBinding.inflate(LayoutInflater.from(ContextThemeWrapper(context, R.style.Theme_Notds_AltTab))) }
    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return pb.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        return object : Dialog(context, R.style.Theme_Notds_AltTab){
            init {
                window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
                window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window!!.setDimAmount(0.4F)
                setCanceledOnTouchOutside(true)
                when(resources.configuration.orientation){
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        window!!.setLayout(window!!.windowManager.defaultDisplay.width/2,
                            (window!!.windowManager.defaultDisplay.height/1.12).toInt()
                        )
                        window?.setGravity(Gravity.CENTER)
                    }

                }

            }

            override fun onBackPressed() {

                val fragmentManager: FragmentManager = childFragmentManager
                modeClose()
                if (!fragmentManager.isStateSaved() && fragmentManager.popBackStackImmediate()) {
                    if(
                        (fragmentManager.backStackEntryCount==0)&&
                        pb.root.displayedChild == 1
                        ){
                        pb.root.displayedChild = 0
                    }
                    return
                }
                super.onBackPressed()
            }

            override fun onCreateOptionsMenu(menu: Menu): Boolean {
                this@InfoProDialog.onCreateOptionsMenu(menu, MenuInflater(context))
                return super.onCreateOptionsMenu(menu)
            }
            override fun show() {
                super.show()
                actionBar?.setDisplayUseLogoEnabled(false)
                actionBar?.setHomeButtonEnabled(false)
                actionBar?.elevation = 0f
                actionBar?.setIcon(R.mipmap.ic_launcher_pr2)
            }
        }
    }

    fun getSecretSysId(id: String?, type: String?): Int {
        return activity.getResources().getIdentifier(id, type, "android")
    }


    fun modeOpen(){
        pb.root.setInAnimation(activity, getSecretSysId("activity_open_enter", "anim"));
        pb.root.setOutAnimation((activity), getSecretSysId("activity_open_exit", "anim"));
    }
    fun modeClose(){
        pb.root.setInAnimation(activity, getSecretSysId("activity_close_enter", "anim"));
        pb.root.setOutAnimation((activity), getSecretSysId("activity_close_exit", "anim"));
    }
    private val kom = object: ActionMode.Callback{
        override fun onCreateActionMode(
            p0: ActionMode?,
            p1: Menu?
        ): Boolean {

            pb.root.showNext()
            p0?.setTitle("Comment")
            p1?.add("Reload")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)?.setOnMenuItemClickListener {
                childFragmentManager.findFragmentByTag("komen")?.let{k->
                    if(k is r){
                        k.forceLoad()
                    }
                }
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
            modeClose()
            pb.root.displayedChild = 0
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menu == null)return

        menu.add(0,0,2,"Close").setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
            this@InfoProDialog.dismiss()
            true
        }
        menu.addSubMenu(0,0,1,"Options").also {mo->
            mo.getItem().setEnabled(itemdata != null)
            mo.getItem().setIcon(R.drawable.more).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            if (itemdata != null) {
                mo.apply {
                    add("Share")
                        ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                        ?.setOnMenuItemClickListener {
                            try {
                                val i = Intent(Intent.ACTION_SEND)
                                i.type = "text/plain"
                                i.putExtra(
                                    Intent.EXTRA_TEXT,
                                    "https://scratch.mit.edu/projects/${itemdata!!.id}/"
                                )
                                activity.startActivity(
                                    Intent.createChooser(
                                        i,
                                        "share"
                                    )
                                )
                                true
                            } catch (_: Exception) {
                                Toast.makeText(
                                    activity,
                                    "Project unload!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                false
                            }
                        }
                    add("Comment")
                        ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                        ?.setOnMenuItemClickListener {
                            r().also {f->
                                Bundle().also {
                                    it.putInt("id", itemdata?.id?:0)
                                    it.putString("usr", itemdata?.creator)
                                    f.arguments = it
                                }
                            }.let {
                                modeOpen()
                                childFragmentManager.beginTransaction().add(R.id.swl,it, "komen").addToBackStack(null).setReorderingAllowed(true).setBreadCrumbTitle("Comment").commit()
                                pb.root.postDelayed({ pb.root.showNext() },600L)
                            }
                            true
                        }
                    add("Learn more").setOnMenuItemClickListener {
                        try {
                            val i = Intent(
                                Intent.ACTION_VIEW,
                                "https://scratch.mit.edu/projects/${itemdata!!?.id}/".toUri()
                            )
                            activity.startActivity(
                                Intent.createChooser(
                                    i,
                                    "share"
                                )
                            )
                            true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                activity,
                                "Project unload!",
                                Toast.LENGTH_SHORT
                            ).show()
                            false
                        }
                    }
                    add("Status").setOnMenuItemClickListener {
                        Builder(activity).setTitle("Status")
                            .setMessage(
                                "Token key : ${itemdata!!.uninfo["project_token"]}\nVisibility : ${itemdata!!.uninfo["visibility"]}\nIs Public : ${itemdata!!.uninfo["public"]}\nPublish : ${
                                    itemdata?.uninfo?.get(
                                        "posted"
                                    )
                                }"
                            )
                            .setPositiveButton(android.R.string.ok, null)
                            .create().also {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    it!!.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                                    it!!.window?.attributes?.blurBehindRadius = 5
                                }
                            }.show()
                        true
                    }

                    add("Asset").intent = Intent(activity, AssetPage::class.java).putExtra("item", itemdata)
                }
            }
        }
        menu.add(0,0,0,"Play").setIcon(R.drawable.play).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setVisible(itemdata != null).setOnMenuItemClickListener {
            val i = Intent(activity, ProjectActivity::class.java)
            i.putExtra("project", itemdata!!.id)
            itemdata!!.uninfo.get("project_token")?.let {
                i.putExtra("tknp",it.toString())
            }
            activity.startActivity(i)
            true
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        dialog?.setTitle(null)
        super.onViewCreated(view, savedInstanceState)


        if(savedInstanceState?.getParcelable<Project>("p") != null){
            itemdata = savedInstanceState?.getParcelable("p")
            setup()
            return
        }
        if(arguments?.getParcelable<Project>("p") != null){
            itemdata = arguments?.getParcelable("p")
            setup()
            return
        }
        if(arguments?.getInt("id") != null){
            Search(activity).also{s->
                s.token = activity.intent.getStringExtra("tkn")?:""
                s.onError = {
                    println(s.reason)
                    notfound(s.reason)
                }
                s.getProject(arguments?.getInt("id") ?: 0) {
                    itemdata = it
                    setup()
                }
            }
            return
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable("p", itemdata)
    }


    fun notfound(reason: String = ""){
        view?.startActionMode(object: ActionMode.Callback{




            override fun onCreateActionMode(
                a: ActionMode?,
                p1: Menu?
            ): Boolean {
                if(reason == "nfa") {
                    a?.setTitle("Generator error!!")
                }else{
                    a?.setTitle("Abnormal result!!")
                }
                p1?.add(0,0,2,"Close")!!.setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                    this@InfoProDialog.dismiss()
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

            }

        })
    }

    private fun setup(){
        if(context == null){
            return
        }
        val m = Markwon
            .builder(context)
            .usePlugin(CorePlugin.create())
            .usePlugin(TablePlugin.create(context)) // to render tables
            .usePlugin(TaskListPlugin.create(context)) // to render task lists
            .usePlugin(StrikethroughPlugin.create()) // to render strikethrough
            .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
            .build()
        if(itemdata == null){
            System.err.println("Project lost contact")
            notfound("nfa")
            return
        }
        try{

            pb.progressCircular.animate().alpha(0F).withEndAction {
                pb.progressCircular.visibility = View.GONE
            }
            val tabret = StringBuilder()
            if(itemdata?.instructions?.isNullOrEmpty() == false){
                tabret.append("## Instruction\n")
                tabret.append(itemdata!!.instructions)
            }
            if(itemdata?.desc?.isNullOrEmpty() == false){
                tabret.append("\n## Note\n")
                tabret.append(itemdata!!.desc)
            }
            pb.title.text = itemdata!!.title

            tabret.append("\n## Info\n\n")
            tabret.append(try {
                pb.stat.text =
                    "❤️ ${itemdata!!.uninfo["loves"]}  👁 ${itemdata!!.uninfo["views"]}  ⭐️ ${itemdata!!.uninfo["favorites"]}  💥 ${itemdata!!.uninfo["remixes"]}" + if (itemdata!!.uninfo["scratchteam"] == "true") "    ❤️‍🔥" else ""
                pb.crea.text = itemdata!!.creator
                "1. Id : ${itemdata!!.id}\n 2. Created : ${itemdata!!.uninfo["created"]}\n 3. Modified : ${itemdata!!.uninfo["modified"]}\n 4. Shared : ${itemdata!!.uninfo["shared"]}"
            } catch (_: Exception) {
                "Error when taking info. Try again later."
            })
            m.setMarkdown(pb.info, tabret.toString())
            if (!(itemdata!!.uninfo["remix@o"]!="null")) {
                pb.ori.isEnabled = true
                pb.ori.setOnClickListener { _ ->
                    try {
                        val i = Intent(activity, ProjectActivity::class.java)
                        i.putExtra(
                            "project",
                            Integer.valueOf(itemdata!!.uninfo["remix@o"].toString())
                        )
                        itemdata!!.uninfo.get("project_token")?.let {
                            i.putExtra("tknp",it.toString())
                        }

                        startActivity(i)
                    } catch (_: Exception) {
                        Toast.makeText(activity, "Invalid input @o", Toast.LENGTH_SHORT).show()
                    }
                }
                pb.remixLay.visibility = View.VISIBLE
            }
            if (!(itemdata!!.uninfo["remix@p"]!="null")) {
                pb.orire.isEnabled = true
                pb.orire.setOnClickListener { _ ->
                    try {
                        val i = Intent(activity, ProjectActivity::class.java)
                        i.putExtra(
                            "project",
                            Integer.valueOf(itemdata!!.uninfo["remix@p"].toString())
                        )
                        startActivity(i)
                    } catch (_: Exception) {
                        Toast.makeText(activity, "Invalid input @p", Toast.LENGTH_SHORT).show()
                    }
                }
                pb.remixLay.visibility = View.VISIBLE
            }
            pb.thumbnail.load(itemdata?.thumb){
                transformations(RoundedCornersTransformation(10F))
                crossfade(true)
            }
            pb.thumbnail.setOnClickListener {
                it.showContextMenu(it.x,it.y)
            }
            pb.thumbnail.setOnCreateContextMenuListener { menu, view, info ->
                menu.add("Look").setOnMenuItemClickListener {
                    PreviewImgPage(activity, PreviewImgPage.Get(pb.thumbnail.drawable?: ColorDrawable(Color.WHITE), itemdata?.title.orEmpty())).also {
                        it.show()
                    }
                    true
                }
                menu.add("Download").setOnMenuItemClickListener {
                    activity?.downloadSmthUri(Uri.parse(itemdata?.thumb), "Downloading ${itemdata?.title}'s profile picture"){
                        if(it <= -1L){
                            Toast.makeText(activity, "Cannot download!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
            }





            pb.crea.setOnClickListener {
                try {
                    val i = Intent(activity, PP::class.java)
                    i.putExtra("user", itemdata!!.creator)

                    activity.startActivity(i)
                } catch (_: Exception) {
                    Toast.makeText(activity, "SOme component id unload", Toast.LENGTH_SHORT).show()
                }
            }
            dialog?.invalidateOptionsMenu()
        }catch (e: Exception){
            e.printStackTrace()
            notfound(e.toString())
        }
    }



    class r() : DialogFragment(){

        val f :Akun.Client get()=Akun.Client.I


        val m by lazy{
            Markwon
                .builder(activity)
                .usePlugin(CorePlugin.create())
                .usePlugin(TablePlugin.create(activity)) // to render tables
                .usePlugin(TaskListPlugin.create(activity)) // to render task lists
                .usePlugin(StrikethroughPlugin.create()) // to render strikethrough
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build()
        }
        var ms = arrayListOf<Komentar>()

        val ad = object :BaseAdapter(){
            override fun getCount(): Int = ms.size


            override fun getItem(p0: Int): Komentar? = ms.get(p0)



            override fun getItemId(p0: Int): Long {
                return 0L
            }

            override fun getView(
                p0: Int,
                p1: View?,
                p2: ViewGroup?
            ): View? {
                val view: ViewGroup
                val j = getItem(p0)
                if(p1 == null){
                    view = layoutInflater.inflate(R.layout.message, null) as ViewGroup
                }else{
                    view = p1 as ViewGroup
                }
                if(j != null){
                    view.findViewById<TextView>(android.R.id.text1)?.setText(j.usr)
                    m.setMarkdown(view.findViewById<TextView>(android.R.id.text2), j.komentar)
                    val ic = view.findViewById<ImageView>(android.R.id.icon)
                    ic.imageTintList = null
                    ic.setPadding(0,0,0,0)
                    ic.setBackgroundDrawable(null)
                    ic.load(j.usrThumb)
                    val i = Intent(context, PP::class.java)
                    i.putExtra("user", j.usr)
                    view.setOnLongClickListener {
                        val pm = PopupMenu(activity, ic)
                        val menu = pm.menu
                        menu.add("Visit ${j.usr}").intent = i
                        menu.add("Copy comment").setOnMenuItemClickListener {
                            val c = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            c.setPrimaryClip(ClipData.newPlainText("scratch comment", j.komentar))
                            true
                        }
                        if(j.origin.has("reply_count")){
                            if(j.origin.optInt("reply_count",0)>=1){
                                menu.add("Peek reply").setOnMenuItemClickListener {
                                    r().also {f->
                                        Bundle().also {
                                            f.arguments = this@r.arguments.clone() as Bundle
                                            f.arguments.putInt("comid", j.id)
                                        }
                                    }.let{
                                        fragmentManager.beginTransaction()
                                            .hide(this@r)
                                            .add(R.id.swl, it, "komen")
                                            .addToBackStack(null)
                                            .setReorderingAllowed(true)
                                            .setBreadCrumbTitle("Reply of ${j.usr}").commit()
                                    }
                                    true
                                }
                            }
                        }
                        menu.add("Delete").setOnMenuItemClickListener {
                            val request = okhttp3.Request.Builder()
                                .url("https://api.scratch.mit.edu/proxy/comments/project/${arguments.getInt("id")}/comment/${j.id}".also {
                                    println(it)
                                })
                                .delete()
                                .header("X-Token", tkn)
                                .header("X-CSRFToken", f.csrfToken()?:"")
                                .header("Referer", "https://scratch.mit.edu/projects/${arguments.getInt("id")}/")
                                .header("Origin", "https://scratch.mit.edu")
                                .header("User-Agent", "Mozilla/5.0")
                                .build()
                            client.newCall(request).enqueue(object: Callback{
                                override fun onFailure(call: Call, e: IOException) {
                                    e.printStackTrace()
                                }

                                override fun onResponse(
                                    call: Call,
                                    it: Response
                                ) {
                                    println("[kmtr]CodeRes:"+it.code)
                                    val s= StringBuilder(it.body?.string().orEmpty())
                                    println("[kmtr]ResBod:\n"+s)
                                    view.post{
                                        forceLoad()
                                    }
                                }
                            })
                            true
                        }
                        pm.show()
                        true
                    }
                    ic.setOnClickListener {
                        view.performLongClick()
                    }
                }

                return view
            }

        }


        override fun onSaveInstanceState(outState: Bundle?) {
            super.onSaveInstanceState(outState)
            outState?.putParcelableArrayList("ks", ms)
        }

        override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater?.inflate(R.layout.kmtr, null)
        }

        val client by lazy {
            OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.HOURS)
                .writeTimeout(1, TimeUnit.HOURS)
                .readTimeout(1, TimeUnit.HOURS)
                .cookieJar(f.cookieJar)
                .build()
        }
        fun View.o(){
            val input = findViewById<View?>(R.id.input) as EditText

            val send = findViewById<View>(R.id.send)
            val typco = findViewById<ViewFlipper>(R.id.typingCon)

            findViewById<View>(R.id.add).setOnClickListener {
                PopupMenu(context, it).also {
                    val menu = it.menu
                    menu.add("Reload").setOnMenuItemClickListener {
                        forceLoad()
                        true
                    }
                    menu.add("Load more").setOnMenuItemClickListener {
                        offset = offset + 20
                        Search(activity).fetchCommentFromProject(
                            Pair(arguments.getString("usr").orEmpty(),arguments.getInt("id")?:0), offset = offset
                        ) {
                            ms.add(it)
                            ad.notifyDataSetChanged()
                        }
                        true
                    }
                }.show()
            }
            typco.displayedChild = 0
            val sjsb = sendWhenTypingEvent(send)
            input.addTextChangedListener(sjsb)
            send.setOnClickListener({

                    val prompt = input.text.toString().trim()

                    if (prompt.isEmpty()) return@setOnClickListener
                    val json = JSONObject()
                        .put("content", prompt) .also{
                            if(arguments.getInt("comid")!=0){
                                it.put("parent_id", arguments.getInt("comid"))
                            }else{
                                it.put("parent_id", JSONObject.NULL)
                            }
                        }
                        .put("commentee_id", JSONObject.NULL)
                        .toString()

                    val body =
                        json.toRequestBody(
                            "application/json".toMediaType()
                        )

                    println("dp- "+tkn)
                    val request = okhttp3.Request.Builder()
                        .url("https://api.scratch.mit.edu/proxy/comments/project/${arguments.getInt("id")}".also {
                            println(it)
                        })
                        .post(body)
                        .header("X-Token", tkn)
                        .header("X-CSRFToken", f.csrfToken()?:"")
                        .header("Referer", "https://scratch.mit.edu/projects/${arguments.getInt("id")}/")
                        .header("Origin", "https://scratch.mit.edu")
                        .header("User-Agent", "Mozilla/5.0")
                        .build()

                    typco.displayedChild = 1
                    val sda = client.newCall(request)
                    sda.enqueue(object: Callback{
                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                                this@o.post {
                                    typco.displayedChild = 0
                                }
                            }

                            override fun onResponse(
                                call: Call,
                                it: Response
                            ) {


                                println("[kmtr]CodeRes:"+it.code)
                                val s= StringBuilder(it.body?.string().orEmpty())
                                println("[kmtr]ResBod:\n"+s)
                                var cd = ""
                                if(s.startsWith("{")){
                                    val j = JSONObject(s.toString())
                                    if(j.has("code")){
                                        this@o.post {
                                            Toast.makeText(activity, j.get("code").toString(), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    if(j.has("rejected")){
                                        this@o.post {
                                            when(j.optString("rejected")){
                                                "isFlood"-> "You spamming too many comment under 1 second"
                                                "isSpam"-> "You spamming too many comment"
                                                "isEmpty"-> "Invalid text"
                                                "isBad" -> "Don't send a bad stuff plz"
                                                "isUnconstructive" -> "Don't send a bad stuff plz"
                                                "hasChatSite" -> "Suspicious link"
                                                "isDisallowed" -> "Author disable the comment in this project. \nContact the author in another way."
                                                "isTooLong"->"ARE YOU MAKING A DIARY??"
                                                "isMuted" -> "You're ground to NOT COMMENT"
                                                else -> "what? how?\n${j.optString("rejected")}"
                                            }.let {e->
                                                Toast.makeText(activity, e, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                                this@o.post {
                                    typco.displayedChild = 0
                                    forceLoad()
                                }


                            }

                        })
                    findViewById<View>(R.id.stop).setOnClickListener {
                        sda.cancel()
                        typco.displayedChild = 0
                    }
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(input.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    input.setText("")
            })
        }

        private fun sendWhenTypingEvent(send: View)  = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {

            }

            override fun onTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                send.alpha = if(p0?.length == 0){
                    0.3F
                }else{
                    1F
                }
            }

        }
        val tkn get() = activity.intent.getStringExtra("tkn")?:""

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view?.findViewById<ListView>(android.R.id.list)?.let{l->
                l.adapter = ad
                l.emptyView = view?.findViewById<View>(android.R.id.empty)
            }

            view?.setBackgroundResource(R.drawable.full_frame)
            view?.o()
            if(savedInstanceState==null){
                forceLoad()
            }else{
                ms.clear()
                savedInstanceState.getParcelableArrayList<Komentar>("ks")?.let{
                    ms.addAll(it)
                    ad.notifyDataSetChanged()
                }
            }
        }

        var offset = 0

        internal fun forceLoad(clearFirst:Boolean = true){
            if(clearFirst){
                ms.clear()
                ad.notifyDataSetChanged()
            }
            val e = fun(it:Komentar) {
                ms.add(it)
                ad.notifyDataSetChanged()
            }

            Search(activity).fetchCommentFromProject(Pair(arguments.getString("usr").orEmpty(),arguments.getInt("id")), fromComment = arguments.getInt("comid"), onGet = e)
        }




    }

}