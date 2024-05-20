package uz.abubakir_khakimov.product_controller.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orhanobut.hawk.Hawk
import uz.abubakir_khakimov.product_controller.BuildConfig

interface WorkPerManagerCallBack{
    fun notPermitted()
}

class WorkPermissionManager(private val context: Context) {

    private val currentVersionCode = BuildConfig.VERSION_CODE
    private lateinit var workPerManagerCallBack: WorkPerManagerCallBack

    fun checkPermitted(workPerManagerCallBack: WorkPerManagerCallBack){
        this.workPerManagerCallBack = workPerManagerCallBack

        if (!isNetworkConnected()){
            checkVersionOffline()
        }
        checkVersionOnline()
    }

    private fun checkVersionOnline() {
        Firebase.database.getReference("permittedVersionCode")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val permittedVersionCode = snapshot.getValue(Int::class.java)!!
                    val isPermitted = currentVersionCode >= permittedVersionCode

                    Hawk.put("isPermitted", isPermitted)
                    if (!isPermitted){
                        workPerManagerCallBack.notPermitted()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private fun checkVersionOffline(){
        val isPermitted = Hawk.get("isPermitted", true)
        if (!isPermitted){
            workPerManagerCallBack.notPermitted()
        }
    }

    fun writeVersionCode(){
        FirebaseDatabase.getInstance().getReference("permittedVersionCode").setValue(1)
    }

    private fun isNetworkConnected():Boolean{
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            result = when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }else{
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }
        return result
    }

}