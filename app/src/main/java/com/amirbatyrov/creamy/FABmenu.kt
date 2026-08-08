package com.amirbatyrov.creamy

import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirbatyrov.creamy.db.DBAccessModel
import com.amirbatyrov.creamy.db.Record
import com.amirbatyrov.creamy.ui.theme.setColors
import java.time.LocalDate

@Composable
fun FABmenu(
    open: MutableState<Boolean>,
    dbAccess: DBAccessModel,
    openAddRecord: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val commonRecords = dbAccess.commonRecordsState.collectAsStateWithLifecycle()
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = open.value,
        button = {
            ToggleFloatingActionButton(
                checked = open.value,
                onCheckedChange = { open.value = !open.value }) {
                val imageVector =
                    remember { derivedStateOf { if (checkedProgress > 0.5f) R.drawable.close else R.drawable.add } }
                Icon(
                    painter = painterResource(imageVector.value),
                    contentDescription = "Add/Close",
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        }
    ) {
        if (open.value && commonRecords.value.isNotEmpty()) {
            val max = if (10 > commonRecords.value.size) commonRecords.value.size else 10
            for (i in 0 until max) {
                val record = commonRecords.value[i]
                FloatingActionButtonMenuItem(
                    onClick = {
                        dbAccess.insert(
                            Record(
                                record.cid, record.aid, LocalDate.now()
                            )
                        )
                        open.value = false
                    }, text = { Text(record.cream + " - " + record.area) }, icon = {
                        Icon(
                            painter = painterResource(R.drawable.add), contentDescription = "Add"
                        )
                    },
                    containerColor = setColors[record.color],
                    contentColor = Color.White
                )
            }

        }
        FloatingActionButtonMenuItem(
            onClick = {
                openAddRecord.value = true
                open.value = false
            },
            text = { Text("Add New") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.add), contentDescription = "Add"
                )
            },
        )
    }
}