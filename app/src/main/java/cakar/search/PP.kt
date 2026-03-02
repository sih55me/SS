package cakar.search

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.graphics.drawable.toDrawable
import cakar.search.adapter.Adapter
import cakar.search.databinding.HislistBinding
import cakar.search.databinding.PpBinding
import cakar.search.filetype.Project
import cakar.search.filetype.User
import com.bumptech.glide.Glide
import io.getstream.photoview.dialog.PhotoViewDialog
import kotlin.getValue

class PP: AppCompatActivity() {

    lateinit var p: User
    
    val prevp = arrayListOf<User>()

    var hideD= arrayListOf<Dialog>()

    val bin by lazy { PpBinding.inflate(layoutInflater) }

    var onBacks: MutableList<Any> = mutableListOf()


    private fun ListView.hU(l: List<User>){
        onItemClickListener = AdapterView.OnItemClickListener{ _, _, ind, _ ->
            val i = Intent(context, PP::class.java)
            i.putExtra("user", l[ind].title)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }

    private fun fo(){
        try{
            val w = hideD.last().window
            val e = w!!.decorView
            w!!.attributes.alpha = 1F
            w!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            w!!.windowManager!!.updateViewLayout(e, window!!.attributes)
            p = prevp.last()
            d(prevp.last())
            prevp.removeAt(prevp.lastIndex)
            hideD.removeAt(hideD.lastIndex)
        }catch (_: Exception){

        }
    }
    inner class HisFlwi: ListDialog(this) {
        private val l = arrayListOf<User>()
        override fun onSaveInstanceState(): Bundle {
            return super.onSaveInstanceState().also {
                it.putParcelableArrayList("l", l)
            }
        }
        init {
            setTitle("Following")
        }
        var onOpenNewP:((Dialog)->Unit) = { d->
            hideD.add(d)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val d = ArrayAdapter(this@PP,android.R.layout.simple_list_item_1, arrayListOf<String>()).also {
                hb.list.adapter = it
            }
            hb.list.hU(l)
            savedInstanceState?.getParcelableArrayList<User>("l") .also {
                if (!it.isNullOrEmpty()) {
                    l.addAll(it)
                    it.forEach { o->
                        d.add(o?.title.orEmpty())
                        d.notifyDataSetChanged()
                    }
                } else {
                    Search(this@PP).fetchFollowingFromUser(p.title) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }

            hb.next.setOnClickListener {
                if(d.count == 20){
                    ofs += 20
                    l.clear()
                    d.clear()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    Search(this@PP).fetchFollowingFromUser(p.title, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    ofs -= 20
                    l.clear()
                    d.clear()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    Search(this@PP).fetchFollowersFromUser(p.title, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    inner class HisFlwe: ListDialog(this) {
        private val l = arrayListOf<User>()

        override fun onSaveInstanceState(): Bundle {
            return super.onSaveInstanceState().also {
                it.putParcelableArrayList("l", l)
            }
        }
        init {
            setTitle("Followers")
        }
        var onOpenNewP:((Dialog)->Unit) = { d->
            hideD.add(d)
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val d = ArrayAdapter(this@PP,android.R.layout.simple_list_item_1, arrayListOf<String>()).also {
                hb.list.adapter = it
            }
            hb.list.hU(l)
            savedInstanceState?.getParcelableArrayList<User>("l") .also {
                if (!it.isNullOrEmpty()) {
                    l.addAll(it)
                    it.forEach { o->
                        d.add(o?.title.orEmpty())
                        d.notifyDataSetChanged()
                    }
                } else {
                    Search(this@PP).fetchFollowersFromUser(p.title) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.next.setOnClickListener {
                if(d.count == 20){
                    ofs += 20
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    l.clear()
                    d.clear()
                    Search(this@PP).fetchFollowersFromUser(p.title, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    ofs -= 20
                    l.clear()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    d.clear()
                    Search(this@PP).fetchFollowersFromUser(p.title, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    inner class HisProjects: ListDialog(this) {
        init {
            setTitle("Shared project")
        }
        private val d = Adapter(this@PP, arrayListOf(), true)

        override fun onSaveInstanceState(): Bundle {
            return super.onSaveInstanceState().also {
                it.putParcelableArrayList("l", d.data)
            }
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            hb.list.adapter = d
            d.data.clear()
            savedInstanceState?.getParcelableArrayList<Project>("l") .also {
                if (!it.isNullOrEmpty()) {
                    d.data.addAll(it)
                    d.notifyDataSetChanged()
                }else{
                    Search(this@PP).searchProjectFromUser(p.title){
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = false
                    }
                }
            }


            hb.next.setOnClickListener {
                if(d.count == 20){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs += 20
                    d.flush()
                    Search(this@PP).searchProjectFromUser(p.title, ofs) {
                        hb.pr.isIndeterminate = false
                        hb.pr.progress +=1
                        d.setdata(it)
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs -= 20
                    d.flush()
                    Search(this@PP).searchProjectFromUser(p.title, ofs) {
                        hb.pr.isIndeterminate = false
                        hb.pr.progress +=1
                        d.setdata(it)
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    inner class HisFProjects: ListDialog(this) {
        init {
            setTitle("Favorites project")
        }
        private val d = Adapter(this@PP, arrayListOf(), true)

        override fun onSaveInstanceState(): Bundle {
            return super.onSaveInstanceState().also {
                it.putParcelableArrayList("l", d.data)
            }
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            hb.list.adapter = d
            d.data.clear()
            savedInstanceState?.getParcelableArrayList<Project>("l") .also {
                if (!it.isNullOrEmpty()) {
                    d.data.addAll(it)
                    d.notifyDataSetChanged()
                }else{
                    Search(this@PP).searchFavProjectFromUser(p.title) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = false
                    }
                }
            }
            hb.next.setOnClickListener {
                if(d.count == 20){
                    ofs += 20
                    d.flush()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    Search(this@PP).searchFavProjectFromUser(p.title, ofs) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs -= 20
                    d.flush()
                    Search(this@PP).searchFavProjectFromUser(p.title, ofs) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 20
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    open class ListDialog(a: Activity): AppCompatDialog(a, R.style.Theme_SS_ProPre){
        val hb  = HislistBinding.inflate(LayoutInflater.from(context))
        var onButtonClick = DialogInterface.OnClickListener{_,_->}

        init {
            setContentView(hb.root)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            ofs = (savedInstanceState?.getInt("ofs")?:0).also {
                hb.pr.isIndeterminate = false
                hb.pr.progress = it
            }
            hb.list.emptyView = hb.loa
            super.onCreate(savedInstanceState)
        }

        override fun show() {
            super.show()
        }


        override fun onStart() {
            super.onStart()
        }


        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menu.add("Close").setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                dismiss()
                true
            }
            return super.onCreateOptionsMenu(menu)
        }
        var ofs = 0

        override fun onSaveInstanceState(): Bundle {
            return super.onSaveInstanceState().also {
                it.putInt("ofs", ofs)
            }
        }


        override fun dismiss() {
            hb.list.adapter = null
            super.dismiss()
        }
    }
    
    fun d(s: User){
        s.also {
            Glide.with(this).load(it.thumb).into(bin.imageView)
            bin.imageView.setOnClickListener { _->
                PhotoViewDialog.Builder(this, arrayOf(s!!.thumb)){ v, u->
                    Glide.with(this)
                        .load(u)
                        .placeholder(resources.getColor(android.R.color.background_dark).toDrawable())
                        .error(resources.getColor(android.R.color.holo_red_light).toDrawable())
                        .into(v)
                }.allowSwipeToDismiss(true).withHiddenStatusBar(false).show(true)
            }
            bin.usr.text = it.title
            val meUser = listOf<String>("stablecat", "zombiew358")
            val spea = AlertDialog.Builder(this).setTitle("What's does it mean?")
            if(it.title in meUser){
                bin.usr.setTextColor(resources.getColor(android.R.color.holo_blue_bright))
                spea.setMessage("He/She is developer's alt account (of this app)")
                val di = spea
                    .setNegativeButton("Oh, i see.", null)
                    .setPositiveButton("But who?"){_,_->
                        val i = Intent(this, PP::class.java)
                        i.putExtra("user", "wiwolf360")
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                    }
                    .create()
                bin.usr.setOnClickListener {
                    di.show()
                }
            }else if(it.title == "wiwolf360"){
                spea.setMessage("Developer of this app")
                bin.usr.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                bin.usr.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_manage,0,0,0)
                val di = spea.setPositiveButton("Oh, i see", null).create()
                bin.usr.setOnClickListener {
                    di.show()
                }
            }else{
                bin.usr.setOnClickListener {
                    it.performLongClick()
                }
            }
            if(it.bio.isNotEmpty()){
                bin.ams.visibility = View.VISIBLE
            }
            if(it.status.isNotEmpty()){
                bin.ids.visibility = View.VISIBLE
            }
            bin.bio.text = it.bio
            bin.status.text = it.status
            bin.ibio.text = try{
                var e = ""
                it.uninfo["scratchteam"].let{i->
                    if(i == "true"){
                        e = "Scratch Team | "
                    }
                }
                bin.con.text = it.from
                "${e}Join ${it.uninfo["created"]}"
            }catch (_: Exception){
                "Error when taking info. Try again later."
            }
        }
        bin.usrcon.animate().alpha(1F)
        bin.action.animate().alpha(1F)
    }

    private fun lay(newConfig: Configuration){
        val s  = Point()
        windowManager.defaultDisplay.getSize(s)
        if(!resources.getBoolean(R.bool.tablet)){
            when(newConfig.orientation){
                Configuration.ORIENTATION_LANDSCAPE -> {
                    bin.root.layoutParams.width = (windowManager.defaultDisplay.width/2).toInt()
                }

                else -> {
                    bin.root.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        }else{
            bin.root.layoutParams.width = window.decorView.width/2
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bin.root)
        lay(resources.configuration)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val u = intent.getStringExtra("user")?: return finish()
        val s = Point()
        val se = Search(this)
        val e = ProgressDialog(this)
        e.apply{
            isIndeterminate =  true
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setMessage("Loading user data")
            setOnDismissListener {
                se.cancelAll()
            }
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }

        se.onError = {
            if(it == -1.0){
                bin.root.animate().alpha(0F)
                object : AlertDialog(this){
                    val tps = "If username that you'd enter is typo, type correctly."
                    var mo = 0
                    init {
                        setCanceledOnTouchOutside(false)
                        setIconAttribute(android.R.attr.alertDialogIcon)
                        setTitle("User not found")
                        setButton2("Go back"){_,_->finish()}
                        setMessage(tps)
                        setButton("Logs"){_,_->}
                    }

                    override fun onBackPressed() {
                        if(mo == 1){
                            getButton(DialogInterface.BUTTON_POSITIVE)?.callOnClick()
                        }else{
                            dismiss()
                            finish()
                        }
                    }

                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState);

                        val m = findViewById<TextView>(android.R.id.message)
                        m?.apply {
                            setTextIsSelectable(true)
                        }
                        getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                            if(m == null)return@setOnClickListener
                            if(mo == 0){
                                mo = 1
                                m.text = se.reason
                            }else{
                                mo = 0
                                m.text = tps
                            }
                        }
                    }
                }.show()
                if (e.isShowing) {
                    e.dismiss()
                }
            }
        }

        val copyCM = View.OnCreateContextMenuListener{ menu, v, _ ->
            menu.add("Copy text").setOnMenuItemClickListener {
                if(v is TextView){
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("text", v.text))
                }
                true
            }

            menu.add("Select text").setOnMenuItemClickListener {
                if(v is TextView){
                    val p = AlertDialog.Builder(this)
                    val sl = EditText(this)
                    p.setView(sl)
                    p.setTitle("Select text...")
                    sl.setBackgroundDrawable(null)
                    sl.setPaddingRelative(10,5,10,5)
                    p.setIconAttribute(android.R.attr.actionModeCopyDrawable)
                    p.setPositiveButton("Done", null)
                    val pe = p.create()
                    pe.window!!.setGravity(Gravity.BOTTOM)
                    sl.setTextIsSelectable(true)
                    sl.setKeyListener(null);
                    sl.setInputType(InputType.TYPE_NULL);
                    sl.text = SpannableStringBuilder(v.text)
                    pe.setOnShowListener {
                        Handler(mainLooper).postDelayed({
                            pe.setTitle("Select text")
                            sl.selectAll()
                        },2000L)

                    }
                    pe.show()
                }
                true
            }

        }

        bin.usr.setOnCreateContextMenuListener(copyCM)
        

        bin.action.let{
            it.menu.apply {
                addSubMenu("Projects") .also{p->
                    p.item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    p.add(0,0,0,"Shared").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    p.add(0,0,1,"Favorites").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                }
                addSubMenu("Users") .also { p ->
                    p.item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    p.add(0,0,2,"Followers").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    p.add(0,0,3,"Following").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                }
            }
            it.setOnMenuItemClickListener {
                if(it.hasSubMenu())return@setOnMenuItemClickListener false
                if(!::p.isInitialized){
                    Toast.makeText(this, "User not loaded!", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener false
                }
                showDialog(it.order)

                true
            }
        }



        fun s(){
            e.show()
            se.getUser(u) { d ->
                if(e.isShowing){
                    e.dismiss()
                }
                p = d
                d(p)
            }
        }

        if(savedInstanceState?.getParcelable<User>("p") != null){
            p = savedInstanceState.getParcelable("p")!!
            d(p)
        }else if(::p.isInitialized) {
            s()
        }else{
            s()
        }
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?) {

        super.onPrepareDialog(id, dialog)
    }

    override fun onCreateDialog(id: Int): Dialog? {
        return when(id){
            0 -> {
                HisProjects()
            }
            1 -> {
                HisFProjects()
            }
            2 -> {
                HisFlwe()
            }
            3 -> {
                HisFlwi()
            }
            else -> null
        }?.also{d->
            d.setOnDismissListener {
                removeDialog(id)
            }
            d.window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("Share")?.setIcon(R.drawable.share)?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)?.setOnMenuItemClickListener {
            if(!::p.isInitialized) {
                Toast.makeText(this, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            try{
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                i.putExtra(Intent.EXTRA_TEXT, "https://scratch.mit.edu/users/${p.id}/")
                startActivity(Intent.createChooser(i, "share"))
                true
            }catch (_: Exception){
                Toast.makeText(this, "User unload!", Toast.LENGTH_SHORT).show()
                false
            }
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::p.isInitialized){
            outState.putParcelable("p", p)
        }
    }

    override fun onBackPressed() {
        if(hideD.isNotEmpty()){
            try{
                fo()
                return
            }catch (_: Exception){
                Toast.makeText(this, "Back unload!", Toast.LENGTH_SHORT).show()
                return
            }
        }
        super.onBackPressed()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            if(hideD.isNotEmpty()){
                try{
                    fo()
                    return true
                }catch (_: Exception){
                    Toast.makeText(this, "Back unload!", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        lay(newConfig)

        super.onConfigurationChanged(newConfig)
    }
}