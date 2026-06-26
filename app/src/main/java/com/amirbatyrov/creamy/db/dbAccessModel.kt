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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarRecords(
    var records: List<CreamColor>,
    val start: LocalDate,
    val end: LocalDate
)

sealed interface OperationResult {
    object Idle : OperationResult
    object Success : OperationResult
    data class Error(val message: String) : OperationResult
}


class DBAccessModel(private val db: CreamyDB) : ViewModel() {
    private val _creamState = MutableStateFlow<List<Cream>>(emptyList())
    val creamState = _creamState.asStateFlow()

    private val _areaState = MutableStateFlow<List<Area>>(emptyList())
    val areaState = _areaState.asStateFlow()

    private val _recordState = MutableStateFlow(
        CalendarRecords(
            emptyList(),
            LocalDate.now(),
            LocalDate.now()
        )
    )
    val recordState = _recordState.asStateFlow()

    private val _insertResult = MutableStateFlow<OperationResult>(OperationResult.Idle)
    val insertResult = _insertResult.asStateFlow()

    fun resetInsertResult() {
        _insertResult.value = OperationResult.Idle
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _creamState.value = db.CreamDao().getAll()
            _areaState.value = db.AreaDao().getAll()
        }
    }

    fun getRecordsBetween(
        start: LocalDate = _recordState.value.start,
        end: LocalDate = _recordState.value.end
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _recordState.value = CalendarRecords(
                db.recordDao().getRecordsBetween(start, end),
                start,
                end
            )
        }
    }

    fun insert(item: CreamOrAreaOrRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            when (item) {
                is Cream -> {
                    db.CreamDao().insertAll(item)
                    _creamState.value += item
                }

                is Area -> {
                    db.AreaDao().insertAll(item)
                    _areaState.value += item
                }

                is record -> {
                    try {
                        db.recordDao().insert(item)
                        _insertResult.value = OperationResult.Success
                        if (item.date >= _recordState.value.start && item.date <= _recordState.value.end) {
                            _recordState.update { it ->
                                it.copy(
                                    records = _recordState.value.records + CreamColor(
                                        _creamState.value.find { it.cid == item.cid }?.name
                                            ?: "null",
                                        _areaState.value.find { it.aid == item.aid }?.name
                                            ?: "null",
                                        item.date,
                                        _creamState.value.find { it.cid == item.cid }?.color ?: 0,
                                    )
                                )
                            }
                        } else {
                            println("No loading!")
                        }
                    } catch (e: Exception) {
                        _insertResult.value =
                            OperationResult.Error("Record already exists for this date.")
                    }
                }
            }
        }
    }


    fun delete(
        item: CreamOrArea
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (item) {
                is Cream -> {
                    if (db.recordDao().getAllWithCream(item.cid).isEmpty()) {
                        db.CreamDao().delete(item)
                        _creamState.value = _creamState.value.dropWhile { it.cid == item.cid }
                    }
                }

                is Area -> {
                    if (db.recordDao().getAllWithArea(item.aid).isEmpty()) {
                        db.AreaDao().delete(item)
                        _areaState.value = _areaState.value.dropWhile { it.aid == item.aid }
                    }
                }
            }
        }
    }

    fun deleteRecord(cream: String, area: String, date: LocalDate, color: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val cid = _creamState.value.find { it.name == cream }
            val aid = _areaState.value.find { it.name == area }
            if (cid != null && aid != null) {
                db.recordDao().delete(record(cid.cid, aid.aid, date))
                _recordState.update {
                    it.copy(records = _recordState.value.records.dropWhile {
                        it == CreamColor(
                            cream,
                            area,
                            date,
                            color
                        )
                    })
                }
            }
        }
    }


    fun update(item: CreamOrArea) {
        viewModelScope.launch(Dispatchers.IO) {
            when (item) {
                is Cream -> {
                    db.CreamDao().update(item)
                    _creamState.value =
                        _creamState.value.dropWhile { it.cid == item.cid } + item
                    getRecordsBetween()
                }

                is Area -> {
                    db.AreaDao().update(item)
                    _areaState.value =
                        _areaState.value.dropWhile { it.aid == item.aid } + item
                    getRecordsBetween()
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
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
            }

            val resolver = context.contentResolver

            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )
            val data = db.recordDao().getAll().joinToString("\n") {
                "${it.date},${it.cream},${it.area}"
            }.toByteArray()

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(data)
                }
            }
        }
    }

    fun import(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val dates = mutableListOf<LocalDate>()
            val cids = mutableListOf<String>()
            val aids = mutableListOf<String>()
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                reader.forEachLine { line ->
                    val row = line.split(",")
                    dates.add(LocalDate.parse(row[0]))
                    cids.add(row[1])
                    aids.add(row[2])
                }
            }
            if (creamState.value.isEmpty() || areaState.value.isEmpty()) {
                load()
            }
            cids.replaceAll { name ->
                val cid = creamState.value.find { cream -> cream.name == name }
                cid?.cid?.toString() ?: db.CreamDao().insertAll(Cream(name, 0)).toString()
            }
            aids.replaceAll { name ->
                val aid = areaState.value.find { area -> area.name == name }
                aid?.aid?.toString() ?: db.AreaDao().insertAll(Area(name)).toString()
            }
            val records = dates.indices.map { index ->
                record(
                    cids[index].toLong(),
                    aids[index].toLong(),
                    dates[index]
                )
            }
            db.recordDao().deleteAll()
            db.recordDao().insertAll(records)
            getRecordsBetween()
        }
    }
}