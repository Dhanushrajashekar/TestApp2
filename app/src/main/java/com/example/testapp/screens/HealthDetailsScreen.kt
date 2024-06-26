package com.example.testapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.testapp.viewmodel.StepCounterViewModel
import kotlinx.coroutines.launch

@Composable
fun HealthDetailsScreen(navController: NavController, viewModel: StepCounterViewModel) {

    var selectedAge by remember { mutableStateOf<String?>(null) }
    var isAgeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedHeight by remember { mutableStateOf<String?>(null) }
    var isHeightDropdownExpanded by remember { mutableStateOf(false) }
    var selectedWeight by remember { mutableStateOf<String?>(null) }
    var isWeightDropdownExpanded by remember { mutableStateOf(false) }
    var selectedSex by remember { mutableStateOf<String?>(null) }
    var isSexDropdownExpanded by remember { mutableStateOf(false) }
    var weightUnit by remember { mutableStateOf("kg") }
    var heightUnit by remember { mutableStateOf("cm") }
    val coroutineScope = rememberCoroutineScope()

    val ageOptions = (18..100).map { it.toString() }
    val heightOptions = (30..275).map { it.toString() }
    val weightOptions = (1..454).map { it.toString() }
    val sexOptions = listOf("Male", "Female")
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Set Health Details",
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                backgroundColor = Color(0xff4a6572)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Health Details", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(16.dp))
            // Age selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAgeDropdownExpanded = true }
                    .border(
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                        shape = MaterialTheme.shapes.small)
            ) {
                Text(
                    text = selectedAge ?: "Select Age",
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
                DropdownMenu(
                    expanded = isAgeDropdownExpanded,
                    onDismissRequest = { isAgeDropdownExpanded = false }
                ) {
                    ageOptions.forEach { age ->
                        DropdownMenuItem(onClick = {
                            selectedAge = age
                            isAgeDropdownExpanded = false
                        }) {
                            Text(age)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isHeightDropdownExpanded = true }
                    .border(
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                        shape = MaterialTheme.shapes.small)
            ) {
                Text(
                    text = buildHeightString(selectedHeight, heightUnit),
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
                DropdownMenu(
                    expanded = isHeightDropdownExpanded,
                    onDismissRequest = { isHeightDropdownExpanded = false }
                ) {
                    heightOptions.forEach { height ->
                        DropdownMenuItem(onClick = {
                            selectedHeight = height
                            isHeightDropdownExpanded = false
                        }) {
                            Text(height)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .clickable { isWeightDropdownExpanded = true }
                        .border(
                            border = BorderStroke(width = 1.dp, color = Color.Gray),
                            shape = MaterialTheme.shapes.small)
                ) {
                    Text(
                        text = buildWeightString(selectedWeight, weightUnit),
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    )
                    DropdownMenu(
                        expanded = isWeightDropdownExpanded,
                        onDismissRequest = { isWeightDropdownExpanded = false }
                    ) {
                        weightOptions.forEach { weight ->
                            DropdownMenuItem(onClick = {
                                selectedWeight = weight
                                isWeightDropdownExpanded = false
                            }) {
                                Text(weight)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = weightUnit == "kg",
                        onCheckedChange = { weightUnit = if (it) "kg" else "lbs" },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("kg")

                    Spacer(modifier = Modifier.width(8.dp))

                    Checkbox(
                        checked = weightUnit == "lbs",
                        onCheckedChange = { weightUnit = if (it) "lbs" else "kg" },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("lbs")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(

                modifier = Modifier
                    .border(
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                        shape = MaterialTheme.shapes.small)
                    .fillMaxWidth()
                    .clickable { isSexDropdownExpanded = true }
            ) {
                Text(
                    text = selectedSex ?: "Select Sex",
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
                DropdownMenu(
                    expanded = isSexDropdownExpanded,
                    onDismissRequest = { isSexDropdownExpanded = false }
                ) {
                    sexOptions.forEach { sex ->
                        DropdownMenuItem(onClick = {
                            selectedSex = sex
                            isSexDropdownExpanded = false
                        }) {
                            Text(sex)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    val age = selectedAge?.toIntOrNull() ?: 0
                    val height = selectedHeight?.toDoubleOrNull() ?: 0.0
                    val weight = selectedWeight?.toDoubleOrNull() ?: 0.0
                    coroutineScope.launch {
                        viewModel.setHealthDetails(
                            height,
                            weight,
                            age,
                            selectedSex ?: ""
                        )
                    }
                    navController.navigateUp()
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(0.dp,8.dp),
                    shape = RoundedCornerShape(24),
                    colors = androidx.compose.material.ButtonDefaults.buttonColors(Color(0xfff9aa33))
                ) {
                    Text("Save", fontSize = 20.sp)
                }
            }
        }
    }

}

private fun buildWeightString(weight: String?, unit: String): String {
    val weightValue = weight?.toDoubleOrNull() ?: 0.0
    val convertedWeight = if (unit == "kg") {
        convertKgToLbs(weightValue)
    } else {
        convertLbsToKg(weightValue)
    }
    return String.format("%.2f", weightValue) + " $unit" + " (${String.format("%.2f", convertedWeight)} ${if (unit == "kg") "lbs" else "kg"})"
}

private fun buildHeightString(height: String?, unit: String): String {
    val heightValue = height?.toDoubleOrNull() ?: 0.0
    val convertedHeight = if (unit == "cm") {
        convertCmToFeet(heightValue)
    } else {
        convertFeetToCm(heightValue)
    }
    return String.format("%.2f", heightValue) + " $unit" + " (${String.format("%.2f", convertedHeight)} ${if (unit == "cm") "feet" else "cm"})"
}


private fun convertKgToLbs(kg: Double): Double {
    return kg * 2.20462
}

private fun convertLbsToKg(lbs: Double): Double {
    return lbs / 2.20462
}

private fun convertCmToFeet(cm: Double): Double {
    return cm * 0.0328084
}

private fun convertFeetToCm(feet: Double): Double {
    return feet / 0.0328084
}
