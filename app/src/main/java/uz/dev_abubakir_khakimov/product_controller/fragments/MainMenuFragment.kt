package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uz.dev_abubakir_khakimov.product_controller.BuildConfig
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentMainMenuBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel

class MainMenuFragment : Fragment() {

    lateinit var binding: FragmentMainMenuBinding
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.productsList.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_homeFragment)
        }

        binding.addProduct.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_addProductFragment)
        }

        binding.language.setOnClickListener {
            ChangeLanguageFragment().show(childFragmentManager, "tag")
        }

        binding.scanner.setOnClickListener {
            showScanner()
        }

        viewModel.checkVersion(BuildConfig.VERSION_CODE).observe(viewLifecycleOwner){
            if (!it){
                NotPermittedFragment().show(childFragmentManager, "tag")
            }
        }

    }

    private fun showScanner(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            findNavController().navigate(R.id.action_mainMenuFragment_to_scannerFragment, bundleOf(
                "fromAddProductFragment" to false
            ))
        }else{
            requestCameraPermission()
        }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            findNavController().navigate(R.id.action_mainMenuFragment_to_scannerFragment, bundleOf(
                "fromAddProductFragment" to false
            ))
        }
    }

    private fun requestCameraPermission(){
        cameraPermission.launch(Manifest.permission.CAMERA)
    }

}