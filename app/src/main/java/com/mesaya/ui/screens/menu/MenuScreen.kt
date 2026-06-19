package com.mesaya.ui.screens.menu

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mesaya.domain.model.Meal
import com.mesaya.ui.theme.Accent
import com.mesaya.utils.UiState
import com.mesaya.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    reservaId: Int,
    viewModel: MenuViewModel,
    onNavigateBack: () -> Unit
) {
    val mealsUiState by viewModel.mealsUiState.collectAsState()
    val pedidoItems by viewModel.pedidoItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var selectedMeal by remember { mutableStateOf<Meal?>(null) }

    LaunchedEffect(reservaId) {
        viewModel.initialize(reservaId)
    }

    if (selectedMeal != null) {
        CantidadDialogPremium(
            meal = selectedMeal!!,
            onConfirm = { cantidad ->
                viewModel.addToPedido(selectedMeal!!, cantidad)
                selectedMeal = null
            },
            onDismiss = { selectedMeal = null }
        )
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchBarPremium(
                    query = searchQuery,
                    onQueryChange = { viewModel.searchMeals(it) },
                    onClose = { showSearch = false; viewModel.searchMeals("") }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text("Nuestra Carta", fontWeight = FontWeight.Black)
                            Text("MesaYa Gourmet", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, null)
                        }
                        if (pedidoItems.isNotEmpty()) {
                            BadgedBox(
                                modifier = Modifier.padding(end = 16.dp),
                                badge = { 
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) { 
                                        Text("${pedidoItems.size}", color = Color.White) 
                                    } 
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, null)
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadByCategory(cat) },
                            label = { Text(cat, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(containerColor = Color.White)
                        )
                    }
                }
            }

            when (val state = mealsUiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message) }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(state.data, key = { it.mealId }) { meal ->
                            val cant = pedidoItems.find { it.mealId == meal.mealId }?.cantidad ?: 0
                            MealCardPremium(meal, cant, onAdd = { selectedMeal = meal })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarPremium(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Busca tu plato favorito...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        },
        navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) } }
    )
}

@Composable
fun MealCardPremium(meal: Meal, cantidad: Int, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = meal.imagenUrl,
                contentDescription = null,
                modifier = Modifier.width(140.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(16.dp).weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        meal.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        meal.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "S/ ${"%.2f".format(meal.precio)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Box {
                        IconButton(
                            onClick = onAdd,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        if (cantidad > 0) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                                containerColor = Accent
                            ) { Text("$cantidad") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CantidadDialogPremium(meal: Meal, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var cant by remember { mutableStateOf(1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(cant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Agregar al pedido", fontWeight = FontWeight.Bold) }
        },
        title = { Text(meal.nombre, fontWeight = FontWeight.Black) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = meal.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.size(160.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (cant > 1) cant-- }) { 
                        Text("-", style = MaterialTheme.typography.headlineLarge)
                    }
                    Text("$cant", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 24.dp))
                    IconButton(onClick = { cant++ }) { 
                        Text("+", style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
