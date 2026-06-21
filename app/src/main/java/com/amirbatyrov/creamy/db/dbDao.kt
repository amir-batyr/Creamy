package com.amirbatyrov.creamy.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

data class CreamColor(val cream: String, val area: String, val date: LocalDate, val color: Int)

@Dao
interface CreamDao {
    @Query("SELECT * FROM Cream")
    suspend fun getAll(): List<Cream>

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
    suspend fun getAll(): List<Area>
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
    @Delete
    fun delete(vararg record: record)

    @Query("SELECT Cream.name AS cream, Area.name AS area, record.date, Cream.color  FROM record, Cream, Area WHERE record.date BETWEEN :start AND :end AND Cream.cid = record.cid AND Area.aid = record.aid")
    fun getRecordsBetween(start: LocalDate, end: LocalDate): List<CreamColor>

    @Query("SELECT Cream.name AS cream, Area.name AS area, record.date, Cream.color  FROM record, Cream, Area WHERE Cream.cid = record.cid AND Area.aid = record.aid")
    fun getAll(): List<CreamColor>

    @Query("SELECT * FROM record WHERE cid = :cid")
    fun getAllWithCream(cid: Long): List<record>

    @Query("SELECT * FROM record WHERE aid = :aid")
    fun getAllWithArea(aid: Long): List<record>
}