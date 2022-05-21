package uz.dev_abubakir_khakimov.product_controller.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "product_table", indices = [Index(value = ["barcode"], unique = true)])
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val count: Int,
    val entryPrice: Double,
    val percent: Double,
    val sellingPrice: Double,
    val term: Int,
    val firm: String,
    val barcode: String,
    var barcodeImagePath: String,
    val entryDate: Long
): Serializable
