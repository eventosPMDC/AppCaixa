package com.pmdceventos

import android.Manifest.permission
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.PermissionChecker
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.pmdceventos.databinding.ActivityMainBinding

var serialNnbr: String? = null
private const val REQUEST_CODE_READ_PHONE_STATE = 1

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hasPermission = PermissionChecker.checkSelfPermission(
            this,
            permission.READ_PHONE_STATE
        )

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            // A permissão já foi concedida
            // Capturar o número de série
            serialNnbr = Build.ID
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
}