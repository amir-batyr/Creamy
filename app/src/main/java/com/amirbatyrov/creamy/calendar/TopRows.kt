package com.amirbatyrov.creamy.calendar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amirbatyrov.creamy.db.DBAccessModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TopRows(
    month: MutableState<String>,
    year: MutableState<String>,
    filter: MutableState<String>,
    dbAccess: DBAccessModel
) {
    dbAccess.load()
    val areas by dbAccess.areaState.collectAsState()

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
        val areaNames = areas.map { it -> it.name }.toTypedArray()
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
}