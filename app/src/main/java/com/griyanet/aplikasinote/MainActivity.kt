package com.griyanet.aplikasinote

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.griyanet.aplikasinote.adapter.NoteAdapter
import com.griyanet.aplikasinote.db.NoteHelper
import com.griyanet.aplikasinote.helper.MappingHelper
import com.griyanet.aplikasinote.note.Note
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: NoteAdapter
    private lateinit var noteHelper: NoteHelper

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Notes"

        rv_note.layoutManager = LinearLayoutManager(this)
        rv_note.setHasFixedSize(true)
        adapter = NoteAdapter(this)
        rv_note.adapter = adapter

        fab_add.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD)
        }

        noteHelper = NoteHelper.getInstance(applicationContext)
        noteHelper.open()

        if (savedInstanceState == null) {
            loadNotesAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
            list?.let {
                adapter.listNotes = it
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }

    private fun loadNotesAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = noteHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            progressBar.visibility - View.GONE
            val notes = deferredNotes.await()
            if (notes.size > 0) {
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            when (requestCode) {
                NoteAddUpdateActivity.REQUEST_ADD -> if (resultCode == NoteAddUpdateActivity.RESULT_ADD) {
                    val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)
                    note?.let { adapter.addItem(it) }
                    rv_note.smoothScrollToPosition(adapter.itemCount - 1)
                    showSnackbarMessage("Satu item berhasil ditambahkan")
                }
                NoteAddUpdateActivity.REQUEST_UPDATE ->
                    when (resultCode) {
                        NoteAddUpdateActivity.RESULT_UPDATE -> {
                            val note =
                                data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)
                            note?.let { adapter.updateItem(position, it) }
                            rv_note.smoothScrollToPosition(position)
                            showSnackbarMessage("Satu item berhasil diubah")
                        }
                    }
                NoteAddUpdateActivity.RESULT_DELETE -> {
                    val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)
                    adapter.removeItem(position)
                    showSnackbarMessage("Satu item berhasil dihapus")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        noteHelper.close()
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(rv_note, message, Snackbar.LENGTH_SHORT).show()
    }

}