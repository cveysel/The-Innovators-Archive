package com.example.finalprojem
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso

//recycle viewdeki elemanlar arasında boşluk bırakmak için
class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = space // Alt tarafa boşluk ekle
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space // İlk öğenin üstüne boşluk ekle
        } } }

//BilimAdami Classımız
data class BilimAdami(
    val ad: String,
    val soyisim: String,
    val dogumtarih: String,
    val olumtarih: String,
    val dogumyeri: String,
    val katkilar: String,
    val email: String,
    val imageUrl: String
)

class AnaSayfa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    //BilimAdami türünde liste için tanımlama
    private lateinit var bilimAdamlarıList: ArrayList<BilimAdami>
    //adapter
    private lateinit var adapter: BilimAdamıAdapter
    //FirebaseFirestore kullanımı
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ana_sayfa)

        // RecyclerView ve diğer değişkenleri başlatma
        recyclerView = findViewById(R.id.bilimAdamlarıRecyclerView)
        bilimAdamlarıList = ArrayList()
        adapter = BilimAdamıAdapter(bilimAdamlarıList)

        //recycle view temel yapılandırmaları
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // recycle viewe 16dp boşluk ekle
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.recycler_view_spacing)
        recyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels))


        // Firebase Firestore'dan verileri alma
        db.collection("BilimAdamları")
            //sıralama için
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                //bilimAdamı olarak firebaseden alınan ve zamana göre sıralanan postlar
                for (document in documents) {
                    val ad = document.getString("ad") ?: ""
                    val soyisim = document.getString("soyisim") ?: ""
                    val dogumtarih = document.getString("dogumtarih") ?: ""
                    val olumtarih = document.getString("olumtarih") ?: ""
                    val dogumyeri = document.getString("dogumyeri") ?: ""
                    val katkilar = document.getString("katkilar") ?: ""
                    val email = document.getString("email") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    val bilimAdamı = BilimAdami(ad, soyisim, dogumtarih, olumtarih, dogumyeri, katkilar, email, imageUrl)
                    bilimAdamlarıList.add(bilimAdamı)
                }
                // Veriler alındıktan sonra RecyclerView'u güncelle
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Veriler alınamadı: ${exception.message}")
            }
    }

    // Menü oluşturma
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Menü işlemleri
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addGonderi -> {
                val intent = Intent(this, GonderiEkle::class.java)
                startActivity(intent)
                true
            }
            R.id.cikis -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Çıkış yapıldı.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
//RecyclerView için bir adapter
class BilimAdamıAdapter(private val bilimAdamları: MutableList<BilimAdami>) :
    RecyclerView.Adapter<BilimAdamıAdapter.ViewHolder>() {

    //Recycler Viewdaki her bir öğenin görünümünü  temsil eden ViewHolder sınıfı
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adSoyad: TextView = itemView.findViewById(R.id.adSoyadTextView)
        val dogumBilgi: TextView = itemView.findViewById(R.id.dogumBilgiTextView)
        val katkilar: TextView = itemView.findViewById(R.id.katkilarTextView)
        val email:TextView = itemView.findViewById(R.id.emailTextView)
        val postImageView: ImageView = itemView.findViewById(R.id.postImageView)
    }
    //RecyclerView öğesi oluşturulurken çağrılır. Her bir öğe için `bilimadami_item` XML dosyası kullanılır.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bilimadami_item, parent, false)
        return ViewHolder(view)
    }
    // RecyclerView e veri bağlamak için çağrılır
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bilimAdamı = bilimAdamları[position]
        holder.email.text = "Post Sahibi : ${bilimAdamı.email}"
        holder.adSoyad.text = "Adı ve Soyadı : ${bilimAdamı.ad} ${bilimAdamı.soyisim}"
        holder.dogumBilgi.text = "Doğum ve Ölüm Bilgisi :${bilimAdamı.dogumtarih} - ${bilimAdamı.olumtarih} (${bilimAdamı.dogumyeri})"
        holder.katkilar.text = "Katkıları : ${bilimAdamı.katkilar}"

        //Bilim insanının görselini Picasso kütüphanesi ile ImageView'e yükler.
        Picasso.get()
            .load(bilimAdamı.imageUrl)
            .placeholder(R.drawable.istockphoto) // Yükleme sırasında gösterilecek resim
            .error(R.drawable.istockphoto) // Hata durumunda gösterilecek resim
            .into(holder.postImageView)
    }

    override fun getItemCount(): Int = bilimAdamları.size
}

