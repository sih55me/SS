package cakar.search

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import cakar.search.databinding.BottomSheetHelpBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.android.material.sidesheet.SideSheetCallback

open class ActivityModalSheet(activity: Activity): AppCompatDialog(activity, R.style.Theme_SS) {
    private var modalSheetLay = BottomSheetHelpBinding.inflate(layoutInflater)
    private val bottomSheetBehavior = BottomSheetBehavior.from(modalSheetLay.konten)
    init {
        super.setContentView(modalSheetLay.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheetBehavior as BottomSheetBehavior<*>
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Respond to state changes
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        dismiss()
                    }

                    // ... other states like STATE_COLLAPSED, STATE_DRAGGING, etc.
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Respond to dragging events
            }
        })
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.skipCollapsed = true
        modalSheetLay.dismiss.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }


    override fun setContentView(layoutResID: Int) {
        setContentView(layoutInflater.inflate(layoutResID, null))
    }

    override fun setContentView(view: View) {
        modalSheetLay.konten.addView(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        modalSheetLay.konten.addView(view, params)
    }
}