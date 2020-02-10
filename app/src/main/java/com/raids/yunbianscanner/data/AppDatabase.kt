package com.raids.yunbianscanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raids.yunbianscanner.data.dao.HistoryDao
import com.raids.yunbianscanner.data.model.History
import com.raids.yunbianscanner.support.utils.MyUtils

@Database(entities = [History::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    companion object {
        @Volatile private var INSTANCE : AppDatabase? = null

        public fun getInstance(context: Context) : AppDatabase{
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, MyUtils.APP_DATABASE).build()
            }
            return INSTANCE as AppDatabase
        }
    }

    public abstract fun getHistoryDao(): HistoryDao

}