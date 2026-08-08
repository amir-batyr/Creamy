package com.amirbatyrov.creamy.addRecord

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
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
import com.amirbatyrov.creamy.R
import com.amirbatyrov.creamy.db.Area
import com.amirbatyrov.creamy.db.Cream
import com.amirbatyrov.creamy.db.CreamOrArea
import com.amirbatyrov.creamy.ui.theme.setColors


@Composable
fun FormDropDown(
    value: MutableState<Int>,
    items: List<CreamOrArea>,
    onClick: () -> Unit,
    type: String,
    modifier: Modifier = Modifier
) {
    val selected = remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val expanded = remember { mutableStateOf(false) }
        SplitButtonDefaults.LeadingButton(
            modifier = Modifier.weight(1f),
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
            Box(modifier = Modifier.fillMaxWidth()) {
                val text = if (value.value == -1) type else items[value.value].name
                Text(text)
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = {
                        expanded.value = false
                    }
                ) {
                    for ((index, item) in items.withIndex()) {
                        when (item) {
                            is Cream ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            item.name,
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        value.value = index
                                        selected.value = true
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(color = setColors[item.color])
                                )

                            is Area -> DropdownMenuItem(
                                text = {
                                    Text(
                                        item.name,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                onClick = {
                                    value.value = index
                                    selected.value = true
                                }
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(SplitButtonDefaults.Spacing))
        SplitButtonDefaults.TrailingButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = "Edit"
            )
        }
    }
}
