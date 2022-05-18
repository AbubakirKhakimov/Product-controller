package uz.dev_abubakir_khakimov.product_controller.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import uz.dev_abubakir_khakimov.product_controller.models.Product

@Dao
interface ProductDao {

    @Query("select * from product_table")
    fun getAllProducts(): LiveData<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product)

    @Delete
    fun removeProduct(product: Product)

    @Query("select * from product_table where name like :name")
    fun searchProducts(name: String): LiveData<List<Product>>

    @Update
    fun editProduct(product: Product)

    @Query("select * from product_table where barcode=:barcode")
    fun getProductEqualThisBarcode(barcode: String):LiveData<Product?>

    @Query("select * from product_table order by barcode asc")
    fun sortByBarcode(): LiveData<List<Product>>

    @Query("select * from product_table order by name asc")
    fun sortByName(): LiveData<List<Product>>

    @Query("select * from product_table order by name desc")
    fun sortByDate(): LiveData<List<Product>>

}