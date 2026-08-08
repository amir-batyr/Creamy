package com.amirbatyrov.creamy.addRecord

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amirbatyrov.creamy.db.DBAccessModel
import com.amirbatyrov.creamy.db.OperationResult
import com.amirbatyrov.creamy.db.Record
import java.time.LocalDate


@Composable
fun AddRecordDialog(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    vmFactory: ViewModelProvider.Factory,
    dbAccess: DBAccessModel = viewModel(factory = vmFactory)
) {
    // Reset status whenever the dialog is opened or closed
    LaunchedEffect(openDialog.value) {
        dbAccess.resetInsertResult()
    }
    if (openDialog.value) {

        Dialog(onDismissRequest = { openDialog.value = false }) {
            val status by dbAccess.insertResult.collectAsState()

            // Handle auto-close only on success
            LaunchedEffect(status) {
                if (status == OperationResult.Success) {
                    openDialog.value = false
                }
            }

            val creams by dbAccess.creamState.collectAsStateWithLifecycle()
            val areas by dbAccess.areaState.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val cream = remember { mutableIntStateOf(-1) }
                val area = remember { mutableIntStateOf(-1) }
                val date = remember { mutableStateOf(LocalDate.now()) }
                val creamDialog = remember { mutableStateOf(false) }
                val areaDialog = remember { mutableStateOf(false) }
                val dateDialog = remember { mutableStateOf(false) }


                LaunchedEffect(openDialog.value, status) {
                    if (!openDialog.value || status == OperationResult.Success) {
                        openDialog.value = false
                        dbAccess.resetInsertResult()
                    }
                }

                Text("Add to Record", fontSize = 24.sp, modifier = Modifier.padding(10.dp))

                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    Button(
                        onClick = { dateDialog.value = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(date.value.toString())
                    }

                    if (dateDialog.value) {
                        DatePicker(
                            onDateSelected = { newDate -> date.value = newDate; dbAccess.getCommonRecords(newDate) },
                            onDismiss = { dateDialog.value = false })
                    }

                    FormDropDown(
                        cream,
                        creams,
                        onClick = { creamDialog.value = true },
                        "Cream"
                    )
                    FormDropDown(
                        area, areas, onClick = { areaDialog.value = true },
                        "Area"
                    )


                }

                if (status == OperationResult.Error("Record already exists for this date.")) {
                    Text(
                        "Record already exists for this date.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {

                        if (cream.intValue != -1 && area.intValue != -1) {
                            dbAccess.insert(
                                Record(
                                    creams[cream.intValue].cid,
                                    areas[area.intValue].aid,
                                    date.value
                                )
                            )
                        }

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