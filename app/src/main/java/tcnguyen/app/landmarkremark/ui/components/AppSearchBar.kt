package tcnguyen.app.landmarkremark.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tcnguyen.app.landmarkremark.R
import tcnguyen.app.landmarkremark.RootViewModel

@Composable
fun AppSearchBar(modifier: Modifier = Modifier, viewModel: RootViewModel) {
    val searchContent by viewModel.searchText.collectAsState()
    val isSearch by viewModel.isSearching.collectAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(id = R.string.search),
            modifier = Modifier.padding(start = 16.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        AreaTextField(
            hintText = stringResource(id = R.string.search_note),
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            value = searchContent,
            onValueChanged = viewModel::onSearchTextChange
        )
        ProfileImage(
            drawableResource = R.drawable.ic_launcher_background,
            description = stringResource(id = R.string.home),
            modifier = Modifier
                .padding(12.dp)
                .size(32.dp)
        )
    }
}

@Composable
fun ProfileImage(
    drawableResource: Int,
    description: String,
    modifier: Modifier = Modifier.size(40.dp),
) {
    Image(
        modifier = modifier.clip(CircleShape),
        painter = painterResource(id = drawableResource),
        contentDescription = description,
    )
}
