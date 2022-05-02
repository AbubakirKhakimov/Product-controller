package uz.dev_abubakir_khakimov.product_controller.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentScannerBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel

class ScannerFragment : Fragment(), ZBarScannerView.ResultHandler {
    
    lateinit var binding: FragmentScannerBinding
    lateinit var viewModel: MainViewModel
    val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

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
                Toast.makeText(requireActivity(), "No such barcode found!", Toast.LENGTH_SHORT).show()
            }else{
                MoreInfoFragment.newInstance(it).show(childFragmentManager, "tag")
            }
        }
    }

    private fun initScanning(){
        binding.zbView.setResultHandler(this)
        binding.zbView.startCamera()
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