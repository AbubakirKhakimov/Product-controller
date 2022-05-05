package uz.dev_abubakir_khakimov.product_controller.fragments

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
import com.google.android.material.snackbar.Snackbar
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentScannerBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel
import uz.dev_abubakir_khakimov.product_controller.models.Product

class ScannerFragment : Fragment(), ZBarScannerView.ResultHandler {
    
    lateinit var binding: FragmentScannerBinding
    lateinit var viewModel: MainViewModel
    val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    var snackBar: Snackbar? = null

    var fromAddProductFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initObservers()

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

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.refresh.setOnClickListener {
            initScanning()
        }

    }

    private fun initObservers() {
        viewModel.compareResultData.observe(this){
            vibrate()

            if (it == null){
                showSnackBar(getString(R.string.no_product_found_this_barcode), null)
            }else{
                showSnackBar(it.name, it)
            }
        }
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

    private fun initScanning(){
        binding.zbView.setResultHandler(this)
        binding.zbView.startCamera()

        snackBar?.dismiss()
        snackBar = null
    }

    override fun onPause() {
        super.onPause()
        binding.zbView.stopCamera()

        snackBar?.dismiss()
        snackBar = null
    }

    override fun onResume() {
        super.onResume()
        initScanning()
    }

    override fun handleResult(result: Result?) {
        if (fromAddProductFragment){
            setFragmentResult("result", bundleOf("barcode" to result!!.contents))
            findNavController().popBackStack()
        }else {
            viewModel.getProductEqualThisBarcode(result!!.contents)
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