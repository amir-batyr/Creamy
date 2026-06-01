package com.amirbatyrov.creamy.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amirbatyrov.creamy.db.CreamColor
import com.amirbatyrov.creamy.ui.theme.setColors
import java.time.LocalDate

@Composable
fun Card(
    date: LocalDate,
    records: List<CreamColor>,
    filter: MutableState<String>,
    modifier: Modifier,
    textColor: Color,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (LocalDate.now() == date) {

                Text(
                    "${date.dayOfMonth}",
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

        } else {
            Text("${date.dayOfMonth}")
        }
        val matchingResuts =
            records.filter { it.date == date && (filter.value == "All" || it.area == filter.value) }
        matchingResuts.forEach { it ->
            Text(
                 it.cream[0] + " - " + it.area,
                modifier = Modifier
                    .background(setColors[it.color])
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White
            )

        }
    }
}