package com.amirbatyrov.creamy.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.amirbatyrov.creamy.db.creamColor
import com.amirbatyrov.creamy.db.dbAccessModel
import com.amirbatyrov.creamy.ui.theme.setColors
import java.time.Month

@Composable
fun Calendar(
    innerPadding: PaddingValues,
    dbAccess: dbAccessModel
) {
    dbAccess.load()
    val creams by dbAccess.creamState.collectAsState()
    val areas by dbAccess.areaState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        val currentDate = LocalDate.now()
        val month = remember {
            mutableStateOf(
                currentDate.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()
                )
            )
        }
        val year = remember { mutableStateOf(currentDate.year.toString()) }
        val filter = remember { mutableStateOf("All") }

        // Top Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val modifier = Modifier.padding(5.dp)
            DropDownMenu(
                month, arrayOf(
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December"
                ),
                modifier
            )

            DropDownMenu(
                year,
                (2020..LocalDate.now().year).map { it.toString() }.toTypedArray(),
                modifier
            )
            val areaNames = areas.map { it -> it.name}.toTypedArray()
            DropDownMenu(filter, arrayOf("All", *areaNames), modifier)
        }

        // Days Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            for (column in 0 until 7) {
                Text(days[column], modifier = Modifier.weight(1f))
            }
        }

        val selectedDate =
            LocalDate.of(year.value.toInt(), Month.valueOf(month.value.uppercase()).value, 1)
        var firstDay = selectedDate.dayOfWeek.value
        var start = selectedDate.minusMonths(1)
            .withDayOfMonth(((selectedDate.minusMonths(1).lengthOfMonth() - firstDay) + 1))
        if (selectedDate.dayOfWeek.value == 7) {
            firstDay = 0
            start = selectedDate.withDayOfMonth(1)
        }
        val end = selectedDate.plusMonths(1)
            .withDayOfMonth(42 - (selectedDate.lengthOfMonth() + firstDay))


        val records = dbAccess.recordState.collectAsState()
        dbAccess.getRecordsBetween(start, end)
        if (records.value.isNotEmpty()) {
            println(filter)
            for (row in 0 until 6) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (column in 0 until 7) {
                        val i = (row * 7) + column
                        if (i >= firstDay && i < selectedDate.lengthOfMonth() + firstDay) {
                            // Add the current month's dates
                            Card(
                                selectedDate.withDayOfMonth((i - firstDay) + 1),
                                records.value,
                                filter,
                                modifier = Modifier.weight(1f),
                                MaterialTheme.colorScheme.onBackground
                            )
                        } else if (i >= selectedDate.lengthOfMonth() + firstDay) {
                            // Add the next month's dates
                            Card(
                                selectedDate.plusMonths(1)
                                    .withDayOfMonth(i - (selectedDate.lengthOfMonth() + firstDay) + 1),
                                records.value,
                                filter,
                                modifier = Modifier.weight(1f),
                                MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            // Add the previous month's dates
                            Card(
                                selectedDate.minusMonths(1).withDayOfMonth(
                                    (selectedDate.minusMonths(1).lengthOfMonth() - firstDay) + i + 1
                                ),
                                records.value,
                                filter,
                                modifier = Modifier.weight(1f),
                                MaterialTheme.colorScheme.secondary
                            )

                        }
                    }
                }
            }
        } else {
            Text("Loading Data")
        }
    }
}

@Composable
fun Card(
    date: LocalDate,
    records: List<creamColor>,
    filter: MutableState<String>,
    modifier: Modifier,
    textColor: Color,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (LocalDate.now().dayOfMonth == date.dayOfMonth) {
            Text(
                "${date.dayOfMonth}",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(2.dp),
                color = textColor
            )
        } else {
            Text("${date.dayOfMonth}")
        }
        val matchingResuts =
            records.takeWhile { it.date == date && (filter.value == "All" || it.area == filter.value) }

        matchingResuts.forEach { it ->
            Text(
                it.area,
                modifier = Modifier
                    .background(setColors[it.color])
                    .fillMaxWidth()
            )

        }
    }
}