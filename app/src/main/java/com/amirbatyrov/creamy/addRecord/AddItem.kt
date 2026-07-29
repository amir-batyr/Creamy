package com.amirbatyrov.creamy.addRecord

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.amirbatyrov.creamy.db.Area
import com.amirbatyrov.creamy.db.Cream
import com.amirbatyrov.creamy.db.CreamOrArea
import com.amirbatyrov.creamy.db.DBAccessModel
import com.amirbatyrov.creamy.ui.theme.setColors

@Composable
fun AddMenu(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    title: String,
    items: List<CreamOrArea>,
    dbAccess: DBAccessModel,
    pos: MutableState<Int>
) {
    if (openDialog.value) {

        Dialog(onDismissRequest = {
            openDialog.value = true
        }) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val newValue = rememberTextFieldState()
                val color = remember { mutableIntStateOf(0) }
                val error = remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    if (pos.value != -1) {
                        val item = items[pos.value]
                        if (item is Cream) {
                            color.intValue = item.color
                        }
                        newValue.edit { replace(0, length, item.name) }
                    }
                }

                if (pos.value != -1) {
                    Text("Edit ${title}s", fontSize = 24.sp)
                } else {
                    Text("Add ${title}s", fontSize = 24.sp)
                }

                if (title == "Cream") {
                    LazyVerticalGrid(columns = GridCells.Fixed(5)) {
                        items(20) { index ->
                            Button(
                                onClick = { color.intValue = index },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(5.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = setColors[index]),
                                border = if (color.intValue == index) {
                                    BorderStroke(color = Color.White, width = 2.dp)
                                } else {
                                    BorderStroke(color = Color.White, width = 0.dp)
                                }
                            ) {}
                        }
                    }
                }
                OutlinedTextField(
                    state = newValue,
                    label = { Text(text = error.value.ifEmpty { title }) },
                    isError = error.value.isNotEmpty()
                )

                Row {
                    Button(
                        onClick = {
                            // Validation
                            error.value = ""
                            val name = newValue.text.toString()
                            val names = items.map {it.name}

                            when {
                                name.contains(",") -> error.value = "Can't Contain ,"
                                name == "" -> {
                                    error.value = "Can't Be Empty"
                                }
                                name in names && name != items.getOrNull(pos.value)?.name-> {
                                    error.value = "Already Exists"
                                }
                                else -> {
                                    if (pos.value != -1) {
                                        // EDIT

                                        when (val item = items[pos.value]) {
                                            is Cream -> dbAccess.update(
                                                Cream(
                                                    item.cid,
                                                    newValue.text.toString(),
                                                    color.intValue,
                                                )
                                            )

                                            is Area -> dbAccess.update(
                                                Area(
                                                    item.aid, newValue.text.toString()
                                                )
                                            )
                                        }
                                        pos.value = -1
                                    } else {
                                        // WRITE

                                        if (title == "Cream") {
                                            dbAccess.insert(
                                                Cream(newValue.text.toString(), color.intValue)
                                            )

                                        } else {
                                            dbAccess.insert(
                                                Area(newValue.text.toString())
                                            )
                                        }
                                    }
                                    openDialog.value = false
                                }
                            }
                        },
                    ) { Text("Add") }
                    OutlinedButton(onClick = {
                        openDialog.value = false
                        pos.value = -1
                    }) { Text("Cancel") }
                }
            }
        }
    }
}