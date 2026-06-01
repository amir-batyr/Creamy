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

@Database(entities = [Cream::class, Area::class, record::class], version = 1)
@TypeConverters(Converters::class)
abstract class CreamyDB : RoomDatabase() {
    abstract fun CreamDao(): CreamDao
    abstract fun AreaDao(): AreaDao
    abstract fun recordDao(): recordDao
}