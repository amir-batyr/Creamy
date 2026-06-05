package com.amirbatyrov.creamy.db

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
    val allRecords = _allRecordsState.asStateFlow()

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
                db.recordDao().insertAll(record)
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

    fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _allRecordsState.value = db.recordDao().getAll()
        }
    }
}