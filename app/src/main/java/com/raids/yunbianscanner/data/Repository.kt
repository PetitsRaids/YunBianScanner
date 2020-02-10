package com.raids.yunbianscanner.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.raids.yunbianscanner.data.AppDatabase.Companion.getInstance
import com.raids.yunbianscanner.data.dao.HistoryDao
import com.raids.yunbianscanner.data.model.History
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Repository private constructor(context: Context) {

    private val historyDao: HistoryDao
    private var database: AppDatabase? = null
    private val allHistoryLive: LiveData<List<History>>
    private val executor: Executor

    fun getAllHistoryLive() : LiveData<List<History>>{
        return allHistoryLive
    }

    fun deleteHistoryById(id: Int) {
        executor.execute { historyDao.deleteById(id) }
    }

    fun insertHistory(history: History) {
        executor.execute { historyDao.insertHistory(history) }
    }

    companion object {
        private var repository: Repository? = null
        fun getRepository(context: Context): Repository {
            if (repository == null) {
                repository =
                    Repository(context)
            }
            return repository as Repository
        }
    }

    init {
        database = getInstance(context)
        historyDao = database!!.getHistoryDao()
        executor = Executors.newSingleThreadExecutor()
        allHistoryLive = historyDao.getAllHistories()
    }
}