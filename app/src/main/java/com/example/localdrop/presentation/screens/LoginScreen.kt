package com.example.localdrop.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LoginScreen(
    onNameEntered : (String) -> Unit
){
    var inputName by remember { mutableStateOf("") }

    Column(){
        OutlinedTextField(
            value = inputName,
            onValueChange = {inputName = it}
        )

        Button(
            onClick = { onNameEntered(inputName) }
        ){
            Text(text = "Войти в сеть")
        }
    }
}