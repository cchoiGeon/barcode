package com.example.barcode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.barcode.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
    }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.signupBtn.setOnClickListener{
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }
        // Firebase 초기화 확인 및 실행
        binding.loginBtn.setOnClickListener {
            auth = Firebase.auth
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, SelectButton::class.java)
                        startActivity(intent)
                    }
                }
        }
    }
}
