package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY timestamp DESC")
    fun getAllCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY timestamp DESC")
    suspend fun getAllCustomers(): List<Customer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomers(customers: List<Customer>)

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone AND product = :product LIMIT 1")
    suspend fun getCustomerByPhoneAndProduct(phone: String, product: String): Customer?

    @Query("DELETE FROM customers")
    suspend fun clearAllCustomers()
}

@Dao
interface SmsLogDao {
    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllSmsLogsFlow(): Flow<List<SmsLog>>

    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    suspend fun getAllSmsLogs(): List<SmsLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsLog(log: SmsLog): Long

    @Query("DELETE FROM sms_logs")
    suspend fun clearAllSmsLogs()
}

@Dao
interface WhatsAppLogDao {
    @Query("SELECT * FROM whatsapp_logs ORDER BY timestamp DESC")
    fun getAllWhatsAppLogsFlow(): Flow<List<WhatsAppLog>>

    @Query("SELECT * FROM whatsapp_logs ORDER BY timestamp DESC")
    suspend fun getAllWhatsAppLogs(): List<WhatsAppLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppLog(log: WhatsAppLog): Long

    @Query("DELETE FROM whatsapp_logs")
    suspend fun clearAllWhatsAppLogs()
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates WHERE id = :id")
    fun getTemplateFlow(id: String): Flow<Template?>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplate(id: String): Template?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: Template)
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key")
    fun getSettingFlow(key: String): Flow<Setting?>

    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSetting(key: String): Setting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<Setting>
}
