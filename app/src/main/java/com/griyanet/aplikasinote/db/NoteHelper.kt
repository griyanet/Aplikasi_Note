package com.griyanet.aplikasinote.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.griyanet.aplikasinote.db.DatabaseContract.NoteColumns.Companion.TABLE_NAME
import com.griyanet.aplikasinote.db.DatabaseContract.NoteColumns.Companion._ID
import java.sql.SQLException

class NoteHelper(context: Context) {

    private var dataBaseHelper: DatabaseHelper = DatabaseHelper(context)
    private lateinit var database: SQLiteDatabase

    companion object {
        private const val DATABASE_TABLE = TABLE_NAME
        private var INSTANCE: NoteHelper? = null

        fun getInstance(context: Context): NoteHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteHelper(context)
            }
    }

    init {
        dataBaseHelper = DatabaseHelper(context)
    }

    @Throws(SQLException::class)
    fun open() {
        database = dataBaseHelper.writableDatabase
    }

    fun close() {
        dataBaseHelper.close()
        if (database.isOpen)
            database.close()
    }

    fun queryAll(): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            null,
            null,
            null,
            null,
            "$_ID ASC",
            null
        )
    }

    fun queryById(id: String): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            "$_ID = ?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
    }

    fun insert(values: ContentValues?): Long {
        return database.insert(DATABASE_TABLE, null, values)
    }

    fun update(id: String, values: ContentValues?): Int {
        return database.update(DATABASE_TABLE, values, "$_ID = ?", arrayOf(id))
    }

    fun deleteById(id: String): Int {
        return database.delete(DATABASE_TABLE, "$_ID = ?", null)
    }

//    fun getAllNotes(): ArrayList<Note> {
//        val arrayList = ArrayList<Note> ()
//        val cursor = database.query(DATABASE_TABLE, null, null, null, null, null,
//            "$_ID ASC", null)
//        cursor.moveToFirst()
//        var note: Note
//        if (cursor.count > 0) {
//            do {
//                note = Note()
//                note.id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID))
//                note.title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE))
//                note.description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION))
//                note.date = cursor.getString(cursor.getColumnIndexOrThrow(DATE))
//
//                arrayList.add(note)
//                cursor.moveToNext()
//            } while (!cursor.isAfterLast)
//        }
//        cursor.close()
//        return arrayList
//    }
}