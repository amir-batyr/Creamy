package com.amirbatyrov.creamy.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Cream::class, Area::class, Record::class], version = 1)
@TypeConverters(Converters::class)
abstract class CreamyDB : RoomDatabase() {
    abstract fun CreamDao(): CreamDao
    abstract fun AreaDao(): AreaDao
    abstract fun recordDao(): RecordDao
}