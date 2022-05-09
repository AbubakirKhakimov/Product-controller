package uz.dev_abubakir_khakimov.product_controller.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MediaSaveManager(val context: Context) {

    fun saveMediaToStorage(bitmap: Bitmap, barcode: String, productName: String): File {
        val fileName = "${barcode}_${productName}.jpg"

        //Output stream
        var fos: OutputStream? = null
        val imageFile = checkFileExists(fileName)

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Product images")
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            fos = FileOutputStream(imageFile)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            getChangedBitmap(bitmap, barcode, productName)
                .compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        return imageFile
    }

    private fun checkFileExists(fileName: String): File{
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Product images")

        if (imagesDir != null && !imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val imageFile = File(imagesDir, fileName)

        if (imageFile.exists()){
            imageFile.delete()
        }

        return imageFile
    }

    //barcode size: 420 x 210
    private fun getChangedBitmap(oldBitmap: Bitmap, barcode: String, productName: String): Bitmap{
        val newBitmap = Bitmap.createBitmap(oldBitmap.width, oldBitmap.height+60, oldBitmap.config)
        newBitmap.eraseColor(Color.WHITE)

        val canvas = Canvas(newBitmap)

        val paint = Paint()
        paint.color = Color.BLACK // Text Color
        paint.textSize = 24f // Text Size
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern

        // some more settings...
        canvas.drawBitmap(oldBitmap, 0f, 30f, null)
        canvas.drawText(productName, getTextWidth(productName, newBitmap, paint), 22f, paint)
        canvas.drawText(barcode, getTextWidth(barcode, newBitmap, paint), 264f, paint)

        return newBitmap
    }

    private fun getTextWidth(text: String, bitmap: Bitmap, paint: Paint):Float{
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        return bitmap.width / 2f - bounds.width() / 2f
    }

}