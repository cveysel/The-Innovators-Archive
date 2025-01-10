package com.example.finalprojem

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class GonderiEkle : AppCompatActivity() {

    //resim, firestore ve storage için tanımlamalar
    private var selectedImageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gonderi_ekle)

        //bileşenleri tanımla
        val imageView = findViewById<ImageView>(R.id.imageView)
        val adEditText = findViewById<EditText>(R.id.ad)
        val soyisimEditText = findViewById<EditText>(R.id.soyisim)
        val dogumtarihEditText = findViewById<EditText>(R.id.dogumtarih)
        val olumtarihEditText = findViewById<EditText>(R.id.olumtarih)
        val dogumyeriEditText = findViewById<EditText>(R.id.dogumyeri)
        val dogumyeri4EditText = findViewById<EditText>(R.id.dogumyeri4)
        val yukleButton = findViewById<Button>(R.id.yukle)

        // Resim seçme işlemi
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // Butona tıklama olayı
        yukleButton.setOnClickListener {

            //girilen metinlerimiz
            val ad = adEditText.text.toString()
            val soyisim = soyisimEditText.text.toString()
            val dogumtarih = dogumtarihEditText.text.toString()
            val olumtarih = olumtarihEditText.text.toString()
            val dogumyeri = dogumyeriEditText.text.toString()
            val katkilar = dogumyeri4EditText.text.toString()
            val email = FirebaseAuth.getInstance().currentUser?.email

            // Yazılacak kısımların hepsi dolu mu kontrol et
            if (ad.isEmpty() || soyisim.isEmpty() || dogumtarih.isEmpty() || olumtarih.isEmpty() || dogumyeri.isEmpty() || katkilar.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Resim seçilmiş mi kontrol et
            if (selectedImageUri == null) {
                Toast.makeText(this, "Lütfen bir resim seçin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Resmi yükle ve veriyi kaydet
            uploadImageAndSaveData(ad, soyisim, dogumtarih, olumtarih, dogumyeri, katkilar, email)


        }
    }
    //resim seçmede kullanılır
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            findViewById<ImageView>(R.id.imageView).setImageURI(selectedImageUri) // Seçilen resmi ImageView'e yerleştir
        }
    }
    //resimi yükleme ve girilen datayı kaydetme
    private fun uploadImageAndSaveData(ad: String, soyisim: String, dogumtarih: String, olumtarih: String, dogumyeri: String, katkilar: String, email: String?)
    {
        val imageName = UUID.randomUUID().toString() // Benzersiz bir isim oluştur
        //resmin adını tutmmak için
        val storageRef = storage.reference.child("images/$imageName")

        // Resmi Firebase Storage'a yükle
        //resmin urlsini de dataya kaydediyoruz bu sayede her postun alakalı resmi belli olmuş oluyor.
        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveDataToFirestore(ad, soyisim, dogumtarih, olumtarih, dogumyeri, katkilar, email, downloadUri.toString()) // Resim URL'sini kaydet
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Resim yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    // firestore içine post data verilerini kaydetme
    private fun saveDataToFirestore(ad: String, soyisim: String, dogumtarih: String, olumtarih: String, dogumyeri: String, katkilar: String, email: String?, imageUrl: String)
    {
        val postData = hashMapOf(
            "ad" to ad,
            "soyisim" to soyisim,
            "dogumtarih" to dogumtarih,
            "olumtarih" to olumtarih,
            "dogumyeri" to dogumyeri,
            "katkilar" to katkilar,
            "email" to email,
            "imageUrl" to imageUrl,
            "timestamp" to FieldValue.serverTimestamp()
        )
        //post data , Bilim Adamları collection'una  kaydediliyor.
        db.collection("BilimAdamları")
            .add(postData)
            .addOnSuccessListener {
                Toast.makeText(this, "Post başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                // Alanları temizle
                findViewById<EditText>(R.id.ad).text.clear()
                findViewById<EditText>(R.id.soyisim).text.clear()
                findViewById<EditText>(R.id.dogumtarih).text.clear()
                findViewById<EditText>(R.id.olumtarih).text.clear()
                findViewById<EditText>(R.id.dogumyeri).text.clear()
                findViewById<EditText>(R.id.dogumyeri4).text.clear()
                findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.istockphoto) // Varsayılan resmi geri yükle
                selectedImageUri = null
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
