package cakar.search

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.FragmentTransaction
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cakar.search.databinding.ActivityProjectBinding
import cakar.search.filetype.Project


class ProjectActivity : Activity() {
    private val bin by lazy { ActivityProjectBinding.inflate(layoutInflater) }
    var isFullScreen = false
    var isMinimize = false
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setWindowAnimations(android.R.style.Animation_Activity)
        setupFloatingWindowMovement()
        window?.setDimAmount(0F)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(bin.root)
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        val p = intent.getIntExtra("project", 0)
        window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        if(actionBar == null){
        }else{
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        bin.close.setOnClickListener {
            finish()
        }

        bin.menu.setOnCreateContextMenuListener { menu, _, _ ->
            makeMenu(menu)
        }
        bin.more.setOnClickListener {
            bin.menu.showContextMenu()
        }
        bin.main.apply{
            settings.javaScriptEnabled = true
            webChromeClient = object:WebChromeClient(){
                private var mCustomView: View? = null
                private var mCustomViewCallback: CustomViewCallback? = null
                private var mOriginalOrientation = 0
                private var mOriginalSystemUiVisibility = 0

                private var isLDS = false

                var lp = Dialog(this@ProjectActivity, android.R.style.Theme_DeviceDefault_NoActionBar_Overscan)
                val f = FrameLayout(this@ProjectActivity)
                init {
                    lp.window!!.attributes.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
                    lp.window!!.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    lp.setContentView(f)
                }

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
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE)
                    (f).addView(
                        mCustomView,
                        FrameLayout.LayoutParams(-1, -1)
                    )
                    lp.show()
                    lp.getWindow()!!.getDecorView().setSystemUiVisibility(
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
                    (f).removeView(mCustomView)
                    mCustomView = null
                    lp.dismiss()
                    lp.getWindow()!!.getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility)
                    setRequestedOrientation(mOriginalOrientation)
                    mCustomViewCallback!!.onCustomViewHidden()
                }
            }
        }
        bin.main.loadUrl("https://turbowarp.org/$p/embed?settings-button&addons=pause,remove-curved-stage-border,mute-project")

    }


    override fun onNewIntent(intent: Intent?) {
        val p = intent?.getIntExtra("project", 0)
        bin.main.loadUrl("https://turbowarp.org/$p/embed?settings-button&addons=pause,remove-curved-stage-border,mute-project")
    }
    private fun setupFloatingWindowMovement() {
        var initialX = 0.0
        var initialY = 0.0
        var initialTouchX = 0.0
        var initialTouchY = 0.0

        // Set a touch listener to detect dragging
        bin.drag.setOnTouchListener({ view, event ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    // Record the initial position and touch points when the touch starts
                    initialX = window!!.attributes.x.toDouble()
                    initialY = window!!.attributes.y.toDouble()
                    initialTouchX = event.getRawX().toDouble()
                    initialTouchY = event.getRawY().toDouble()
                }

                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new position of the window based on the movement of the touch
                    window!!.attributes.x = ((initialX + event.getRawX()) - initialTouchX).toInt()
                    window!!.attributes.y = ((initialY + event.getRawY()) - initialTouchY).toInt()
                    // Update the layout
                    window!!.windowManager.updateViewLayout(window!!.decorView, window!!.attributes)
                }
            }
            false
        })
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
        if(menu == null)return true
        makeMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun makeMenu(menu: Menu){
        menu?.add("Refresh")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
            bin.main.reload()
            true
        }
        menu.add("Minimize").setCheckable(true).setChecked(isMinimize).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)?.setOnMenuItemClickListener {
            isMinimize = !isMinimize
            if(isMinimize){
                bin.main.visibility = View.GONE
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            }else {
                bin.main.visibility = View.VISIBLE
                window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            }
            true
        }
        menu?.add("Key")?.setOnMenuItemClickListener {
            showDialog(0)
            true
        }
        menu?.add("Close")?.setOnMenuItemClickListener {
            finish()
            true
        }
    }




    override fun onCreateDialog(id: Int): Dialog? {
        if(id == 0){
            val d= KeyHelper(ContextThemeWrapper(this, 0), bin.main)
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