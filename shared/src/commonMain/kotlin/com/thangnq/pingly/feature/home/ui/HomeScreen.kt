package com.thangnq.pingly.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thangnq.pingly.feature.home.presentation.HomeEvent
import com.thangnq.pingly.feature.home.presentation.HomeInputError
import com.thangnq.pingly.feature.home.presentation.HomePingItemUi
import com.thangnq.pingly.feature.home.presentation.HomeUiState
import com.thangnq.pingly.ui.theme.PinglyTheme
import org.jetbrains.compose.resources.stringResource
import pingly.shared.generated.resources.Res
import pingly.shared.generated.resources.add_ping
import pingly.shared.generated.resources.app_subtitle
import pingly.shared.generated.resources.app_title
import pingly.shared.generated.resources.empty_body
import pingly.shared.generated.resources.empty_title
import pingly.shared.generated.resources.input_empty
import pingly.shared.generated.resources.input_too_long
import pingly.shared.generated.resources.load_failed
import pingly.shared.generated.resources.loading
import pingly.shared.generated.resources.message_hint
import pingly.shared.generated.resources.new_ping
import pingly.shared.generated.resources.retry
import pingly.shared.generated.resources.save_failed
import pingly.shared.generated.resources.your_pings

@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "intro", contentType = "intro") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.app_title), style = MaterialTheme.typography.headlineLarge)
                Text(
                    stringResource(Res.string.app_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item(key = "composer", contentType = "composer") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = { onEvent(HomeEvent.DraftChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.new_ping)) },
                    placeholder = { Text(stringResource(Res.string.message_hint)) },
                    minLines = 2,
                    maxLines = 4,
                    enabled = !state.isSubmitting,
                    isError = state.inputError != null,
                )
                val error = state.inputError
                if (error != null) {
                    Text(
                        when (error) {
                            HomeInputError.Empty -> stringResource(Res.string.input_empty)
                            is HomeInputError.TooLong -> stringResource(
                                Res.string.input_too_long,
                                error.maxLength,
                            )
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item(key = "submit", contentType = "action") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = { onEvent(HomeEvent.Submit) },
                    enabled = !state.isSubmitting,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(Res.string.add_ping))
                }
            }
        }

        if (state.saveError) {
            item(key = "save-error", contentType = "message") {
                Text(
                    stringResource(Res.string.save_failed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item(key = "section-title", contentType = "title") {
            Text(stringResource(Res.string.your_pings), style = MaterialTheme.typography.titleLarge)
        }

        if (state.loadError) {
            item(key = "load-error", contentType = "message") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(Res.string.load_failed),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onEvent(HomeEvent.RetryLoad) }) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }
        }

        when {
            state.isLoading && state.items.isEmpty() -> item(key = "loading", contentType = "message") {
                CenterMessage(stringResource(Res.string.loading))
            }
            state.items.isEmpty() && !state.loadError -> item(key = "empty", contentType = "message") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(Res.string.empty_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(Res.string.empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> items(state.items, key = { it.id }, contentType = { "ping" }) { item ->
                PingCard(item)
            }
        }
    }
}

@Composable
private fun CenterMessage(message: String) {
    Text(
        message,
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PingCard(item: HomePingItemUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            item.message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    PinglyTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                items = listOf(HomePingItemUi(1, "Plan the next ping")),
            ),
            onEvent = {},
        )
    }
}
