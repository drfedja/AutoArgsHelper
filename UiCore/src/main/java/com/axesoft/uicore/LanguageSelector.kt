package com.axesoft.uicore

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier,
    availableLocales: List<Locale>,
    currentLocale: Locale,
    onLocaleChange: (Locale) -> Unit,
    buttonBackgroundColor: @Composable (isSelected: Boolean) -> Color = { isSelected ->
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    },
    textColor: @Composable (isSelected: Boolean) -> Color = { isSelected ->
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    },
    style: TextStyle = LocalTextStyle.current,
    outlineColor: Color = MaterialTheme.colorScheme.outline,
    roundedCorner: RoundedCornerShape = RoundedCornerShape(0.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(roundedCorner)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                outlineColor,
                roundedCorner
            )
    ) {
        availableLocales.forEach { locale ->
            val isSelected = locale == currentLocale
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLocaleChange(locale) }
                    .background(buttonBackgroundColor(isSelected))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = locale.displayLanguage,
                    color = textColor(isSelected),
                    style = style
                )
            }
        }
    }
}

@SuppressLint("LocalContextConfigurationRead", "ComposableNaming")
@Composable
fun Locale.LocalizedString(
    resId: Int,
    vararg formatArgs: Any
): String {
    if (LocalInspectionMode.current) {
        return stringResource(resId, *formatArgs)
    }

    val context = LocalContext.current

    val config = Configuration(context.resources.configuration).apply {
        setLocale(this@LocalizedString)
    }

    val localizedContext = context.createConfigurationContext(config)

    return localizedContext.resources.getString(resId, *formatArgs)
}

@Composable
fun customButtonColorSelector(
    selectedColor: Color,
    unselectedColor: Color
): @Composable (Boolean) -> Color = remember(selectedColor, unselectedColor) {
    { isSelected ->
        if (isSelected) selectedColor else unselectedColor
    }
}

@Composable
fun customButtonTextColorSelector(
    selectedColor: Color,
    unselectedColor: Color
): @Composable (Boolean) -> Color = remember(selectedColor, unselectedColor) {
    { isSelected ->
        if (isSelected) selectedColor else unselectedColor
    }
}


@Preview
@Composable
private fun PreviewLanguageSelector() {
    LanguageSelector(
        availableLocales = listOf(
            Locale.forLanguageTag("en"),
            Locale.forLanguageTag("de")
        ),
        currentLocale = Locale.forLanguageTag("en"),
        buttonBackgroundColor = customButtonColorSelector(
            selectedColor = Color(0xFF2979FF),
            unselectedColor = Color.Transparent
        ),
        textColor = customButtonTextColorSelector(
            selectedColor = Color.White,
            unselectedColor = Color(0xFF2979FF)
        ),
        onLocaleChange = {
        }
    )
}
