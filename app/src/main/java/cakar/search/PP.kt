package cakar.search

import android.app.ActionBar
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.os.Build
import android.os.Handler
import android.text.SpannableStringBuilder
import android.transition.Explode
import android.transition.Fade
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import cakar.search.adapter.Adapter
import cakar.search.databinding.HislistBinding
import cakar.search.databinding.PpBinding
import cakar.search.filetype.Project
import cakar.search.filetype.User
import com.squareup.picasso.Picasso

import kotlin.getValue

class PP: Activity() {

    lateinit var p: User
    
    val prevp = arrayListOf<User>()

    var hideD= arrayListOf<Dialog>()

    val bin by lazy { PpBinding.inflate(layoutInflater) }

    var onBacks: MutableList<Any> = mutableListOf()


    companion object{
        private fun ListView.hU(l: List<User>) {
            onItemClickListener = AdapterView.OnItemClickListener { _, _, ind, _ ->
                val i = Intent(context, PP::class.java)
                i.putExtra("user", l[ind].title)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }
        }
    }


    private var openPage = 0

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
    class HisFlwi: Listo() {
        private val l = arrayListOf<User>()
        override fun onSaveInstanceState(outState: Bundle) {
            outState.also {
                it.putParcelableArrayList("l", l)
            }
            super.onSaveInstanceState(outState)
        }


        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            dialog?.setTitle("Followers")
            val d = ArrayAdapter(activity,android.R.layout.simple_list_item_1, arrayListOf<String>()).also {
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
                    sm.fetchFollowingFromUser(u) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }

            hb.next.setOnClickListener {
                if(d.count == 10){
                    ofs += 10
                    l.clear()
                    d.clear()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    sm.fetchFollowingFromUser(u, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    ofs -= 10
                    l.clear()
                    d.clear()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    sm.fetchFollowersFromUser(u, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    class HisFlwe: Listo() {
        private val l = arrayListOf<User>()

        override fun onSaveInstanceState(outState: Bundle) {
            outState.also {
                it.putParcelableArrayList("l", l)
            }
            super.onSaveInstanceState(outState)
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            dialog?.setTitle("Followers")
            val d = ArrayAdapter(activity,android.R.layout.simple_list_item_1, arrayListOf<String>()).also {
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
                    sm.fetchFollowersFromUser(u) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.next.setOnClickListener {
                if(d.count == 10){
                    ofs += 10
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    l.clear()
                    d.clear()
                    sm.fetchFollowersFromUser(u, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    ofs -= 10
                    l.clear()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    d.clear()
                    sm.fetchFollowersFromUser(u, ofs) {
                        l.add(it)
                        d.add(it.title)
                        d.notifyDataSetChanged()
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    class HisProjects: Listo() {
        init{
            showsDialog = false
        }
        private val d by lazy{ Adapter(activity, arrayListOf(), true)}

        override fun onSaveInstanceState(outState: Bundle) {
            outState.also {
                it.putParcelableArrayList("l", d.data)
            }
            super.onSaveInstanceState(outState)
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            hb.list.adapter = d
            d.data.clear()
            savedInstanceState?.getParcelableArrayList<Project>("l") .also {
                if (!it.isNullOrEmpty()) {
                    d.data.addAll(it)
                    d.notifyDataSetChanged()
                }else{
                    sm.searchProjectFromUser(u){
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = false
                    }
                }
            }


            hb.next.setOnClickListener {
                if(d.count == 10){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs += 10
                    d.flush()
                    sm.searchProjectFromUser(u, ofs) {
                        hb.pr.isIndeterminate = false
                        hb.pr.progress +=1
                        d.setdata(it)
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs -= 10
                    d.flush()
                    sm.searchProjectFromUser(u, ofs) {
                        hb.pr.isIndeterminate = false
                        hb.pr.progress +=1
                        d.setdata(it)
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    class HisFProjects: Listo() {
        init{
            showsDialog = false
        }
        private val d by lazy{ Adapter(activity, arrayListOf(), true)}

        override fun onSaveInstanceState(outState: Bundle) {
            outState.also {
                it.putParcelableArrayList("l", d.data)
            }
            super.onSaveInstanceState(outState)
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            hb.list.adapter = d
            d.data.clear()
            savedInstanceState?.getParcelableArrayList<Project>("l") .also {
                if (!it.isNullOrEmpty()) {
                    d.data.addAll(it)
                    d.notifyDataSetChanged()
                }else{
                    sm.searchFavProjectFromUser(u) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = false
                    }
                }
            }
            hb.next.setOnClickListener {
                if(d.count == 10){
                    ofs += 10
                    d.flush()
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    sm.searchFavProjectFromUser(u, ofs) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
            hb.prev.setOnClickListener {
                if(ofs != 0){
                    hb.pr.progress = 0
                    hb.pr.isIndeterminate = true
                    ofs -= 10
                    d.flush()
                    sm.searchFavProjectFromUser(u, ofs) {
                        d.setdata(it)
                        hb.pr.isIndeterminate = false
                        hb.pr.progress += 1
                        hb.next.isEnabled = hb.pr.progress == 10
                        hb.prev.isEnabled = ofs != 0
                    }
                }
            }
        }
    }
    open class Listo(): DialogFragment(){

        var u : String= ""
        val sm by lazy{
            Search(activity)
        }
        val hb  by lazy { HislistBinding.inflate(LayoutInflater.from(activity)) }
        var onButtonClick = DialogInterface.OnClickListener{_,_->}

        override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            if (hb.root.getParent() != null) { (( hb.root).getParent() as ViewGroup).removeView(hb.root); }
            return (hb.root)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
            return object : Dialog(activity, R.style.Theme_SS_ProPre){
                init {
                    window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
                    window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    window!!.setDimAmount(0.4F)
                    setCanceledOnTouchOutside(true)
                    when(resources.configuration.orientation){
                        Configuration.ORIENTATION_LANDSCAPE -> {
                            window!!.setLayout(window!!.windowManager.defaultDisplay.width/2, window!!.windowManager.defaultDisplay.height)
                            window!!.setGravity(Gravity.END)
                        }
                        Configuration.ORIENTATION_PORTRAIT -> {
                            window!!.setLayout(window!!.windowManager.defaultDisplay.width, (window!!.windowManager.defaultDisplay.height/1.2).toInt())
                            window!!.setGravity(Gravity.BOTTOM)
                        }
                    }

                }

                override fun onCreateOptionsMenu(menu: Menu): Boolean {
                    menu.add("Close").setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                        this@Listo.dismiss()
                        true
                    }
                    return super.onCreateOptionsMenu(menu)
                }
            }
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            u = (savedInstanceState?.getCharSequence("usr", u)?.toString()?:u).also {
                if(it.isNullOrEmpty()){
                    Toast.makeText(activity, "no user\n$it", Toast.LENGTH_SHORT).show()
                }
            }
            ofs = (savedInstanceState?.getInt("ofs")?:0).also {
                hb.pr.isIndeterminate = false
                hb.pr.progress = it
            }
            sm.limitGet = 10
            sm.onError = {
                hb.loa.text = "Error when fetching data"
                Log.e("Error@rslt", sm.resultF)
                Log.e("Error@type", it.toString())
                Log.e("Error@reason", sm.reason)
            }
            hb.list.emptyView = hb.loa
        }

        
        var ofs = 0

        override fun onSaveInstanceState(outState: Bundle) {
            val `in` = SpannableStringBuilder(u)
            outState.also {
                it.putCharSequence("usr", `in`)
                it.putInt("ofs", ofs)
            }
        }


        override fun onDestroyView() {
            super.onDestroyView()
            hb.list.adapter = null
        }
    }
    
    fun d(s: User){
        s.also {
            Picasso.get().load(it.thumb).into(bin.imageView)
            bin.imageView.setOnClickListener { _->

            }

            bin.usr.text = it.title
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

    }

    val u get()= intent.getStringExtra("user")?:""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(bin.root)
        lay(resources.configuration)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)

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


        bin.usr.customSelectionActionModeCallback = object : ActionMode.Callback{
            override fun onCreateActionMode(
                p0: ActionMode?,
                menu: Menu?
            ): Boolean {

                return true
            }

            override fun onPrepareActionMode(
                p0: ActionMode?,
                menu: Menu?
            ): Boolean {
                if(menu == null)return true
                val meUser = listOf<String>("stablecat", "zombiew358")
                val spea = AlertDialog.Builder(this@PP).setTitle("What's does it mean?")
                if(bin.usr.text in meUser){
                    spea.setMessage("He/She is developer's alt account (of this app)")
                    val di = spea
                        .setNegativeButton("Oh, i see.", null)
                        .setPositiveButton("But who?"){_,_->
                            val i = Intent(this@PP, PP::class.java)
                            i.putExtra("user", "wiwolf360")
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(i)
                        }
                        .create()
                    menu.add("What does it mean?").setOnMenuItemClickListener {
                        di.show()
                        true
                    }
                }else if(bin.usr.text == "wiwolf360"){
                    spea.setMessage("Developer of this app")
                    val di = spea.setPositiveButton("Oh, i see", null).create()
                    menu.add("What does it mean?").setOnMenuItemClickListener {
                        di.show()
                        true
                    }
                }
                return true
            }

            override fun onActionItemClicked(
                p0: ActionMode?,
                p1: MenuItem?
            ): Boolean {
                return bin.usr.onTextContextMenuItem(p1?.itemId?:0)
            }

            override fun onDestroyActionMode(p0: ActionMode?) {

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







    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("Shared project")?.setOnMenuItemClickListener {
            if(!::p.isInitialized) {
                Toast.makeText(this, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            HisProjects().also {
                it.u = p.title
            }.show(fragmentManager, "sp")
            true
        }
        menu?.add("Favorite project")?.setOnMenuItemClickListener {
            if(!::p.isInitialized) {
                Toast.makeText(this, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            HisFProjects().also {
                it.u = p.title
            }.show(fragmentManager, "fp")
            true
        }
        menu?.add("Followers")?.setOnMenuItemClickListener {
            if(!::p.isInitialized) {
                Toast.makeText(this, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            HisFlwe().also {
                it.u = p.title
            }.show(fragmentManager, "flwe")
            true
        }
        menu?.add("Following")?.setOnMenuItemClickListener {
            if(!::p.isInitialized) {
                Toast.makeText(this, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            HisFlwi().also {
                it.u = p.title
            }.show(fragmentManager, "flwi")
            true
        }
        menu?.add("Search")?.setOnMenuItemClickListener {
            onSearchRequested()
            true
        }
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