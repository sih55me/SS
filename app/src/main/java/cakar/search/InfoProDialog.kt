package cakar.search

import android.app.Activity
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TabHost
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import cakar.search.databinding.PinfoBinding
import cakar.search.filetype.Project
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.getstream.photoview.PhotoView
import io.getstream.photoview.dialog.PhotoViewDialog

class InfoProDialog()  : DialogFragment(){
    var itemdata : Project? = null
    private set
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        return object : AppCompatDialog(activity, R.style.Theme_SS_ProPre){
            init {
                window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
                when(resources.configuration.orientation){
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        window!!.setLayout(window!!.windowManager.defaultDisplay.width/2, window!!.windowManager.defaultDisplay.height)
                        window!!.setGravity(Gravity.END)
                    }

//                    Configuration.ORIENTATION_PORTRAIT -> {
//                        window!!.setLayout(window!!.windowManager.defaultDisplay.width, (window!!.windowManager.defaultDisplay.height).toInt()
//                        )
////                        window!!.setGravity(Gravity.BOTTOM)
//                    }
                }
                setCanceledOnTouchOutside(true)
                window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window!!.setDimAmount(0.6F)
            }

            override fun show() {
                super.show()
                supportActionBar?.elevation = 0F

            }
            override fun onCreateOptionsMenu(menu: Menu): Boolean {
                menu.add(0,0,2,"Close").setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                    this@InfoProDialog.dismiss()
                    true
                }
                menu.addSubMenu(0,0,1,"Options").also {mo->
                    mo.item.setIcon(R.drawable.more).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    if (itemdata != null) {
                        mo.apply {
                            add("Share")
                                ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                                ?.setOnMenuItemClickListener {
                                    try {
                                        val i = Intent(Intent.ACTION_SEND)
                                        i.type = "text/plain"
                                        i.putExtra(
                                            Intent.EXTRA_TEXT,
                                            "https://scratch.mit.edu/projects/${itemdata!!?.id}/"
                                        )
                                        activity.startActivity(
                                            Intent.createChooser(
                                                i,
                                                "share"
                                            )
                                        )
                                        true
                                    } catch (_: Exception) {
                                        Toast.makeText(
                                            activity,
                                            "Project unload!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        false
                                    }
                                }
                            add("Learn more").setOnMenuItemClickListener {
                                try {
                                    val i = Intent(
                                        Intent.ACTION_VIEW,
                                        "https://scratch.mit.edu/projects/${itemdata!!?.id}/".toUri()
                                    )
                                    activity.startActivity(
                                        Intent.createChooser(
                                            i,
                                            "share"
                                        )
                                    )
                                    true
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        activity,
                                        "Project unload!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    false
                                }
                                true
                            }
                            add("Status").setOnMenuItemClickListener {
                                Builder(activity).setTitle("Status")
                                    .setMessage(
                                        "Visibility : ${itemdata!!.uninfo["visibility"]}\nIs Public : ${itemdata!!.uninfo["public"]}\n Publish : ${
                                            itemdata?.uninfo?.get(
                                                "posted"
                                            )
                                        }"
                                    )
                                    .setPositiveButton(android.R.string.ok, null)
                                    .create().also {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            it!!.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                                            it!!.window?.attributes?.blurBehindRadius = 5
                                        }
                                    }.show()
                                true
                            }
                        }
                    } else {
                        mo.add("Unavailable")
                    }
                }
                menu.add(0,0,0,"Play").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setVisible(itemdata != null).setOnMenuItemClickListener {
                    val i = Intent(activity, ProjectActivity::class.java)
                    i.putExtra("project", itemdata!!.id)
                    activity.startActivity(i)
                    true
                }

                return super.onCreateOptionsMenu(menu)
            }
        }
    }

    val pb by lazy { PinfoBinding.inflate(LayoutInflater.from(context)) }
    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return pb.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        dialog?.setTitle("Loading")
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState?.getParcelable<Project>("p") != null){
            itemdata = savedInstanceState?.getParcelable("p")
            setup()
            return
        }
        if(arguments?.getParcelable<Project>("p") != null){
            itemdata = arguments?.getParcelable("p")
            setup()
            return
        }
        if(arguments?.getInt("id") != null){
            Search(activity).also{s->
                s.onError = {
                    notfound(s.reason)
                }
                s.getProject(arguments?.getInt("id") ?: 0) {
                    itemdata = it
                    setup()
                }
            }
            return
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable("p", itemdata)
    }

    fun notfound(reason: String = ""){
        pb.title.text = "Error 404 Not Found"
        pb.info.text = "Project not found, it might been deleted or private...."
        pb.stat.text = "Or maybe the app fault..."
        if(reason.isNotEmpty()){
            pb.crea.also {
                it.isEnabled = true
                it.text = "See why..."
                it.setOnClickListener {
                    pb.tbh.currentTab = 1
                }
            }
        }else{
            pb.crea.visibility = View.GONE
        }
        pb.tab2.text = reason
        pb.tbh.apply {
            setup()
            clearAllTabs()
            fun m(v: View): TabHost.TabContentFactory = object : TabHost.TabContentFactory{
                override fun createTabContent(tag: String?): View? {
                    return v
                }

            }
            mutableListOf<TabHost.TabSpec>().also {
                try{
                    it.add(
                        newTabSpec("i").setIndicator(
                            "404",
                            activity.resources.getDrawable(android.R.drawable.stat_notify_more)
                        ).setContent(m(pb.tab1))
                    )
                    if(reason.isNotEmpty()){
                        it.add(
                            newTabSpec("n").setIndicator(
                                "Why?",
                                activity.resources.getDrawable(android.R.drawable.stat_sys_warning)
                            ).setContent(m(pb.tab2))
                        )
                    }else{
                        pb.tab2.visibility = View.GONE
                        pb.tabs.visibility = View.GONE
                    }
                }catch (_: Exception){

                }
            }.forEach {
                addTab(it)
            }
        }
        pb.tab3.visibility = View.GONE
    }

    private fun setup(){
        if(itemdata == null){
            notfound("Project lost contact")
            return
        }
        try{
            pb.tbh.apply {
                setup()
                mutableListOf<TabHost.TabSpec>().also {
                    var ns = 0
                    it!!.add(
                        newTabSpec("i").setIndicator(
                            "Option",
                            activity.resources.getDrawable(android.R.drawable.stat_notify_more)
                        ).setContent(R.id.tab1)
                    )
                    if (itemdata!!.desc.isNotEmpty()) {
                        ns += 1
                        it!!.add(
                            newTabSpec("n").setIndicator(
                                "Note",
                                activity.resources.getDrawable(android.R.drawable.stat_sys_warning)
                            ).setContent(R.id.tab2)
                        )
                    } else {
                        pb.tab2.visibility = View.GONE
                    }
                    if (itemdata!!.instructions.isNotEmpty()) {
                        ns += 1
                        it!!.add(
                            newTabSpec("h").setIndicator(
                                "Help",
                                activity.resources.getDrawable(android.R.drawable.stat_notify_chat)
                            ).setContent(R.id.tab3)
                        )
                    } else {
                        pb.tab3.visibility = View.GONE
                    }
                    if (ns == 0) {
                        pb.tabs.visibility = View.GONE
                    }
                }.forEach {
                    addTab(it!!)
                }
            }
            pb.progressCircular.animate().alpha(0F).withEndAction {
                pb.progressCircular.visibility = View.GONE
            }

            pb.title.text = itemdata!!.title
            pb.tab2.text = itemdata!!.desc
            pb.tab3.text = itemdata!!.instructions
            pb.info.text = try {
                pb.stat.text =
                    "❤️ ${itemdata!!.uninfo["loves"]}  👁 ${itemdata!!.uninfo["views"]}  ⭐️ ${itemdata!!.uninfo["favorites"]}  💥 ${itemdata!!.uninfo["remixes"]}" + if (itemdata!!.uninfo["scratchteam"] == "true") "    ❤️‍🔥" else ""
                pb.crea.text = itemdata!!.creator
                "Id : ${itemdata!!.id}\nCreated : ${itemdata!!.uninfo["created"]}\nModified : ${itemdata!!.uninfo["modified"]}\nShared : ${itemdata!!.uninfo["shared"]}"
            } catch (_: Exception) {
                "Error when taking info. Try again later."
            }
            if (itemdata!!.uninfo["remix@o"]?.toString()?.isNotEmpty() == true) {
                pb.ori.isEnabled = true
                pb.ori.setOnClickListener { _ ->
                    try {
                        val i = Intent(activity, ProjectActivity::class.java)
                        i.putExtra(
                            "project",
                            Integer.valueOf(itemdata!!.uninfo["remix@o"].toString())
                        )
                        startActivity(i)
                    } catch (_: Exception) {
                        Toast.makeText(activity, "Invalid input @o", Toast.LENGTH_SHORT).show()
                    }
                }
                pb.remixLay.visibility = View.VISIBLE
            }
            if ((itemdata!!.uninfo["remix@p"]?.toString()
                    ?.isNotEmpty() == true) and (itemdata!!.uninfo["remix@o"]?.equals(itemdata!!.uninfo["remix@p"]) == false)
            ) {
                pb.orire.isEnabled = true
                pb.orire.setOnClickListener { _ ->
                    try {
                        val i = Intent(activity, ProjectActivity::class.java)
                        i.putExtra(
                            "project",
                            Integer.valueOf(itemdata!!.uninfo["remix@p"].toString())
                        )
                        startActivity(i)
                    } catch (_: Exception) {
                        Toast.makeText(activity, "Invalid input @p", Toast.LENGTH_SHORT).show()
                    }
                }
                pb.remixLay.visibility = View.VISIBLE
            }
            Glide.with(activity)
                .load(itemdata!!.thumb).centerCrop()
                .placeholder(resources.getColor(android.R.color.background_dark).toDrawable())
                .error(resources.getColor(android.R.color.holo_red_light).toDrawable())
                .into(pb.thumbnail)
            pb.thumbnail.setOnClickListener {
                PhotoViewDialog.Builder(activity, arrayOf(itemdata!!.thumb)){v,u->
                    Glide.with(activity)
                        .load(u)
                        .placeholder(resources.getColor(android.R.color.background_dark).toDrawable())
                        .error(resources.getColor(android.R.color.holo_red_light).toDrawable())
                        .into(v)
                }.allowSwipeToDismiss(true).withHiddenStatusBar(false).show(true)
            }
            when(resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    dialog?.setTitle(itemdata?.title)
                }
                else ->dialog?.setTitle(null)
            }


            pb.crea.setOnClickListener {
                try {
                    val i = Intent(activity, PP::class.java)
                    i.putExtra("user", itemdata!!.creator)
                    activity.startActivity(i)
                } catch (_: Exception) {
                    Toast.makeText(activity, "SOme component id unload", Toast.LENGTH_SHORT).show()
                }
            }
            dialog?.invalidateOptionsMenu()
        }catch (e: Exception){
            notfound(e.toString())
        }
    }

}