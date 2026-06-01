package com.amirbatyrov.creamy.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class dbAccessModel(private val db: CreamyDB) : ViewModel() {
    private val _creamState = MutableStateFlow<List<Cream>>(emptyList())
    val creamState = _creamState.asStateFlow()
    private val _areaState = MutableStateFlow<List<Area>>(emptyList())
    val areaState = _areaState.asStateFlow()

    private val _recordState = MutableStateFlow<List<creamColor>>(emptyList())
    val recordState = _recordState.asStateFlow()

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val creams = db.CreamDao().getAll()
            _creamState.value = creams
            val areas = db.AreaDao().getAll()
            _areaState.value = areas
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
            db.recordDao().insertAll(record)
            load()
        }
    }

    fun getRecordsBetween(start: LocalDate, end: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            _recordState.value = db.recordDao().getRecordsBetween(start, end)
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

}