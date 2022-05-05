package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.R.attr.mimeType
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.material.snackbar.Snackbar
import org.apache.poi.hssf.usermodel.HeaderFooter.file
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapter
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapterCallBack
import uz.dev_abubakir_khakimov.product_controller.databinding.ExportExcelDialogLayoutBinding
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentHomeBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel
import uz.dev_abubakir_khakimov.product_controller.models.Product
import uz.dev_abubakir_khakimov.product_controller.utils.ExcelManager
import java.io.File


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
                    if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        exportExcel()
                    }else{
                        requestStoragePermission()
                    }
                }
                R.id.change_language -> {
                    ChangeLanguageFragment().show(childFragmentManager, "tag")
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

        var file: File

        Thread{
            ExcelManager(requireActivity(), productsList).apply {
                file = createExcel(createWorkbook())
            }

            requireActivity().runOnUiThread {
                customDialog.dismiss()
                showSnackBar(file)
            }
        }.start()

        customDialog.show()
    }

    private fun showSnackBar(file: File) {
        val snackBar = Snackbar.make(binding.root, getString(R.string.successfully_saved), Snackbar.LENGTH_LONG)

        snackBar.setAction(getString(R.string.view)) {
            openFile(file)
        }

        snackBar.show()
    }

    private fun openFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val apkURI = FileProvider.getUriForFile(
            requireActivity(),
            requireActivity().packageName, file
        )

        val myMime = MimeTypeMap.getSingleton()
        val mimeType =
            myMime.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(apkURI.toString())) //It will return the mimetype

        intent.setDataAndType(apkURI, mimeType)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        }catch (e: ActivityNotFoundException){
            Toast.makeText(requireActivity(), getString(R.string.no_app_view_excel), Toast.LENGTH_SHORT).show()
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
            findNavController().navigate(R.id.action_homeFragment_to_scannerFragment, bundleOf(
                "fromAddProductFragment" to false
            ))
        }else{
            requestCameraPermission()
        }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            findNavController().navigate(R.id.action_homeFragment_to_scannerFragment, bundleOf(
                "fromAddProductFragment" to false
            ))
        }
    }

    private fun requestCameraPermission(){
        cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private val storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            exportExcel()
        }
    }

    private fun requestStoragePermission(){
        storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}