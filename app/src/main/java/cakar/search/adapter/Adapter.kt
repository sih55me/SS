package cakar.search.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.net.toUri
import cakar.search.InfoProDialog
import cakar.search.PP
import cakar.search.ProjectActivity
import cakar.search.R
import cakar.search.databinding.ItemBinding
import cakar.search.filetype.Project
import coil3.asImage
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation

class Adapter(
    private val activity: Activity,
    val data : ArrayList<Project>,
    var mini: Boolean = false
): BaseAdapter() {

    var a : ActionMode? = null

    private fun optIt(itemdata: Project, view: View?=null) = object: ActionMode.Callback2() {
        override fun onGetContentRect(mode: ActionMode?, o: View?, outRect: Rect?) {
            if (view != null) {
                outRect!!.set(0, 0, view.getWidth(), view.getHeight())
                return
            }
            super.onGetContentRect(mode, o, outRect)
        }
        override fun onCreateActionMode(
            p: ActionMode?,
            p1: Menu?
        ): Boolean {
            a = p
            if(p == null)return true
            p.title = itemdata.title
            p.subtitle = itemdata.creator
            p.menu.add("Show[old]").setOnMenuItemClickListener {
                Toast.makeText(activity, "Warning!\nthis data is outdate!", Toast.LENGTH_SHORT).show()
                InfoProDialog().apply {
                    arguments = Bundle().also {
                        it.putParcelable("p", itemdata)
                    }
                }.show(activity.fragmentManager, "ipd")
                true
            }
            p.menu.add("Share").setOnMenuItemClickListener {
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
            }
            p.menu.add("Show creator page").setOnMenuItemClickListener {
                try {
                    val i = Intent(activity, PP::class.java)
                    i.putExtra("user", itemdata!!.creator)

                    activity.startActivity(i)
                } catch (_: Exception) {
                    Toast.makeText(activity, "SOme component id unload", Toast.LENGTH_SHORT).show()
                }
                true
            }
            p.menu.add(0,0,0,"Play").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setVisible(itemdata != null).setOnMenuItemClickListener {
                val i = Intent(activity, ProjectActivity::class.java)
                i.putExtra("project", itemdata!!.id)
                itemdata!!.uninfo.get("project_token")?.let {
                    i.putExtra("tknp",it.toString())
                }
                activity.startActivity(i)
                true
            }
            p.menu.add("Learn more").setOnMenuItemClickListener {
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
            return true
        }

        override fun onPrepareActionMode(
            p0: ActionMode?,
            p1: Menu?
        )=false

        override fun onActionItemClicked(
            p0: ActionMode?,
            p1: MenuItem?
        )=false

        override fun onDestroyActionMode(p0: ActionMode?) {
            a = null
        }

    }

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
        binding.thumbnail.load(
            itemdata.thumb,
            builder = {
                crossfade(true)
                transformations(RoundedCornersTransformation(10F))
                placeholder(ColorDrawable(Color.BLACK).asImage())
                this.error(ColorDrawable(Color.BLACK))
                listener(
                    onError = {i,o->
                        o.throwable.printStackTrace()
                    }
                )
            }
        )
        binding.root.setOnClickListener{
            InfoProDialog().apply {
                arguments = Bundle().also {
                    it.putInt("id", itemdata.id)
                }
            }.show(activity.fragmentManager, "ipd")
        }
        binding.root.setOnLongClickListener{
            PopupMenu(activity, binding.title).also { p->


                p.menu.add("Share").setOnMenuItemClickListener {
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
                }
                p.menu.add("Show creator page").setOnMenuItemClickListener {
                    try {
                        val i = Intent(activity, PP::class.java)
                        i.putExtra("user", itemdata!!.creator)

                        activity.startActivity(i)
                    } catch (_: Exception) {
                        Toast.makeText(activity, "SOme component id unload", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                p.menu.add(0,0,0,"Play").setIcon(R.drawable.play).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setVisible(itemdata != null).setOnMenuItemClickListener {
                    val i = Intent(activity, ProjectActivity::class.java)
                    i.putExtra("project", itemdata!!.id)
                    itemdata!!.uninfo.get("project_token")?.let {
                        i.putExtra("tknp",it.toString())
                    }
                    activity.startActivity(i)
                    true
                }
                val opl = p.menu.addSubMenu("Other")
                opl.add("Show[old]").setOnMenuItemClickListener {
                    Toast.makeText(activity, "Warning!\nthis data is outdate!", Toast.LENGTH_SHORT).show()
                    InfoProDialog().apply {
                        arguments = Bundle().also {
                            it.putParcelable("p", itemdata)
                        }
                    }.show(activity.fragmentManager, "ipd")
                    true
                }
                opl.add("Learn more").setOnMenuItemClickListener {
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
            }.show()
            true
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
