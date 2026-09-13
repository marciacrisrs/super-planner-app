package com.superplanner.app.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superplanner.app.R

@Composable
fun SuperPlannerLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 52.dp,
    showTagline: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_super_planner_logo),
            contentDescription = "Super Planner",
            modifier = Modifier.width(iconSize).height(iconSize),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "SUPER",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 1.2.sp,
                lineHeight = 20.sp,
            )
            Text(
                text = "Planner",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Cursive,
                fontSize = 24.sp,
                lineHeight = 24.sp,
            )
            if (showTagline) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Planeje. Foque. Realize.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                )
            }
        }
    }
}
