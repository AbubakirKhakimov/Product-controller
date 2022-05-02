package uz.dev_abubakir_khakimov.product_controller.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.random.Random


class BarcodeManager(val sizeWidth: Int, val sizeHeight: Int) {

    @Throws(WriterException::class)
    fun createImage(code: String): Bitmap{
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            code,
            BarcodeFormat.CODE_128,
            sizeWidth,
            sizeHeight
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (bitMatrix[j, i]) {
                    pixels[i * width + j] = -0x1000000
                } else {
                    pixels[i * width + j] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun generateBarcode(): String{
        return Random.nextLong(1000000000000, 10000000000000).toString()
    }

    fun saveBitmap(bitmap: Bitmap, message: String, bitName: String, context: Context) {
        val PERMISSIONS = arrayOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
        val permission = ContextCompat.checkSelfPermission(
            context,
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, PERMISSIONS, 1)
        }
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val hour: Int = calendar.get(Calendar.HOUR)
        val minute: Int = calendar.get(Calendar.MINUTE)
        val second: Int = calendar.get(Calendar.SECOND)
        val millisecond: Int = calendar.get(Calendar.MILLISECOND)
        val fileName =
            message + "_at_" + year.toString() + "_" + month.toString() + "_" + day.toString() + "_" + hour.toString() + "_" + minute.toString() + "_" + second.toString() + "_" + millisecond.toString()
        val file: File
        val fileLocation: String
        val folderLocation: String
        if (Build.BRAND == "Xiaomi") {
            fileLocation =
                Environment.getExternalStorageDirectory().path + "/DCIM/Camera/AndroidBarcodeGenerator/" + fileName + bitName
            folderLocation =
                Environment.getExternalStorageDirectory().path + "/DCIM/Camera/AndroidBarcodeGenerator/"
        } else {
            fileLocation =
                Environment.getExternalStorageDirectory().path + "/DCIM/AndroidBarcodeGenerator/" + fileName + bitName
            folderLocation =
                Environment.getExternalStorageDirectory().path + "/DCIM/AndroidBarcodeGenerator/"
        }
        Log.d("file_location", fileLocation)
        file = File(fileLocation)
        val folder = File(folderLocation)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        if (file.exists()) {
            file.delete()
        }
        val out: FileOutputStream
        try {
            out = FileOutputStream(file)
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush()
                out.close()
            }
        } catch (fnfe: FileNotFoundException) {
            fnfe.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://$fileName")
            )
        )
    }

}