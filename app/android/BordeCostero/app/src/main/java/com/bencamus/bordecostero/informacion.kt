package com.bencamus.bordecostero

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bencamus.bordecostero.databinding.ActivityInformacionBinding

class informacion : AppCompatActivity() {

    private lateinit var binding: ActivityInformacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "Información"


    }


}