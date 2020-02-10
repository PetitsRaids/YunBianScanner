package com.raids.yunbianscanner.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.raids.yunbianscanner.R
import com.raids.yunbianscanner.data.model.History
import com.raids.yunbianscanner.data.viewmodel.HistoryViewModel
import com.raids.yunbianscanner.support.adapter.HistoryAdapter
import com.raids.yunbianscanner.support.utils.MyUtils

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var historyList: MutableList<History>
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView = view!!.findViewById(R.id.history_recyclerView)
        historyList = mutableListOf()
        historyViewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(activity!!.application).create(HistoryViewModel::class.java)
        historyViewModel.allHistoryLiveData.observe(this, Observer {
            historyList.addAll(it)
            Log.d(MyUtils.TAG, historyList.toString())
        })
        adapter = HistoryAdapter(context!!, historyList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

}
