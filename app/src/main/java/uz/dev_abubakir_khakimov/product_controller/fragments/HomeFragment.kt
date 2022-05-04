package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.apache.poi.hssf.usermodel.HeaderFooter.file
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapter
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapterCallBack
import uz.dev_abubakir_khakimov.product_controller.databinding.ExportExcelDialogLayoutBinding
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentHomeBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel
import uz.dev_abubakir_khakimov.product_controller.models.Product
import uz.dev_abubakir_khakimov.product_controller.utils.ExcelManager


class HomeFragment : Fragment(), ProductsListAdapterCallBack {

    lateinit var binding: FragmentHomeBinding
    lateinit var viewModel: MainViewModel
    lateinit var productsListAdapter: ProductsListAdapter

    val productsList = ArrayList<Product>()

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
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productsListAdapter = ProductsListAdapter(productsList, this)
        binding.productsListRv.adapter = productsListAdapter

        readAllProducts()

        binding.openDrawer.setOnClickListener {
            binding.root.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.scanner -> {
                    showScanner()
                }
                R.id.add_product -> {
                    findNavController().navigate(R.id.action_homeFragment_to_addProductFragment)
                }
                R.id.export_excel -> {
                    exportExcel()
                }
            }

            binding.root.closeDrawers()
            true
        }

        binding.scanner.setOnClickListener {
            showScanner()
        }

    }

    private fun exportExcel(){
        val customDialog = AlertDialog.Builder(requireActivity()).create()
        customDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customDialog.setCancelable(false)
        val dialogBinding = ExportExcelDialogLayoutBinding.inflate(layoutInflater)
        customDialog.setView(dialogBinding.root)

        var path: String

        Thread{
            ExcelManager(requireActivity(), productsList).apply {
                path = createExcel(createWorkbook())
            }

            requireActivity().runOnUiThread {
                customDialog.dismiss()
                Toast.makeText(requireActivity(), "Successfully saved!", Toast.LENGTH_SHORT).show()
                openFile(path)
            }
        }.start()

        customDialog.show()
    }

    private fun openFile(path: String) {
        val excelIntent = Intent(Intent.ACTION_VIEW, "ms-excel:ofv|u|${path}".toUri())
        excelIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        try {
            startActivity(excelIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireActivity(),
                "No Application available to viewExcel",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun readAllProducts(){
        viewModel.readAllProducts()
    }

    private fun initObservers() {
        viewModel.readAllProductsData.observe(this){
            productsList.apply {
                clear()
                addAll(it)
            }

            productsListAdapter.notifyDataSetChanged()
        }
    }

    override fun itemSelectedListener(position: Int) {
        MoreInfoFragment.newInstance(productsList[position]).show(childFragmentManager, "tag")
    }

    private fun showScanner(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            findNavController().navigate(R.id.action_homeFragment_to_scannerFragment)
        }else{
            requestPermission()
        }
    }

    private val requestPermissionCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            findNavController().navigate(R.id.action_homeFragment_to_scannerFragment)
        }
    }

    private fun requestPermission(){
        requestPermissionCamera.launch(Manifest.permission.CAMERA)
    }

}