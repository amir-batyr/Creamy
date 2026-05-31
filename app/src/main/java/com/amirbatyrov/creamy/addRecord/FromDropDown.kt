package com.amirbatyrov.creamy.addRecord

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.amirbatyrov.creamy.R
import com.amirbatyrov.creamy.ui.theme.setColors

@Composable
fun FormDropDown(
    value: MutableState<String>,
    items: List<String>,
    colors: List<Int>? = null,
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
                        for (i in 0 until items.size) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        items[i],
                                        color = if (colors != null) Color.White else MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                onClick = {
                                    value.value = items[i]
                                    selected.value = true
                                },
                                modifier = if (colors != null) Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color = setColors[colors[i]]) else Modifier
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