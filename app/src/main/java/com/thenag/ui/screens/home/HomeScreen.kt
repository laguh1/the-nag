package com.thenag.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thenag.R
import com.thenag.data.local.entity.NagItem
import com.thenag.ui.theme.TheNagTheme

/**
 * Home screen showing the list of nags.
 *
 * @param onNavigateToCreate Callback when user wants to create a new nag
 * @param onNavigateToEdit Callback when user wants to edit a nag
 * @param onNavigateToStats Callback when user wants to view statistics
 * @param viewModel ViewModel for the home screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_more_options)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_nag)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (showSearchBar) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onAction(HomeAction.SearchQueryChanged(it)) },
                    placeholder = { Text(stringResource(R.string.home_search_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
            }

            // Filter chips
            FilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.onAction(HomeAction.FilterSelected(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content
            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.onAction(HomeAction.ClearError) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.filteredNags.isEmpty() -> {
                    EmptyState(modifier = Modifier.fillMaxSize())
                }

                else -> {
                    NagList(
                        nags = uiState.filteredNags,
                        onNagClick = onNavigateToEdit,
                        onToggleActive = { nagId ->
                            viewModel.onAction(HomeAction.ToggleActive(nagId))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Loading state indicator.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state display.
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty state display.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.home_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

/**
 * List of nags.
 */
@Composable
private fun NagList(
    nags: List<NagItem>,
    onNagClick: (Int) -> Unit,
    onToggleActive: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nags, key = { it.id }) { nag ->
            NagListItem(
                nag = nag,
                onClick = { onNagClick(nag.id) },
                onToggleActive = { onToggleActive(nag.id) }
            )
        }
    }
}

/**
 * Individual nag list item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NagListItem(
    nag: NagItem,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nag.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nag.event,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (nag.count > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Triggered ${nag.count} time${if (nag.count != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Switch(
                checked = nag.isActive,
                onCheckedChange = { onToggleActive() }
            )
        }
    }
}

/**
 * Filter chips for filtering nags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == FilterType.ALL,
                onClick = { onFilterSelected(FilterType.ALL) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == FilterType.ACTIVE,
                onClick = { onFilterSelected(FilterType.ACTIVE) },
                label = { Text(stringResource(R.string.filter_active)) }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == FilterType.INACTIVE,
                onClick = { onFilterSelected(FilterType.INACTIVE) },
                label = { Text(stringResource(R.string.filter_inactive)) }
            )
        }
    }
}

/**
 * Preview for HomeScreen.
 */
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TheNagTheme {
        Surface {
            EmptyState()
        }
    }
}
