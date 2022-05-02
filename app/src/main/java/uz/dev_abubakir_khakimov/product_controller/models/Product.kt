package uz.dev_abubakir_khakimov.product_controller.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val count: Int,
    val entryPrice: String,
    val percent: Double,
    val sellingPrice: String,
    val term: String,
    val firm: String,
    val barcode: String,
    var barcodeImagePath: String,
    val entryDate: Long
):Serializable
