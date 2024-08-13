package com.example.barcode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.barcode.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
    }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase 초기화
        auth = Firebase.auth

        // 현재 로그인된 사용자가 있는지 확인
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // 사용자가 이미 로그인된 경우 바로 SelectButton 액티비티로 이동
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        binding.signupLink.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }
        binding.loginBtn.setOnClickListener {
            val email = binding.emailValue.text.toString()
            val password = binding.passwordValue.text.toString()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, HomePage::class.java)
                        startActivity(intent)
                        finish() // 현재 액티비티 종료
                    }
                }
        }
    }
}
