package com.amirbatyrov.creamy.db

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate

sealed interface OperationResult {
    object Idle : OperationResult
    object Success : OperationResult
    data class Error(val message: String) : OperationResult
}

data class RecordFilter(
    var start: LocalDate = LocalDate.now(),
    var end: LocalDate = LocalDate.now(),
    var area: String = ""
)

class DBAccessModel(private val db: CreamyDB) : ViewModel() {
    val creamState = db.CreamDao().getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val areaState = db.AreaDao().getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filter = MutableStateFlow(RecordFilter())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recordState = filter.flatMapLatest {
        db.recordDao().getRecordsBetween(it.start, it.end, if (it.area == "All") "%" else it.area)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    private val _insertResult = MutableStateFlow<OperationResult>(OperationResult.Idle)
    val insertResult = _insertResult.asStateFlow()

    fun resetInsertResult() {
        _insertResult.value = OperationResult.Idle
    }

    fun getRecordsBetween(
        newFilter: RecordFilter = filter.value
    ) {
        filter.value = newFilter
    }

    fun insert(item: CreamOrAreaOrRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            when (item) {
                is Cream -> db.CreamDao().insert(item)
                is Area -> db.AreaDao().insert(item)
                is Record -> {
                    try {
                        db.recordDao().insert(item)
                        _insertResult.value = OperationResult.Success
                    } catch (e: Exception) {
                        _insertResult.value =
                            OperationResult.Error("Record already exists for this date.")
                    }
                }
            }
        }
    }


    fun delete(
        item: CreamOrAreaOrCreamColor
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (item) {
                is Cream -> {
                    if (db.recordDao().getAllWithCream(item.cid).isEmpty()) {
                        db.CreamDao().delete(item)
                    }
                }

                is Area -> {
                    if (db.recordDao().getAllWithArea(item.aid).isEmpty()) {
                        db.AreaDao().delete(item)
                    }
                }

                is CreamColor -> db.recordDao().delete(item.cream, item.area, item.date)
            }
        }
    }


    fun update(item: CreamOrArea) {
        viewModelScope.launch(Dispatchers.IO) {
            when (item) {
                is Cream -> {
                    db.CreamDao().update(item)
                }

                is Area -> {
                    db.AreaDao().update(item)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun writeAll(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "records")
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(
                    MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS
                )
            }

            val resolver = context.contentResolver

            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            )

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write("Cream, Area, Date\n")

                        val chunkSize = 1000
                        var offset = 0
                        var hasMoreData = true

                        while (hasMoreData) {
                            val chunk = db.recordDao().getChunk(chunkSize, offset)
                            if (chunk.isEmpty()) {
                                hasMoreData = false
                            } else {
                                chunk.forEach {
                                    writer.write("${it.cream},${it.area},${it.date}\n")
                                }
                                writer.flush() // Pushes chunk safely to disk
                                offset += chunkSize
                            }
                        }

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun import(uri: Uri, context: Context) {
        writeAll(context)
        viewModelScope.launch(Dispatchers.IO) {
            db.recordDao().deleteAll()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use {
                    reader ->
                    reader.readLine() ?: return@launch

                    val chunkSize = 1000
                    val chunkBuffer = mutableListOf<Record>()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        val record = parseCsvToRecord(line!!)
                        if (record != null) {
                            chunkBuffer.add(record)
                        }

                        if (chunkBuffer.size >= chunkSize) {
                            db.recordDao().insertAll(chunkBuffer)
                            chunkBuffer.clear()
                        }
                    }

                    if (chunkBuffer.isNotEmpty()) {
                        db.recordDao().insertAll(chunkBuffer)
                        chunkBuffer.clear()
                    }
                }
            }
        }
    }

    private fun parseCsvToRecord(line: String): Record? {
        val tokens = line.split(",")
        if (tokens.size < 3) return null

        return Record(
            nameToID(Cream(tokens[0], 0)),
            nameToID(Area(tokens[1])),
            LocalDate.parse(tokens[2])
        )
    }

    private fun nameToID(item: CreamOrArea): Long {
        return when (item) {
            is Cream -> creamState.value.find { it.name == item.name }?.cid ?: db.CreamDao()
                .insert(item)

            is Area -> areaState.value.find { it.name == item.name }?.aid ?: db.AreaDao()
                .insert(item)
        }
    }
}
