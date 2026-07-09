package com.amirbatyrov.creamy.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CreamDao {
    @Query("SELECT * FROM Cream")
    fun getAll(): Flow<List<Cream>>

    @Insert
    fun insertAll(cream: Cream): Long

    @Update
    fun update(vararg cream: Cream)

    @Delete
    fun delete(vararg cream: Cream)
}

@Dao
interface AreaDao {
    @Query("SELECT * FROM Area")
    fun getAll(): Flow<List<Area>>
    @Insert
    fun insertAll(area: Area): Long

    @Update
    fun update(vararg area: Area)

    @Delete
    fun delete(vararg area: Area)
}

@Dao
interface RecordDao {
    @Insert
    fun insert(vararg record: record)

    @Insert
    fun insertAll(record: List<record>)

    @Query("DELETE FROM record")
    fun deleteAll()

    @Query("DELETE FROM record WHERE cid IN (SELECT cid FROM Cream WHERE name = :cream) AND aid IN (SELECT aid FROM Area WHERE name = :area) AND date = :date")
    fun delete(cream: String, area: String, date: LocalDate)

    @Query("SELECT Cream.name AS cream, Area.name AS area, record.date, Cream.color  FROM record, Cream, Area WHERE record.date BETWEEN :start AND :end AND Area.name LIKE :area AND Cream.cid = record.cid AND Area.aid = record.aid")
    fun getRecordsBetween(start: LocalDate, end: LocalDate, area: String = "%"): Flow<List<CreamColor>>

    @Query("SELECT Cream.name AS cream, Area.name AS area, record.date, Cream.color  FROM record, Cream, Area WHERE Cream.cid = record.cid AND Area.aid = record.aid")
    fun getAll(): Flow<List<CreamColor>>

    @Query("SELECT * FROM record WHERE cid = :cid")
    fun getAllWithCream(cid: Long): List<record>

    @Query("SELECT * FROM record WHERE aid = :aid")
    fun getAllWithArea(aid: Long): List<record>
}