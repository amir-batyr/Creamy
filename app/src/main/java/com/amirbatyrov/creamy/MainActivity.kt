package com.amirbatyrov.creamy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.amirbatyrov.creamy.addRecord.AddRecordDialog
import com.amirbatyrov.creamy.calendar.Calendar
import com.amirbatyrov.creamy.db.CreamyDB
import com.amirbatyrov.creamy.db.dbAccessModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreamyTheme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    CreamyDB::class.java, "Creamy"
                ).build()

                val dbAccess = dbAccessModel(db)
                dbAccess.load()
                val creams by dbAccess.creamState.collectAsState()
                val colors by dbAccess.colorState.collectAsState()
                val areas by dbAccess.areaState.collectAsState()

                val records by dbAccess.recordState.collectAsState()

                val openAddRecord = remember { mutableStateOf(false) }
                Scaffold(
                    floatingActionButton = {

                        // Add record button
                        FloatingActionButton(onClick = {
                            openAddRecord.value = true
                            dbAccess.load()
                            if (records.size > 0) { println(records[0].date.toString()) }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.add),
                                contentDescription = "Add",
                                modifier = Modifier.size(48.dp)
                            )
                        }


                    }
                ) { innerPadding ->
                    Calendar(
                        innerPadding,
                        dbAccess
                    )
                    AddRecordDialog(openAddRecord, dbAccess)
                }
            }
        }
    }
}