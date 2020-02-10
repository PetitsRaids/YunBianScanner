package com.raids.yunbianscanner.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.raids.yunbianscanner.data.model.History

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getAllHistories():LiveData<List<History>>

    @Query("DELETE FROM history WHERE id = :id")
    fun deleteById(id:Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertHistory(history: History)
}