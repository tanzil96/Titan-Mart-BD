package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Customer::class,
        SmsLog::class,
        WhatsAppLog::class,
        Template::class,
        Setting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun smsLogDao(): SmsLogDao
    abstract fun whatsappLogDao(): WhatsAppLogDao
    abstract fun templateDao(): TemplateDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "titan_mart_bd_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
