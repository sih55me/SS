package cakar.search

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar

class CP @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0): ProgressBar(context, attrs, defStyleAttr, defStyleRes) {
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        tooltipText = "$progress / $max"
    }
}