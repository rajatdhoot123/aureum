package kwiktwik.ratewatch.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.data.remote.GrowwSearchResultItem
import kwiktwik.ratewatch.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (StockQuote) -> Unit,
    onAddToWatchlist: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDetailsLoading by viewModel.isDetailsLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigateToDetail.collect { quote ->
            onNavigateToDetail(quote)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AureumBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            Surface(modifier = Modifier.fillMaxWidth(), color = AureumBg, tonalElevation = 0.dp) {
                Row(
                    modifier = Modifier.padding(16.dp).statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Search stocks, ETFs, indices...", color = Color.White.copy(alpha = 0.4f)) },
                        modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(16.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GoldAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldAccent) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldAccent,
                    trackColor = GoldAccent.copy(alpha = 0.1f)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (searchResults.isEmpty() && searchQuery.length >= 2 && !isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            Text("No results for \"$searchQuery\"", color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }
                items(searchResults, key = { it.searchId + it.isin.orEmpty() }) { result ->
                    SearchResultCard(result = result, onClick = {
                        val id = result.id ?: result.searchId
                        viewModel.fetchDetails(id)
                    })
                }
            }
        }

        // Full-screen loading overlay while fetching details
        if (isDetailsLoading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GoldAccent)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading details...", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultCard(result: GrowwSearchResultItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AureumCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                if (!result.logoUrl.isNullOrEmpty()) {
                    AsyncImage(model = result.logoUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else {
                    Text((result.entityType ?: "S").take(1).uppercase(), color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(result.title, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text("${result.entityType ?: "Stock"} • ${result.searchId}",
                    style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
            Icon(Icons.Default.ArrowBack, null, tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(16.dp).rotate(180f))
        }
    }
}
