package uz.abubakir_khakimov.product_controller.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.snackbar.Snackbar
import uz.abubakir_khakimov.product_controller.R
import uz.abubakir_khakimov.product_controller.databinding.FragmentScannerBinding
import uz.abubakir_khakimov.product_controller.models.MainViewModel
import uz.abubakir_khakimov.product_controller.models.Product

class ScannerFragment : Fragment() {

    private lateinit var binding: FragmentScannerBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var codeScanner: CodeScanner
    private val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    private var snackBar: Snackbar? = null

    private var fromAddProductFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        fromAddProductFragment = arguments?.getBoolean("fromAddProductFragment", false)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScannerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initScanning()

        codeScanner.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                if (fromAddProductFragment){
                    setFragmentResult("result", bundleOf("barcode" to it.text))
                    findNavController().popBackStack()
                }else {
                    viewModel.getProductEqualThisBarcode(it.text).observe(viewLifecycleOwner){
                        compareResult(it)
                    }
                }
            }
        }

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.refresh.setOnClickListener {
            codeScanner.startPreview()

            snackBar?.dismiss()
            snackBar = null
        }
    }

    private fun initScanning(){
        codeScanner = CodeScanner(requireActivity(), binding.scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ONE_DIMENSIONAL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false
    }

    private fun showSnackBar(message: String, product: Product?){
        snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)

        if (product != null){
            snackBar!!.setAction(getString(R.string.view)){
                MoreInfoFragment.newInstance(product).show(childFragmentManager, "tag")
            }
        }

        snackBar!!.show()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()

        snackBar?.dismiss()
        snackBar = null
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    private fun compareResult(it: Product?) {
        vibrate()

        if (it == null){
            showSnackBar(getString(R.string.no_product_found_this_barcode), null)
        }else{
            showSnackBar(it.name, it)
        }
    }

    private fun vibrate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(200)
        }
    }

}