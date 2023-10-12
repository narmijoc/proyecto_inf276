package com.bencamus.bordecostero

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
                    // Agregar valores a texto de las views
                    rootView.findViewById<TextView>(R.id.Temperatura_sonda).text = "Temperatura Sonda: " + tempSonda.toString() + " C°"
                    rootView.findViewById<TextView>(R.id.Temperatura_ambiente).text = "Temperatura Ambiente: " + tempAmbiente.toString() + " C°"
                    rootView.findViewById<TextView>(R.id.presion).text = "Presión: " + presion.toString() + " hPa"
                    rootView.findViewById<TextView>(R.id.humedad).text = "Humedad: " + humedadAmbiente.toString() + " %"
                    rootView.findViewById<TextView>(R.id.altitud).text = "Altitud: " + altitud.toString() + " m"
                    rootView.findViewById<TextView>(R.id.uv).text = "UV Intensity: " + uv.toString()
                    rootView.findViewById<TextView>(R.id.Light).text = "Intensidad luminosa: " + lightIntensity.toString() + " lux"
                    rootView.findViewById<TextView>(R.id.Tenedor).text = "Humedad de suelo: " + humedadSuelo.toString()
                    rootView.findViewById<TextView>(R.id.TDS).text = "Sólidos disueltos: " + ppm.toString() + " ppm"

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
                    registro.put("tempSonda", tempSonda.toString())
                    registro.put("tempAmbiente", tempAmbiente.toString())
                    registro.put("presion", presion.toString())
                    registro.put("humedadAmbiente", humedadAmbiente.toString())
                    registro.put("altitud", altitud.toString())
                    registro.put("UV", uv.toString())
                    registro.put("lightIntensity", lightIntensity.toString())
                    registro.put("humedadSuelo", humedadSuelo.toString())
                    registro.put("ppm", ppm.toString())

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