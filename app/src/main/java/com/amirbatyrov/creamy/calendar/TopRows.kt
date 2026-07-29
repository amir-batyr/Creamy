package com.amirbatyrov.creamy.calendar

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amirbatyrov.creamy.R
import com.amirbatyrov.creamy.db.DBAccessModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TopRows(
    month: MutableState<String>,
    year: MutableState<String>,
    filter: MutableState<String>,
    dbAccess: DBAccessModel
) {
    val areas by dbAccess.areaState.collectAsState()

    val importDialog = remember { mutableStateOf(false) }
    // Top Bar Row
    Row(
        modifier = Modifier.fillMaxWidth()
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
            ), modifier
        )

        DropDownMenu(
            year, (2020..LocalDate.now().year).map { it.toString() }.toTypedArray(), modifier
        )
        val areaNames = areas.map { it.name }.toTypedArray()
        DropDownMenu(filter, arrayOf("All", *areaNames), modifier)

        val context = LocalContext.current
        OutlinedIconButton(
            onClick = {
                dbAccess.writeAll(context)
            },
            border = ButtonDefaults.outlinedButtonBorder(true),
            modifier = Modifier.padding(0.dp, 4.dp)
        ) {
            Icon(painter = painterResource(R.drawable.download), "Download", tint = ButtonDefaults.outlinedButtonColors().contentColor)
        }


        val csvPicker =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

                if (uri != null) {
                    dbAccess.import(uri, context)
                }
            }
        OutlinedIconButton(
            onClick = {
                importDialog.value = true
            },
            border = ButtonDefaults.outlinedButtonBorder(true),
            modifier = Modifier.padding(0.dp, 4.dp)
        ) {
            Icon(painter = painterResource(R.drawable.upload), "Import", tint = ButtonDefaults.outlinedButtonColors().contentColor)
        }

        if (importDialog.value) {
            ConfirmImport(importDialog) {
                csvPicker.launch(
                    arrayOf(
                        "text/csv", "text/comma-separated-values", "*/*"
                    )
                )
                importDialog.value = false
            }
        }
    }

    // Days Row
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (column in 0 until 7) {
            Text(days[column], modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
}