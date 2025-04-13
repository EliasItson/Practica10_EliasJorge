package elias.jorge.practica10_eliasjorge

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    object Global {
        var preferencias_compartidas = "sharedpreferences"
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLoginGoogle = findViewById<Button>(R.id.btnLoginGoogle)

        val etCorreo = findViewById<EditText>(R.id.edtEmail)
        val etPassword = findViewById<EditText>(R.id.edtPassword)


        verificarSesionAbierta()

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString()
            val pass = etPassword.text.toString()

            if (correo.isNotEmpty() && pass.isNotEmpty()) {
                try {
                    loginFirebase(correo, pass)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al iniciar sesion: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        btnLoginGoogle.setOnClickListener {
            loginGoogle()
        }

    }

    private fun verificarSesionAbierta(){
        var openSession = getSharedPreferences(Global.preferencias_compartidas, MODE_PRIVATE)

        var correo = openSession.getString("Correo", "")
        var proveedor = openSession.getString("Proveedor", "")

        if (correo == null || proveedor == null || correo.isEmpty() || proveedor.isEmpty()) {
            return
        }

        var intent = Intent(this, BienvenidaActivity::class.java)
        intent.putExtra("Correo", correo)
        intent.putExtra("Proveedor", proveedor)
        startActivity(intent)
    }

    private fun guardar_sesion(correo: String, proveedor: String) {
        var guardar_sesion = getSharedPreferences(Global.preferencias_compartidas, MODE_PRIVATE).edit()

        guardar_sesion.putString("Correo", correo)
        guardar_sesion.putString("Proveedor", proveedor)
        guardar_sesion.apply()
        guardar_sesion.commit()

    }

    private fun loginGoogle() {
        val context = this
        val credentialManager = CredentialManager.create(context)

        val signInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(getString(R.string.web_client))
                .setNonce("nonce")
                .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                handleSignIn(result)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error lifecycle: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        verificarSesionAbierta()
    }

    override fun onResume() {
        super.onResume()
        verificarSesionAbierta()
    }

    private fun isValidCredential(credential: Credential): Boolean {
        if (credential !is CustomCredential) {
            return false
        }

        if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return false
        }

        return true
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        if (!isValidCredential(credential)) {
            Toast.makeText(this, "User or password incorrect", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val newCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken,
                null
            )
            FirebaseAuth.getInstance().signInWithCredential(newCredential)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Error task unsuccessful: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    var intent = Intent(this, BienvenidaActivity::class.java)
                    intent.putExtra("Correo", task.result?.user?.email)
                    intent.putExtra("Proveedor", "Google")
                    startActivity(intent)
                    guardar_sesion(task.result?.user?.email.toString(), "Google")

                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error handle Sign In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginFirebase(correo: String, pass: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        this,
                        task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnCompleteListener
                }
                var intent = Intent(this, BienvenidaActivity::class.java)
                intent.putExtra("Correo", task.result?.user?.email)
                intent.putExtra("Proveedor", "Firebase")
                startActivity(intent)
                guardar_sesion(task.result?.user?.email.toString(), "Firebase")

            }
    }
}