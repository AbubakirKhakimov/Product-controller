package uz.dev_abubakir_khakimov.product_controller.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.dev_abubakir_khakimov.product_controller.dao.ProductDao
import uz.dev_abubakir_khakimov.product_controller.models.Product

@Database(entities = [Product::class], version = 1)
abstract class ProductDatabase: RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object{
        private var instance: ProductDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ProductDatabase{
            if (instance == null){
                instance = Room.databaseBuilder(context, ProductDatabase::class.java, "product_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }

            return instance!!
        }
    }

}