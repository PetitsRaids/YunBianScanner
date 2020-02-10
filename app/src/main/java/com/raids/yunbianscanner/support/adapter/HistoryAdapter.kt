package com.raids.yunbianscanner.support.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raids.yunbianscanner.R
import com.raids.yunbianscanner.data.model.History

class HistoryAdapter(private val context: Context, private val historyList: List<History>) :RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){

    inner class ViewHolder(view :View) : RecyclerView.ViewHolder(view){
        val textView: TextView = view.findViewById(R.id.history_item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = historyList[position].goodsName
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

}