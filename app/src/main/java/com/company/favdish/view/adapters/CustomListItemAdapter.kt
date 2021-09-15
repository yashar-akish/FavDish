package com.company.favdish.view.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.company.favdish.databinding.ItemCustomListBinding
import com.company.favdish.view.activities.AddUpdateDishActivity

class CustomListItemAdapter(
    private val activity: Activity,
    private val listItem: List<String>,
    private val selection: String ) : RecyclerView.Adapter<CustomListItemAdapter.ViewHolder>(){

        class ViewHolder(view: ItemCustomListBinding): RecyclerView.ViewHolder(view.root) {
            val tvText = view.tvText
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCustomListBinding =
            ItemCustomListBinding.inflate(LayoutInflater.from(activity), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.tvText.text = item
        /**
         * setting the clickListener to selected item
         */
        holder.itemView.setOnClickListener{
            if (activity is AddUpdateDishActivity) {
                activity.selectedListItem(item, selection)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItem.size
    }
}