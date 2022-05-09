package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.databinding.ExportExcelDialogLayoutBinding
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentMainMenuBinding
import uz.dev_abubakir_khakimov.product_controller.utils.ExcelManager
import java.io.File

class MainMenuFragment : Fragment() {

    lateinit var binding: FragmentMainMenuBinding

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