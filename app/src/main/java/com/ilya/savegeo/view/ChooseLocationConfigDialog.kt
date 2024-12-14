package com.ilya.savegeo.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilya.savegeo.location_tracking.LocationConfig

@Composable
fun ChoiceDialog(
    showDialog: Boolean,
    onConfirm: (LocationConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val options = remember { LocationConfig.entries.toList() }
    var selectedOption by remember { mutableStateOf(LocationConfig.HIGH_ACCURACY) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Выберите опцию") },
            text = {
                Column {
                    Text("Пожалуйста, выберите один из вариантов записи геолокации:")
                    Spacer(modifier = Modifier.height(8.dp))

                    options.forEach { option ->
                        RadioButtonWithLabel(option, selectedOption) { selectedOption = it }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(selectedOption)
                    },
                ) {
                    Text("ОК")
                }
            },
        )
    }
}

@Composable
fun RadioButtonWithLabel(
    label: LocationConfig,
    selectedOption: LocationConfig,
    onSelect: (LocationConfig) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedOption == label,
            onClick = { onSelect(label) }
        )
        Text(text = stringResource(label.title))
    }
}
