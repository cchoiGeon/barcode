package com.example.barcode

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barcode.databinding.ActivityHomePageBinding
import com.example.barcode.databinding.ItemProductBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

data class Product(
    val productName: String = "",
    val productExpiryDate: String = "" // 유통기한
)

class HomePage : AppCompatActivity() {
    private val binding : ActivityHomePageBinding by lazy {
        ActivityHomePageBinding.inflate(layoutInflater).also{
            setContentView(it.root)
        }
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        firestore = FirebaseFirestore.getInstance()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter()
        binding.recyclerView.adapter = adapter

        loadProducts()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {
                val intent = Intent(this, HomePage::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_barcode_scan -> {
                val intent = Intent(this, BarcodeScan::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_recommendation -> {
//                val intent = Intent(this, A::class.java) // A 클래스를 추천 화면으로 교체
//                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadProducts() {
        val uid = Firebase.auth.currentUser?.uid ?: "123" // 기본 UID 설정
        firestore.collection("data").document(uid).collection("barcode")
            .get()
            .addOnSuccessListener { documents ->
                val productList = ArrayList<Product>()
                for (document in documents) {
                    val productName = document.getString("제품 이름") ?: "제품명 없음"
                    val expiryDate = document.getString("제품 유통기한") ?: ""

                    val daysLeft = calculateDaysLeft(expiryDate)
                    val displayExpiry = if (daysLeft != null) "D-$daysLeft" else "유통기한 없음"

                    productList.add(Product(productName, displayExpiry))
                }
                adapter.submitList(productList)
            }
            .addOnFailureListener { exception ->
                // 실패 시 처리
            }
    }

    // 유통기한 남은 일수 계산
    private fun calculateDaysLeft(expiryDate: String): Int? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) // 날짜 형식 변경
            val date = dateFormat.parse(expiryDate)
            val today = Date()
            val diff = date?.time?.minus(today.time)
            val daysLeft = diff?.div(1000 * 60 * 60 * 24)?.toInt()
            daysLeft
        } catch (e: Exception) {
            null
        }
    }

    class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ProductViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(product: Product) {
                binding.productName.text = product.productName
                binding.productExpiry.text = product.productExpiryDate
            }
        }

        class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
                return oldItem.productName == newItem.productName
            }

            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
                return oldItem == newItem
            }
        }
    }
}
