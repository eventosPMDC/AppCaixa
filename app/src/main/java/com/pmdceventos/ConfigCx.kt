package com.pmdceventos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pmdceventos.databinding.ActivityConfigCxBinding

class ConfigCx : AppCompatActivity() {
    private lateinit var binding : ActivityConfigCxBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigCxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.edtChaveunica.setText(intent.getStringExtra("serialNmbr"))
    }
}