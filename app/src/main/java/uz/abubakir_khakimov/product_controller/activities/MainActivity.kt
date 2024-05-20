package uz.abubakir_khakimov.product_controller.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import uz.abubakir_khakimov.product_controller.R
import uz.abubakir_khakimov.product_controller.fragments.NotPermittedFragment
import uz.abubakir_khakimov.product_controller.utils.LocaleManager
import uz.abubakir_khakimov.product_controller.utils.WorkPerManagerCallBack
import uz.abubakir_khakimov.product_controller.utils.WorkPermissionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WorkPermissionManager(this).checkPermitted(object :WorkPerManagerCallBack{
            override fun notPermitted() {
                NotPermittedFragment().show(supportFragmentManager, "tag")
            }
        })

    }

    override fun attachBaseContext(newBase: Context?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

}