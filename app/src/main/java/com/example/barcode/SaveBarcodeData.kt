package com.example.barcode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.barcode.databinding.ActivitySaveBarcodeDataBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SaveBarcodeData : AppCompatActivity() {

    data class ApiResponse(
        val C005: ResponseDetails
    )

    data class ResponseDetails(
        val total_count: String,
        val row: List<Product>,
        val RESULT: ApiResult
    )

    data class ApiResult(
        val MSG: String,
        val CODE: String
    )

    data class Product(
        val PRDLST_NM: String, // 제품명
        val POG_DAYCNT: String, // 소비기한
        val PRDLST_DCNM: String, // 식품 유형
        val SITE_ADDR: String, // 주소
        val BAR_CD: String, // 바코드
        val BSSH_NM: String // 회사명
    )

    interface ProductApiService {
        @GET("C005/json/1/5/")
        fun getProductByBarcode(@Query("BAR_CD") barcode: String): Call<ApiResponse>
    }

    private val binding: ActivitySaveBarcodeDataBinding by lazy {
        ActivitySaveBarcodeDataBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // intentData 초기화는 onCreate 내부에서만 수행
        val intentData = intent.getStringExtra("intentDataKey").takeUnless { it.isNullOrBlank() } ?: "8801062713271"
        val db = Firebase.firestore
        if (intentData != "바코드 정보 없음") {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://openapi.foodsafetykorea.go.kr/api/0cca38a8d99f4fe89e1d/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ProductApiService::class.java)
            apiService.getProductByBarcode(intentData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { apiResponse ->
                            val product = apiResponse.C005.row.firstOrNull()
                            if (product != null) {
                                Log.d("SaveBarcodeData", product.toString());
                                binding.productionName.text = product.PRDLST_NM
                                binding.productionDue.text = product.POG_DAYCNT
                                binding.productionKind.text = product.PRDLST_DCNM
                            } else {
                                binding.productionName.text = "제품 정보가 없습니다"
                                binding.productionDue.text = "제품 정보가 없습니다"
                                binding.productionKind.text = "제품 정보가 없습니다"
                            }
                        }
                    } else {
                        Log.e("MyRefrigerator", "Response not successful: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("MyRefrigerator", "Error fetching product data", t)
                    Toast.makeText(this@SaveBarcodeData, "제품 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_LONG).show()
                }
            })
        }

        binding.submitBtn.setOnClickListener{
            val user = Firebase.auth.currentUser
            val uid = user?.uid ?: "123"
            // 여기도 동일한 방식으로 intentData 초기화
            val data = hashMapOf(
                "제품 이름" to binding.productionName.text.toString(),
                "제품 소비기한" to binding.productionDue.text.toString(),
                "제품 유형" to binding.productionKind.text.toString(),
                "제품 유통기한" to binding.Due.text.toString()
            )

            db.collection("data").document(uid).collection("barcode").document(intentData).set(data)
                .addOnSuccessListener { documentReference ->
                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        }
    }
}
