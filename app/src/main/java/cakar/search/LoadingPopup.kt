package cakar.search

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import cakar.search.databinding.LoadingBinding

class LoadingPopup(a: Activity) : Dialog(a){
    val  bin  = LoadingBinding.inflate(layoutInflater)
    init {
        setContentView(bin.root)
        bin.close.setOnClickListener {
            dismiss()
            true
        }
    }



}