package uz.dev_abubakir_khakimov.product_controller.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapter
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapterCallBack
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentSearchBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel
import uz.dev_abubakir_khakimov.product_controller.models.Product

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

    }

    override fun moreSelectedListener(position: Int, view: View) {

    }

}