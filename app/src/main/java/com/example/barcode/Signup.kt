package com.example.barcode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.barcode.databinding.ActivitySignupBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Signup : AppCompatActivity() {
    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
    }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.okBtn.setOnClickListener {
            auth = Firebase.auth
            var email =  binding.emailValue.text.toString()
            var password =  binding.passwordValue.text.toString()
            var checkPassword = binding.checkPassword.text.toString()
            if(password == checkPassword){
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, SelectButton::class.java)
                            startActivity(intent)
                        }else{
                            binding.errorCode.text = task.exception.toString()
                        }
                    }
            }
        }
    }
}