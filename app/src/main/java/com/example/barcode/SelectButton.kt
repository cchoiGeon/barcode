package com.example.barcode

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.barcode.databinding.ActivitySelectButtonBinding

class SelectButton : AppCompatActivity() {
    // 변수 선언: 카메라 권한 요청을 처리하기 위한 ActivityResultLauncher
    private var requestCamera: ActivityResultLauncher<String>? = null // ActivityResultLauncher: 카메라 권한 요청에 대한 결과를 처리하기 위한 인터페이스
    private val binding : ActivitySelectButtonBinding by lazy {
        ActivitySelectButtonBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 카메라 권한 요청에 대한 결과를 처리하기 위한 ActivityResultLauncher 설정
        requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // 권한이 허용되면 바코드 스캔 액티비티를 시작, 그렇지 않으면 퍼미션 거부 메시지 출력
            if (isGranted) {
                startBarcodeScanActivity()
            } else {
                Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show()
            }
        }

        // 버튼 클릭 시 카메라 권한 요청
        binding.barcodeScanBtn.setOnClickListener {
            requestCamera?.launch(Manifest.permission.CAMERA)
        }
    }

    // 바코드 스캔 액티비티 시작 함수
    private fun startBarcodeScanActivity() {
        val intent = Intent(this, BarcodeScan::class.java)
        startActivity(intent)
    }
}