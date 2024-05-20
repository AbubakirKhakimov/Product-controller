package uz.abubakir_khakimov.product_controller.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uz.abubakir_khakimov.product_controller.R
import uz.abubakir_khakimov.product_controller.adapters.ProductsListAdapter
import uz.abubakir_khakimov.product_controller.adapters.ProductsListAdapterCallBack
import uz.abubakir_khakimov.product_controller.databinding.FragmentSearchBinding
import uz.abubakir_khakimov.product_controller.models.MainViewModel
import uz.abubakir_khakimov.product_controller.models.Product
import java.io.File

class SearchFragment : Fragment(), ProductsListAdapterCallBack {

    lateinit var binding: FragmentSearchBinding
    lateinit var productsListAdapter: ProductsListAdapter
    lateinit var viewModel: MainViewModel

    val searchResultsList = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productsListAdapter = ProductsListAdapter(searchResultsList, this)
        binding.searchResultsRv.adapter = productsListAdapter

        binding.searchText.requestFocus()
        showKeyboard()

        binding.backStack.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.searchText.addTextChangedListener {
            if (it.toString().isEmpty()){
                updateUI(emptyList())
            }else {
                viewModel.searchProducts("%$it%").observe(viewLifecycleOwner) {
                    updateUI(it)
                }
            }
        }

    }

    private fun updateUI(it: List<Product>) {
        searchResultsList.apply {
            clear()
            addAll(it)
        }

        productsListAdapter.notifyDataSetChanged()
    }

    private fun showKeyboard(){
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun itemSelectedListener(position: Int) {
        MoreInfoFragment.newInstance(searchResultsList[position]).show(childFragmentManager, "tag")
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
                    findNavController().navigate(R.id.action_searchFragment_to_addProductFragment, bundleOf(
                        "selectedProduct" to searchResultsList[position]
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
        viewModel.removeProduct(searchResultsList[position])
        deleteImage(searchResultsList[position].barcodeImagePath)

        searchResultsList.removeAt(position)
        productsListAdapter.notifyItemRemoved(position)
        productsListAdapter.notifyItemRangeChanged(0, searchResultsList.size)

        Toast.makeText(requireActivity(), getString(R.string.successfully_removed), Toast.LENGTH_SHORT).show()
    }

    private fun deleteImage(path: String){
        val file = File(path)
        if (file.exists()){
            file.delete()
        }
    }

}