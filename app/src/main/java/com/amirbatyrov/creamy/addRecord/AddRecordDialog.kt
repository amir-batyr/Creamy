package com.amirbatyrov.creamy.addRecord

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.amirbatyrov.creamy.db.dbAccessModel
import com.amirbatyrov.creamy.db.record
import java.time.LocalDate


@Composable
fun AddRecordDialog(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    dbAccess: dbAccessModel
) {
    if (openDialog.value) {

        Dialog(onDismissRequest = { openDialog.value = true }) {

            dbAccess.load()
            val creams by dbAccess.creamState.collectAsState()
            val areas by dbAccess.areaState.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val cream = remember { mutableStateOf("Cream") }
                val area = remember { mutableStateOf("Area") }
                val creamDialog = remember { mutableStateOf(false) }
                val areaDialog = remember { mutableStateOf(false) }

                Text("Add to Record", fontSize = 24.sp, modifier = Modifier.padding(10.dp))
                Text(LocalDate.now().toString())
                FormDropDown(
                    cream,
                    creams,
                    onClick = { creamDialog.value = true })
                FormDropDown(area, areas, onClick = { areaDialog.value = true })
                Button(
                    onClick = {
                        val cid = creams.find { it.name == cream.value }
                        val aid = areas.find { it.name == area.value }

                        if (cid != null && aid != null) {
                            println(cid.cid)
                            println(aid.aid)
                            dbAccess.insertRecord(
                                record(
                                    cid.cid,
                                    aid.aid,
                                    LocalDate.now()
                                )
                            )
                        }
                        openDialog.value = false
                    },
                    modifier = Modifier.padding(10.dp)
                ) { Text("Add") }

                SelectionEditMenu(
                    creamDialog,
                    "Cream",
                    creams,
                    dbAccess

                )
                SelectionEditMenu(
                    areaDialog, "Area", areas, dbAccess
                )
            }
        }
    }
}