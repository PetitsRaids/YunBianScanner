package com.raids.yunbianscanner.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.raids.yunbianscanner.data.Repository
import com.raids.yunbianscanner.data.model.History

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: Repository = Repository.getRepository(application)

    var allHistoryLiveData: LiveData<List<History>> = repository.getAllHistoryLive()

    public fun deleteHistoryById(id: Int) {
        repository.deleteHistoryById(id)
    }

    fun insertHistory(history: History) {
        repository.insertHistory(history)
    }
}