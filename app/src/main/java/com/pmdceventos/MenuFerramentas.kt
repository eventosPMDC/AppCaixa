package com.pmdceventos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pmdceventos.databinding.ActivityMenuFerramentasBinding

class MenuFerramentas : AppCompatActivity() {

    private lateinit var binding : ActivityMenuFerramentasBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuFerramentasBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}