package com.amirbatyrov.creamy.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun DropDownMenu(
    value: MutableState<String>,
    items: Array<String>,
    modifier: Modifier = Modifier,
) {
    val expanded = remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        OutlinedButton(onClick = { expanded.value = !expanded.value }) { Text(value.value) }
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
                        println(option)
                    }
                )
            }
        }
    }
}