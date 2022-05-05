package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.zxing.WriterException
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentAddProductBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel
import uz.dev_abubakir_khakimov.product_controller.models.Product
import uz.dev_abubakir_khakimov.product_controller.utils.BarcodeManager
import java.io.*
import java.text.DecimalFormat
import java.util.*


class AddProductFragment : Fragment() {

    lateinit var binding: FragmentAddProductBinding
    lateinit var viewModel: MainViewModel
    lateinit var barcodeManager: BarcodeManager

    var bitmap: Bitmap? = null
    var thisBarcode: String = ""
    var thisGeneratedBarcode = false
    var sellingPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeManager = BarcodeManager(420, 210)

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.save.setOnClickListener {
            saveProduct()
        }

        binding.scanner.setOnClickListener {
            checkPermission()
        }

        binding.autoGenerate.setOnClickListener {
            generateBarcode()
        }

        binding.entryPrice.addTextChangedListener {
            generateSellingPrice()
        }

        binding.percent.addTextChangedListener {
            generateSellingPrice()
        }

    }

    private fun initObservers(){
        viewModel.compareResultData.observe(this){
            compareResult(it)
        }
    }

    private fun compareResult(it: Product?){
        if (it == null){
            thisGeneratedBarcode = false
            binding.barcode.setText(thisBarcode)
            createImageByBarcode(thisBarcode)
            binding.barcodeLayout.error = null
        }else{
            if (thisGeneratedBarcode) {
                generateBarcode()
            }else{
                Toast.makeText(requireActivity(), getString(R.string.barcode_registered), Toast.LENGTH_SHORT).show()
                binding.barcode.text?.clear()
                bitmap = null
                binding.barcodeImage.postDelayed({
                    binding.barcodeImage.setImageResource(R.drawable.ic_barcode_icon)
                }, 0)
            }
        }
    }

    private fun generateSellingPrice(){
        val entryPrice = binding.entryPrice.text.toString().toDoubleOrNull()
        val percent = binding.percent.text.toString().toDoubleOrNull()

        if (entryPrice != null && percent != null) {
            sellingPrice = entryPrice + (entryPrice * percent / 100)
            binding.sellingPrice.setText(getDecimalFormat(sellingPrice))
        }else{
            sellingPrice = 0.0
            binding.sellingPrice.text?.clear()
        }
    }

    private fun getDecimalFormat(it: Double): String{
        return DecimalFormat("#.###").format(it)
    }

    private fun showScanner(){
        setFragmentResultListener("result"){ requestKey, bundle ->
            thisBarcode = bundle.getString("barcode")!!
            viewModel.getProductEqualThisBarcode(thisBarcode)
        }

        findNavController().navigate(R.id.action_addProductFragment_to_scannerFragment, bundleOf(
            "fromAddProductFragment" to true
        ))
    }

    private fun generateBarcode(){
        thisBarcode = barcodeManager.generateBarcode()
        thisGeneratedBarcode = true
        viewModel.getProductEqualThisBarcode(thisBarcode)
    }

    private fun saveProduct(){
        val product: Product

        try {
            product = Product(
                0,
                binding.name.text.toString().trim(),
                binding.count.text.toString().toInt(),
                binding.entryPrice.text.toString().toDouble(),
                binding.percent.text.toString().toDouble(),
                sellingPrice,
                binding.term.text.toString().trim(),
                binding.firm.text.toString().trim(),
                binding.barcode.text.toString(),
                "",
                Date().time
            )
        }catch (e: NumberFormatException){
            Toast.makeText(requireActivity(), getString(R.string.please_fill_in_all_the_boxes), Toast.LENGTH_SHORT).show()
            return
        }

        if (checkForIsEmpty(product)){
            product.barcodeImagePath = saveBitmap(bitmap!!, "${product.barcode}_${product.name}")

            viewModel.insertProduct(product)
            Toast.makeText(requireActivity(), getString(R.string.successfully_saved), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }else{
            Toast.makeText(requireActivity(), getString(R.string.please_fill_in_all_the_boxes), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForIsEmpty(product: Product):Boolean{
        return product.name.isNotEmpty() &&
                product.term.isNotEmpty() &&
                product.firm.isNotEmpty() &&
                if (bitmap != null) true else {
                    binding.barcodeLayout.error = getString(R.string.barcode_fill)
                    false
                }
    }

    private fun createImageByBarcode(code: String){
        try {
            bitmap = barcodeManager.createImage(code)
            Glide.with(requireActivity()).load(bitmap).into(binding.barcodeImage)
        }catch (e: WriterException){
            e.printStackTrace()
        }
    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            showScanner()
        }else{
            requestCameraPermission()
        }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            showScanner()
        }
    }

    private fun requestCameraPermission(){
        cameraPermission.launch(Manifest.permission.CAMERA)
    }

    @Throws(IOException::class)
    fun saveBitmap(bmp: Bitmap, fileName: String): String {
        val bytes = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val f = File(requireActivity().filesDir,"$fileName.jpg")
        f.createNewFile()
        val fo = FileOutputStream(f)
        fo.write(bytes.toByteArray())
        fo.close()
        return f.absolutePath
    }

}
