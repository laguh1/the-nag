package com.thenag.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenag.data.local.entity.NagItem
import com.thenag.data.repository.NagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Create Nag screen.
 * Handles form state, validation, and saving to database.
 */
@HiltViewModel
class CreateNagViewModel @Inject constructor(
    private val repository: NagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNagUiState())
    val uiState: StateFlow<CreateNagUiState> = _uiState.asStateFlow()

    /**
     * Handle user actions.
     */
    fun onAction(action: CreateNagAction) {
        when (action) {
            is CreateNagAction.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = action.name,
                        nameError = null
                    )
                }
            }

            is CreateNagAction.CategoryChanged -> {
                _uiState.update { it.copy(category = action.category) }
            }

            is CreateNagAction.DateSelected -> {
                _uiState.update {
                    it.copy(
                        selectedDate = action.date,
                        dateError = null
                    )
                }
            }

            is CreateNagAction.TimeSelected -> {
                _uiState.update {
                    it.copy(
                        selectedTime = action.time,
                        timeError = null
                    )
                }
            }

            is CreateNagAction.TimeRangeChanged -> {
                _uiState.update { it.copy(timeRange = action.minutes) }
            }

            is CreateNagAction.MessageChanged -> {
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages[action.index] = action.text
                _uiState.update {
                    it.copy(
                        messages = updatedMessages,
                        messagesError = null
                    )
                }
            }

            CreateNagAction.AddMessage -> {
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add("")
                _uiState.update { it.copy(messages = updatedMessages) }
            }

            is CreateNagAction.RemoveMessage -> {
                val updatedMessages = _uiState.value.messages.toMutableList()
                if (updatedMessages.size > 1) {
                    updatedMessages.removeAt(action.index)
                    _uiState.update { it.copy(messages = updatedMessages) }
                }
            }

            is CreateNagAction.ActiveToggled -> {
                _uiState.update { it.copy(isActive = action.isActive) }
            }

            CreateNagAction.Save -> {
                if (validateForm()) {
                    saveNag()
                }
            }
        }
    }

    /**
     * Validate the form and update error states.
     */
    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        // Validate name
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            isValid = false
        }

        // Validate date
        if (state.selectedDate == null) {
            _uiState.update { it.copy(dateError = "Please select a date") }
            isValid = false
        }

        // Validate time
        if (state.selectedTime == null) {
            _uiState.update { it.copy(timeError = "Please select a time") }
            isValid = false
        }

        // Validate that date/time is in the future
        if (state.selectedDate != null && state.selectedTime != null) {
            val scheduledTimestamp = calculateTimestamp(state.selectedDate, state.selectedTime)
            if (scheduledTimestamp <= System.currentTimeMillis()) {
                _uiState.update { it.copy(dateError = "Date and time must be in the future") }
                isValid = false
            }
        }

        // Validate messages
        val validMessages = state.messages.filter { it.isNotBlank() }
        if (validMessages.isEmpty()) {
            _uiState.update { it.copy(messagesError = "At least one message is required") }
            isValid = false
        }

        return isValid
    }

    /**
     * Save the nag to the database.
     */
    private fun saveNag() {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val date = state.selectedDate!!
                val time = state.selectedTime!!
                val timestamp = calculateTimestamp(date, time)

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                }

                val nag = NagItem(
                    name = state.name.trim(),
                    event = state.category,
                    timeRange = state.timeRange,
                    isActive = state.isActive,
                    isDateSet = true,
                    year = date.year,
                    month = date.monthValue,
                    weekInYear = calendar.get(Calendar.WEEK_OF_YEAR),
                    dayInYear = date.dayOfYear,
                    hour = time.hour,
                    minute = time.minute,
                    scheduledTimestamp = timestamp,
                    completeMessages = state.messages.filter { it.isNotBlank() }
                )

                repository.insertNag(nag)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = e.message ?: "Failed to save nag"
                    )
                }
            }
        }
    }

    /**
     * Calculate Unix timestamp from LocalDate and LocalTime.
     */
    private fun calculateTimestamp(date: LocalDate, time: LocalTime): Long {
        return date.atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Clear save success state after navigation.
     */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}

/**
 * UI state for the Create Nag screen.
 */
data class CreateNagUiState(
    val name: String = "",
    val category: String = "Personal",
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val timeRange: Int = 60,
    val messages: List<String> = listOf(""),
    val isActive: Boolean = true,

    // Errors
    val nameError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val messagesError: String? = null,

    // Loading states
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

/**
 * User actions on the Create Nag screen.
 */
sealed interface CreateNagAction {
    data class NameChanged(val name: String) : CreateNagAction
    data class CategoryChanged(val category: String) : CreateNagAction
    data class DateSelected(val date: LocalDate) : CreateNagAction
    data class TimeSelected(val time: LocalTime) : CreateNagAction
    data class TimeRangeChanged(val minutes: Int) : CreateNagAction
    data class MessageChanged(val index: Int, val text: String) : CreateNagAction
    object AddMessage : CreateNagAction
    data class RemoveMessage(val index: Int) : CreateNagAction
    data class ActiveToggled(val isActive: Boolean) : CreateNagAction
    object Save : CreateNagAction
}
