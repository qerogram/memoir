package com.memoir.app.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.memoir.app.R
import com.memoir.app.domain.model.IndustryCode

/**
 * Profile Setup screen (screen 5)
 */
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onProfileSubmitted: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name field
        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text(stringResource(R.string.profile_name_label)) },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role field
        OutlinedTextField(
            value = state.role,
            onValueChange = { viewModel.updateRole(it) },
            label = { Text(stringResource(R.string.profile_role_label)) },
            isError = state.roleError != null,
            supportingText = state.roleError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Industry dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = state.industry?.let { stringResource(it.displayNameResId) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.profile_industry_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                IndustryCode.values().forEach { industryCode ->
                    DropdownMenuItem(
                        text = { Text(stringResource(industryCode.displayNameResId)) },
                        onClick = {
                            viewModel.updateIndustry(industryCode)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Growth goals field
        OutlinedTextField(
            value = state.growthGoals,
            onValueChange = { viewModel.updateGrowthGoals(it) },
            label = { Text(stringResource(R.string.profile_growth_goals_label)) },
            isError = state.growthGoalsError != null,
            supportingText = state.growthGoalsError?.let { { Text(it) } } ?: {
                Text(stringResource(R.string.profile_growth_goals_counter, state.growthGoals.length))
            },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Legal checkboxes
        CheckboxWithLabel(
            checked = state.tosAccepted,
            onCheckedChange = { viewModel.toggleTos() },
            label = stringResource(R.string.profile_tos_label)
        )

        CheckboxWithLabel(
            checked = state.privacyAccepted,
            onCheckedChange = { viewModel.togglePrivacy() },
            label = stringResource(R.string.profile_privacy_label)
        )

        CheckboxWithLabel(
            checked = state.depositAccepted,
            onCheckedChange = { viewModel.toggleDeposit() },
            label = stringResource(R.string.profile_deposit_label)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        state.submitError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Submit button
        Button(
            onClick = { viewModel.submitProfile(onProfileSubmitted) },
            enabled = state.isFormValid && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.profile_submit_button))
            }
        }
    }
}

@Composable
fun CheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
