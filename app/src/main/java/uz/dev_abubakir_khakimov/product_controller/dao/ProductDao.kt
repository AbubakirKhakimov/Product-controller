package uz.dev_abubakir_khakimov.product_controller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uz.dev_abubakir_khakimov.product_controller.models.Product

@Dao
interface ProductDao {

    @Query("select * from product_table")
    fun getAllProducts(): List<Product>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertProduct(product: Product)

    @Query("select * from product_table where barcode=:barcode")
    fun getProductEqualThisBarcode(barcode: String):Product?

}