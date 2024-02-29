package tcnguyen.app.landmarkremark.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tcnguyen.app.landmarkremark.RootViewModel
import tcnguyen.app.landmarkremark.ui.components.AppSearchBar
import tcnguyen.app.landmarkremark.ui.components.CardNoteItem

@Composable
fun NotesScreenContent(modifier: Modifier, viewModel: RootViewModel) {
    val locationNotes by viewModel.locationNotesQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    if (isSearching) {
        Box(modifier = modifier) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                AppSearchBar(
                    modifier,
                    viewModel
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            items(locationNotes) { note ->
                CardNoteItem(note)
            }
        }
    }
}
