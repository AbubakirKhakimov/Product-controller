package uz.dev_abubakir_khakimov.product_controller.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import uz.dev_abubakir_khakimov.product_controller.dao.ProductDao
import uz.dev_abubakir_khakimov.product_controller.database.ProductDatabase

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val productDao: ProductDao = ProductDatabase.getInstance(application).productDao()

    private val isPermitted = MutableLiveData<Boolean>()

    fun readAllProducts(): LiveData<List<Product>> {
        return productDao.getAllProducts()
    }

    fun getProductEqualThisBarcode(barcode: String): LiveData<Product?> {
        return productDao.getProductEqualThisBarcode(barcode)
    }

    fun insertProduct(product: Product){
        productDao.insertProduct(product)
    }

    fun editProduct(product: Product){
        productDao.editProduct(product)
    }

    fun removeProduct(product: Product){
        productDao.removeProduct(product)
    }

    fun sortByBarcode():LiveData<List<Product>>{
        return productDao.sortByBarcode()
    }

    fun sortByName():LiveData<List<Product>>{
        return productDao.sortByName()
    }

    fun sortByDate():LiveData<List<Product>>{
        return productDao.sortByDate()
    }

    fun searchProducts(name: String):LiveData<List<Product>>{
        return productDao.searchProducts(name)
    }

    fun checkVersion(currentVersionCode: Int): LiveData<Boolean>{
        FirebaseDatabase.getInstance().getReference("permittedVersionCode")
            .addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val permittedVersionCode = snapshot.getValue(Int::class.java)!!
                isPermitted.value = currentVersionCode >= permittedVersionCode
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return isPermitted
    }

}