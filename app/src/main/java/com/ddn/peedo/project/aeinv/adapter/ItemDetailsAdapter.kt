package com.ddn.peedo.project.aeinv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ddn.peedo.project.aeinv.R

class ItemDetailsAdapter(private val itemData: Map<String, Any>) :
    RecyclerView.Adapter<ItemDetailsAdapter.ViewHolder>() {

    private val filteredData = itemData.filterKeys { it != "SourceTable" && it != "ITEMID" }



    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val keyTextView: TextView = view.findViewById(R.id.keyTextView)
        val valueTextView: TextView = view.findViewById(R.id.valueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = filteredData.entries.toList()[position]
        holder.keyTextView.text = entry.key
        holder.valueTextView.text = entry.value.toString()
    }

    override fun getItemCount(): Int = filteredData.size
}
