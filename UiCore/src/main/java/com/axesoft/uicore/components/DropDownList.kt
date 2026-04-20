package com.axesoft.uicore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T> DropDownList(
    text: String,
    variants: List<T>,
    variantNames: List<String>,
    backgroundColor: Color = Color.LightGray,
    dropdownColor: Color = Color.Cyan,
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(8.dp),
    buttonTextColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    isDropDownAlignedToButton: Boolean = true,
    isEnabled: Boolean = true,
    dropdownButtonLabelTypograph: TextStyle = MaterialTheme.typography.labelLarge,
    onSelect: (T) -> Unit
) where T : Any {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    buttonWidth = coordinates.size.width
                }
                .shadow(6.dp, roundedCornerShape)
                .clip(roundedCornerShape)
                .background(
                    if (isEnabled) backgroundColor else Color.LightGray
                )
                .height(55.dp)
                .then(
                    if (isEnabled) {
                        Modifier.clickable { expanded = true }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = buttonTextColor,
                style = dropdownButtonLabelTypograph,
                textAlign = TextAlign.Center
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = if (isDropDownAlignedToButton) {
                Modifier.width(with(density) { buttonWidth.toDp() })
            } else {
                Modifier
            },
            containerColor = dropdownColor,
            shape = roundedCornerShape
        ) {
            variants.forEachIndexed { index, variant ->
                DropdownMenuItem(
                    text = { Text(variantNames[index], color = textColor) },
                    onClick = {
                        onSelect(variant)
                        expanded = false
                    }
                )
            }
        }
    }
}
