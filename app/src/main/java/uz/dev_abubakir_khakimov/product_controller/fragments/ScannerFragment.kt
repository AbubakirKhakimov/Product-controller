package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
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
                showSnackBar("No product found with this barcode!", null)
            }else{
                showSnackBar(it.name, it)
            }
        }
    }

    private fun showSnackBar(message: String, product: Product?){
        snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)

        if (product != null){
            snackBar!!.setAction("View"){
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
    }

    override fun onResume() {
        super.onResume()
        initScanning()
    }

    override fun handleResult(result: Result?) {
        viewModel.getProductEqualThisBarcode(result!!.contents)
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