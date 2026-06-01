package com.amirbatyrov.creamy.addRecord

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.amirbatyrov.creamy.R
import com.amirbatyrov.creamy.db.Area
import com.amirbatyrov.creamy.db.Cream
import com.amirbatyrov.creamy.db.CreamOrArea
import com.amirbatyrov.creamy.db.dbAccessModel
import com.amirbatyrov.creamy.ui.theme.setColors


@Composable
fun SelectionEditMenu(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    title: String,
    items: List<CreamOrArea>,
    dbAccess: dbAccessModel
) {
    if (openDialog.value) {
        dbAccess.load()
        Dialog(onDismissRequest = {
            openDialog.value = false
        }) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Edit ${title}s", fontSize = 24.sp, modifier = Modifier.padding(10.dp))
                val pos = remember { mutableIntStateOf(-1) }
                val add = remember { mutableStateOf(false) }

                if (items.isNotEmpty()) {
                    LazyColumn() {

                        items(items.size) { index ->
                            val item = items[index]
                            SplitButtonLayout(
                                leadingButton = {
                                    SplitButtonDefaults.LeadingButton(
                                        onClick = {
                                            pos.intValue = index
                                            add.value = true
                                        },
                                        colors =
                                            if (item is Cream)
                                                ButtonDefaults.buttonColors(
                                                    containerColor = setColors[item.color]
                                                )
                                            else
                                                ButtonDefaults.buttonColors()
                                    ) {
                                        Text(
                                            item.name
                                        )
                                    }
                                },
                                trailingButton = {
                                    SplitButtonDefaults.TrailingButton(
                                        onClick = {
                                            when (item) {
                                               is Cream -> dbAccess.deleteCream(item)
                                               is Area -> dbAccess.deleteArea(item)
                                            }
                                        },
                                        colors =
                                            when (item) {
                                                is Cream -> ButtonDefaults.buttonColors(
                                                    containerColor = setColors[item.color]
                                                )
                                                else -> ButtonDefaults.buttonColors()
                                            }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.delete),
                                            contentDescription = "Edit"
                                        )
                                    }
                                }
                            )
                        }

                    }
                } else {
                    Text("No ${title}s Found")
                }

                Button(
                    onClick = {
                        add.value = true
                    },
                    modifier = Modifier.padding(10.dp)
                ) { Text("Add") }

                AddMenu(add, title, items, dbAccess, pos)

            }
        }
    }
}