package uz.dev_abubakir_khakimov.product_controller.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.apache.poi.openxml4j.exceptions.InvalidFormatException
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapter
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapterCallBack
import uz.dev_abubakir_khakimov.product_controller.databinding.ExcelProgressDialogLayoutBinding
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentHomeBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel
import uz.dev_abubakir_khakimov.product_controller.models.Product
import uz.dev_abubakir_khakimov.product_controller.utils.ExcelManager
import uz.dev_abubakir_khakimov.product_controller.utils.ExcelExportCallBack
import uz.dev_abubakir_khakimov.product_controller.utils.ExcelImportCallBack
import java.io.File


class HomeFragment : Fragment(), ProductsListAdapterCallBack {

    lateinit var binding: FragmentHomeBinding
    lateinit var viewModel: MainViewModel
    lateinit var excelManager: ExcelManager
    lateinit var productsListAdapter: ProductsListAdapter
    lateinit var productsLiveData: LiveData<List<Product>>

    val productsList = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
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

        excelManager = ExcelManager(requireActivity())
        productsListAdapter = ProductsListAdapter(productsList, this)
        binding.productsListRv.adapter = productsListAdapter

        readAllProducts()

        binding.exportImport.setOnClickListener {
            showExportImportMenu()
        }

        binding.sortMode.setOnClickListener {
            showSortMenu()
        }

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.search.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

    }

    private fun showSortMenu(){
        val popupMenu = PopupMenu(requireActivity(), binding.sortMode)
        popupMenu.inflate(R.menu.sort_popup_menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.sortByBarcode -> {
                    viewModel.sortByBarcode().observe(viewLifecycleOwner){
                        updateUI(it)
                    }
                }
                R.id.sortByName -> {
                    viewModel.sortByName().observe(viewLifecycleOwner){
                        updateUI(it)
                    }
                }
                R.id.sortByDate -> {
                    viewModel.sortByDate().observe(viewLifecycleOwner){
                        updateUI(it)
                    }
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun updateUI(it: List<Product>){
        productsList.apply {
            clear()
            addAll(it)
        }

        productsListAdapter.notifyDataSetChanged()
    }

    private fun readAllProducts() {
        productsLiveData = viewModel.getAllProducts()
        productsLiveData.observe(viewLifecycleOwner) {
            updateUI(it)
        }
    }

    override fun itemSelectedListener(position: Int) {
        MoreInfoFragment.newInstance(productsList[position]).show(childFragmentManager, "tag")
    }

    override fun moreSelectedListener(position: Int, view: View) {
        showItemMoreMenu(position, view)
    }

    private fun showItemMoreMenu(position: Int, view: View){
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.inflate(R.menu.more_func_menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.edit -> {
                    findNavController().navigate(R.id.action_homeFragment_to_addProductFragment, bundleOf(
                        "selectedProduct" to productsList[position]
                    ))
                }
                R.id.remove -> {
                    removeProduct(position)
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun removeProduct(position: Int){
        viewModel.removeProduct(productsList[position])
        deleteImage(productsList[position].barcodeImagePath)

        productsList.removeAt(position)
        productsListAdapter.notifyItemRemoved(position)
        productsListAdapter.notifyItemRangeChanged(0, productsList.size)

        Toast.makeText(requireActivity(), getString(R.string.successfully_removed), Toast.LENGTH_SHORT).show()
    }

    private fun deleteImage(path: String){
        val file = File(path)
        if (file.exists()){
            file.delete()
        }
    }

    private fun showExportImportMenu(){
        val popupMenu = PopupMenu(requireActivity(), binding.exportImport)
        popupMenu.inflate(R.menu.export_import_menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.export_excel -> {
                    preparationExport()
                }
                R.id.import_excel -> {
                    showFileChooser()
                }
            }
            true
        }
        popupMenu.show()
    }

    val fileChooser = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null){
            importExcel(uri)
        }
    }

    private fun showFileChooser(){
        fileChooser.launch("*/*")
    }

    private fun importExcel(uri: Uri){
        val customDialog = AlertDialog.Builder(requireActivity()).create()
        customDialog.setCancelable(false)
        val dialogBinding = ExcelProgressDialogLayoutBinding.inflate(layoutInflater)
        customDialog.setView(dialogBinding.root)

        dialogBinding.title.text = getString(R.string.importing)
        var max = 0

        Thread{
            try {
                excelManager.importExcel(uri, object :ExcelImportCallBack{
                    override fun getItemSize(size: Int) {
                        max = size

                        requireActivity().runOnUiThread {
                            productsLiveData.removeObservers(viewLifecycleOwner)
                            dialogBinding.progressBar.max = size
                            dialogBinding.progress.text = getProgress(0, size)
                        }
                    }

                    override fun saveItem(product: Product, progress: Int) {
                        viewModel.insertProduct(product)

                        requireActivity().runOnUiThread {
                            dialogBinding.progressBar.progress = progress
                            dialogBinding.progress.text = getProgress(progress, max)
                        }
                    }
                })
            }catch (e: InvalidFormatException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, getString(R.string.file_error), Toast.LENGTH_SHORT).show()
                }
            }catch (e: IllegalStateException){
                requireActivity().runOnUiThread {
                    Toast.makeText(context, getString(R.string.cell_type_error), Toast.LENGTH_SHORT).show()
                }
            }

            requireActivity().runOnUiThread {
                customDialog.dismiss()
                readAllProducts()
            }
        }.start()

        customDialog.show()
    }

    private fun preparationExport(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            exportExcel()
        }else{
            requestStoragePermission()
        }
    }

    private fun exportExcel(){
        val customDialog = AlertDialog.Builder(requireActivity()).create()
        customDialog.setCancelable(false)
        val dialogBinding = ExcelProgressDialogLayoutBinding.inflate(layoutInflater)
        customDialog.setView(dialogBinding.root)

        var file: File

        dialogBinding.title.text = getString(R.string.exporting)
        dialogBinding.progressBar.max = productsList.size
        dialogBinding.progress.text = getProgress(0, productsList.size)

        Thread{
            file = excelManager.exportExcel(productsList, object :ExcelExportCallBack{
                override fun itemAdded(progress: Int) {
                    requireActivity().runOnUiThread {
                        dialogBinding.progressBar.progress = progress
                        dialogBinding.progress.text = getProgress(progress, productsList.size)
                    }
                }
            })

            requireActivity().runOnUiThread {
                customDialog.dismiss()
                showSnackBar(file)
            }
        }.start()

        customDialog.show()
    }

    private fun getProgress(progress: Int, max: Int): String {
        return "$progress / $max"
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

    private val storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            exportExcel()
        }
    }

    private fun requestStoragePermission(){
        storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}