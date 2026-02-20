package cakar.search

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.app.FragmentTransaction
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import cakar.search.databinding.ActivityProjectBinding


class ProjectActivity : Activity() {
    private val bin by lazy { ActivityProjectBinding.inflate(layoutInflater) }
    lateinit var p: Project


    private class TabContentViewListener(val v: View) : ActionBar.TabListener{
        override fun onTabSelected(
            p0: ActionBar.Tab?,
            p1: FragmentTransaction?
        ) {
            v.visibility = View.VISIBLE
        }

        override fun onTabUnselected(
            p0: ActionBar.Tab?,
            p1: FragmentTransaction?
        ) {
            v.visibility = View.GONE
        }

        override fun onTabReselected(
            p0: ActionBar.Tab?,
            p1: FragmentTransaction?
        ) {

        }

    }
    var isFullScreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(bin.root)
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        val project = intent.getIntExtra("project", 0)
        val tkn = intent.getStringExtra("tkn")


        fun d(s: Project){
            s.also {
                bin.title.text = it.title
                bin.desc.text = it.desc
                bin.ins.text = it.instructions
                bin.info.text = try{
                    bin.stat.text ="❤️ ${it.uninfo["loves"]}  👁 ${it.uninfo["views"]}  ⭐️ ${it.uninfo["favorites"]}  💥 ${it.uninfo["remixes"]}" + if(it.uninfo["scratchteam"] == "true") "    ❤️‍🔥" else ""
                    bin.crea.text = it.creator
                    "Id : ${it.id}\n"
                }catch (_: Exception){
                    "Error when taking info. Try again later."
                }
                if(it.uninfo["remix@o"]?.toString()?.isNotEmpty() == true){
                    bin.ori.isEnabled = true
                    bin.ori.setOnClickListener {_->
                        try{
                            val i = Intent(this, ProjectActivity::class.java)
                            i.putExtra("project", Integer.valueOf(it.uninfo["remix@o"].toString()))
                            startActivity(i)
                        }catch (_: Exception){
                            Toast.makeText(this, "Invalid input @o", Toast.LENGTH_SHORT).show()
                        }
                    }
                    bin.remixLay.visibility = View.VISIBLE
                }
                if((it.uninfo["remix@p"]?.toString()?.isNotEmpty() == true) and (it.uninfo["remix@o"]?.equals(it.uninfo["remix@p"]) == false)){
                    bin.orire.isEnabled = true
                    bin.orire.setOnClickListener {_->
                        try{
                            val i = Intent(this, ProjectActivity::class.java)
                            i.putExtra("project", Integer.valueOf(it.uninfo["remix@p"].toString()))
                            startActivity(i)
                        }catch (_: Exception){
                            Toast.makeText(this, "Invalid input @p", Toast.LENGTH_SHORT).show()
                        }
                    }
                    bin.remixLay.visibility = View.VISIBLE
                }
            }
            actionBar?.apply {
                navigationMode = ActionBar.NAVIGATION_MODE_TABS
                mutableListOf<ActionBar.Tab>().also {
                    it.add(
                        newTab().setText("This").setContentDescription("This project").setTabListener(TabContentViewListener(bin.more))
                    )
                    if (s.desc.isNotEmpty()) {
                        it.add(
                            newTab().setText("Note").setTabListener(TabContentViewListener(bin.desc))
                        )
                    } else {
                        bin.desc.visibility = LinearLayout.GONE
                    }
                    if (s.instructions.isNotEmpty()) {
                        it.add(
                            newTab().setText("Help").setContentDescription("Instruction").setTabListener(TabContentViewListener(bin.ins))
                        )
                    } else {
                        bin.ins.visibility = LinearLayout.GONE
                    }
                }.forEach {
                    addTab(it)
                }
            }

        }

        fun s(){
            Search(this).getProject(project) { d ->
                p = d
                d(p)
            }
        }

        if((savedInstanceState != null)){
            p =savedInstanceState?.getParcelable("p")!!
            d(p)
        }else if(::p.isInitialized) {
            s()
        }else{
            s()
        }



        if(actionBar == null){
        }else{
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        bin.main.apply{
            settings.javaScriptEnabled = true
            webChromeClient = object:WebChromeClient(){
                private var mCustomView: View? = null
                private var mCustomViewCallback: CustomViewCallback? = null
                private var mOriginalOrientation = 0
                private var mOriginalSystemUiVisibility = 0

                private var isLDS = false

                var ld: ProgressDialog? = null

                public override fun onShowCustomView(view: View?, callback: CustomViewCallback) {
                    if (mCustomView != null) {
                        onHideCustomView()
                        return
                    }
                    isFullScreen = true
                    mCustomView = view
                    mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility()
                    mOriginalOrientation = getRequestedOrientation()
                    mCustomViewCallback = callback
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    (getWindow().getDecorView() as FrameLayout).addView(
                        mCustomView,
                        FrameLayout.LayoutParams(-1, -1)
                    )
                    getWindow().getDecorView().setSystemUiVisibility(
                        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    )
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if(newProgress != 100){
                        if(ld == null){
                            ld = ProgressDialog(this@ProjectActivity).also {
                                it.setOnShowListener {
                                    isLDS = true
                                }
                                it.setMessage("Loading")
                                it.setButton(getString(android.R.string.cancel)){_,_->
                                    (view?:this@apply).stopLoading()
                                }
                                it.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                                it.max = 100
                                it.progress = 0
                                it.setOnDismissListener {
                                    (view?:this@apply).stopLoading()
                                    isLDS = false
                                    ld = null
                                }
                                it.setCanceledOnTouchOutside(false)
                                it.show()
                            }
                        }else{
                            ld?.progress = newProgress
                        }
                    }else{
                        ld?.setOnDismissListener {
                            isLDS = false
                        }
                        ld?.dismiss()
                        ld = null
                    }
                }

                public override fun onHideCustomView() {
                    isFullScreen = false
                    (getWindow().getDecorView() as FrameLayout).removeView(mCustomView)
                    mCustomView = null
                    getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    mCustomViewCallback!!.onCustomViewHidden()
                }
            }
        }
        bin.crea.setOnClickListener {
            try{
                val i = Intent(this, PP::class.java)
                i.putExtra("user", p.creator)
                startActivity(i)
            }catch (_: Exception){
                Toast.makeText(this, "SOme component id unload", Toast.LENGTH_SHORT).show()
            }
        }
        bin.expanded.setOnClickListener {
            if(it.tag == false.toString()){
                it.tag = true.toString()
                it.animate().scaleY(-1f)
                bin.root.animate().translationY(-bin.mainc.height.toFloat()).withEndAction {
                    bin.mainc.visibility = View.GONE
                    bin.root.translationY = 0F
                }
            }else{
                bin.root.translationY = -bin.mainc.height.toFloat()
                bin.mainc.visibility = View.VISIBLE
                it.tag = false.toString()
                it.animate().scaleY(1f)
                bin.root.animate().translationY(0F)
            }
        }
        bin.start.setOnClickListener{
            try{
                bin.main.loadUrl("https://turbowarp.org/$project/embed?settings-button&addons=pause,remove-curved-stage-border,mute-project&autoplay")
                it.animate().scaleY(-0F).withEndAction {
                    it.visibility = View.GONE
                }
            }catch (_: Exception){
                it.animate().scaleY(0.5F).withEndAction {
                    it.animate().scaleY(1F).withEndAction {
                        Toast.makeText(this, "Some component project unload", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }


        // Add an ID to your root layout
        bin.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            bin.root.getWindowVisibleDisplayFrame(r)

            val screenHeight = bin.root.rootView.height
            val heightDiff = screenHeight - r.bottom

            // Define a threshold (e.g., 128 dp, converted to pixels)
            // A generic soft keyboard is typically at least 4 rows of keys
            val softKeyboardHeightThreshold = 128 * resources.displayMetrics.density

            onPictureInPictureModeChanged(heightDiff > softKeyboardHeightThreshold, resources.configuration)
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::p.isInitialized){
            outState.putParcelable("p", p)
        }
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("Refresh")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
            bin.main.reload()
            true
        }
        menu?.add("Minimize")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
            enterPictureInPictureMode()
            true
        }
        menu?.add("Key")?.setOnMenuItemClickListener {
            val d= KeyHelper(this, bin.main)
            d.show()
            true
        }
        menu?.add("Close")?.setOnMenuItemClickListener {
            finish()
            true
        }
        menu?.add("Share")?.setIcon(R.drawable.share)?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)?.setOnMenuItemClickListener {
            try{
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, "https://scratch.mit.edu/projects/${p.id}/")
                startActivity(Intent.createChooser(i, "share"))
                true
            }catch (_: Exception){
                Toast.makeText(this, "Project unload!", Toast.LENGTH_SHORT).show()
                false
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if(isInPictureInPictureMode){
            actionBar?.hide()
            (bin.mainc.layoutParams as LinearLayout.LayoutParams).weight = 0F
            bin.expanded.visibility = View.GONE
            bin.tabcontent.visibility = View.GONE
        }else{
            actionBar?.show()
            (bin.mainc.layoutParams as LinearLayout.LayoutParams).weight = 1F
            bin.tabcontent.visibility = View.VISIBLE
            bin.expanded.visibility = View.VISIBLE
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    override fun onDestroy() {
        bin.main.destroy()
        super.onDestroy()
    }
}