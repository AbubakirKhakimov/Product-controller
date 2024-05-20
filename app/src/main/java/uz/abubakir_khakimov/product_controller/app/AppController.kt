package uz.abubakir_khakimov.product_controller.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.orhanobut.hawk.Hawk

class AppController: Application() {

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        );
    }

    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
        FirebaseApp.initializeApp(this)
    }

}