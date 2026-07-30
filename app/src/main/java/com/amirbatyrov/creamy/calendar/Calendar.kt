package com.amirbatyrov.creamy.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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

@OptIn(ExperimentalGridApi::class)
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

        val firstDay = selectedMonth.dayOfWeek.value

        val firstDate = if (firstDay == 7) {
            selectedMonth.withDayOfMonth(1)
        } else {
            val prevMonth = selectedMonth.minusMonths(1)
            prevMonth.withDayOfMonth(prevMonth.lengthOfMonth() - (firstDay - 1))
        }


        val rawRecords = dbAccess.recordState.collectAsState()
        dbAccess.getRecordsBetween(RecordFilter(firstDate, firstDate.plusDays(41), filter.value))
        LaunchedEffect(filter) {
            dbAccess.getRecordsBetween(
                RecordFilter(
                    firstDate,
                    firstDate.plusDays(41),
                    filter.value
                )
            )
        }
        val scrollState = rememberScrollState()

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val minRowHeight = maxHeight / 6
            val rowWidth = maxWidth / 7
            Grid(config = {
                repeat(7) {
                    column(rowWidth)
                }
                repeat(6) {
                    row(GridTrackSize.Auto)
                }
            },
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                for (i in 0 until 42) {
                    val date = firstDate.plusDays(i.toLong())
                    Card(
                        date,
                        rawRecords.value.filter { it.date == date },
                        Modifier
                            .fillMaxSize().heightIn(min=minRowHeight),
                        MaterialTheme.colorScheme.secondary,
                        selectedRecord,
                        deleteDialog
                    )
                }
            }
        }
        DeleteDialog(deleteDialog, selectedRecord.value, dbAccess)
    }
}