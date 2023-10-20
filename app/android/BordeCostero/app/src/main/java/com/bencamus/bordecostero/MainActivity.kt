package com.bencamus.bordecostero
import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.bencamus.bordecostero.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.ingenieriajhr.blujhr.BluJhr


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //fragment
    private lateinit var fragmentManager: FragmentManager

    //para el drawer
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle


    private lateinit var binding: ActivityMainBinding
    lateinit var blue:BluJhr //biblioteca bluetooth

    //estado conexion
    var estadoConexionBlue = false

    //Array para dispositivos vinculados
    var devicesBluetooth = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Drawer
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)


        //Pedir permisos de bluetooth
        //Bluetooth
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH), 1)
        }

        //iniciar bluetooth
        blue = BluJhr(this)
        blue.onBluetooth()

        //MOSTAR DISPOSITIVOS EN PANTALLA PRINCIPAL
        //val listDeviceBluetooth = findViewById<ListView>(R.id.listDeviceBluetooth)
        //listDeviceBluetooth.setOnItemClickListener { adapterView, view, i, l ->
        //    connectToDeviceAndListen(i)
        //}

        //establecer boton desconectar como invisible
        binding.btnDesconectarDisp.visibility = View.GONE

        //boton desconectar
        binding.btnDesconectarDisp.setOnClickListener{
            estadoConexionBlue = false
            establecerEstadoConexion()
            //establecer estado del boton conectar
            binding.btnConectar.isEnabled = !estadoConexionBlue
            blue.closeConnection()
            //hacer boton invisble al desconectar
            binding.btnDesconectarDisp.visibility = View.GONE

        }

        //Cambiar de vistas segun los botones
        //Boton Conectar
        binding.btnConectar.setOnClickListener {
            Toast.makeText(applicationContext, "BotonConectar", Toast.LENGTH_SHORT).show()

            showCustomDialogBox()
        }

        binding.btnTomarMedicion.setOnClickListener{
            goToFragment(MedicionesFragment())
            binding.listaBtnMain.visibility = View.GONE
        }

        binding.btMedicionesRealizadas.setOnClickListener{
            goToFragment(MedicionesRealizadasFragment())
            binding.listaBtnMain.visibility = View.GONE
        }

        //establecer estado inicial
        establecerEstadoConexion()

        //Botontes para enviar texto, no serán usados
        //binding.buttonSend.setOnClickListener {
        //    blue.bluTx(binding.edtTx.text.toString())
        //}

        //binding.buttonSend.setOnLongClickListener {
        //    blue.closeConnection()
        //true
        //}

    }

    private fun goToFragment(fragment: Fragment) {
        //Toast.makeText(applicationContext, "BotonMediciones", Toast.LENGTH_SHORT).show()
        fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.frameContainer,fragment)

        transaction.addToBackStack(null)

        transaction.commit()


    }
    //fuera de onCreate

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.frameContainer) // Reemplaza "fragmentContainer" con el ID de tu contenedor de fragmentos

        // Verifica si el fragment actual es el fragment principal
        if (fragment is MedicionesFragment || fragment is MedicionesRealizadasFragment) {
            // Muestra el LinearLayout
            binding.listaBtnMain.visibility = View.VISIBLE
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun showCustomDialogBox() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog.setCancelable(false)
        dialog.setContentView(R.layout.devices_dailog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val listaDispositivosBluetooth: ListView = dialog.findViewById(R.id.listDeviceBluetooth2)
        devicesBluetooth = blue.deviceBluetooth() //el array de los dispositivos

        //listDeviceBluetooth2.setBackgroundColor(Color.parseColor("#0000FF")) // Cambia el color de fondo aquí

        //agregar dispositivos al listview
        if (devicesBluetooth.isNotEmpty()){
            val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, devicesBluetooth)
            listaDispositivosBluetooth.adapter = adapter
        }

        dialog.findViewById<Button>(R.id.btnCancelarDialog).setOnClickListener{
            dialog.dismiss()
        }

        listaDispositivosBluetooth.setOnItemClickListener { adapterView, view, i, l ->
            //val viewConn: LinearLayout = dialog.findViewById(R.id.viewConn2)

            if (devicesBluetooth.isNotEmpty()) {
                blue.connect(devicesBluetooth[i])
                blue.setDataLoadFinishedListener(object : BluJhr.ConnectedBluetooth {
                    override fun onConnectState(state: BluJhr.Connected) {
                        when (state) {
                            BluJhr.Connected.True -> {
                                Toast.makeText(applicationContext, "True", Toast.LENGTH_SHORT).show()
                                listaDispositivosBluetooth.visibility = View.GONE

                                //hacer visible el boton desconectar
                                binding.btnDesconectarDisp.visibility = View.VISIBLE

                                //establecer estado
                                estadoConexionBlue = true
                                establecerEstadoConexion()
                                //establecer estado del boton conectar
                                binding.btnConectar.isEnabled = !estadoConexionBlue

                                //viewConn.visibility = View.VISIBLE
                                rxReceived() //La ejecuta el fragment_mediciones
                                dialog.dismiss()
                            }

                            BluJhr.Connected.Pending -> {
                                Toast.makeText(applicationContext, "Pending", Toast.LENGTH_SHORT).show()
                                //establecer estado
                                estadoConexionBlue = false
                                establecerEstadoConexion()
                                //establecer estado del boton conectar
                                binding.btnConectar.isEnabled = !estadoConexionBlue

                            }

                            BluJhr.Connected.False -> {
                                Toast.makeText(applicationContext, "False", Toast.LENGTH_SHORT).show()
                                //establecer estado
                                estadoConexionBlue = false
                                establecerEstadoConexion()
                                //establecer estado del boton conectar
                                binding.btnConectar.isEnabled = !estadoConexionBlue
                            }

                            BluJhr.Connected.Disconnect -> {
                                //establecer estado
                                estadoConexionBlue = false
                                establecerEstadoConexion()
                                Toast.makeText(applicationContext, "Disconnect", Toast.LENGTH_SHORT).show()
                                listaDispositivosBluetooth.visibility = View.VISIBLE
                                //viewConn.visibility = View.GONE

                                //establecer estado del boton conectar
                                binding.btnConectar.isEnabled = !estadoConexionBlue
                            }
                        }
                    }
                })
            }

        }



    }


    override fun onPostCreate(savedInstanceState: Bundle?){
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_item_one -> Toast.makeText(this, "Item 1", Toast.LENGTH_SHORT).show()
            R.id.nav_item_two -> Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()
            R.id.nav_item_three -> Toast.makeText(this, "Item 3", Toast.LENGTH_SHORT).show()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun establecerEstadoConexion(){
        if (estadoConexionBlue == true) {
            binding.tvStatusConection.text = "Conectado"
            // Color verde en hexadecimal (#RRGGBB)
            binding.tvStatusConection.setTextColor(Color.parseColor("#00FF00")) // Verde
        } else {
            binding.tvStatusConection.text = "Desconectado"
            // Color rojo en hexadecimal (#RRGGBB)
            binding.tvStatusConection.setTextColor(Color.parseColor("#FF0000")) // Rojo
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }




    private fun rxReceived() {
        blue.loadDateRx(object:BluJhr.ReceivedData{
            override fun rxDate(datos: String) {
                Log.d("TAG", "Este es un mensaje de depuración")
            }
        })
    }
    /**
     * pedimos los permisos correspondientes, para android 12 hay que pedir los siguientes admin y scan
     * en android 12 o superior se requieren permisos diferentes
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (blue.checkPermissions(requestCode,grantResults)){
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                blue.initializeBluetooth()
            }else{
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    //binding.listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}