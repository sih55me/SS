package cakar.search.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.TabHost
import android.widget.Toast
import cakar.search.InfoProDialog
import cakar.search.PP
import cakar.search.filetype.Project
import cakar.search.ProjectActivity
import cakar.search.R
import cakar.search.databinding.ItemBinding
import cakar.search.databinding.PinfoBinding

import com.squareup.picasso.Picasso

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
        Picasso.get()
            .load(itemdata.thumb)
            .error(R.drawable.ic_launcher_background)
            .into(binding.thumbnail)
        binding.root.setOnClickListener{
            InfoProDialog().apply {
                arguments = Bundle().also {
                    it.putInt("id", itemdata.id)
                }
            }.show(activity.fragmentManager, "ipd")
        }
        binding.root.setOnLongClickListener{
            Toast.makeText(activity, "Warning!\nthis data is outdate!", Toast.LENGTH_SHORT).show()
            InfoProDialog().apply {
                arguments = Bundle().also {
                    it.putParcelable("p", itemdata)
                }
            }.show(activity.fragmentManager, "ipd")
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
