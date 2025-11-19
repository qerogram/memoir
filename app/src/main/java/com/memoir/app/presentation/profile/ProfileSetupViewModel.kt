package com.memoir.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memoir.app.domain.model.IndustryCode
import com.memoir.app.domain.model.UserProfile
import com.memoir.app.domain.usecase.SaveProfileUseCase
import com.memoir.app.util.Result
import com.memoir.app.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile Setup screen
 */
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val saveProfileUseCase: SaveProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupState())
    val state: StateFlow<ProfileSetupState> = _state.asStateFlow()

    fun updateName(name: String) {
        val validation = ValidationUtils.validateKoreanName(name)
        _state.update {
            it.copy(
                name = name,
                nameError = validation.errorOrNull()
            )
        }
        validateForm()
    }

    fun updateRole(role: String) {
        val validation = ValidationUtils.validateRole(role)
        _state.update {
            it.copy(
                role = role,
                roleError = validation.errorOrNull()
            )
        }
        validateForm()
    }

    fun updateIndustry(industryCode: IndustryCode) {
        _state.update {
            it.copy(
                industry = industryCode,
                industryError = null
            )
        }
        validateForm()
    }

    fun updateGrowthGoals(goals: String) {
        val validation = ValidationUtils.validateGrowthGoals(goals)
        _state.update {
            it.copy(
                growthGoals = goals,
                growthGoalsError = validation.errorOrNull()
            )
        }
        validateForm()
    }

    fun toggleTos() {
        _state.update { it.copy(tosAccepted = !it.tosAccepted) }
        validateForm()
    }

    fun togglePrivacy() {
        _state.update { it.copy(privacyAccepted = !it.privacyAccepted) }
        validateForm()
    }

    fun toggleDeposit() {
        _state.update { it.copy(depositAccepted = !it.depositAccepted) }
        validateForm()
    }

    private fun validateForm() {
        val currentState = _state.value
        val isValid = currentState.nameError == null &&
                currentState.roleError == null &&
                currentState.industryError == null &&
                currentState.growthGoalsError == null &&
                currentState.name.isNotBlank() &&
                currentState.role.isNotBlank() &&
                currentState.growthGoals.isNotBlank() &&
                currentState.tosAccepted &&
                currentState.privacyAccepted &&
                currentState.depositAccepted

        _state.update { it.copy(isFormValid = isValid) }
    }

    fun submitProfile(onSuccess: () -> Unit) {
        val currentState = _state.value
        if (!currentState.isFormValid) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }

            val profile = UserProfile(
                userId = "", // Will be set by backend
                name = currentState.name,
                role = currentState.role,
                industryCode = currentState.industry ?: IndustryCode.OTHER,
                growthGoals = currentState.growthGoals
            )

            val result = saveProfileUseCase(profile)

            _state.update {
                it.copy(
                    isSubmitting = false,
                    submitError = if (result is Result.Error) result.message else null
                )
            }

            if (result is Result.Success) {
                onSuccess()
            }
        }
    }
}

data class ProfileSetupState(
    val name: String = "",
    val nameError: String? = null,
    val role: String = "",
    val roleError: String? = null,
    val industry: IndustryCode? = null,
    val industryError: String? = null,
    val growthGoals: String = "",
    val growthGoalsError: String? = null,
    val tosAccepted: Boolean = false,
    val privacyAccepted: Boolean = false,
    val depositAccepted: Boolean = false,
    val isFormValid: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
)
