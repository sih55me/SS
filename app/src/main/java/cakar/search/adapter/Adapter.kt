package cakar.search.adapter

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TabHost
import android.widget.Toast
import cakar.search.PP
import cakar.search.Project
import cakar.search.ProjectActivity
import cakar.search.R
import cakar.search.databinding.ItemBinding
import cakar.search.databinding.PinfoBinding
import com.bumptech.glide.Glide

class Adapter(
    private val activity: Activity,
    val data : ArrayList<Project>,
    val mini: Boolean = false
): BaseAdapter() {

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemBinding
        val view: View

        if (convertView == null) {
            binding = if(mini){
                val l = LayoutInflater.from(activity).inflate(R.layout.item_mini, parent, false)
                ItemBinding.bind(l)
            }else{
                ItemBinding.inflate(LayoutInflater.from(activity), parent, false)
            }
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ItemBinding
        }

        val itemdata = data[position]
        binding.title.text = itemdata.title
        binding.count.text = itemdata.creator
        binding.title.setOnLongClickListener {
            object : AlertDialog(activity){
                val pb = PinfoBinding.inflate(LayoutInflater.from(context))
                init {
                    window!!.setLayout(view.width, view.height)
                    setButton3("Back"){_,_->
                        dismiss()
                    }
                    setButton("Menu"){_,_-> }
                    setView(pb.root)
                }

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    pb.tbh.apply{
                        setup()
                        mutableListOf<TabHost.TabSpec>().also {
                            it.add(
                                newTabSpec("i").setIndicator("Option", activity.resources.getDrawable(android.R.drawable.stat_notify_more)).setContent(R.id.tab1)
                            )
                            if (itemdata.desc.isNotEmpty()) {
                                it.add(
                                    newTabSpec("n").setIndicator("Note", activity.resources.getDrawable(android.R.drawable.stat_sys_warning)).setContent(R.id.tab2)
                                )
                            } else {
                                pb.tab2.visibility = View.GONE
                            }
                            if (itemdata.instructions.isNotEmpty()) {
                                it.add(
                                    newTabSpec("h").setIndicator("Help", activity.resources.getDrawable(android.R.drawable.stat_notify_chat)).setContent (R.id.tab3)
                                )
                            } else {
                                pb.tab3.visibility = View.GONE
                            }
                        }.forEach {
                            addTab(it)
                        }
                    }
                    pb.uninfo.text = "${itemdata.title}\n\nMade by ${itemdata.creator}\nId : ${itemdata.id}\nCreated at ${itemdata.uninfo["created"]}"
                    pb.tab2.text = itemdata.desc
                    pb.tab3.text = itemdata.instructions
                    pb.crea.text = itemdata.creator
                    pb.stat.text = try{
                        "❤️ ${itemdata.uninfo["loves"]}  👁 ${itemdata.uninfo["views"]}  ⭐️ ${itemdata.uninfo["favorites"]}  💥 ${itemdata.uninfo["remixes"]}" + if(itemdata.uninfo["scratchteam"] == "true") "    ❤️‍🔥" else ""
                    }catch (_: Exception){
                        "???"
                    }

                    if(itemdata.uninfo["visibility"] == "visible"){
                        if(itemdata.uninfo["public"].toString() == "true"){
                            pb.uninfo.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0,  0)
                        }else if(itemdata.uninfo["public"].toString() == "false"){
                            pb.uninfo.setBackgroundColor(activity.resources.getColor(android.R.color.holo_green_dark))
                            pb.uninfo.setTextColor(activity.resources.getColor(android.R.color.black))
                            pb.uninfo.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0,  0)
                        }
                    }else if(itemdata.uninfo["visibility"] == "notvisible"){
                        pb.uninfo.setTextColor(activity.resources.getColor(android.R.color.holo_orange_light))
                        pb.uninfo.setCompoundDrawablesWithIntrinsicBounds( 0, 0, android.R.drawable.presence_invisible, 0)
                        if(itemdata.uninfo["public"].toString() == "true"){
                            pb.uninfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        }else if(itemdata.uninfo["public"].toString() == "false"){
                            pb.uninfo.setTextColor(activity.resources.getColor(android.R.color.black))
                            pb.uninfo.setBackgroundColor(activity.resources.getColor(android.R.color.holo_red_light))
                            pb.uninfo.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0,  0)
                        }
                    }

                    pb.crea.setOnClickListener {
                        try{
                            val i = Intent(activity, PP::class.java)
                            i.putExtra("user", itemdata.creator)
                            activity.startActivity(i)
                        }catch (_: Exception){
                            Toast.makeText(activity, "SOme component id unload", Toast.LENGTH_SHORT).show()
                        }
                    }
                    getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                        closeOptionsMenu()
                        invalidateOptionsMenu()
                        openOptionsMenu()
                    }
                }
                override fun onCreateOptionsMenu(menu: Menu): Boolean {
                    menu.apply {
                        add("Share")?.setIcon(R.drawable.share)?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)?.setOnMenuItemClickListener {
                            try{
                                val i = Intent(Intent.ACTION_SEND)
                                i.type = "text/plain"
                                i.putExtra(Intent.EXTRA_TEXT, "https://scratch.mit.edu/projects/${itemdata.id}/")
                                activity.startActivity(Intent.createChooser(i, "share"))
                                true
                            }catch (_: Exception){
                                Toast.makeText(activity, "Project unload!", Toast.LENGTH_SHORT).show()
                                false
                            }
                        }
                        add("Learn more").setOnMenuItemClickListener {
                            val i = Intent(activity, ProjectActivity::class.java)
                            i.putExtra("project", itemdata.id)
                            try{
                                i.putExtra("tkn", itemdata.uninfo["tkn"].toString())
                            }catch (_: Exception){

                            }
                            activity.startActivity(i)
                            true
                        }
                        add("Status").setOnMenuItemClickListener {
                            Builder(activity).setTitle("Status").setMessage("Visibility : ${itemdata.uninfo["visibility"]}\nIs Public : ${itemdata.uninfo["public"]}\n Publish : ${itemdata.uninfo["posted"]}\nsus url : ${itemdata.uninfo["tkn"]}").setPositiveButton(android.R.string.ok, null).create().also {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    it.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                                    it.window?.attributes?.blurBehindRadius = 5
                                }
                            }.show()
                            true
                        }
                    }
                    return super.onCreateOptionsMenu(menu)
                }
            }.show()
            true
        }
        Glide.with(activity)
            .load(itemdata.thumb).centerCrop()
            .error(R.drawable.ic_launcher_background)
            .into(binding.thumbnail)
        binding.root.setOnClickListener{
            val i = Intent(activity, ProjectActivity::class.java)
            i.putExtra("project", itemdata.id)
            activity.startActivity(i)
        }


        return binding.root
    }



    @SuppressLint("NotifyDataSetChanged")
    fun setdata(data : Project){
        this.data.add(data)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun flush() {
        data.clear()
        notifyDataSetChanged()
    }
}
