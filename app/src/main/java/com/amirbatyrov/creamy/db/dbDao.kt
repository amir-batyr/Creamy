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

    @Query("""
        DELETE FROM Record
        WHERE cid IN (SELECT cid FROM Cream WHERE name = :cream)
        AND aid IN (SELECT aid FROM Area WHERE name = :area)
        AND date = :date
        """)
    fun delete(cream: String, area: String, date: LocalDate)

    @Query("""
        SELECT Cream.name AS cream, Area.name AS area, Record.date, Cream.color 
        FROM Record
        JOIN Cream ON Cream.cid = Record.cid
        JOIN Area ON Area.aid = Record.aid
        WHERE Record.date BETWEEN :start AND :end AND Area.name LIKE :area
        ORDER BY Cream.name ASC, Area.name ASC
        """)
    fun getRecordsBetween(start: LocalDate, end: LocalDate, area: String = "%"): Flow<List<CreamColor>>

    @Query("""
        SELECT Cream.name AS cream, Area.name AS area, Record.date, Cream.color
        FROM Record
        JOIN Cream ON Cream.cid = Record.cid
        JOIN Area ON Area.aid = Record.aid
        LIMIT :limit OFFSET :offset
        """)
    fun getChunk(limit: Int, offset: Int): List<CreamColor>

    @Query("SELECT * FROM Record WHERE cid = :cid")
    fun getAllWithCream(cid: Long): List<Record>

    @Query("SELECT * FROM Record WHERE aid = :aid")
    fun getAllWithArea(aid: Long): List<Record>

    @Query("""
            SELECT Cream.name AS cream, Record.cid AS cid, Area.name AS area, Record.aid AS aid, Cream.color AS color
            FROM Record
            JOIN Cream ON Cream.cid = Record.cid
            JOIN Area ON Area.aid = Record.aid
            WHERE Record.date BETWEEN :start AND :end
            GROUP BY Cream.name, Area.name
            HAVING NOT EXISTS (
                SELECT 1 FROM Record R2 
                WHERE R2.cid = Record.cid 
                AND R2.aid = Record.aid 
                AND R2.date = :end
            )
            ORDER BY COUNT(*) DESC
            """)
    fun getCommonRecords(start: LocalDate, end: LocalDate): Flow<List<BasicRecord>>
}