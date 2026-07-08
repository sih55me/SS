package cakar.search.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import cakar.search.R


class NumberPickerPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): DialogPreference(context, attrs) {


    var min = 0
    private val change = object: SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(
            p0: SeekBar?,
            p1: Int,
            p2: Boolean
        ) {
            inNow = p1
            dialog?.findViewById<TextView>(R.id.info)?.setText("* $p1 / $max")
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {

        }

    }
    var summaryForValue = true
    set(value) {
        internalSet(now, )
        field = value
    }

    //indialog
    private var inNow= 0

    var now
    get() = preferenceManager.getSharedPreferences().getInt(key, min)
    set(value) {
        internalSet(value)
    }

    var max = 1

    init {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference)
        now = ta.getInt(R.styleable.SeekbarPreference_android_progress, 0)
        max = ta.getInt(R.styleable.SeekbarPreference_android_max, 1)
        ta.recycle()
    }

    private fun internalSet(value: Int) {
        //do not change seekbar if true
        summary = if(summaryForValue){
            value.toString()
        }else{
            ""
        }
        if (callChangeListener(value)) {


            persistInt(value)
            notifyDependencyChange(shouldDisableDependents())
            notifyChanged()
        }
    }

    override fun showDialog(state: Bundle?) {
        super.showDialog(state)
    }


    override fun onCreateDialogView(): View {
        return LayoutInflater.from(context).inflate(R.layout.volume_layout, null)
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        view?.findViewById<ImageView>(R.id.icon)?.setImageDrawable(icon)
        view?.findViewById<TextView>(R.id.info)?.setText("$now / $max")
        val s = view?.findViewById<View>(R.id.volumeseek)
        if(s is SeekBar) {
            s.apply {
                setOnSeekBarChangeListener(change)
                val prmi = this@NumberPickerPreference.min
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    min = prmi
                }
                max = this@NumberPickerPreference.max

                progress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) now else now - prmi
            }
        }
    }



    override fun onDialogClosed(positiveResult: Boolean) {
        if(positiveResult){
            internalSet(inNow)
        }
        super.onDialogClosed(positiveResult)
    }



    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getInt(index, 10)
    }



    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        now = (if (restoreValue) getPersistedInt(now) else defaultValue as Int)
    }






    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        val myState: SavedState = SavedState(superState)

        myState.min = min

        myState.now = now
        myState.max = max
        return myState
    }







    private class SavedState : BaseSavedState {
        var min = 0
        var now = 0
        var max = 1
        constructor(source: Parcel): super(source){
            min = source.readInt()
            now = source.readInt()
            max = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState)



        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.apply{
                writeInt(min)
                writeInt(now)
                writeInt(max)
            }
        }


        companion object CREATOR: Parcelable.Creator<SavedState> {
            override fun createFromParcel(`in`: Parcel): SavedState {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}