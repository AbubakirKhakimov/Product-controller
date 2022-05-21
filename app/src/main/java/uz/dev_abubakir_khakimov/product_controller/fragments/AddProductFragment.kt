package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import uz.dev_abubakir_khakimov.product_controller.utils.Constants
import uz.dev_abubakir_khakimov.product_controller.utils.MediaSaveManager
import java.io.*
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class AddProductFragment : Fragment() {

    lateinit var binding: FragmentAddProductBinding
    lateinit var viewModel: MainViewModel
    lateinit var barcodeManager: BarcodeManager
    lateinit var autoCompleteAdapter: ArrayAdapter<String>

    val namesList = ArrayList<String>()
    var product: Product? = null
    var bitmap: Bitmap? = null
    var thisBarcode: String = ""
    var thisGeneratedBarcode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        product = arguments?.getSerializable("selectedProduct") as Product?
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

        barcodeManager = BarcodeManager(Constants.BARCODE_IMAGE_WIDTH, Constants.BARCODE_IMAGE_HEIGHT)
        getAllNames()

        checkEditOrAddMode()

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.save.setOnClickListener {
            saveProduct()
        }

        binding.barcodeImage.setOnClickListener {
            if (product == null) {
                checkPermission()
            }
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

    private fun getAllNames() {
        viewModel.getAllProducts().observe(viewLifecycleOwner){ products ->
            namesList.clear()
            products.forEach {
                namesList.add(it.name)
            }
            autoCompleteAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, namesList)
            binding.name.setAdapter(autoCompleteAdapter)
        }
    }

    private fun checkEditOrAddMode(){
        if (product != null){
            binding.barcode.setText(product!!.barcode)
            binding.name.setText(product!!.name)
            binding.count.setText(product!!.count.toString())
            binding.entryPrice.setText(getDecimalFormat(product!!.entryPrice))
            binding.percent.setText(getDecimalFormat(product!!.percent))
            binding.sellingPrice.setText(getDecimalFormat(product!!.sellingPrice))
            binding.term.setText(product!!.term.toString())
            binding.firm.setText(product!!.firm)
            Glide.with(requireActivity()).load(product!!.barcodeImagePath).into(binding.barcodeImage)
            binding.autoGenerate.visibility = View.GONE
            binding.title.text = getString(R.string.edit_product)
        }else{
            if(bitmap != null){
                Glide.with(requireActivity()).load(bitmap).into(binding.barcodeImage)
            }
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
                product = it
                checkEditOrAddMode()
            }
        }
    }

    private fun generateSellingPrice(){
        val entryPrice = binding.entryPrice.text.toString().toDoubleOrNull()
        val percent = binding.percent.text.toString().toDoubleOrNull()

        if (entryPrice != null && percent != null) {
            val sellingPrice = entryPrice + (entryPrice * percent / 100)
            binding.sellingPrice.setText(getDecimalFormat(sellingPrice))
        }else{
            binding.sellingPrice.text?.clear()
        }
    }

    private fun getDecimalFormat(it: Double): String{
        return DecimalFormat("#.###").format(it).replace(",", ".")
    }

    private fun showScanner(){
        setFragmentResultListener("result"){ requestKey, bundle ->
            thisBarcode = bundle.getString("barcode")!!
            viewModel.getProductEqualThisBarcode(thisBarcode).observe(viewLifecycleOwner){
                compareResult(it)
            }
        }

        findNavController().navigate(R.id.action_addProductFragment_to_scannerFragment, bundleOf(
            "fromAddProductFragment" to true
        ))
    }

    private fun generateBarcode(){
        thisBarcode = barcodeManager.generateBarcode()
        thisGeneratedBarcode = true
        viewModel.getProductEqualThisBarcode(thisBarcode).observe(viewLifecycleOwner){
            compareResult(it)
        }
    }

    private fun saveProduct(){
        val newProduct: Product

        try {
            newProduct = Product(
                product?.id ?: 0,
                binding.name.text.toString().trim(),
                binding.count.text.toString().toInt(),
                binding.entryPrice.text.toString().toDouble(),
                binding.percent.text.toString().toDouble(),
                binding.sellingPrice.text.toString().toDouble(),
                binding.term.text.toString().toInt(),
                binding.firm.text.toString().trim(),
                binding.barcode.text.toString(),
                "",
                Date().time
            )
        }catch (e: NumberFormatException){
            Toast.makeText(requireActivity(), getString(R.string.please_fill_in_all_the_boxes), Toast.LENGTH_SHORT).show()
            return
        }

        if (checkForIsEmpty(newProduct)){
            if (product == null){
                addProduct(newProduct)
            }else{
                editProduct(newProduct)
            }
        }else{
            Toast.makeText(requireActivity(), getString(R.string.please_fill_in_all_the_boxes), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addProduct(newProduct: Product){
        newProduct.barcodeImagePath = MediaSaveManager(requireActivity())
            .saveBitmapToInterStorage(bitmap!!, "${newProduct.barcode}_${newProduct.name}")

        viewModel.insertProduct(newProduct)
        Toast.makeText(requireActivity(), getString(R.string.successfully_saved), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun editProduct(newProduct: Product){
        newProduct.barcodeImagePath = product!!.barcodeImagePath

        viewModel.editProduct(newProduct)
        Toast.makeText(requireActivity(), getString(R.string.successfully_saved), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun checkForIsEmpty(newProduct: Product):Boolean{
        return newProduct.name.isNotEmpty() &&
                newProduct.firm.isNotEmpty() &&
                if (bitmap != null || product != null) true else {
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

}
