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
    fun insert(cream: Cream): Long

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
    fun insert(area: Area): Long

    @Update
    fun update(vararg area: Area)

    @Delete
    fun delete(vararg area: Area)
}

@Dao
interface RecordDao {
    @Insert
    fun insert(vararg record: Record)

    @Insert
    fun insertAll(record: List<Record>)

    @Query("DELETE FROM Record")
    fun deleteAll()

    @Query("DELETE FROM Record WHERE cid IN (SELECT cid FROM Cream WHERE name = :cream) AND aid IN (SELECT aid FROM Area WHERE name = :area) AND date = :date")
    fun delete(cream: String, area: String, date: LocalDate)

    @Query("SELECT Cream.name AS cream, Area.name AS area, Record.date, Cream.color  FROM Record, Cream, Area WHERE Record.date BETWEEN :start AND :end AND Area.name LIKE :area AND Cream.cid = Record.cid AND Area.aid = Record.aid")
    fun getRecordsBetween(start: LocalDate, end: LocalDate, area: String = "%"): Flow<List<CreamColor>>

    @Query("SELECT Cream.name AS cream, Area.name AS area, Record.date, Cream.color  FROM Record, Cream, Area WHERE Cream.cid = Record.cid AND Area.aid = Record.aid LIMIT :limit OFFSET :offset")
    fun getChunk(limit: Int, offset: Int): List<CreamColor>

    @Query("SELECT * FROM Record WHERE cid = :cid")
    fun getAllWithCream(cid: Long): List<Record>

    @Query("SELECT * FROM Record WHERE aid = :aid")
    fun getAllWithArea(aid: Long): List<Record>
}