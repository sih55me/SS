package cakar.search.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import cakar.search.Project
import cakar.search.ProjectActivity
import cakar.search.R
import cakar.search.databinding.ItemBinding
import com.bumptech.glide.Glide

class Adapter(
    private val activity: Activity,
    private val data : ArrayList<Project>
): RecyclerView.Adapter<Adapter.Holder>() {
    class Holder(item : ItemBinding) : RecyclerView.ViewHolder(item.root){
        val thumb = item.thumbnail
        val title = item.title
        val more = item.overflow
        val root = item.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemdata = data[position]
        holder.title.text = itemdata.title
        Glide.with(holder.itemView.context)
            .load(itemdata.thumb)
            .centerCrop()
            .error(R.drawable.ic_launcher_background)
            .into(holder.thumb)
        holder.root.setOnClickListener {
            val i = Intent(it.context, ProjectActivity::class.java)
            i.putExtra("project", itemdata.id)
            it.context.startActivity(i)
        }
        holder.more.setOnClickListener {
            AlertDialog.Builder(activity).apply {
                setTitle("More")
                setSingleChoiceItems(ArrayAdapter(activity, android.R.layout.simple_list_item_1, arrayOf("Desc : \n${itemdata.desc}","Instruction : \n${itemdata.desc}",  "Creator : `${itemdata.creator}`")), -1){_,_->

                }
                show()
            }
        }

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