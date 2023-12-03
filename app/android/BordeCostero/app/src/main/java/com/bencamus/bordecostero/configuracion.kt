package com.bencamus.bordecostero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bencamus.bordecostero.databinding.ActivityConfiguracionBinding

class configuracion : AppCompatActivity() {
    private lateinit var binding: ActivityConfiguracionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "Configuración"
    }

}