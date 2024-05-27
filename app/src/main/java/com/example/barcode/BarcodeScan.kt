package com.example.barcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.barcode.databinding.ActivityBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class BarcodeScan : AppCompatActivity() {
    private val binding: ActivityBarcodeScanBinding by lazy {
        ActivityBarcodeScanBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
    }

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private var intentData = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            initializeBarcodeScanner()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        binding.btnAction.setOnClickListener {
            if (intentData.isNotEmpty()) {
                val intent = Intent(this, MyRefrigerator::class.java)
                intent.putExtra("intentDataKey", intentData)
                startActivity(intent)
            } else {
                Toast.makeText(this, "바코드가 아직 스캔되지 않았습니다", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MyRefrigerator::class.java)
                intent.putExtra("intentDataKey", "intentData")
                startActivity(intent)
            }
        }
    }

    private fun initializeBarcodeScanner() {
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()

        binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "바코드 스캐너가 중지되었습니다", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    binding.textBarcodeValue.post {
                        binding.btnAction.text = "바코드 확인"
                        intentData = barcodes.valueAt(0).displayValue
                        binding.textBarcodeValue.text = intentData
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeBarcodeScanner()
            } else {
                Toast.makeText(this, "카메라 권한이 거부되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.release()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
