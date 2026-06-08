package com.amirbatyrov.creamy.calendar

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.amirbatyrov.creamy.db.DBAccessModel
import java.time.Month

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun Calendar(
    innerPadding: PaddingValues,
    dbAccess: DBAccessModel,
    context: Context
) {
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

        TopRows(month, year, filter, dbAccess,context)

        val selectedMonth =
            LocalDate.of(
                year.value.toInt(),
                Month.valueOf(month.value.uppercase()).value,
                1
            )

        var firstDay = selectedMonth.dayOfWeek.value

        val previousMonth = selectedMonth.minusMonths(1)
        var start = previousMonth.withDayOfMonth(
            (previousMonth.lengthOfMonth() - firstDay) + 1
        )

        if (selectedMonth.dayOfWeek.value == 7) {
            // If month starts on a Sunday
            firstDay = 0
            // Exclude the month before
            start = selectedMonth.withDayOfMonth(1)
        }
        val end = selectedMonth.plusMonths(1)
            .withDayOfMonth(42 - (selectedMonth.lengthOfMonth() + firstDay))

        // Fetch all required records
        val records = dbAccess.recordState.collectAsState()
        dbAccess.getRecordsBetween(start, end)

        if (records.value.records.isNotEmpty()) {
            for (row in 0 until 6) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (column in 0 until 7) {
                        val i = (row * 7) + column
                        if (i >= firstDay && i < selectedMonth.lengthOfMonth() + firstDay) {
                            // Add the current month's dates
                            Card(
                                selectedMonth.withDayOfMonth((i - firstDay) + 1),
                                records.value.records,
                                filter,
                                modifier = Modifier.weight(1f),
                                dbAccess,
                                MaterialTheme.colorScheme.onBackground                            )
                        } else if (i >= selectedMonth.lengthOfMonth() + firstDay) {
                            // Add the next month's dates
                            Card(
                                selectedMonth.plusMonths(1)
                                    .withDayOfMonth(i - (selectedMonth.lengthOfMonth() + firstDay) + 1),
                                records.value.records,
                                filter,
                                modifier = Modifier.weight(1f),
                                dbAccess,
                                MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            // Add the previous month's dates
                            Card(
                                previousMonth.withDayOfMonth(
                                    (previousMonth.lengthOfMonth() - firstDay) + i + 1
                                ),
                                records.value.records,
                                filter,
                                modifier = Modifier.weight(1f),
                                dbAccess,
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