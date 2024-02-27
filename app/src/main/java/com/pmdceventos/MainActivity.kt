package com.pmdceventos

import android.Manifest.permission
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.PermissionChecker
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdceventos.databinding.ActivityMainBinding

var serialNnbr: String? = ""
var numCx: String? = ""
private const val REQUEST_CODE_READ_PHONE_STATE = 1

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private var db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hasPermission = PermissionChecker.checkSelfPermission(
            this,
            permission.READ_PHONE_STATE
        )

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            // TP1A.220624.014
            serialNnbr = Build.ID
            getCaixa(Build.ID)
        } else {
            // Solicitar a permissão ao usuário
            //requestPermissions(arrayOf(permission.READ_PHONE_STATE),0)
            ActivityCompat.requestPermissions(this,
                arrayOf(permission.READ_PHONE_STATE),REQUEST_CODE_READ_PHONE_STATE
            )
        }
        if (serialNnbr != "") {
            binding.tvText.text = serialNnbr

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // A permissão foi concedida
                // Capturar o número de série
                serialNnbr = Build.ID
                Toast.makeText(
                    this,
                    "Não houve configuração de caixa ainda, por favor fazer a configuração para utilizar o sistema.$serialNnbr",
                    Toast.LENGTH_LONG).show()
                getCaixa(serialNnbr)
            } else {
                // A permissão foi negada
                // Mostrar uma mensagem ao usuário informando que a permissão é necessária
                Toast.makeText(
                    this,
                    "Não foi permitido capturar informações para continuar o processo de configuração!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun showDialog(view: View) {
        if (view.id == R.id.ibtn_config){
            val alertDialog = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val viewMF = inflater.inflate(R.layout.activity_menu_ferramentas, null)
            alertDialog.setView(viewMF)
            val dialog = alertDialog.create()
            val btnCnfcx = viewMF.findViewById<ImageButton>(R.id.ibtn_configcx)
            btnCnfcx.setOnClickListener{
                val intent = Intent(this, ConfigCx::class.java)
                intent.putExtra("serialNmbr", serialNnbr)
                if (numCx != "") {
                    intent.putExtra("caixa", numCx)
                }
                startActivity(intent)
                dialog.dismiss()
            }
            dialog.show()
        }
    }
    private fun getCaixa(srlNmb: String?){
        val rqstCaixa = db.collection("Config").document(srlNmb.toString())
        rqstCaixa.get().addOnSuccessListener {
            if (it != null){
                numCx = it.data?.get("caixa").toString()
            }
        }
    }
}