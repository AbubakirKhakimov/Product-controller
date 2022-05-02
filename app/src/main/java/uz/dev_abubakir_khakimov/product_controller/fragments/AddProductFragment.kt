package uz.dev_abubakir_khakimov.product_controller.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
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
import java.util.*


class AddProductFragment : Fragment() {

    lateinit var binding: FragmentAddProductBinding
    lateinit var viewModel: MainViewModel
    lateinit var barcodeManager: BarcodeManager

    var bitmap: Bitmap? = null
    var generatedBarcode: String = ""

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

        binding.barcode.addTextChangedListener {
            if (it.toString().length == 13){
                createImageByBarcode(it.toString())
            }else{
                bitmap = null
                binding.barcodeImage.setImageResource(R.drawable.ic_barcode_icon)
            }

            binding.barcodeLayout.error = null
        }

        binding.autoGenerate.setOnClickListener {
            generateBarcode()
        }

    }

    private fun initObservers(){
        viewModel.compareResultData.observe(this){
            if (it == null){
                binding.barcode.setText(generatedBarcode)
            }else{
                generateBarcode()
            }
        }
    }

    private fun generateBarcode(){
        generatedBarcode = barcodeManager.generateBarcode()
        viewModel.getProductEqualThisBarcode(generatedBarcode)
    }

    private fun saveProduct(){
        val product: Product

        try {
            product = Product(
                0,
                binding.name.text.toString().trim(),
                binding.count.text.toString().toInt(),
                binding.entryPrice.text.toString().trim(),
                binding.percent.text.toString().toDouble(),
                binding.sellingPrice.text.toString().trim(),
                binding.term.text.toString().trim(),
                binding.firm.text.toString().trim(),
                binding.barcode.text.toString(),
                "",
                Date().time
            )
        }catch (e: NumberFormatException){
            Toast.makeText(requireActivity(), "Please fill in all the boxes!", Toast.LENGTH_SHORT).show()
            return
        }

        if (checkForIsEmpty(product)){
            product.barcodeImagePath = saveBitmap(bitmap!!, "${product.barcode}_${product.name}")

            viewModel.insertProduct(product)
            Toast.makeText(requireActivity(), "Successfully saved!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }else{
            Toast.makeText(requireActivity(), "Please fill in all the boxes!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForIsEmpty(product: Product):Boolean{
        return product.name.isNotEmpty() &&
                product.entryPrice.isNotEmpty() &&
                product.sellingPrice.isNotEmpty() &&
                product.term.isNotEmpty() &&
                product.firm.isNotEmpty() &&
                if (bitmap != null) true else {
                    binding.barcodeLayout.error = "The barcode must not be less than 13 digits!"
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
