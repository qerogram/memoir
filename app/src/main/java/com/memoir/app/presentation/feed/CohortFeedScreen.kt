package com.memoir.app.presentation.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.memoir.app.R

/**
 * Cohort Feed screen (placeholder for MVP)
 */
@Composable
fun CohortFeedScreen(
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.feed_welcome),
            style = MaterialTheme.typography.displayMedium
        )

        Button(
            onClick = onLogout,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(text = stringResource(R.string.feed_logout))
        }
    }
}
