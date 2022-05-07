package uz.dev_abubakir_khakimov.product_controller.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.dev_abubakir_khakimov.product_controller.databinding.ProductsListItemLayoutBinding
import uz.dev_abubakir_khakimov.product_controller.models.Product
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

interface ProductsListAdapterCallBack{
    fun itemSelectedListener(position: Int)
    fun moreSelectedListener(position: Int, view: View)
}

class ProductsListAdapter(val productsList: ArrayList<Product>, val productsListAdapterCallBack: ProductsListAdapterCallBack)
    :RecyclerView.Adapter<ProductsListAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ProductsListItemLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ProductsListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = productsList[position]
        holder.binding.productName.text = item.name
        holder.binding.firmName.text = item.firm
        holder.binding.entryDate.text = getStringDate(item.entryDate)
        holder.binding.barcode.text = item.barcode
        Glide.with(holder.binding.root).load(item.barcodeImagePath).into(holder.binding.barCodeImage)

        holder.binding.root.setOnClickListener {
            productsListAdapterCallBack.itemSelectedListener(position)
        }

        holder.binding.moreFunc.setOnClickListener {
            productsListAdapterCallBack.moreSelectedListener(position, it)
        }
    }

    override fun getItemCount(): Int {
        return productsList.size
    }

    private fun getStringDate(dateMillis: Long):String{
        return SimpleDateFormat("dd.MM.yyyy").format(Date(dateMillis))
    }

}