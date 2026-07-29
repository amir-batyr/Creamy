package com.amirbatyrov.creamy.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.amirbatyrov.creamy.db.CreamColor
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.amirbatyrov.creamy.db.DBAccessModel
import com.amirbatyrov.creamy.db.RecordFilter
import java.time.Month

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun Calendar(
    innerPadding: PaddingValues,
    dbAccess: DBAccessModel
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

        TopRows(month, year, filter, dbAccess)

        val deleteDialog = remember { mutableStateOf(false) }
        val selectedRecord: MutableState<CreamColor> = remember {
            mutableStateOf(
                CreamColor(
                    "cream", "area",
                    LocalDate.now(), 0
                )
            )
        }

        val selectedMonth =
            LocalDate.of(
                year.value.toInt(),
                Month.valueOf(month.value.uppercase()).value,
                1
            )

        var firstDay = selectedMonth.dayOfWeek.value
        if (firstDay == 7) firstDay = 0

        val previousMonth = selectedMonth.minusMonths(1)
        val nextMonth = selectedMonth.plusMonths(1)
        val dates = arrayOfNulls<LocalDate>(42)
        for (i in 0 until 42) {
            if (i < firstDay) {
                dates[i] =
                    previousMonth.withDayOfMonth(previousMonth.lengthOfMonth() - (firstDay - (i + 1)))
            } else if (i < firstDay + selectedMonth.lengthOfMonth()) {
                dates[i] = selectedMonth.withDayOfMonth((i + 1) - firstDay)
            } else {
                dates[i] =
                    nextMonth.withDayOfMonth((i - (firstDay + selectedMonth.lengthOfMonth())) + 1)
            }
        }
        val rawRecords = dbAccess.recordState.collectAsState()
        dbAccess.getRecordsBetween(RecordFilter(dates.first()!!, dates.last()!!, filter.value))
        LaunchedEffect(filter) {
            dbAccess.getRecordsBetween(RecordFilter(dates.first()!!, dates.last()!!, filter.value))
        }
        val scrollState = rememberScrollState()
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val minRowHeight = maxHeight / 6
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                for (row in 0 until 6) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        for (colum in 0 until 7) {
                            val i = (row * 7) + colum

                            Card(
                                dates[i]!!,
                                rawRecords.value.filter { it.date == dates[i] },
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .heightIn(min = minRowHeight),
                                MaterialTheme.colorScheme.secondary,
                                selectedRecord,
                                deleteDialog
                            )
                        }
                    }
                }
            }
        }
        DeleteDialog(deleteDialog, selectedRecord.value, dbAccess)
    }
}