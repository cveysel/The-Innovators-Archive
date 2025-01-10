package com.example.finalprojem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    //tanımlamalar
    private lateinit var auth: FirebaseAuth
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Authentication başlat
        auth = FirebaseAuth.getInstance()

        // Kullanıcı oturum açmış mı kontrol et
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Kullanıcı zaten giriş yapmışsa AnaSayfa'ya yönlendir
            navigateToHome()
        }

        //Elemanları tanımla
        emailField = findViewById(R.id.text3) // E-posta EditText
        passwordField = findViewById(R.id.text4) // Şifre EditText
        registerButton = findViewById(R.id.button) // Kayıt Ol Button
        loginButton = findViewById(R.id.button3) // Giriş Yap Button

        // Kayıt Ol Butonu
        registerButton.setOnClickListener {
            //email ve passwordu al
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            //email ve password boş değilse
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
            }
        }

        // Giriş Yap Butonu
        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kullanıcı Kayıt İşlemi
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password) //auth kullanarak E mail ve password ile kaydet
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Kullanıcı Giriş İşlemi
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password) //auth kullanarak giriş yap
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // AnaSayfa'ya yönlendirme fonksiyonu
    private fun navigateToHome() {
        val intent = Intent(this, AnaSayfa::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Geri butonuyla MainActivity'e dönmeyi engeller
        startActivity(intent)
        finish()
    }
}
