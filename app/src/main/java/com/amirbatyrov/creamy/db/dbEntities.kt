package com.amirbatyrov.creamy.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

sealed interface CreamOrAreaOrRecord

data class CreamColor(val cream: String, val area: String, val date: LocalDate, val color: Int): CreamOrAreaOrCreamColor

data class BasicRecord(val cream: String, val cid: Long, val area: String, val aid: Long, val color: Int)

sealed interface CreamOrAreaOrCreamColor

@Entity
data class Cream(
    @PrimaryKey(autoGenerate = true) val cid: Long,
    override val name: String,
    val color: Int,
)  : CreamOrArea, CreamOrAreaOrRecord, CreamOrAreaOrCreamColor {
    constructor(name: String, color: Int) : this (cid = 0, name = name, color = color)
}

@Entity
data class Area(
    @PrimaryKey(autoGenerate = true)
    val aid: Long,
    override val name: String
) : CreamOrArea, CreamOrAreaOrRecord, CreamOrAreaOrCreamColor {
    constructor(name: String) : this (aid = 0, name = name)
}


@Entity(
    primaryKeys = ["cid", "aid", "date"],
    foreignKeys = [ForeignKey(entity = Cream::class, parentColumns = ["cid"], childColumns = ["cid"]),
        ForeignKey(entity = Area::class, parentColumns = ["aid"], childColumns = ["aid"])]
) data class Record(
    val cid: Long,
    val aid: Long,
    val date: LocalDate
) : CreamOrAreaOrRecord