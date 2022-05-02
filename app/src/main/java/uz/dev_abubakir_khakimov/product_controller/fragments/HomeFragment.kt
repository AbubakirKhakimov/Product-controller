package uz.dev_abubakir_khakimov.product_controller.fragments

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapter
import uz.dev_abubakir_khakimov.product_controller.adapters.ProductsListAdapterCallBack
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentHomeBinding
import uz.dev_abubakir_khakimov.product_controller.models.MainViewModel
import uz.dev_abubakir_khakimov.product_controller.models.Product

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
                    findNavController().navigate(R.id.action_homeFragment_to_scannerFragment)
                }
                R.id.add_product -> {
                    findNavController().navigate(R.id.action_homeFragment_to_addProductFragment)
                }
                R.id.export_excel -> {

                }
            }

            binding.root.closeDrawers()
            true
        }

        binding.scanner.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_scannerFragment)
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

}