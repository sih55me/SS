package cakar.search

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import cakar.search.adapter.Adapter
import cakar.search.databinding.HislistBinding
import cakar.search.databinding.PpBinding
import com.bumptech.glide.Glide
import kotlin.getValue

class PP: Activity() {

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
            Search(this@PP).fetchFollowingFromUser(p.title){
                l.add(it)
                d.add(it.title)
                d.notifyDataSetChanged()
                hb.pr.isIndeterminate = false
                hb.pr.progress += 1
            }
            getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
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
                    }
                }
            }
            getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener {
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
                    }
                }
            }
        }
    }
    inner class HisFlwe: ListDialog(this) {
        private val l = arrayListOf<User>()
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
            Search(this@PP).fetchFollowersFromUser(p.title){
                l.add(it)
                d.add(it.title)
                d.notifyDataSetChanged()
                hb.pr.isIndeterminate = false
                hb.pr.progress += 1
            }
            getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
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
                    }
                }
            }
            getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener {
                if(ofs != 0){
                    ofs -= 20
                    l.clear()
                    d.clear()
                    Search(this@PP).fetchFollowersFromUser(p.title, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        this.setTitle("Followers | ${d.count}")
                    }
                }
            }
        }
    }
    inner class HisProjects: ListDialog(this) {
        init {
            setTitle("Shared project")
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val d = Adapter(this@PP, arrayListOf(), true).also {
                hb.list.adapter = it
            }
            Search(this@PP).searchProjectFromUser(p.title){
                d.setdata(it)
                hb.pr.isIndeterminate = false
                hb.pr.progress += 1
            }
            getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                if(d.count == 20){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs += 20
                    d.flush()
                    Search(this@PP).searchProjectFromUser(p.title, ofs) {
                        hb.pr.isIndeterminate = false
                        hb.pr.progress +=1
                        d.setdata(it)
                    }
                }
            }
            getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener {
                if(ofs != 0){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs -= 20
                    d.flush()
                    Search(this@PP).searchProjectFromUser(p.title, ofs) {
                        hb.pr.isIndeterminate = false
                        hb.pr.progress +=1
                        d.setdata(it)
                    }
                }
            }
        }
    }
    inner class HisFProjects: ListDialog(this) {
        init {
            setTitle("Fav project")
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val d = Adapter(this@PP, arrayListOf(), true).also {
                hb.list.adapter = it
            }
            Search(this@PP).searchFavProjectFromUser(p.title){
                d.setdata(it)
                hb.pr.isIndeterminate = false
                hb.pr.progress += 1
            }
            getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                if(d.count == 20){
                    ofs += 20
                    d.flush()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    Search(this@PP).searchFavProjectFromUser(p.title, ofs) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                    }
                }
            }
            getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener {
                if(ofs != 0){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs -= 20
                    d.flush()
                    Search(this@PP).searchFavProjectFromUser(p.title, ofs) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                    }
                }
            }
        }
    }
    open class ListDialog(a: Activity): AlertDialog(a){
        val hb  = HislistBinding.inflate(LayoutInflater.from(context))
        var onButtonClick = DialogInterface.OnClickListener{_,_->}
        init {
            setView(hb.root)
            setButton("Next", onButtonClick)
            setButton2("Previous", onButtonClick)
            setButton3("Close", onButtonClick)
        }


        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menu.add("Close").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                dismiss()
                true
            }
            return super.onCreateOptionsMenu(menu)
        }
        var ofs = 0


        override fun dismiss() {
            hb.list.adapter = null
            super.dismiss()
        }
    }
    
    fun d(s: User){
        s.also {
            Glide.with(this).load(it.thumb).into(bin.imageView)
            bin.usr.text = it.title
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

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bin.root)
        if(actionBar == null){
        }else{
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        val u = intent.getStringExtra("user")?: return finish()
        val s = Point()
        windowManager.defaultDisplay.getSize(s)
        if(s.x > s.y) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }else{
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        

        bin.action.let{
            it.menu.apply {
                add("Shared project").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                add("Followers").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                add("Following").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                add("Favorite project").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
            it.orientation = LinearLayout.VERTICAL
            it.requestLayout()
            it.setOnMenuItemClickListener {
                if(!::p.isInitialized){
                    Toast.makeText(this, "User not loaded!", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener false
                }
                val o :((Dialog) -> Unit) = {d->
                    hideD.add(d)
                }
                when(it.title){
                    "Shared project" -> {
                        HisProjects().show()
                    }
                    "Favorite project" -> {
                        HisFProjects().show()
                    }
                    "Followers" -> {
                        HisFlwe().also{ d->
                            d.show()
                        }
                    }
                    "Following" -> {
                        HisFlwi().also { d ->
                            d.show()
                        }
                    }
                    else ->{
                        Toast.makeText(this, "Coming soon for ${it.title} section", Toast.LENGTH_SHORT).show()
                    }
                }

                true
            }
        }



        fun s(){
            Search(this).getUser(u) { d ->
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



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("Share")?.setIcon(R.drawable.share)?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)?.setOnMenuItemClickListener {
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
                Toast.makeText(this, "Project unload!", Toast.LENGTH_SHORT).show()
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
}