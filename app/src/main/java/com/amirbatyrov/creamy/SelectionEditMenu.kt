package com.amirbatyrov.creamy

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton

@Composable
fun SelectionEditMenu(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    title: String,
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

                Text("Edit ${title}s", fontSize = 24.sp)

                LazyColumn() {
                    try {
                        context.openFileInput("$title.csv").bufferedReader().useLines { lines ->
                            lines.forEach { line ->
                                item {
                                    if (title == "Cream") {
                                        Text(
                                            line.split(",")[0],
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors[line.split(",")[1].toInt()])
                                                .padding(5.dp)

                                        )
                                    } else {
                                        Text(
                                            line
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        item {Text("No ${title}s Found")}
                        e.printStackTrace()
                    }
                }
                val add = remember { mutableStateOf(false) }
                Button(
                    onClick = { add.value = true },
                ) { Text("Add") }
                AddMenu(add, title, context)
            }
        }
    }
}

@Composable
fun AddMenu(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    title: String,
    context: Context
) {
    if (openDialog.value) {

        Dialog(onDismissRequest = { openDialog.value = true }) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Add ${title}s", fontSize = 24.sp)

                val newValue = rememberTextFieldState()
                val color = remember { mutableStateOf(0) }
                var error = remember { mutableStateOf("") }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    if (title == "Cream") {
                        val pickColor = remember { mutableStateOf(false) }

                        Button(
                            onClick = { pickColor.value = true },
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .padding(10.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors[color.value])
                        ) {
                            ColorPickerDialog(pickColor, color)
                        }
                    }
                    OutlinedTextField(
                        state = newValue,
                        label = { Text(text = if (error.value.isNotEmpty()) error.value else title) },
                        isError = error.value.isNotEmpty()
                    )
                }
                Row() {
                    Button(
                        onClick = {
                            // Validation
                            error.value = ""
                            if (newValue.text.toString() != "") {
                                try {
                                    context.openFileInput("$title.csv").bufferedReader()
                                        .useLines { lines ->
                                            lines.forEach { line ->
                                                if (line.split(",")[0] == newValue.text.toString()) {
                                                    error.value = "Cream Already Exists"
                                                }
                                            }
                                        }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                error.value = "Can't Be Empty"
                            }

                            if (error.value.isEmpty()) {
                                context.openFileOutput("$title.csv", Context.MODE_APPEND).use {
                                    if (title == "Cream") {
                                        it.write((newValue.text.toString() + "," + color.value + "\n").toByteArray())
                                    } else {
                                        it.write((newValue.text.toString() + "\n").toByteArray())
                                    }
                                }
                                openDialog.value = false
                            }
                        },
                    ) { Text("Add") }
                    OutlinedButton(onClick = { openDialog.value = false }) { Text("Cancel") }
                }
            }
        }
    }
}