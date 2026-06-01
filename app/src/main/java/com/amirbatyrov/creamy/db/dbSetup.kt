package com.amirbatyrov.creamy.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.amirbatyrov.creamy.db.Cream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class creamColor(val cream: String,val area: String,val date: LocalDate,val color: Int)

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE // Format: YYYY-MM-DD

    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, formatter) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.format(formatter)
    }
}
sealed interface CreamOrArea {
    val name: String
}
@Entity
data class Cream(
    @PrimaryKey(autoGenerate = true) val cid: Long,
    override val name: String,
    val color: Int,
)  : CreamOrArea {
    constructor(name: String, color: Int) : this (cid = 0, name = name, color = color)
}

@Entity
data class Area(
    @PrimaryKey(autoGenerate = true)
    val aid: Long,
    override val name: String
) : CreamOrArea {
    constructor(name: String) : this (aid = 0, name = name)
}


@Entity(
    primaryKeys = ["cid", "aid", "date"],
    foreignKeys = [ForeignKey(entity = Cream::class, parentColumns = ["cid"], childColumns = ["cid"]),
        ForeignKey(entity = Area::class, parentColumns = ["aid"], childColumns = ["aid"])]
) data class record(
    val cid: Long,
    val aid: Long,
    val date: LocalDate
)

@Dao
interface CreamDao {
    @Query("SELECT * FROM Cream")
    suspend fun getAll(): List<Cream>

    @Insert
    fun insertAll(vararg cream: Cream)

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
    fun insertAll(vararg area: Area)

    @Update
    fun update(vararg area: Area)

    @Delete
    fun delete(vararg area: Area)
}

@Dao
interface recordDao {

    @Insert
    fun insertAll(vararg record: record)

    @Query("SELECT Cream.name AS cream, Area.name AS area, record.date, Cream.color  FROM record, Cream, Area WHERE record.date BETWEEN :start AND :end AND Cream.cid = record.cid AND Area.aid = record.aid")
    fun getRecordsBetween(start: LocalDate, end: LocalDate): List<creamColor>

    @Query("SELECT * FROM record WHERE cid = :cid")
    fun getAllWithCream(cid: Long): List<record>

    @Query("SELECT * FROM record WHERE aid = :aid")
    fun getAllWithArea(aid: Long): List<record>
}

@Database(entities = [Cream::class, Area::class, record::class], version = 1)
@TypeConverters(Converters::class)
abstract class CreamyDB : RoomDatabase() {
    abstract fun CreamDao(): CreamDao
    abstract fun AreaDao(): AreaDao
    abstract fun recordDao(): recordDao
}