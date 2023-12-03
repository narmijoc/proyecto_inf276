package com.bencamus.bordecostero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bencamus.bordecostero.databinding.ActivityAyudaBinding
import com.bencamus.bordecostero.databinding.ActivityInformacionBinding

class ayuda : AppCompatActivity() {

    private lateinit var binding: ActivityAyudaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayuda)

        binding = ActivityAyudaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "Ayuda"


    }
}