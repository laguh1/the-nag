package com.thenag.ui.screens.edit

import androidx.lifecycle.SavedStateHandle
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Edit Nag screen.
 * Handles loading existing nag, form state, validation, and updating.
 */
@HiltViewModel
class EditNagViewModel @Inject constructor(
    private val repository: NagRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val nagId: Int = savedStateHandle.get<Int>("nagId") ?: -1

    private val _uiState = MutableStateFlow(EditNagUiState())
    val uiState: StateFlow<EditNagUiState> = _uiState.asStateFlow()

    init {
        if (nagId != -1) {
            loadNag()
        }
    }

    /**
     * Load the nag from database.
     */
    private fun loadNag() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val nag = repository.getNagById(nagId)
            if (nag != null) {
                val date = nag.scheduledTimestamp?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                val time = nag.scheduledTimestamp?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                }

                _uiState.update {
                    it.copy(
                        nagItem = nag,
                        name = nag.name,
                        category = nag.event,
                        selectedDate = date,
                        selectedTime = time,
                        timeRange = nag.timeRange,
                        messages = nag.completeMessages.ifEmpty { listOf("") },
                        isActive = nag.isActive,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = "Nag not found"
                    )
                }
            }
        }
    }

    /**
     * Handle user actions.
     */
    fun onAction(action: EditNagAction) {
        when (action) {
            is EditNagAction.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = action.name,
                        nameError = null
                    )
                }
            }

            is EditNagAction.CategoryChanged -> {
                _uiState.update { it.copy(category = action.category) }
            }

            is EditNagAction.DateSelected -> {
                _uiState.update {
                    it.copy(
                        selectedDate = action.date,
                        dateError = null
                    )
                }
            }

            is EditNagAction.TimeSelected -> {
                _uiState.update {
                    it.copy(
                        selectedTime = action.time,
                        timeError = null
                    )
                }
            }

            is EditNagAction.TimeRangeChanged -> {
                _uiState.update { it.copy(timeRange = action.minutes) }
            }

            is EditNagAction.MessageChanged -> {
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages[action.index] = action.text
                _uiState.update {
                    it.copy(
                        messages = updatedMessages,
                        messagesError = null
                    )
                }
            }

            EditNagAction.AddMessage -> {
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add("")
                _uiState.update { it.copy(messages = updatedMessages) }
            }

            is EditNagAction.RemoveMessage -> {
                val updatedMessages = _uiState.value.messages.toMutableList()
                if (updatedMessages.size > 1) {
                    updatedMessages.removeAt(action.index)
                    _uiState.update { it.copy(messages = updatedMessages) }
                }
            }

            is EditNagAction.ActiveToggled -> {
                _uiState.update { it.copy(isActive = action.isActive) }
            }

            EditNagAction.Save -> {
                if (validateForm()) {
                    updateNag()
                }
            }

            EditNagAction.Delete -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }

            EditNagAction.ConfirmDelete -> {
                deleteNag()
            }

            EditNagAction.CancelDelete -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
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

        // Validate messages
        val validMessages = state.messages.filter { it.isNotBlank() }
        if (validMessages.isEmpty()) {
            _uiState.update { it.copy(messagesError = "At least one message is required") }
            isValid = false
        }

        return isValid
    }

    /**
     * Update the nag in the database.
     */
    private fun updateNag() {
        val state = _uiState.value
        val existingNag = state.nagItem ?: return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val date = state.selectedDate!!
                val time = state.selectedTime!!
                val timestamp = calculateTimestamp(date, time)

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                }

                val updatedNag = existingNag.copy(
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
                    completeMessages = state.messages.filter { it.isNotBlank() },
                    updatedAt = System.currentTimeMillis()
                )

                repository.updateNag(updatedNag)

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
                        saveError = e.message ?: "Failed to update nag"
                    )
                }
            }
        }
    }

    /**
     * Delete the nag from the database.
     */
    private fun deleteNag() {
        _uiState.update { it.copy(isDeleting = true, showDeleteDialog = false) }

        viewModelScope.launch {
            try {
                repository.deleteNagById(nagId)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        saveError = e.message ?: "Failed to delete nag"
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
        _uiState.update { it.copy(saveSuccess = false, deleteSuccess = false) }
    }
}

/**
 * UI state for the Edit Nag screen.
 */
data class EditNagUiState(
    val nagItem: NagItem? = null,
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
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val showDeleteDialog: Boolean = false
)

/**
 * User actions on the Edit Nag screen.
 */
sealed interface EditNagAction {
    data class NameChanged(val name: String) : EditNagAction
    data class CategoryChanged(val category: String) : EditNagAction
    data class DateSelected(val date: LocalDate) : EditNagAction
    data class TimeSelected(val time: LocalTime) : EditNagAction
    data class TimeRangeChanged(val minutes: Int) : EditNagAction
    data class MessageChanged(val index: Int, val text: String) : EditNagAction
    object AddMessage : EditNagAction
    data class RemoveMessage(val index: Int) : EditNagAction
    data class ActiveToggled(val isActive: Boolean) : EditNagAction
    object Save : EditNagAction
    object Delete : EditNagAction
    object ConfirmDelete : EditNagAction
    object CancelDelete : EditNagAction
}
