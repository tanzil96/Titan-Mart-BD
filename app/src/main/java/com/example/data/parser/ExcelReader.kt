package com.example.data.parser

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.Customer
import org.apache.poi.ss.usermodel.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object ExcelReader {

    fun readImportedData(context: Context, uri: Uri): List<Customer> {
        val fileName = getFileName(context, uri) ?: ""
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()

        return if (fileName.endsWith(".csv", ignoreCase = true)) {
            parseCsv(inputStream)
        } else {
            parseExcel(inputStream)
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    // Clean and validate BD phone numbers
    fun cleanAndValidatePhoneNumber(raw: String): String? {
        var cleaned = raw.replace(Regex("[^0-9+]"), "")
        if (cleaned.startsWith("+88")) {
            cleaned = cleaned.substring(3)
        } else if (cleaned.startsWith("88")) {
            cleaned = cleaned.substring(2)
        }
        if (cleaned.startsWith("00")) {
            cleaned = cleaned.substring(2)
        }
        
        // Remove leading 0 if we have it, then re-check
        if (cleaned.length == 11 && cleaned.startsWith("01")) {
            return cleaned
        }
        if (cleaned.length == 10 && cleaned.startsWith("1")) {
            return "0$cleaned"
        }
        return null // Returns null for invalid numbers
    }

    private fun parseCsv(inputStream: InputStream): List<Customer> {
        val customers = mutableListOf<Customer>()
        try {
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val lines = reader.readLines()
            if (lines.isEmpty()) return emptyList()

            // Header mapping
            val headers = parseCsvLine(lines[0])
            val colIndices = detectHeaderIndices(headers)

            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) continue
                val rowValues = parseCsvLine(line)
                if (rowValues.all { it.isEmpty() }) continue // Ignore blank rows

                val customer = createCustomerFromRow(rowValues, colIndices)
                if (customer != null) {
                    customers.add(customer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { inputStream.close() } catch (ignored: Exception) {}
        }
        return customers
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var curVal = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(ch)
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true
                } else if (ch == ',') {
                    result.add(curVal.toString().trim())
                    curVal = StringBuilder()
                } else {
                    curVal.append(ch)
                }
            }
            i++
        }
        result.add(curVal.toString().trim())
        return result
    }

    private fun parseExcel(inputStream: InputStream): List<Customer> {
        val customers = mutableListOf<Customer>()
        try {
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0) ?: return emptyList()
            val rowIterator = sheet.rowIterator()

            if (!rowIterator.hasNext()) return emptyList()

            // Find first non-empty row for headers
            var headerRow: Row? = null
            while (rowIterator.hasNext()) {
                val row = rowIterator.next()
                if (row.physicalNumberOfCells > 0) {
                    headerRow = row
                    break
                }
            }

            if (headerRow == null) return emptyList()

            val headers = mutableListOf<String>()
            for (c in 0 until headerRow.lastCellNum) {
                val cell = headerRow.getCell(c)
                headers.add(getCellValueAsString(cell))
            }

            val colIndices = detectHeaderIndices(headers)

            while (rowIterator.hasNext()) {
                val row = rowIterator.next()
                val rowValues = mutableListOf<String>()
                var isRowBlank = true
                for (c in 0 until headerRow.lastCellNum) {
                    val cell = row.getCell(c)
                    val value = getCellValueAsString(cell)
                    if (value.isNotEmpty()) {
                        isRowBlank = false
                    }
                    rowValues.add(value)
                }
                if (isRowBlank) continue // Ignore blank rows

                val customer = createCustomerFromRow(rowValues, colIndices)
                if (customer != null) {
                    customers.add(customer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { inputStream.close() } catch (ignored: Exception) {}
        }
        return customers
    }

    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.toString()
                } else {
                    val numValue = cell.numericCellValue
                    if (numValue == numValue.toLong().toDouble()) {
                        numValue.toLong().toString()
                    } else {
                        numValue.toString()
                    }
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue.trim()
                } catch (e: Exception) {
                    try {
                        val numValue = cell.numericCellValue
                        if (numValue == numValue.toLong().toDouble()) {
                            numValue.toLong().toString()
                        } else {
                            numValue.toString()
                        }
                    } catch (ex: Exception) {
                        ""
                    }
                }
            }
            else -> ""
        }
    }

    private fun detectHeaderIndices(headers: List<String>): Map<String, Int> {
        val indices = mutableMapOf<String, Int>()
        headers.forEachIndexed { index, header ->
            val lower = header.lowercase().trim()
            when {
                lower.contains("name") || lower.contains("customer") || lower.contains("নাম") -> {
                    if (!indices.containsKey("name")) indices["name"] = index
                }
                lower.contains("phone") || lower.contains("mobile") || lower.contains("contact") || lower.contains("মোবাইল") || lower.contains("নাম্বার") -> {
                    if (!indices.containsKey("phone")) indices["phone"] = index
                }
                lower.contains("product") || lower.contains("item") || lower.contains("পণ্য") || lower.contains("প্রোডাক্ট") -> {
                    if (!indices.containsKey("product")) indices["product"] = index
                }
                lower.contains("quantity") || lower.contains("qty") || lower.contains("পরিমাণ") -> {
                    if (!indices.containsKey("quantity")) indices["quantity"] = index
                }
                lower.contains("delivery") || lower.contains("charge") || lower.contains("ডেলিভারি") -> {
                    if (!indices.containsKey("deliveryCharge")) indices["deliveryCharge"] = index
                }
                lower.contains("total") || lower.contains("amount") || lower.contains("bill") || lower.contains("মোট") || lower.contains("টাকা") -> {
                    if (!indices.containsKey("totalAmount")) indices["totalAmount"] = index
                }
                lower.contains("order") || lower.contains("id") || lower.contains("অর্ডার") -> {
                    if (!indices.containsKey("orderId")) indices["orderId"] = index
                }
                lower.contains("status") || lower.contains("অবস্থা") -> {
                    if (!indices.containsKey("status")) indices["status"] = index
                }
                lower.contains("remarks") || lower.contains("note") || lower.contains("মন্তব্য") -> {
                    if (!indices.containsKey("remarks")) indices["remarks"] = index
                }
                lower.contains("date") || lower.contains("time") || lower.contains("তারিখ") -> {
                    if (!indices.containsKey("date")) indices["date"] = index
                }
            }
        }
        return indices
    }

    private fun createCustomerFromRow(row: List<String>, indices: Map<String, Int>): Customer? {
        val rawPhone = getValueByMappedIndex(row, indices["phone"])
        if (rawPhone.isEmpty()) return null

        val phone = cleanAndValidatePhoneNumber(rawPhone) ?: return null // Returns null if phone number is invalid

        val name = getValueByMappedIndex(row, indices["name"]).ifEmpty { "Customer" }
        val product = getValueByMappedIndex(row, indices["product"]).ifEmpty { "General Product" }
        val quantity = getValueByMappedIndex(row, indices["quantity"]).ifEmpty { "1" }
        val deliveryCharge = getValueByMappedIndex(row, indices["deliveryCharge"]).ifEmpty { "0" }
        val totalAmount = getValueByMappedIndex(row, indices["totalAmount"]).ifEmpty { "0" }
        val orderId = getValueByMappedIndex(row, indices["orderId"]).ifEmpty { "TM-${System.currentTimeMillis().toString().takeLast(6)}" }
        val status = getValueByMappedIndex(row, indices["status"]).ifEmpty { "Pending" }
        val remarks = getValueByMappedIndex(row, indices["remarks"])
        val date = getValueByMappedIndex(row, indices["date"]).ifEmpty { "Today" }

        return Customer(
            name = name,
            phone = phone,
            product = product,
            quantity = quantity,
            deliveryCharge = deliveryCharge,
            totalAmount = totalAmount,
            orderId = orderId,
            status = status,
            remarks = remarks,
            date = date
        )
    }

    private fun getValueByMappedIndex(row: List<String>, index: Int?): String {
        if (index == null || index < 0 || index >= row.size) return ""
        return row[index].trim()
    }
}
