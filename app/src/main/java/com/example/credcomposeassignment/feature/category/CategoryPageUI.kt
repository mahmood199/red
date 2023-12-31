package com.example.credcomposeassignment.feature.category

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.credcomposeassignment.data.models.CategoryItem
import com.example.credcomposeassignment.data.models.Section
import com.example.credcomposeassignment.feature.common.GridItem
import com.example.credcomposeassignment.feature.common.ListItem
import com.example.credcomposeassignment.ui.theme.CredComposeAssignmentTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun CategoryPageUI(
    viewModel: CategoryViewModel,
    closeCategorySelection: () -> Unit,
    modifier: Modifier = Modifier
) {

    val sections by viewModel.sections.collectAsState()
    val selectedItems by viewModel.selectedCategories.collectAsState()
    val state by viewModel.state.collectAsState()

    println("ashar: $selectedItems")

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colorScheme.surface
    )

    BackHandler(enabled = true) {
        closeCategorySelection()
    }

    Scaffold(
        topBar = {
            if (state.isLoading.not()) {
                CategoryHeader(
                    state = state,
                    switchLayout = {
                        viewModel.updateLayout(state.gridSpan)
                    },
                    closeCategorySelection = {
                        closeCategorySelection()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                )
            }
        },
        bottomBar = {
            if (state.isLoading.not()) {
                Button(
                    onClick = {
                        closeCategorySelection()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text(text = "Done with Selection")
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) { paddingValues ->
        AnimatedContent(
            targetState = state.isLoading,
            label = "Loader Animation",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
        ) {
            if (it) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                Column {
                    ListItems(
                        state = state,
                        sections = sections,
                        selectedItems = selectedItems,
                    ) { item ->
                        viewModel.selectItem(item)
                    }
                }
            }
        }
    }
}


@Composable
fun ListItems(
    state: CategoryViewState,
    sections: List<Section>,
    selectedItems: List<CategoryItem>,
    itemSelected: (CategoryItem) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
        columns = GridCells.Fixed(state.gridSpan.columns),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        gridSetup(
            gridSpan = state.gridSpan,
            sections = sections,
            selectedItems = selectedItems,
            itemSelected = itemSelected,
        )
    }
}

@Composable
fun CategoryHeader(
    state: CategoryViewState,
    switchLayout: () -> Unit,
    closeCategorySelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "explore",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Cred",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Switch(
            checked = state.gridSpan == GridSpan.Single,
            onCheckedChange = {
                switchLayout()
            }
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.clickable {
                closeCategorySelection()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyGridScope.gridSetup(
    gridSpan: GridSpan,
    sections: List<Section>,
    selectedItems: List<CategoryItem>,
    itemSelected: (CategoryItem) -> Unit
) {
    sections.forEachIndexed { index, section ->
        item(key = section.id + index, span = {
            GridItemSpan(currentLineSpan = gridSpan.columns)
        }) {
            Text(
                text = section.sectionProperty.title,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 12.dp).animateItemPlacement()
            )
        }

        items(
            items = section.sectionProperty.categories,
            key = { categoryItem ->
                categoryItem.id + index
            }
        ) { categoryItem ->
            Crossfade(
                targetState = gridSpan,
                label = "CategoryItemCrossfadeAnimation",
                modifier = Modifier.animateItemPlacement()
            ) {
                when (it) {
                    GridSpan.Single -> {
                        ListItem(
                            categoryProperty = categoryItem.categoryProperty,
                            isSelected = categoryItem in selectedItems,
                            onClick = {
                                itemSelected(categoryItem)
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    GridSpan.Triple -> {
                        GridItem(
                            categoryProperty = categoryItem.categoryProperty,
                            isSelected = selectedItems.find { item -> item.id == categoryItem.id } != null,
                            onClick = {
                                itemSelected(categoryItem)
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CategoryPageUIPreview() {
    val viewModel = CategoryViewModel()
    CredComposeAssignmentTheme {
        CategoryPageUI(
            viewModel = viewModel,
            closeCategorySelection = {}
        )
    }
}