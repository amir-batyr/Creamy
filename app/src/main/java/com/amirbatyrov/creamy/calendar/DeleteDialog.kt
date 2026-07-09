package com.amirbatyrov.creamy.calendar


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amirbatyrov.creamy.R
import com.amirbatyrov.creamy.db.CreamColor
import com.amirbatyrov.creamy.db.DBAccessModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(
    openDialog: MutableState<Boolean>,
    record: CreamColor,
    dbAccess: DBAccessModel
) {
    if (openDialog.value) {
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Delete",
                    modifier = Modifier.size(35.dp)
                )
            },
            title = {
                Text("Delete this record?")
            },
            text = {
                Text(
                    "${record.cream} - ${record.area}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(fontSize = 20.sp)
                )
            },
            onDismissRequest = { openDialog.value = true },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        dbAccess.delete(record)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}