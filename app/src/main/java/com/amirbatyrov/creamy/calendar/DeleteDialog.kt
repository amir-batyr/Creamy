package com.amirbatyrov.creamy.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.amirbatyrov.creamy.db.DBAccessModel
import com.amirbatyrov.creamy.db.record
import org.w3c.dom.Text
import java.time.LocalDate

@Composable
fun DeleteDialog(
    openDialog: MutableState<Boolean>,
    cream: String,
    area: String,
    date: LocalDate,
    dbAccess: DBAccessModel
) {
    if (openDialog.value) {

        Dialog(onDismissRequest = {
            openDialog.value = true
        }) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Delete this record?", modifier = Modifier.padding(10.dp))
                Text("$cream - $area")
                Row(modifier = Modifier.padding(10.dp)) {
                    TextButton(
                        onClick = {
                            openDialog.value = false
                            dbAccess.deleteRecord(cream, area, date)
                        }

                    ) {
                        Text("Delete")
                    }
                    TextButton(
                        onClick = { openDialog.value = false }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}