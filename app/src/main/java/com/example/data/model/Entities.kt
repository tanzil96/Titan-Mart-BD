package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val product: String,
    val quantity: String,
    val deliveryCharge: String,
    val totalAmount: String,
    val orderId: String,
    val status: String, // "Pending", "Sent", "Failed", "Delivered"
    val remarks: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sms_logs")
data class SmsLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val name: String,
    val message: String,
    val status: String, // "Sent", "Failed", "Pending"
    val timestamp: Long = System.currentTimeMillis(),
    val failureReason: String? = null
)

@Entity(tableName = "whatsapp_logs")
data class WhatsAppLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val name: String,
    val message: String,
    val status: String, // "Sent", "Failed", "Pending"
    val timestamp: Long = System.currentTimeMillis(),
    val failureReason: String? = null
)

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey val id: String, // "sms_default", "whatsapp_default"
    val type: String, // "sms", "whatsapp"
    val content: String
)

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val key: String,
    val value: String
)
