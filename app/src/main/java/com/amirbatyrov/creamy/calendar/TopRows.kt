package com.amirbatyrov.creamy.calendar

import android.R.attr.enabled
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amirbatyrov.creamy.R
import com.amirbatyrov.creamy.db.Area
import com.amirbatyrov.creamy.db.Cream
import com.amirbatyrov.creamy.db.DBAccessModel
import com.amirbatyrov.creamy.db.record
import java.time.LocalDate
import kotlin.collections.toByteArray

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TopRows(
    month: MutableState<String>,
    year: MutableState<String>,
    filter: MutableState<String>,
    dbAccess: DBAccessModel,
    context: Context
) {
    dbAccess.load()
    val areas by dbAccess.areaState.collectAsState()
    val creams by dbAccess.creamState.collectAsState()

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
        val areaNames = areas.map { it -> it.name }.toTypedArray()
        DropDownMenu(filter, arrayOf("All", *areaNames), modifier)

        val records = dbAccess.allRecordsState.collectAsState()

        OutlinedIconButton(
            onClick = {
                dbAccess.writeAll(context)
            }, border = ButtonDefaults.outlinedButtonBorder(true), modifier = Modifier.padding(0.dp,4.dp)
        ) {
            Icon(painter = painterResource(R.drawable.download), "Download")
        }

        val csvPicker =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

                if (uri != null) {
                    dbAccess.import(uri, context)
                }
            }

        OutlinedIconButton(onClick = {
            csvPicker.launch(
                arrayOf(
                    "text/csv", "text/comma-separated-values", "*/*"
                )
            )
        }, border = ButtonDefaults.outlinedButtonBorder(true), modifier = Modifier.padding(0.dp,4.dp)) {
            Icon(painter = painterResource(R.drawable.upload), "Import")
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