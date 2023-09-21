package com.bencamus.bordecostero
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.bencamus.bordecostero.databinding.ActivityMainBinding
import com.ingenieriajhr.blujhr.BluJhr


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var blue:BluJhr


    //Array para dispositivos
    var devicesBluetooth = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH), 1)
        }

        val listDeviceBluetooth = binding.listDeviceBluetooth
        val viewConn = binding.viewConn

        //iniciar bluetooth
        blue = BluJhr(this)
        blue.onBluetooth()


        listDeviceBluetooth.setOnItemClickListener { adapterView, view, i, l ->
            if (devicesBluetooth.isNotEmpty()){
                blue.connect(devicesBluetooth[i])
                blue.setDataLoadFinishedListener(object:BluJhr.ConnectedBluetooth{
                    override fun onConnectState(state: BluJhr.Connected) {
                        when(state){

                            BluJhr.Connected.True->{
                                Toast.makeText(applicationContext,"True",Toast.LENGTH_SHORT).show()
                                listDeviceBluetooth.visibility = View.GONE
                                viewConn.visibility = View.VISIBLE
                                rxReceived()
                            }

                            BluJhr.Connected.Pending->{
                                Toast.makeText(applicationContext,"Pending",Toast.LENGTH_SHORT).show()

                            }

                            BluJhr.Connected.False->{
                                Toast.makeText(applicationContext,"False",Toast.LENGTH_SHORT).show()
                            }

                            BluJhr.Connected.Disconnect->{
                                Toast.makeText(applicationContext,"Disconnect",Toast.LENGTH_SHORT).show()
                                listDeviceBluetooth.visibility = View.VISIBLE
                                viewConn.visibility = View.GONE
                            }

                        }
                    }
                })
            }

        }


        binding.buttonSend.setOnClickListener {
            blue.bluTx(binding.edtTx.text.toString())
        }

        binding.buttonSend.setOnLongClickListener {
            blue.closeConnection()
            true
        }

    }
    //fuera de onCreate


    private fun rxReceived() {
        blue.loadDateRx(object:BluJhr.ReceivedData{
            override fun rxDate(datos: String) {
                //para ir agregando a la pantalla
                //consola.text = consola.text.toString()+datos

                //para verlo en una sola linea
                binding.consola.text = datos

                //separar datos
                val pattern = """(\w+):([0-9]+\.[0-9]+)""".toRegex()
                val matchResults = pattern.findAll(datos)

                var temp1: Double? = null
                var temp2: Double? = null
                var pres: Double? = null
                var humi: Double? = null
                var alt: Double? = null
                var uv: Double? = null

                for (matchResult in matchResults) {
                    val (key, value) = matchResult.destructured
                    when (key) {
                        "Temp1" -> temp1 = value.toDouble()
                        "Temp2" -> temp2 = value.toDouble()
                        "Pres" -> pres = value.toDouble()
                        "Humi" -> humi = value.toDouble()
                        "Alt" -> alt = value.toDouble()
                        "UV" -> uv = value.toDouble()
                    }
                }
                //Agregar valores a texto de las views
                binding.TemperaturaSonda.text =  "Temperatura Sonda: " + temp1.toString() + " C°"
                binding.TemperaturaAmbiente.text = "Temperatura Ambiente: " + temp2.toString() + " C°"
                binding.presion.text = "Presión: " + pres.toString() + " hPa"
                binding.humedad.text = "Humedad: " + humi.toString() + " %"
                binding.altitud.text = "Altitud: " + alt.toString() + " m"
                binding.uv.text = "UV Intensity: " + uv.toString() +" (mW/cm^2)"

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
                    binding.listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}