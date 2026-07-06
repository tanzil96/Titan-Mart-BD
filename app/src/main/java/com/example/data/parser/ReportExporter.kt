package com.example.data.parser

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.SmsLog
import com.example.data.model.WhatsAppLog
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportSmsLogsToExcel(context: Context, logs: List<SmsLog>): Uri? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("SMS Logs Report")
            
            // Header Row
            val headerRow = sheet.createRow(0)
            val columns = listOf("ID", "Phone", "Name", "Message", "Status", "Date/Time", "Failure Reason")
            columns.forEachIndexed { idx, col ->
                headerRow.createCell(idx).setCellValue(col)
            }

            // Data Rows
            logs.forEachIndexed { idx, log ->
                val row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(log.id.toDouble())
                row.createCell(1).setCellValue(log.phone)
                row.createCell(2).setCellValue(log.name)
                row.createCell(3).setCellValue(log.message)
                row.createCell(4).setCellValue(log.status)
                row.createCell(5).setCellValue(dateFormat.format(Date(log.timestamp)))
                row.createCell(6).setCellValue(log.failureReason ?: "")
            }

            val file = File(context.cacheDir, "TitanMartBD_SMS_Logs_${System.currentTimeMillis()}.xlsx")
            FileOutputStream(file).use { out ->
                workbook.write(out)
            }
            workbook.close()

            FileProvider.getUriForFile(
                context,
                "com.titanmartbd.smssender.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportSmsLogsToCsv(context: Context, logs: List<SmsLog>): Uri? {
        return try {
            val file = File(context.cacheDir, "TitanMartBD_SMS_Logs_${System.currentTimeMillis()}.csv")
            file.printWriter(Charsets.UTF_8).use { writer ->
                // BOM for Excel Bengali character compatibility
                writer.print('\ufeff')
                writer.println("ID,Phone,Name,Message,Status,Date/Time,Failure Reason")
                for (log in logs) {
                    val escapedMsg = escapeCsvValue(log.message)
                    val escapedReason = escapeCsvValue(log.failureReason ?: "")
                    val dateStr = dateFormat.format(Date(log.timestamp))
                    writer.println("${log.id},\"${log.phone}\",\"${log.name}\",\"$escapedMsg\",\"${log.status}\",\"$dateStr\",\"$escapedReason\"")
                }
            }
            FileProvider.getUriForFile(
                context,
                "com.titanmartbd.smssender.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportWhatsAppLogsToExcel(context: Context, logs: List<WhatsAppLog>): Uri? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("WhatsApp Logs Report")
            
            val headerRow = sheet.createRow(0)
            val columns = listOf("ID", "Phone", "Name", "Message", "Status", "Date/Time", "Failure Reason")
            columns.forEachIndexed { idx, col ->
                headerRow.createCell(idx).setCellValue(col)
            }

            logs.forEachIndexed { idx, log ->
                val row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(log.id.toDouble())
                row.createCell(1).setCellValue(log.phone)
                row.createCell(2).setCellValue(log.name)
                row.createCell(3).setCellValue(log.message)
                row.createCell(4).setCellValue(log.status)
                row.createCell(5).setCellValue(dateFormat.format(Date(log.timestamp)))
                row.createCell(6).setCellValue(log.failureReason ?: "")
            }

            val file = File(context.cacheDir, "TitanMartBD_WhatsApp_Logs_${System.currentTimeMillis()}.xlsx")
            FileOutputStream(file).use { out ->
                workbook.write(out)
            }
            workbook.close()

            FileProvider.getUriForFile(
                context,
                "com.titanmartbd.smssender.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportWhatsAppLogsToCsv(context: Context, logs: List<WhatsAppLog>): Uri? {
        return try {
            val file = File(context.cacheDir, "TitanMartBD_WhatsApp_Logs_${System.currentTimeMillis()}.csv")
            file.printWriter(Charsets.UTF_8).use { writer ->
                writer.print('\ufeff')
                writer.println("ID,Phone,Name,Message,Status,Date/Time,Failure Reason")
                for (log in logs) {
                    val escapedMsg = escapeCsvValue(log.message)
                    val escapedReason = escapeCsvValue(log.failureReason ?: "")
                    val dateStr = dateFormat.format(Date(log.timestamp))
                    writer.println("${log.id},\"${log.phone}\",\"${log.name}\",\"$escapedMsg\",\"${log.status}\",\"$dateStr\",\"$escapedReason\"")
                }
            }
            FileProvider.getUriForFile(
                context,
                "com.titanmartbd.smssender.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsvValue(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "")
    }

    fun shareExportedFile(context: Context, uri: Uri, title: String = "Share Log Report") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = context.contentResolver.getType(uri) ?: "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
