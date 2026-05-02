package cakar.search

import android.app.ActionBar
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.app.DialogFragment
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TabHost
import android.widget.Toast
import android.app.Dialog
import android.app.FragmentTransaction
import android.graphics.drawable.ColorDrawable
import android.transition.Explode
import android.transition.Fade
import android.transition.Transition
import android.util.Log
import android.view.MenuInflater
import android.view.Window
import android.widget.ArrayAdapter
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import cakar.search.com.ProjectComponent
import cakar.search.databinding.PinfoBinding
import cakar.search.filetype.Project
import cakar.search.wtbcore.PreviewImgPage
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException

class InfoProDialog()  : DialogFragment(){
    var itemdata : Project? = null
    private set
    private var isTabReady = false


    val pb by lazy { PinfoBinding.inflate(LayoutInflater.from(context)) }
    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return pb.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        return object : Dialog(context, R.style.Theme_SS_ProPre){
            init {
                window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
                window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window!!.setDimAmount(0.4F)
                setCanceledOnTouchOutside(true)
                when(resources.configuration.orientation){
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        window!!.setLayout(window!!.windowManager.defaultDisplay.width/2,
                            (window!!.windowManager.defaultDisplay.height/1.12).toInt()
                        )
                        window?.setGravity(Gravity.CENTER)
                    }

                }

            }

            override fun onCreateOptionsMenu(menu: Menu): Boolean {
                this@InfoProDialog.onCreateOptionsMenu(menu, MenuInflater(context))
                return super.onCreateOptionsMenu(menu)
            }
            override fun show() {
                super.show()
                actionBar?.setDisplayUseLogoEnabled(false)
                actionBar?.setHomeButtonEnabled(false)
                actionBar?.elevation = 0f
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menu == null)return
        fun Menu.opt(){
            apply {
                add("Share")
                    ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    ?.setOnMenuItemClickListener {
                        try {
                            val i = Intent(Intent.ACTION_SEND)
                            i.type = "text/plain"
                            i.putExtra(
                                Intent.EXTRA_TEXT,
                                "https://scratch.mit.edu/projects/${itemdata!!.id}/"
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            activity,
                            "Project unload!",
                            Toast.LENGTH_SHORT
                        ).show()
                        false
                    }
                }
                add("Status").setOnMenuItemClickListener {
                    Builder(activity).setTitle("Status")
                        .setMessage(
                            "Token key : ${itemdata!!.uninfo["project_token"]}\nVisibility : ${itemdata!!.uninfo["visibility"]}\nIs Public : ${itemdata!!.uninfo["public"]}\nPublish : ${
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

                add("Asset").intent = Intent(activity, AssetPage::class.java).putExtra("item", itemdata)
            }
        }
        menu.add(0,0,2,"Close").setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setVisible(itemdata != null).setOnMenuItemClickListener {
            this@InfoProDialog.dismiss()
            true
        }
        menu.addSubMenu(0,0,1,"Options").also {mo->
            mo.item.setIcon(R.drawable.more).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            if (itemdata != null) {
                mo.opt()
            } else {
                mo.add("Unavailable")
            }
        }
        menu.add(0,0,0,"Play").setIcon(R.drawable.play).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setVisible(itemdata != null).setOnMenuItemClickListener {
            val i = Intent(activity, ProjectActivity::class.java)
            i.putExtra("project", itemdata!!.id)
            activity.startActivity(i)
            true
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        dialog?.setTitle(null)
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
                            "This",
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
                }.forEach {
                    isTabReady = true
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
            Picasso.get()
                .load(itemdata!!.thumb)
                .placeholder(resources.getColor(android.R.color.background_dark).toDrawable())
                .error(resources.getColor(android.R.color.holo_red_light).toDrawable())
                .into(pb.thumbnail)
            pb.thumbnail.setOnClickListener {
                PreviewImgPage(activity, PreviewImgPage.Get(pb.thumbnail.drawable?: ColorDrawable(), itemdata?.title.orEmpty())).also {
                    it.show()
                }
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