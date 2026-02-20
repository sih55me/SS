package cakar.search

import android.app.Activity
import android.app.Dialog
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.BaseInputConnection
import android.widget.EditText
import android.widget.Toast
import cakar.search.databinding.KeyboardBinding


class KeyHelper(private val a: Activity, val v : View) : Dialog(a), KeyboardView.OnKeyboardActionListener {

    private val binding by lazy { KeyboardBinding.inflate(layoutInflater) }
    private lateinit var keyboard: Keyboard

    init {
        window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window!!.setDimAmount(0F)
        window!!.setGravity(Gravity.BOTTOM)
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyboardView = binding.key
        window!!.setDimAmount(0F)
        keyboard = Keyboard(a, R.xml.key)
        keyboardView.keyboard = keyboard
        binding.back.setOnClickListener {
            dismiss()
        }
        keyboardView.setOnKeyboardActionListener(this)
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = BaseInputConnection(v, false)
        when (primaryCode) {
            // Handle arrow keys by simulating hardware key events
            21, 22, 19, 20, 62 -> {
                a.window?.superDispatchKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        primaryCode
                    )
                )
                Handler(a.mainLooper).postDelayed({
                    a.window?.superDispatchKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_UP,
                            primaryCode
                        )
                    )
                },1000L)

            }
            // Handle other keys (e.g., character input)
            else -> {
                val code = primaryCode.toChar()
                ic.commitText(code.toString(), 1)
            }
        }
    }

    // Other required overrides for OnKeyboardActionListener
    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {
        a.window?.superDispatchKeyEvent(
            KeyEvent(
                KeyEvent.ACTION_UP,
                primaryCode
            )
        )
    }
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
