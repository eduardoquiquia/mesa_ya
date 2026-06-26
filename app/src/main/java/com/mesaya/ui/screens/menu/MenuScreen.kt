package com.mesaya.ui.screens.menu

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.mesaya.domain.model.MetodoPago
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
    var showPagoDialog by remember { mutableStateOf(false) }
    var metodoPago by remember { mutableStateOf(MetodoPago.YAPE) }
    val pedidoTotal = remember(pedidoItems) { pedidoItems.sumOf { it.subtotal } }
    val pedidoCantidad = remember(pedidoItems) { pedidoItems.sumOf { it.cantidad } }

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

    if (showPagoDialog) {
        PagoOnlineDialog(
            total = pedidoTotal,
            selected = metodoPago,
            onSelected = { metodoPago = it },
            onConfirm = {
                viewModel.confirmarPago(metodoPago)
                showPagoDialog = false
                onNavigateBack()
            },
            onDismiss = { showPagoDialog = false }
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
                        PedidoTopBadge(cantidad = pedidoCantidad)
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = pedidoItems.isNotEmpty()) {
                PedidoBottomBar(
                    cantidad = pedidoCantidad,
                    total = pedidoTotal,
                    onConfirm = { showPagoDialog = true }
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

@Composable
private fun PedidoTopBadge(cantidad: Int) {
    BadgedBox(
        modifier = Modifier.padding(end = 16.dp),
        badge = {
            if (cantidad > 0) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("$cantidad", color = Color.White)
                }
            }
        }
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = "Pedido",
            tint = if (cantidad > 0) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}

@Composable
private fun PedidoBottomBar(
    cantidad: Int,
    total: Double,
    onConfirm: () -> Unit
) {
    Surface(
        shadowElevation = 10.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = Accent) {
                            Text("$cantidad")
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido actual", fontWeight = FontWeight.Black)
                Text("S/ ${"%.2f".format(total)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Pagar", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun PagoOnlineDialog(
    total: Double,
    selected: MetodoPago,
    onSelected: (MetodoPago) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Confirmar pago", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Volver al pedido", fontWeight = FontWeight.Bold)
            }
        },
        title = { Text("Pago online", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetodoPago.onlineMethods.forEach { metodo ->
                        PagoMethodChip(
                            metodo = metodo,
                            selected = metodo == selected,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelected(metodo) }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (selected == MetodoPago.YAPE) Icons.Default.Phone else Icons.Default.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                        Text(selected.label, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("MesaYa Demo 999 000 111", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        Text("S/ ${"%.2f".format(total)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun PagoMethodChip(
    metodo: MetodoPago,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .height(64.dp)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.White)
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                shape
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (metodo == MetodoPago.YAPE) Icons.Default.Phone else Icons.Default.Send,
            contentDescription = null,
            tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            metodo.label,
            fontWeight = FontWeight.Black,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
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
