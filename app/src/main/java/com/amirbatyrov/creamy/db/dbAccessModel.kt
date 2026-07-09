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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
                is Cream -> db.CreamDao().insertAll(item)
                is Area -> db.AreaDao().insertAll(item)
                is record -> {
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
            val data = db.recordDao().getAll().first().joinToString("\n") {
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
                    cids[index].toLong(), aids[index].toLong(), dates[index]
                )
            }
            db.recordDao().deleteAll()
            db.recordDao().insertAll(records)
            getRecordsBetween()
        }
    }
}