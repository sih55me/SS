package cakar.search

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.PictureInPictureParams
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.text.Html
import android.util.Rational
import android.view.ActionMode
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.text.toSpanned
import cakar.search.com.ProjectComponent
import cakar.search.databinding.ActivityProjectBinding
import cakar.search.ste.Ngatur
import cakar.search.ste.Settings


class ProjectActivity : Activity() {
    private val bin by lazy { ActivityProjectBinding.inflate(layoutInflater) }
    var isFullScreen = false
    var isMinimize = false
    var url = ""
    val s by lazy { Ngatur(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setWindowAnimations(android.R.style.Animation_Activity)
        window?.setDimAmount(0F)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        if(s.getBoolean(s.keys[0], false)){
            //auto fullscreen
            getWindow()!!.getDecorView().setSystemUiVisibility(
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            )
        }

        setContentView(bin.root)
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        val p = intent.getIntExtra("project", 0)
        if(actionBar == null){
        }else {
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        val pom = PopupMenu(this,bin.menufun).also {
            makeMenu(it.menu)
            bin.menufun.setOnTouchListener(it.dragToOpenListener)
        }
        bin.menufun.setOnClickListener {
            pom.show()
        }
        bin.main.apply{
            settings.javaScriptEnabled = true
            webChromeClient = object:WebChromeClient(){
                var ld : ActionMode? = null
                val po = ProgressBar(this@ProjectActivity, null, 0, android.R.style.Widget_Holo_ProgressBar_Horizontal)
                val lolod = object: ActionMode.Callback{

                    override fun onCreateActionMode(
                        p0: ActionMode?,
                        p1: Menu?
                    ): Boolean {
                        if(po.parent ==null){
                            p0?.customView = po
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
                        bin.main.stopLoading()
                    }

                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if(newProgress != 100){
                        if(ld == null){
                            ld = startActionMode(lolod)
                        }
                        po.max = 100
                        po.progress = newProgress
                    }else{
                        ld?.finish()
                        ld = null
                    }
                }
            }
        }
        loadP(p)

    }

    internal fun loadP(p: Int){
        val widthStage = s.getString(s.keys[1], "480")
        val heightStage = s.getString(s.keys[2], "360")
        val addonsreg = s.getStringSet(s.keys[3], setOf<String>("pause"))
        val configEmbed = s.getStringSet(s.keys[4], setOf<String>(
            "settings-button"
        ))
        val fps = s.getInt("fps", 30)
        url = "https://turbowarp.org/$p/embed?size=${widthStage}x${heightStage}&fps=$fps"
        intent.getStringExtra("tknp")?.let {
            url += "&token=$it"
        }
        if(configEmbed.isNotEmpty()){
            url += "&${configEmbed.joinToString(separator = "&")}"
        }

        if(addonsreg.isNotEmpty()){
            url += "&addons=${addonsreg.joinToString(separator = ",")}"
        }
        println(url)
        bin.main.loadUrl(url)
    }


    override fun onNewIntent(intent: Intent?) {
        val p = intent?.getIntExtra("project", 0)
        loadP(p?:0)
    }







    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        bin.main.onPause()
    }

    override fun onResume() {
        super.onResume()
        bin.main.onResume()
        val s = Ngatur(this)
        if(s.getBoolean(s.keys[0], false)){
            //auto fullscreen
            getWindow()!!.getDecorView().setSystemUiVisibility(
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            )
        }else{
            getWindow()!!.getDecorView().setSystemUiVisibility(0)
        }
        val widthStage = s.getString(s.keys[1], "480")
        val heightStage = s.getString(s.keys[2], "360")
        bin.main.evaluateJavascript("vm.setStageSize($widthStage,$heightStage) ", null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu == null)return true
        makeMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }



    fun makeMenu(menu: Menu){
        menu?.add("Play")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
            bin.main.evaluateJavascript("vm.greenFlag()", null)
            true
        }
        menu?.add("Stop")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
            bin.main.evaluateJavascript("vm.stopAll()", null)
            true
        }
        menu.add("Minimize").setCheckable(true).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
            val r = Rect()
            bin.main.getGlobalVisibleRect(r)
            enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(480,360)).setSourceRectHint(r).build())
            true
        }
        menu.add("Key")?.setOnMenuItemClickListener {
            showDialog(0)
            true
        }
        menu.addSubMenu("Developer").also {menu->
            val jsclick = MenuItem.OnMenuItemClickListener{
                bin.main.evaluateJavascript(it.title.toString()){i->
                    AlertDialog.Builder(this).setMessage(Html.fromHtml(i)).setPositiveButton("O", null).show()
                }
                true
            }
            menu.add("vm.toJSON()").setOnMenuItemClickListener{
                val d = ProgressDialog.show(this, "Javascript", "Fetchin' data", true, false)
                bin.main.evaluateJavascript(it.title.toString()){i->
                    d.dismiss()
                    val complie = ProjectComponent.fromJSON(i.replace("""\""", ""))
                    val l = ArrayAdapter(this, android.R.layout.simple_list_item_1, complie.sprites)
                    AlertDialog.Builder(this).setTitle("sss").setSingleChoiceItems(l,0,null).setPositiveButton("O", null).setNegativeButton("p") { _, _ ->
                        jsclick.onMenuItemClick(it)
                    }.show()
                }
                true
            }
            menu.add("vm").setOnMenuItemClickListener(jsclick)
            menu.add("custom").setOnMenuItemClickListener {
                val id = EditText(this)
                id.hint = "e.g. vm"
                id.maxLines = 1
                id.isSingleLine = true
                AlertDialog.Builder(this).apply {
                    setTitle("command")
                    setView(id)
                    setPositiveButton("Visit") { _, _ ->
                        bin.main.evaluateJavascript(it.title.toString()){i->
                            AlertDialog.Builder(context).setMessage(i).setPositiveButton("O", null).show()
                        }
                    }
                    setNegativeButton(android.R.string.cancel, null)
                }.show()
                true
            }
        }

        menu.addSubMenu("Other").also {menu->
            menu.add("Search").setOnMenuItemClickListener {
                onSearchRequested()
                true
            }
            menu.item!!.setOnMenuItemClickListener {
                closeOptionsMenu()
                true
            }
            menu?.add("Refresh")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
                loadP(intent.getIntExtra("project", 0))
                true
            }
            menu?.add("Settings")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.intent = Intent(this, Settings::class.java)
            menu?.add("Close")?.setOnMenuItemClickListener {
                finish()
                true
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if(!isInPictureInPictureMode){
            bin.menufun.visibility = View.VISIBLE

        }else{
            bin.menufun.visibility = View.GONE
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

    }






    override fun onCreateDialog(id: Int): Dialog? {
        if(id == 0){
            val d= KeyHelper(ContextThemeWrapper(this, 0), bin.main)
            d.setOwnerActivity(this)
            d.setOnDismissListener {
                removeDialog(id)
            }
            return d
        }
        return super.onCreateDialog(id)
    }

    override fun onDestroy() {
        bin.main.destroy()
        super.onDestroy()
    }
}