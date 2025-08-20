package com.axesoft.autoargsdemo.composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axesoft.autoargsdemo.navigation.FirstScreenArgs

@Composable
fun FirstScreen(
    args: FirstScreenArgs,
    onContinue: (String) -> Unit
) {
    var doctorName by remember { mutableStateOf(args.userName) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("User ID: ${args.userId}")
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = doctorName,
            onValueChange = { doctorName = it },
            label = { Text("Enter doctor's name") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onContinue(doctorName) }) {
            Text("Continue")
        }
    }
}
