package elias.jorge.practica10_eliasjorge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BienvenidaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val txtCorreo = findViewById<TextView>(R.id.txtCorreo)
        val txtProveedor = findViewById<TextView>(R.id.txtProveedor)

        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)


        val correo = intent.getStringExtra("Correo")
        val proveedor = intent.getStringExtra("Proveedor")

        txtCorreo.text = "Correo: $correo"
        txtProveedor.text = "Proveedor: $proveedor"

        btnCerrarSesion.setOnClickListener {
            val preferencias_compartidas = getSharedPreferences(MainActivity.Global.preferencias_compartidas, MODE_PRIVATE)
            preferencias_compartidas.edit().clear().apply()

            // Redirect to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}