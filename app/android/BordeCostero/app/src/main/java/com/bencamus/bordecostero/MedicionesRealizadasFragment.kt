package com.bencamus.bordecostero

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.opencsv.CSVWriter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MedicionesRealizadasFragment : Fragment() {
    lateinit var rootView: View

    var tablaMediciones: TableLayout? = null

    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_mediciones_realizadas, container, false)

        tablaMediciones = rootView.findViewById(R.id.tablaMedicioness)
        llenarTabla()

        val dbName = "mediciones.db"
        val tableName = "mediciones"

        val tableLayout: TableLayout = rootView.findViewById(R.id.tablaMedicioness)

        // Iterar a través de las filas
        for (i in 0 until tableLayout.childCount) {
            val fila = tableLayout.getChildAt(i)

            if (fila is TableRow) {
                // Asegúrate de que la fila tenga al menos una celda y de que el último elemento sea un botón
                if (fila.childCount > 0 && fila.getChildAt(fila.childCount - 1) is Button) {
                    val boton = fila.getChildAt(fila.childCount - 1) as Button

                    // Acción de clic en el botón
                    boton.setOnClickListener {
                        // Acceder al primer elemento de la fila (índice 0)
                        val primeraCelda = fila.getChildAt(0)

                        // Verificar si la primera celda contiene un TextView
                        if (primeraCelda is TextView) {
                            val medicionID = primeraCelda.text.toString() // numero del id del registro

                            //mostrar el id al apretar elboton
                            //Toast.makeText(requireContext(), medicionID, Toast.LENGTH_SHORT).show()

                            //llamar a funcion para exportar csv
                            exportarCSV(medicionID)
                        }
                    }
                }
            }
        }

        return rootView
    }

    private fun exportarCSV(medicionID: String) {
        // Consulta SQL para obtener registros con el _medicionID proporcionado
        val query = "SELECT * FROM mediciones WHERE _medicionID = ?"
        val params = arrayOf(medicionID)

        val con = SQLite(requireContext(), "mediciones", null, 1)
        val baseDatos = con.readableDatabase
        val cursor = baseDatos.rawQuery(query, params)

        val datosCSV = mutableListOf<String>()

        // Agregar la primera línea con los títulos
        val titulos = "ID, MedicionID, Nombre, Fecha, Lugar, TempSonda, TempAmbiente, Presion, HumedadAmbiente, Altitud, UV, LightIntensity, HumedadSuelo, PPM"
        datosCSV.add(titulos)

        // Crear una lista de strings con los datos para el archivo CSV
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val medicionID = cursor.getInt(1)
                val nombre = cursor.getString(2)
                val fecha = cursor.getString(3)
                val lugar = cursor.getString(4)
                val tempSonda = cursor.getString(5)
                val tempAmbiente = cursor.getString(6)
                val presion = cursor.getString(7)
                val humedadAmbiente = cursor.getString(8)
                val altitud = cursor.getString(9)
                val uv = cursor.getString(10)
                val lightIntensity = cursor.getString(11)
                val humedadSuelo = cursor.getString(12)
                val ppm = cursor.getString(13)

                val lineaCSV = "$id, $medicionID, $nombre, $fecha, $lugar, $tempSonda, $tempAmbiente, $presion, $humedadAmbiente, $altitud, $uv, $lightIntensity, $humedadSuelo, $ppm"
                datosCSV.add(lineaCSV)
            } while (cursor.moveToNext())
        }

        cursor.close()

        // Guardar el archivo CSV en la carpeta "archivosCSV" dentro de "Downloads"
        val directorioDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val carpetaArchivosCSV = File(directorioDownloads, "ArchivosCSV_Borde_Costero")

        if (!carpetaArchivosCSV.exists()) {
            carpetaArchivosCSV.mkdirs()
        }

        // Obtener la fecha y hora actual
        val currentDate = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "medicion_$medicionID" + "_" + "$currentDate.csv"
        val filePath = File(carpetaArchivosCSV, fileName).absolutePath

        try {
            val csvWriter = FileWriter(filePath)
            val csvBuffer = BufferedWriter(csvWriter)

            // Escribir los datos en el archivo CSV
            for (line in datosCSV) {
                csvBuffer.write(line)
                csvBuffer.newLine()
            }

            csvBuffer.close()
            csvWriter.close()

            Toast.makeText(requireContext(), "Datos exportados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al exportar", Toast.LENGTH_SHORT).show()
        }
    }



    fun llenarTabla() {

        try{

            val con = SQLite(requireContext(), "mediciones", null, 1)
            val baseDatos = con.writableDatabase

            // Modificar la consulta SQL para obtener registros únicos según la columna _medicionID
            val consulta = "SELECT _medicionID, nombre, fecha, lugar FROM mediciones GROUP BY _medicionID"
            val fila = baseDatos.rawQuery(consulta, null)

            fila.moveToFirst()

            do {
                val registro = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_table_layout_mediciones, null, false)

                val tvId = registro.findViewById<View>(R.id.tvId) as TextView
                val tvNombre = registro.findViewById<View>(R.id.tvNombre) as TextView
                val tvFecha = registro.findViewById<View>(R.id.tvFecha) as TextView
                val tvLugar = registro.findViewById<View>(R.id.tvLugar) as TextView

                //colocar de bd
                tvId.setText(fila.getString((0)).toString())
                tvNombre.setText(fila.getString(1))
                tvFecha.setText(fila.getString(2))
                tvLugar.setText(fila.getString(3))

                tablaMediciones?.addView(registro)
            } while (fila.moveToNext())
        }catch (e: Exception) {
            //Toast.makeText(context, "Aun nno se han realizado mediciones", Toast.LENGTH_SHORT).show()
            val medHeader = rootView.findViewById<TextView>(R.id.mediciones_header)
            medHeader.text = "Aún no se han realizado mediciones"
        }

    }





}