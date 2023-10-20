package com.bencamus.bordecostero
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLite(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table mediciones (_id INTEGER PRIMARY KEY AUTOINCREMENT,_medicionID INTEGER, nombre text, fecha text, lugar text, tempSonda text, tempAmbiente text, presion text, humedadAmbiente text, altitud text, UV text, lightIntensity text, humedadSuelo text, ppm text)")
        //db?.execSQL("create table mediciones (codigo int primary key, nombre text, tempAmbiente real)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }


}