package com.amirbatyrov.creamy.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.amirbatyrov.creamy.db.CreamColor
import com.amirbatyrov.creamy.ui.theme.setColors
import java.time.LocalDate

@Composable
fun Card(
    date: LocalDate,
    records: List<CreamColor>,
    modifier: Modifier,
    textColor: Color,
    selectedRecord: MutableState<CreamColor>,
    deleteDialog: MutableState<Boolean>
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (LocalDate.now() == date) {

            Text(
                "${date.dayOfMonth}",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

        } else {
            Text("${date.dayOfMonth}", color = textColor)
        }
        records.filter { it.date == date }.forEach {
            Text(
                it.cream[0] + " - " + it.area,
                modifier = Modifier
                    .combinedClickable(
                        onLongClick = {
                            selectedRecord.value = it
                            deleteDialog.value = true
                        },
                        onClick = {}
                    )
                    .background(setColors[it.color])
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White
            )

        }
    }
}