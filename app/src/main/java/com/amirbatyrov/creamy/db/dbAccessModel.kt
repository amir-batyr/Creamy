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
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarRecords(
    val records: List<CreamColor>,
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

    private val _allRecordsState = MutableStateFlow<List<CreamColor>>(emptyList())
    val allRecordsState = _allRecordsState.asStateFlow()

    private val _recordState = MutableStateFlow<CalendarRecords>(
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

    fun insertCream(cream: Cream) {
        viewModelScope.launch(Dispatchers.IO) {
            db.CreamDao().insertAll(cream)
            load()
        }
    }

    fun insertArea(area: Area) {
        viewModelScope.launch(Dispatchers.IO) {
            db.AreaDao().insertAll(area)
            load()
        }
    }

    fun insertRecord(record: record) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                db.recordDao().insert(record)
                _insertResult.value = OperationResult.Success
                getRecordsBetween()
            } catch (e: Exception) {
                _insertResult.value = OperationResult.Error("Record already exists for this date.")
            }

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

    fun deleteCream(cream: Cream) {
        viewModelScope.launch(Dispatchers.IO) {
            if (db.recordDao().getAllWithCream(cream.cid).isEmpty()) {
                db.CreamDao().delete(cream)
                load()
            }

        }
    }

    fun deleteArea(area: Area) {
        viewModelScope.launch(Dispatchers.IO) {
            if (db.recordDao().getAllWithArea(area.aid).isEmpty()) {
                db.AreaDao().delete(area)
                load()
            }

        }
    }

    fun deleteRecord(cream: String, area: String, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val cid = _creamState.value.find { it.name == cream }
            val aid = _areaState.value.find { it.name == area }
            if (cid != null && aid != null) {
                db.recordDao().delete(record(cid.cid, aid.aid, date))
            }
            getRecordsBetween()
        }
    }

    fun update(cream: Cream) {
        viewModelScope.launch(Dispatchers.IO) {
            db.CreamDao().update(cream)
            load()
        }
    }

    fun update(area: Area) {
        viewModelScope.launch(Dispatchers.IO) {
            db.AreaDao().update(area)
            load()
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
            val data = db.recordDao().getAll().map { it ->
                "${it.date},${it.cream},${it.area}"
            }.joinToString("\n").toByteArray()

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