package com.thenag.ui.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thenag.R
import com.thenag.ui.components.DatePickerDialog
import com.thenag.ui.components.TimePickerDialog
import java.time.format.DateTimeFormatter

/**
 * Screen for editing an existing nag.
 *
 * @param nagId ID of the nag to edit
 * @param onNavigateBack Callback when user navigates back
 * @param viewModel ViewModel for the edit screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNagScreen(
    nagId: Int,
    onNavigateBack: () -> Unit,
    viewModel: EditNagViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Handle save/delete success
    LaunchedEffect(uiState.saveSuccess, uiState.deleteSuccess) {
        if (uiState.saveSuccess || uiState.deleteSuccess) {
            viewModel.clearSaveSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_nag_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onAction(EditNagAction.Delete) },
                        enabled = !uiState.isDeleting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.loadError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.loadError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name field
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onAction(EditNagAction.NameChanged(it)) },
                        label = { Text(stringResource(R.string.nag_name_label)) },
                        placeholder = { Text(stringResource(R.string.nag_name_hint)) },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Category dropdown
                    CategoryDropdown(
                        selectedCategory = uiState.category,
                        onCategorySelected = { viewModel.onAction(EditNagAction.CategoryChanged(it)) }
                    )

                    // Date picker button
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                ?: stringResource(R.string.nag_date_label)
                        )
                    }
                    if (uiState.dateError != null) {
                        Text(
                            text = uiState.dateError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Time picker button
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.selectedTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                ?: stringResource(R.string.nag_time_label)
                        )
                    }
                    if (uiState.timeError != null) {
                        Text(
                            text = uiState.timeError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Time range slider
                    Column {
                        Text(
                            text = "${stringResource(R.string.nag_time_range_label)}: ${uiState.timeRange} minutes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = uiState.timeRange.toFloat(),
                            onValueChange = { viewModel.onAction(EditNagAction.TimeRangeChanged(it.toInt())) },
                            valueRange = 5f..180f,
                            steps = 34
                        )
                    }

                    // Messages section
                    Text(
                        text = stringResource(R.string.nag_messages_label),
                        style = MaterialTheme.typography.titleMedium
                    )

                    uiState.messages.forEachIndexed { index, message ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            OutlinedTextField(
                                value = message,
                                onValueChange = {
                                    viewModel.onAction(EditNagAction.MessageChanged(index, it))
                                },
                                label = { Text("Message ${index + 1}") },
                                modifier = Modifier.weight(1f),
                                maxLines = 3
                            )
                            if (uiState.messages.size > 1) {
                                IconButton(
                                    onClick = { viewModel.onAction(EditNagAction.RemoveMessage(index)) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove message"
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.messagesError != null) {
                        Text(
                            text = uiState.messagesError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Add message button
                    OutlinedButton(
                        onClick = { viewModel.onAction(EditNagAction.AddMessage) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_message))
                    }

                    // Active toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.nag_active_label),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = uiState.isActive,
                            onCheckedChange = { viewModel.onAction(EditNagAction.ActiveToggled(it)) }
                        )
                    }

                    // Error message
                    if (uiState.saveError != null) {
                        Text(
                            text = uiState.saveError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Save button
                    Button(
                        onClick = { viewModel.onAction(EditNagAction.Save) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving && !uiState.isDeleting
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.onAction(EditNagAction.DateSelected(date))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                viewModel.onAction(EditNagAction.TimeSelected(time))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = { viewModel.onAction(EditNagAction.ConfirmDelete) },
            onDismiss = { viewModel.onAction(EditNagAction.CancelDelete) }
        )
    }
}

/**
 * Delete confirmation dialog.
 */
@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_nag_title)) },
        text = { Text(stringResource(R.string.delete_nag_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.delete_nag_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Dropdown menu for selecting category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "Work",
        "Personal",
        "Health",
        "Finance",
        "Social"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.nag_category_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
