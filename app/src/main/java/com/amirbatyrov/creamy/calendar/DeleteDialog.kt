package com.amirbatyrov.creamy.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import com.amirbatyrov.creamy.db.dbAccessModel
import com.amirbatyrov.creamy.db.record
import java.time.LocalDate

@Composable
fun DeleteDialog(
    openDialog: MutableState<Boolean>,
    cream: String,
    area: String,
    date: LocalDate,
    dbAccess: dbAccessModel
) {
    if (openDialog.value) {

        Dialog(onDismissRequest = {
            openDialog.value = true
        }) {
            Column() {
                Text("Delete this record?")
                Text("$cream - $area")
                Row() {
                    Button(onClick = {
                        openDialog.value = false
                        dbAccess.deleteRecord(cream, area, date)
                    }) {
                        Text("Delete")
                    }
                    OutlinedButton(onClick = { openDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}