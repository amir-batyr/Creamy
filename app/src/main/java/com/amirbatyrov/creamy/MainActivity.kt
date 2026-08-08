package com.amirbatyrov.creamy

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.amirbatyrov.creamy.addRecord.AddRecordDialog
import com.amirbatyrov.creamy.calendar.Calendar
import com.amirbatyrov.creamy.db.CreamyDB
import com.amirbatyrov.creamy.db.DBAccessModel
import com.amirbatyrov.creamy.ui.theme.CreamyTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreamyTheme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    CreamyDB::class.java, "Creamy"
                ).build()

                val dbAccess = DBAccessModel(db)
                val openAddRecord = remember { mutableStateOf(false) }
                val FABopen = remember { mutableStateOf(false) }
                Scaffold(
                    floatingActionButton = {
                        FABmenu(
                            FABopen,
                            dbAccess,
                            openAddRecord,
                            modifier = Modifier.offset(x = 16.dp, y = 16.dp)
                        )
                    }
                ) { innerPadding ->
                    Calendar(
                        innerPadding,
                        dbAccess
                    )
                    AddRecordDialog(openAddRecord, dbAccess)
                    AnimatedVisibility(
                        visible = FABopen.value,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .pointerInput(Unit) { detectTapGestures { } }) {

                        }
                    }
                }
            }
        }
    }
}