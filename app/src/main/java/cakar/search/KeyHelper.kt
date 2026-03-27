package cakar.search

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.PopupWindow
import android.widget.SeekBar
import cakar.search.databinding.KeyboardBinding
import cakar.search.databinding.KysttBinding


class KeyHelper(private val a: Context, val v : WebView) : Dialog(a, R.style.Theme_SS_Pro), KeyboardView.OnKeyboardActionListener {

    private val binding by lazy { KeyboardBinding.inflate(layoutInflater) }
    private val setbin by lazy { KysttBinding.inflate(layoutInflater) }
    private lateinit var keyboard: Keyboard
    private var isMinimize = false
    private var isDraggable = true

    val keyDict = mapOf(
        "ArrowUp" to 38,
        "ArrowDown" to 40,
        "ArrowLeft" to 37,
        "ArrowRight" to 39,
        " " to 32,
        "w" to 87,
        "a" to 65,
        "s" to 83,
        "d" to 68,
        "z" to 90,
        "x" to 88,
        "b" to 66,
        "c" to 67,
        "e" to 69,
        "f" to 70,
        "g" to 71,
        "h" to 72,
        "i" to 73,
        "j" to 74,
        "k" to 75,
        "l" to 76,
        "m" to 77,
        "n" to 78,
        "o" to 79,
        "p" to 80,
        "q" to 81,
        "r" to 82,
        "t" to 84,
        "u" to 85,
        "v" to 86,
        "y" to 89,
        "0" to 48,
        "1" to 49,
        "2" to 50,
        "3" to 51,
        "4" to 52,
        "5" to 53,
        "6" to 54,
        "7" to 55,
        "8" to 56,
        "9" to 57,
        "Enter" to 13
    )

    init {
        window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window!!.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
        window!!.setDimAmount(0F)
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        window!!.setGravity(Gravity.CENTER)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        if(ownerActivity != null){
            window!!.decorView.systemUiVisibility = ownerActivity!!.window!!.decorView.systemUiVisibility
        }
    }


    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        setupFloatingWindowMovement()
        super.onCreate(savedInstanceState)
        val keyboardView = binding.key
        window!!.setDimAmount(0F)
        keyboard = Keyboard(a, R.xml.keyjs)
        keyboardView.keyboard = keyboard
        val morePopup = Dialog(a, R.style.Theme_SS_Pro)
        if(ownerActivity != null){
            window!!.decorView.systemUiVisibility = ownerActivity!!.window!!.decorView.systemUiVisibility
            morePopup.window!!.decorView.systemUiVisibility = ownerActivity!!.window!!.decorView.systemUiVisibility
        }
        morePopup.window!!.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        morePopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        morePopup.setContentView(setbin.root)
        setbin.close.setOnClickListener {
            morePopup.dismiss()
        }
        setbin.minimize.setOnCheckedChangeListener { button, bool ->
            isMinimize = bool
            if(bool){
                binding.key.visibility = View.GONE
            }else{
                binding.key.visibility = View.VISIBLE
            }
        }
        setbin.isdrg.setOnCheckedChangeListener { button, bool ->
            isDraggable = bool
        }
        setbin.trans.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                window!!.attributes.alpha =(progress.toFloat()/10F)
                window!!.windowManager.updateViewLayout(window!!.decorView, window!!.attributes)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar?.progress == 0){
                    morePopup.dismiss()
                    dismiss()
                }
            }

        })
        binding.more.setOnClickListener {v->
            val window = morePopup.window!!
            window.attributes.also {
                it.x = this.window!!.attributes.x
                it.y = this.window!!.attributes.y
                it.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.setGravity(Gravity.CENTER)
            if(!morePopup.isShowing){
                if(!isMinimize){
                    window.setLayout(this.window!!.decorView.width, this.window!!.decorView.height)
                }
                morePopup.show()
            }else{
                morePopup.dismiss()
            }
        }
        binding.close.setOnClickListener {
            dismiss()
        }
        keyboardView.setOnKeyboardActionListener(this)
    }
    private fun playClick(keyCode: Int) {
        val am = a.getSystemService(AUDIO_SERVICE) as AudioManager
        when (keyCode) {
            13 -> am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            Keyboard.KEYCODE_DONE, 10 -> am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            Keyboard.KEYCODE_DELETE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
            else -> am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        playClick(primaryCode)

    }

    private fun setupFloatingWindowMovement() {
        var initialX = 0.0
        var initialY = 0.0
        var initialTouchX = 0.0
        var initialTouchY = 0.0

        // Set a touch listener to detect dragging
        binding.drag!!.setOnTouchListener({ view, event ->
            if(!isDraggable)return@setOnTouchListener true
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

    fun resolverKey(primaryCode: Int, code: String): Int?{
        return when (primaryCode) {
            Keyboard.KEYCODE_DONE -> keyDict["Enter"]
            else -> keyDict[code]
        }
    }

    fun dispatch(down: Boolean, primaryCode: Int){
        var code = primaryCode.toChar().toString()
        val kc = when (primaryCode) {
            -4 -> {
                code = "Enter"
                13
            }
            13 -> {
                code = " "
                32
            }
            else -> {
                code = when(primaryCode){
                    38 -> "ArrowUp"
                    40 -> "ArrowDown"
                    37 -> "ArrowLeft"
                    39 -> "ArrowRight"
                    else -> code
                }
                resolverKey(primaryCode, code)
            }
        }
        v.evaluateJavascript("vm.postIOData('keyboard',{key:'$code', keyCode:$kc, isDown: $down})", object: ValueCallback<String> {
            override fun onReceiveValue(value: String?) {
                Log.d("KEY@press", value.toString())
            }

        })
    }

    // Other required overrides for OnKeyboardActionListener
    override fun onPress(primaryCode: Int) {
//        val keyEvent = keyDict.filter { it.value == primaryCode }.keys.firstOrNull() ?: return
//        val keyCode= keyDict[keyEvent] ?: return
        dispatch(true, primaryCode)
    }
    override fun onRelease(primaryCode: Int) {
        dispatch(false, primaryCode)
    }
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
