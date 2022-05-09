package uz.dev_abubakir_khakimov.product_controller.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import uz.dev_abubakir_khakimov.product_controller.models.Product
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class ExcelManager(val context: Context, val productsList: ArrayList<Product>) {

    private val SHEET_NAME = "products"
    private val FILE_NAME = "Products_data_(${getStringDate(Date().time)}).xlsx"

    fun createWorkbook(): Workbook {
        // Creating excel workbook
        val workbook = XSSFWorkbook()

        //Creating first sheet inside workbook
        //Constants.SHEET_NAME is a string value of sheet name
        val sheet: Sheet = workbook.createSheet(SHEET_NAME)

        //Create Header Cell Style
//        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(sheet)

        //Adding data to the sheet
        addData(sheet)

        return workbook
    }

    private fun addData(sheet: Sheet) {
        productsList.forEachIndexed { index, product ->
            val row = sheet.createRow(index)
            addRowData(row, product)
        }
    }

    /** private fun addRowTitle(row: Row, cellStyle: CellStyle){
        createCell(row, 0, "id", cellStyle)
        createCell(row, 1, "nomi", cellStyle)
        createCell(row, 2, "soni", cellStyle)
        createCell(row, 3, "kelgannarx", cellStyle)
        createCell(row, 4, "foizi", cellStyle)
        createCell(row, 5, "sotilishnarxi", cellStyle)
        createCell(row, 6, "muddati", cellStyle)
        createCell(row, 7, "firma", cellStyle)
        createCell(row, 8, "shtrixraqam", cellStyle)
        createCell(row, 9, "kelgansana", cellStyle)
    } **/

    private fun addRowData(row: Row, product: Product){
        createCell(row, 0, product.id.toString())
        createCell(row, 1, product.name)
        createCell(row, 2, product.count.toString())
        createCell(row, 3, getDecimalFormat(product.entryPrice))
        createCell(row, 4, getDecimalFormat(product.percent))
        createCell(row, 5, getDecimalFormat(product.sellingPrice))
        createCell(row, 6, product.term)
        createCell(row, 7, product.firm)
        createCell(row, 8, product.barcode)
        createCell(row, 9, getStringDate(product.entryDate))
    }

    private fun getDecimalFormat(it: Double): String{
        return DecimalFormat("#.###").format(it).replace(",", ".")
    }

    private fun getStringDate(dateMillis: Long):String{
        return SimpleDateFormat("yyyy-MM-dd").format(Date(dateMillis))
    }

    private fun createCell(row: Row, columnIndex: Int, value: String?, cellStyle: CellStyle? = null) {
        val cell = row.createCell(columnIndex)
        cell?.setCellValue(value)
        cell?.cellStyle = cellStyle
    }

    /** private fun getHeaderStyle(workbook: Workbook): CellStyle {

        //Cell style for header row
        val cellStyle: CellStyle = workbook.createCellStyle()

        //Apply cell color
        val colorMap: IndexedColorMap = (workbook as XSSFWorkbook).stylesSource.indexedColors
        var color = XSSFColor(IndexedColors.SEA_GREEN, colorMap).indexed
        cellStyle.fillForegroundColor = color
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

        //Apply font style on cell text
        val whiteFont = workbook.createFont()
        color = XSSFColor(IndexedColors.WHITE, colorMap).indexed
        whiteFont.color = color
        whiteFont.bold = true
        cellStyle.setFont(whiteFont)

        cellStyle.setAlignment(HorizontalAlignment.CENTER)

        return cellStyle
    } **/

    private fun createSheetHeader(sheet: Sheet) {
        //setHeaderStyle is a custom function written below to add header style

        //Loop to populate each column of header row
        for (index in 0..9) {
            val columnWidth: Int = if (index == 0){
                15 * 100
            }else{
                15 * 500
            }

            //index represents the column number
            sheet.setColumnWidth(index, columnWidth)
        }
    }

    fun createExcel(workbook: Workbook): File {
        var fos: OutputStream? = null
        val file = checkFileExists()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //For devices running android >= Q
            //getting the contentResolver
            context.contentResolver?.also { resolver ->
                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {
                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val fileUri: Uri? =
                    resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)

                //Opening an outputstream with the Uri that we got
                fos = fileUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            fos = FileOutputStream(file)
        }

        //Write workbook to file using FileOutputStream
        try {
            workbook.write(fos)
            fos?.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    private fun checkFileExists(): File{
        val fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        if (fileDir != null && !fileDir.exists()) {
            fileDir.mkdirs()
        }

        val file = File(fileDir, FILE_NAME)

        if (file.exists()) {
            file.delete()
        }

        return file
    }

}