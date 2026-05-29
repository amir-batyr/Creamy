package com.amirbatyrov.creamy

import android.R
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ColorPickerDialog(
    openDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    color: MutableState<Int>
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
                Text("Color", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
                LazyVerticalGrid(columns = GridCells.Fixed(5)) {
                    items(20) { index ->
                        Button(
                            onClick = { color.value = index },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(5.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors[index]),
                            border = if (color.value == index) {
                                BorderStroke(color = Color.White, width = 2.dp)
                            } else {
                                BorderStroke(color = Color.White, width = 0.dp)
                            }
                        ) {}
                    }
                }
                Button(onClick = {openDialog.value = false}) { Text("Select") }
            }

        }
    }
}