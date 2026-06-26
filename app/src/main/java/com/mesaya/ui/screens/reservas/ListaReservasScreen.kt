package com.mesaya.ui.screens.reservas

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mesaya.domain.model.EstadoReserva
import com.mesaya.domain.model.Reserva
import com.mesaya.ui.components.MesaYaLogo
import com.mesaya.ui.theme.*
import com.mesaya.utils.UiState
import com.mesaya.viewmodel.ReservasViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaReservasScreen(
    viewModel: ReservasViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToForm: (Int?) -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedEstado by viewModel.selectedEstado.collectAsState()
    val reservas = (uiState as? UiState.Success)?.data.orEmpty()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MesaYaLogo(size = 34.dp, backgroundColor = Color.White.copy(alpha = 0.96f))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "MesaYa",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onNavigateToPerfil,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToForm(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Reserva", fontWeight = FontWeight.Bold)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Dashboard
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox("Reservas", reservas.size.toString(), Icons.Default.DateRange, Modifier.weight(1f))
                    StatBox("Mesas", reservas.map { it.mesaId }.distinct().size.toString(), Icons.Default.Place, Modifier.weight(1f))
                    StatBox("Pedidos", reservas.count { it.total > 0 }.toString(), Icons.Default.Star, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                NextVisitCard(
                    reservas = reservas,
                    onNewReserva = { onNavigateToForm(null) },
                    onOpenReserva = { onNavigateToDetail(it.id) }
                )

                EstadoFilterChipsPremium(
                    selectedEstado = selectedEstado,
                    onSelectEstado = { viewModel.setFilter(it) }
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                        is UiState.Error -> ErrorStatePremium(state.message)
                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                EmptyStatePremium()
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(state.data, key = { it.id }) { reserva ->
                                        ReservaTicketCard(
                                            reserva = reserva,
                                            onClick = { onNavigateToDetail(reserva.id) },
                                            onEdit = { onNavigateToForm(reserva.id) },
                                            onDelete = { viewModel.deleteReserva(reserva) }
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(100.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(70.dp),
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun NextVisitCard(
    reservas: List<Reserva>,
    onNewReserva: () -> Unit,
    onOpenReserva: (Reserva) -> Unit
) {
    val nextReserva = remember(reservas) {
        val now = System.currentTimeMillis()
        reservas
            .filter { it.fecha.time >= now && EstadoReserva.fromValue(it.estado) != EstadoReserva.COMPLETADA }
            .minByOrNull { it.fecha.time }
            ?: reservas.firstOrNull()
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale("es", "ES")) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (nextReserva == null) "Tu mesa te espera" else "Proxima visita",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (nextReserva == null) {
                        "Crea una reserva y arma tu pedido."
                    } else {
                        "${dateFormat.format(nextReserva.fecha)} • ${nextReserva.hora.ifBlank { "--:--" }} • Mesa ${nextReserva.mesaId}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = OnSurface
                )
            }

            Button(
                onClick = {
                    if (nextReserva == null) onNewReserva() else onOpenReserva(nextReserva)
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (nextReserva == null) "Reservar" else "Ver", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EstadoFilterChipsPremium(
    selectedEstado: String?,
    onSelectEstado: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChipPremium(selected = selectedEstado == null, onClick = { onSelectEstado(null) }, label = "Todas")
        }
        items(EstadoReserva.entries) { estado ->
            FilterChipPremium(selected = selectedEstado == estado.value, onClick = { onSelectEstado(estado.value) }, label = estado.label)
        }
    }
}

@Composable
fun FilterChipPremium(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
        contentColor = if (selected) Color.White else Color.Black,
        shadowElevation = 2.dp,
        modifier = Modifier.animateContentSize()
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun ReservaTicketCard(
    reserva: Reserva,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM", Locale("es", "ES")) }
    val colorEstado = estadoColor(EstadoReserva.fromValue(reserva.estado))
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp), spotColor = colorEstado),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reserva.clienteNombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    StatusBadge(reserva.estado)
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconDetail(Icons.Default.Notifications, reserva.hora.ifBlank { "--:--" })
                IconDetail(Icons.Default.DateRange, dateFormat.format(reserva.fecha))
                IconDetail(Icons.Default.Place, "Mesa ${reserva.mesaId}")
            }

            if (reserva.total > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL ESTIMADO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(
                            "S/ ${"%.2f".format(reserva.total)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(estado: String) {
    val est = EstadoReserva.fromValue(estado)
    val color = estadoColor(est)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            text = est.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun IconDetail(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color.Gray.copy(alpha = 0.6f))
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyStatePremium() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MesaYaLogo(size = 88.dp, backgroundColor = Color.White, markColor = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("No hay reservas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("Crea tu primera reserva y prepara el pedido antes de llegar.", color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorStatePremium(msg: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(msg, color = MaterialTheme.colorScheme.error)
    }
}

fun estadoColor(estado: EstadoReserva): Color = when (estado) {
    EstadoReserva.PENDIENTE -> StatusPendiente
    EstadoReserva.EN_PREPARACION -> StatusPreparacion
    EstadoReserva.COMPLETADA -> StatusAtendida
}
