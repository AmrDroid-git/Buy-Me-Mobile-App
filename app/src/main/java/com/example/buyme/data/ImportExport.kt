package com.example.buyme.data

data class ExportItem(val name: String, val checked: Boolean)
data class ExportCategory(val name: String, val items: List<ExportItem>)
data class ExportPayload(val version: Int = 1, val categories: List<ExportCategory>)
