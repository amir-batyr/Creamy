package com.amirbatyrov.creamy

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AddRecordDialog(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    context: Context
) {
    if (openDialog.value) {

        Dialog(onDismissRequest = { openDialog.value = false }) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Read data into arrays
                val creams = mutableListOf<String>()
                val areas = mutableListOf<String>()
                try {
                    context.openFileInput("Cream.csv").bufferedReader()
                        .useLines { lines -> lines.forEach { line -> creams.add(line.split(",")[0]) } }
                    context.openFileInput("Area.csv").bufferedReader()
                        .useLines { lines -> lines.forEach { line -> areas.add(line) } }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val cream = remember { mutableStateOf("Cream") }
                val area = remember { mutableStateOf("Area") }
                val creamEdit = remember { mutableStateOf(false) }
                val areaEdit = remember { mutableStateOf(false) }
                SelectionEditMenu(creamEdit, "Cream", context)
                SelectionEditMenu(areaEdit, "Area", context)


                Text("Add to Record", fontSize = 24.sp)
                FormDropDown(cream, creams.toTypedArray(), onClick = { creamEdit.value = true })
                FormDropDown(area, areas.toTypedArray(), onClick = { areaEdit.value = true })
                Button(
                    onClick = { println("Added $cream and $area") },
                ) { Text("Add") }
            }
        }
    }
}

@Composable
fun FormDropDown(
    value: MutableState<String>,
    items: Array<String>,
    modifier: Modifier = Modifier,
    selected: MutableState<Boolean> = remember { mutableStateOf(false) },
    onClick: () -> Unit
) {
    SplitButtonLayout(
        leadingButton = {
            val expanded = remember { mutableStateOf(false) }
            SplitButtonDefaults.LeadingButton(
                onClick = {
                    expanded.value = !expanded.value
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected.value) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.background
                    },
                    contentColor = if (selected.value) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                ),
                border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
            ) {
                Box() {
                    Text(value.value)
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = {
                            expanded.value = false
                        }
                    ) {
                        items.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    value.value = option
                                    selected.value = true
                                }
                            )
                        }
                    }
                }
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(onClick = onClick) {
                Icon(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = "Edit"
                )
            }
        }
    )
}