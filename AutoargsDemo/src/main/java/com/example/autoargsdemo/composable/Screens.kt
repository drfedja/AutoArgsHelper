package com.example.autoargsdemo.composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.autoargsdemo.navigation.FirstScreenArgs
import com.example.autoargsdemo.navigation.SecondScreenArgs

@Composable
fun FirstScreen(
    args: FirstScreenArgs,
    onContinue: (String) -> Unit
) {
    var name by remember { mutableStateOf(args.userName) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("User ID: ${args.userId}")
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Enter your name") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onContinue(name) }) {
            Text("Continue")
        }
    }
}

@Composable
fun SecondScreen(args: SecondScreenArgs) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Second screen received:")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Name: ${args.userName}")
        Text("ID: ${args.userId}")
    }
}