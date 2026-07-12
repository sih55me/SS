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
import android.app.DownloadManager
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.text.SpannableStringBuilder
import android.transition.Explode
import android.transition.Fade
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.MenuInflater
import android.view.ViewGroup
import android.view.Window
import android.view.animation.BounceInterpolator
import android.widget.ProgressBar
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.postDelayed
import cakar.search.MainActivity.Companion.downloadSmthUri
import cakar.search.adapter.Adapter
import cakar.search.databinding.HislistBinding
import cakar.search.databinding.PpBinding
import cakar.search.filetype.Project
import cakar.search.filetype.User
import cakar.search.wtbcore.PreviewImgPage
import coil3.imageLoader
import coil3.load
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import coil3.toUri
import coil3.transform.RoundedCornersTransformation
import com.squareup.picasso.Picasso

import kotlin.getValue

class PP: Activity(), FragmentManager.OnBackStackChangedListener {



    var onBacks: MutableList<Any> = mutableListOf()
    val con get() = Akun.Client.I

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
            sm.token = activity.intent?.getStringExtra("tkn")?:""
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
            return object : Dialog(activity, R.style.Theme_Notds_AltTab){
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


    class Intro: Fragment(){
        lateinit var p: User

        val prevp = arrayListOf<User>()
        val intent get():Intent = activity.intent

        var hideD= arrayListOf<Dialog>()
        val u get()= activity.intent?.getStringExtra("user")?:""
        val bin by lazy { PpBinding.inflate(layoutInflater) }
        fun d(s: User){
            setHasOptionsMenu(true)
            s.also {
                bin.imageView.load(it.thumb){
                    crossfade(true)
                    transformations(RoundedCornersTransformation(10F))
                }
                bin.imageView.setOnClickListener{
                    it.showContextMenu(it.x,it.y)
                }
                bin.imageView.setOnCreateContextMenuListener { menu, view, info ->
                    menu.add("Preview").setOnMenuItemClickListener {_->
                        PreviewImgPage(activity, PreviewImgPage.Get(bin.imageView.drawable, it.title)).show()
                        true
                    }

                    menu.add("Save this pic").setOnMenuItemClickListener {_->
                        activity?.downloadSmthUri(Uri.parse(it.thumb), "Downloading ${it.title}'s profile picture"){
                            if(it <= -1L){
                                Toast.makeText(activity, "Cannot download!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        true
                    }

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

        override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            setHasOptionsMenu(true)
            return bin.root
        }

        fun change(it: Fragment){
            fragmentManager
                .beginTransaction()
                .addToBackStack("")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .hide(this)
                .add(android.R.id.content, it, "sp")
                .setReorderingAllowed(true)
                .commit()
        }

        override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
            menu?.add("Shared project")?.setOnMenuItemClickListener {
                if(!::p.isInitialized) {
                    Toast.makeText(activity, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                HisProjects().also {
                    it.u = p.title
                    change(it)
                }
                true
            }
            menu?.add("Favorite project")?.setOnMenuItemClickListener {
                if(!::p.isInitialized) {
                    Toast.makeText(activity, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                HisFProjects().also {
                    it.u = p.title
                    change(it)
                }
                true
            }
            menu?.add("Followers")?.setOnMenuItemClickListener {
                if(!::p.isInitialized) {
                    Toast.makeText(activity, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                HisFlwe().also {
                    it.u = p.title
                    change(it)
                }
                true
            }
            menu?.add("Following")?.setOnMenuItemClickListener {
                if(!::p.isInitialized) {
                    Toast.makeText(activity, "User not loaded!\nId:${intent.getStringExtra("user")}", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                HisFlwi().also {
                    it.u = p.title
                    change(it)
                }
                true
            }

            super.onCreateOptionsMenu(menu, inflater)
        }


        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val se = Search(activity)
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
                    se?.cancelAll()
                }

            }
            setHasOptionsMenu(true)
            se.token = activity.intent.getStringExtra("tkn")?:""





            fun s(){
                val e = view?.startActionMode(lolod)
                se.onError = {
                    if(it == -1.0){
                        bin.root.animate().alpha(0F)
                        object : AlertDialog(activity){
                            val tps = "If username that you'd enter is typo, type correctly."
                            var mo = 0
                            init {
                                setCanceledOnTouchOutside(false)
                                setIconAttribute(android.R.attr.alertDialogIcon)
                                setTitle("User not found")
                                setButton2("Go back"){_,_->activity?.finish()}
                                setMessage(tps)
                                setButton("Logs"){_,_->}
                            }

                            override fun onBackPressed() {
                                if(mo == 1){
                                    getButton(DialogInterface.BUTTON_POSITIVE)?.callOnClick()
                                }else{
                                    dismiss()
                                    activity?.finish()
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

                    }
                }
                se.getUser(u) { d ->
                    e?.finish()
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

        override fun onSaveInstanceState(outState: Bundle?) {
            super.onSaveInstanceState(outState)
            if(::p.isInitialized){
                outState?.putParcelable("p", p)
            }
        }
    }
    

    //activity here
    var onBack = Any()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        if(savedInstanceState==null){
            fragmentManager.beginTransaction().replace(android.R.id.content, Intro()).commit()
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            fragmentManager.addOnBackStackChangedListener(this)
            onBackStackChanged()
        }



    }


    override fun onBackStackChanged() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            return
        }
        if(fragmentManager.backStackEntryCount >= 1){
            val baseB = OnBackInvokedCallback{
                fun backF(){
                    fragmentManager.popBackStackImmediate()
                    if(fragmentManager.backStackEntryCount ==0){
                        try{
                            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBack as OnBackInvokedCallback)
                        }catch (_: Exception){}
                    }

                }
                backF()

            }
            onBack = baseB
//            onBack = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)object: OnBackAnimationCallback{
//                override fun onBackInvoked() {
//                    baseB.onBackInvoked()
//                    findViewById<View>(android.R.id.content).animate().scaleX(1F).scaleY(1F).translationX(0F)
//                }
//
//                override fun onBackCancelled() {
//                    findViewById<View>(android.R.id.content).animate().scaleX(1F).scaleY(1F).translationX(0F)
//                }
//
//                override fun onBackStarted(backEvent: BackEvent) {
//                    val k = findViewById<View>(android.R.id.content).animate()
//                    if(backEvent.swipeEdge == BackEvent.EDGE_RIGHT){
//                        k.scaleX(0.7F).scaleY(0.8F).translationX(-100F)
//                    }else{
//                        k.scaleX(0.7F).scaleY(0.8F).translationX(100F)
//                    }
//                    k.setInterpolator(BounceInterpolator())
//                }
//            }else baseB
            try{
                onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT,onBack as OnBackInvokedCallback)
            }catch (_: Exception){}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("Search")?.setOnMenuItemClickListener {
            onSearchRequested()
            true
        }
        menu?.add("Share")?.setIcon(R.drawable.share)?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)?.setOnMenuItemClickListener {
            try{
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                i.putExtra(Intent.EXTRA_TEXT, "https://scratch.mit.edu/users/${intent.getStringExtra("user")}/")
                startActivity(Intent.createChooser(i, "share"))
                true
            }catch (_: Exception){
                Toast.makeText(this, "User unload!", Toast.LENGTH_SHORT).show()
                false
            }
        }
        return true
    }


    override fun onNavigateUp(): Boolean {
        onBackStackChanged()
        onBackPressed()
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {


        super.onConfigurationChanged(newConfig)
    }
}