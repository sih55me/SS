package cakar.search

import android.content.Context
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar

class NotFloatingAMLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0): FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        return super.startActionMode(callback, ActionMode.TYPE_PRIMARY)
    }

    override fun startActionModeForChild(
        originalView: View?,
        callback: ActionMode.Callback?,
        type: Int
    ): ActionMode? {
        return super.startActionModeForChild(originalView, callback,ActionMode.TYPE_PRIMARY)
    }
}