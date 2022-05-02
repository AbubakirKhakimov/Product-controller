package uz.dev_abubakir_khakimov.product_controller.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import uz.dev_abubakir_khakimov.product_controller.dao.ProductDao
import uz.dev_abubakir_khakimov.product_controller.database.ProductDatabase

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val productDao: ProductDao = ProductDatabase.getInstance(application).productDao()

    val readAllProductsData = MutableLiveData<List<Product>>()
    val compareResultData = MutableLiveData<Product?>()

    fun readAllProducts(){
        readAllProductsData.value = productDao.getAllProducts()
    }

    fun getProductEqualThisBarcode(barcode: String){
        compareResultData.value = productDao.getProductEqualThisBarcode(barcode)
    }

    fun insertProduct(product: Product){
        productDao.insertProduct(product)
    }

}