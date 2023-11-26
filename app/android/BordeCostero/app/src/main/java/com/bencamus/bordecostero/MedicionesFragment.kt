package com.bencamus.bordecostero

import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.ingenieriajhr.blujhr.BluJhr
import java.text.SimpleDateFormat
import java.util.*

class MedicionesFragment : Fragment() {
    lateinit var blue:BluJhr //biblioteca bluetooth

    lateinit var rootView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_mediciones, container, false)

        // Acceder a la variable del MainActivity
        val mainActivity = activity as MainActivity
        blue = mainActivity.blue

        //llamar a funcion recibir datos
        rxReceived()

        return rootView
    }



    private fun rxReceived() {
        //todos los checkbox
        val cBoxHumedad: CheckBox = rootView.findViewById(R.id.cBox_humedad)
        val cBoxPresion: CheckBox = rootView.findViewById(R.id.cBox_presion)
        val cBoxTDS: CheckBox = rootView.findViewById(R.id.cBox_TDS)
        val cBoxAltitud: CheckBox = rootView.findViewById(R.id.cBox_altitud)
        val cBoxUV: CheckBox = rootView.findViewById(R.id.cBox_uv)
        val cBoxLight: CheckBox = rootView.findViewById(R.id.cBox_Light)
        val cBoxHumedadSuelo: CheckBox = rootView.findViewById(R.id.cBox_humedad_suelo)
        val cboxTemperaturaAmbiente: CheckBox = rootView.findViewById(R.id.cbox_temperatura_ambiente)
        val cBoxTemperaturaSonda: CheckBox = rootView.findViewById(R.id.cBox_temperatura_sonda)




        var medicion_start = false

        //textviews
        var txtLugar =  rootView.findViewById<TextView>(R.id.txtLugar)
        var txtNombre =  rootView.findViewById<TextView>(R.id.txtNombre)
        var txtStatus =  rootView.findViewById<TextView>(R.id.medicionStatus)
        //botones
        val btnIniciarMedicion =  rootView.findViewById<Button>(R.id.btnIniciarMed)
        val btnPararMedicion =  rootView.findViewById<Button>(R.id.btnPararMed)


        //variables de base de datos
        var con = SQLite(requireContext(),"mediciones",null,1)
        var baseDatos =con.writableDatabase

        //variable nombre y lugar
        var bdNombre = ""
        var bdLugar = ""
        var bdMedicionID = 0



        //AL apretar boton iniciar medicion
        btnIniciarMedicion.setOnClickListener{
            if (medicion_start== false){
                //si ha ingresado nombre en el texview nombre
                if (txtNombre.text.toString().isEmpty() == false) {
                    bdNombre = txtNombre.text.toString()
                    bdLugar = txtLugar.text.toString()
                } else {
                    Toast.makeText(requireContext(), "Debe ingresad nombre", Toast.LENGTH_SHORT).show()
                }
                //establecer como true
                medicion_start = true
                txtStatus.text = "Realizando Medición..."
                Toast.makeText(requireContext(), "Iniciando Medición", Toast.LENGTH_SHORT).show()

                //obtener valor siguiente de la tabla _medicionID
                bdMedicionID = establecerNumeroMedicion()
            }

        }

        //al apretar boton parar medicion
        btnPararMedicion.setOnClickListener{
            if (medicion_start ==true){
                medicion_start=false
                Toast.makeText(requireContext(), "Medición Detenida", Toast.LENGTH_SHORT).show()
                txtStatus.text = "Medición Detenida"

                // Hacer todos los CheckBox visibles
                cBoxHumedad.visibility = View.VISIBLE
                cBoxPresion.visibility = View.VISIBLE
                cBoxTDS.visibility = View.VISIBLE
                cBoxAltitud.visibility = View.VISIBLE
                cBoxUV.visibility = View.VISIBLE
                cBoxLight.visibility = View.VISIBLE
                cBoxHumedadSuelo.visibility = View.VISIBLE
                cboxTemperaturaAmbiente.visibility = View.VISIBLE
                cBoxTemperaturaSonda.visibility = View.VISIBLE

                // Hacer todos los TextView visibles
                rootView.findViewById<TextView>(R.id.Temperatura_sonda).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.Temperatura_ambiente).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.presion).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.humedad).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.altitud).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.uv).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.Light).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.humedad_suelo).visibility = View.VISIBLE
                rootView.findViewById<TextView>(R.id.TDS).visibility = View.VISIBLE

            }
        }


        //al recibir un valor por bluetooth
        blue.loadDateRx(object: BluJhr.ReceivedData{
            override fun rxDate(datos: String) {

                //separar datos
                val pattern = """(\w+):([0-9]+\.[0-9]+)""".toRegex()
                val matchResults = pattern.findAll(datos)

                var tempSonda: Double? = null
                var tempAmbiente: Double? = null
                var presion: Double? = null
                var humedadAmbiente: Double? = null
                var altitud: Double? = null
                var uv: Double? = null
                var lightIntensity: Double? = null
                var humedadSuelo: Double? = null
                var ppm: Double? = null

                for (matchResult in matchResults) {
                    val (key, value) = matchResult.destructured
                    when (key) {
                        "tempSonda" -> tempSonda = value.toDouble()
                        "tempAmbiente" -> tempAmbiente = value.toDouble()
                        "presion" -> presion = value.toDouble()
                        "humedadAmbiente" -> humedadAmbiente = value.toDouble()
                        "altitud" -> altitud = value.toDouble()
                        "UV" -> uv = value.toDouble()
                        "lightIntensity" -> lightIntensity = value.toDouble()
                        "humedadSuelo" -> humedadSuelo = value.toDouble()
                        "ppm" -> ppm = value.toDouble()

                    }
                }

                if (medicion_start == true) {
                    cBoxHumedad.visibility = View.GONE
                    cBoxPresion.visibility = View.GONE
                    cBoxTDS.visibility = View.GONE
                    cBoxAltitud.visibility = View.GONE
                    cBoxUV.visibility = View.GONE
                    cBoxLight.visibility = View.GONE
                    cBoxHumedadSuelo.visibility = View.GONE
                    cboxTemperaturaAmbiente.visibility = View.GONE
                    cBoxTemperaturaSonda.visibility = View.GONE


                    // Verificar cada CheckBox y actualizar el texto de las TextView correspondientes
                    if (cBoxTemperaturaSonda.isChecked) {
                        rootView.findViewById<TextView>(R.id.Temperatura_sonda).text = "Temperatura Sonda: " + tempSonda.toString() + " C°"
                    } else {
                        rootView.findViewById<TextView>(R.id.Temperatura_sonda).visibility = View.GONE
                    }

                    if (cboxTemperaturaAmbiente.isChecked) {
                        rootView.findViewById<TextView>(R.id.Temperatura_ambiente).text = "Temperatura Ambiente: " + tempAmbiente.toString() + " C°"
                    } else {
                        rootView.findViewById<TextView>(R.id.Temperatura_ambiente).visibility = View.GONE
                    }

                    if (cBoxPresion.isChecked) {
                        rootView.findViewById<TextView>(R.id.presion).text = "Presión: " + presion.toString() + " hPa"
                    } else {
                        rootView.findViewById<TextView>(R.id.presion).visibility = View.GONE
                    }

                    if (cBoxHumedad.isChecked) {
                        rootView.findViewById<TextView>(R.id.humedad).text = "Humedad: " + humedadAmbiente.toString() + " %"
                    } else {
                        rootView.findViewById<TextView>(R.id.humedad).visibility = View.GONE
                    }

                    if (cBoxAltitud.isChecked) {
                        rootView.findViewById<TextView>(R.id.altitud).text = "Altitud: " + altitud.toString() + " m"
                    } else {
                        rootView.findViewById<TextView>(R.id.altitud).visibility = View.GONE
                    }

                    if (cBoxUV.isChecked) {
                        rootView.findViewById<TextView>(R.id.uv).text = "UV Intensity: " + uv.toString()
                    } else {
                        rootView.findViewById<TextView>(R.id.uv).visibility = View.GONE
                    }

                    if (cBoxLight.isChecked) {
                        rootView.findViewById<TextView>(R.id.Light).text = "Intensidad luminosa: " + lightIntensity.toString() + " lux"
                    } else {
                        rootView.findViewById<TextView>(R.id.Light).visibility = View.GONE
                    }

                    if (cBoxHumedadSuelo.isChecked) {
                        rootView.findViewById<TextView>(R.id.humedad_suelo).text = "Humedad de suelo: " + humedadSuelo.toString()
                    } else {
                        rootView.findViewById<TextView>(R.id.humedad_suelo).visibility = View.GONE
                    }

                    if (cBoxTDS.isChecked) {
                        rootView.findViewById<TextView>(R.id.TDS).text = "Sólidos disueltos: " + ppm.toString() + " ppm"
                    } else {
                        rootView.findViewById<TextView>(R.id.TDS).visibility = View.GONE
                    }

                }


                //Verficar si estado boton es igual a true, si es asi guardar en base de datos
                if (medicion_start == true) {
                    //si no etsan vacios procedo a guardar
                    var registro = ContentValues()
                    val currentDateAndTime: String =
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

                    registro.put("_medicionID",bdMedicionID)
                    registro.put("nombre", bdNombre)
                    registro.put("fecha", currentDateAndTime)
                    registro.put("lugar", bdLugar)

                    if (cBoxTemperaturaSonda.isChecked) {
                        registro.put("tempSonda", tempSonda.toString())
                    }

                    if (cboxTemperaturaAmbiente.isChecked) {
                        registro.put("tempAmbiente", tempAmbiente.toString())
                    }

                    if (cBoxPresion.isChecked) {
                        registro.put("presion", presion.toString())
                    }

                    if (cBoxHumedad.isChecked) {
                        registro.put("humedadAmbiente", humedadAmbiente.toString())
                    }

                    if (cBoxAltitud.isChecked) {
                        registro.put("altitud", altitud.toString())
                    }

                    if (cBoxUV.isChecked) {
                        registro.put("UV", uv.toString())
                    }

                    if (cBoxLight.isChecked) {
                        registro.put("lightIntensity", lightIntensity.toString())
                    }

                    if (cBoxHumedadSuelo.isChecked) {
                        registro.put("humedadSuelo", humedadSuelo.toString())
                    }

                    if (cBoxTDS.isChecked) {
                        registro.put("ppm", ppm.toString())
                    }
                    baseDatos.insert("mediciones", null, registro)
                    txtLugar.text = ""
                    txtNombre.text = ""

                }
            }
        })



    }

    private fun establecerNumeroMedicion(): Int {
        // Abrir la base de datos en modo lectura
        val con = SQLite(requireContext(), "mediciones", null, 1)
        val baseDatos = con.readableDatabase
        var ultimoValor = 0

        // Realiza la consulta SQL para obtener el último valor de la columna
        val query = "SELECT _medicionID FROM mediciones ORDER BY _id DESC LIMIT 1"

        val cursor = baseDatos.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val valor = cursor.getString(0)
            if (valor != null) {
                ultimoValor = valor.toInt()
            }
            // Hacer algo con el valor obtenido, por ejemplo, mostrarlo en un TextView o procesarlo de alguna manera.
        }

        cursor.close()

        return ultimoValor + 1
    }



}