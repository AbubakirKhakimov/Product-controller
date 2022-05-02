package uz.dev_abubakir_khakimov.product_controller.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentMoreInfoBinding
import uz.dev_abubakir_khakimov.product_controller.models.Product
import java.text.SimpleDateFormat
import java.util.*

class MoreInfoFragment : DialogFragment() {

    lateinit var binding: FragmentMoreInfoBinding
    lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = arguments?.getSerializable("selectedProduct") as Product
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMoreInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()

        binding.close.setOnClickListener {
            dismiss()
        }

    }

    private fun updateUI(){
        Glide.with(requireActivity()).load(product.barcodeImagePath).into(binding.barCodeImage)
        binding.barcode.text = product.barcode
        binding.name.text = getConcatenateStr("Name", product.name)
        binding.count.text = getConcatenateStr("Count", product.count.toString())
        binding.entryPrice.text = getConcatenateStr("Entry price", product.entryPrice)
        binding.sellingPrice.text = getConcatenateStr("Selling price", product.sellingPrice)
        binding.percent.text = getConcatenateStr("Percent", product.percent.toString())
        binding.term.text = getConcatenateStr("Term", product.term)
        binding.firmName.text = getConcatenateStr("Firm name", product.firm)
        binding.entryDate.text = getConcatenateStr("Entry date", getStringDate(product.entryDate))
    }

    private fun getStringDate(dateMillis: Long):String{
        return SimpleDateFormat("dd.MM.yyyy").format(Date(dateMillis))
    }

    private fun getConcatenateStr(title: String, info: String):String{
        return "$title: $info"
    }

    companion object {
        @JvmStatic
        fun newInstance(product: Product) =
            MoreInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("selectedProduct", product)
                }
            }
    }
}