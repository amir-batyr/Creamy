package com.amirbatyrov.creamy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun Calendar(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        val currentDate = LocalDate.now()
        val month = remember { mutableStateOf(currentDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())) }
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

            DropDownMenu(year, (2020..year.value.toInt()).map { it.toString() }.toTypedArray(), modifier)

            /*try {
                val creamsFile = File("creams.csv")
                creamsFile.bufferedReader().useLines { lines ->
                    lines.fold("") { some, text ->
                        "$some\n$text"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }*/
            DropDownMenu(filter, arrayOf("All", "A", "B", "C"), modifier)
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

        // Get the first day of the selected month
        val selectedDate = LocalDate.of(year.value.toInt(), Month.valueOf(month.value.uppercase()).value, 1)
        val firstDay = selectedDate.dayOfWeek.value

        for (row in 0 until 6) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (column in 0 until 7) {
                    val i = (row*7)+column
                    if (i >= firstDay && i < selectedDate.lengthOfMonth()+firstDay) {
                        // Add the current month's dates
                        if (currentDate.dayOfMonth == (i-firstDay)+1) {
                            Text("${i - firstDay + 1}", modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.primary), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("${i - firstDay + 1}", modifier = Modifier.weight(1f))
                        }
                    } else if (i >= selectedDate.lengthOfMonth()+firstDay) {
                        // Add the next month's dates
                        Text("${i - (selectedDate.lengthOfMonth()+firstDay) + 1}", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                    } else {
                        // Add the previous month's dates
                        Text("${(selectedDate.minusMonths(1).lengthOfMonth() - firstDay) + i + 1}", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}