package com.mesaya.ui.screens.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mesaya.domain.model.DetallePedido
import com.mesaya.domain.model.MetodoPago
import com.mesaya.domain.model.Reserva
import com.mesaya.ui.screens.reservas.StatusBadge
import com.mesaya.ui.theme.OnSurface
import com.mesaya.utils.UiState
import com.mesaya.viewmodel.ReservaDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleReservaScreen(
    reservaId: Int,
    viewModel: ReservaDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMenu: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val detalles by viewModel.detalles.collectAsState()
    val total by viewModel.total.collectAsState()

    LaunchedEffect(reservaId) {
        viewModel.loadReserva(reservaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Reserva", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message) }
            is UiState.Success -> {
                val reserva = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        Text(reserva.clienteNombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                                        StatusBadge(reserva.estado)
                                    }
                                }
                                
                                Spacer(Modifier.height(24.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                Spacer(Modifier.height(24.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    DetailItemPremium(Icons.Default.DateRange, "FECHA", SimpleDateFormat("dd MMM", Locale.getDefault()).format(reserva.fecha))
                                    DetailItemPremium(Icons.Default.Notifications, "HORA", reserva.hora)
                                    DetailItemPremium(Icons.Default.Place, "MESA", "#${reserva.mesaId}")
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("ORDEN DE COMIDA", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                            TextButton(onClick = { onNavigateToMenu(reservaId) }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Añadir platos", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (detalles.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No hay platos seleccionados", color = Color.LightGray)
                            }
                        }
                    } else {
                        items(detalles) { detalle ->
                            OrderListItemPremium(detalle, onDelete = { viewModel.eliminarDetalle(detalle) })
                        }
                        
                        item {
                            Spacer(Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("TOTAL A PAGAR", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                        Text("S/ ${"%.2f".format(total)}", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                                    }
                                    PagoResumen(MetodoPago.fromValue(reserva.metodoPago))
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
private fun PagoResumen(metodo: MetodoPago) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (metodo) {
                        MetodoPago.PENDIENTE -> Icons.Default.Info
                        MetodoPago.YAPE -> Icons.Default.Phone
                        MetodoPago.PLIN -> Icons.Default.Send
                    },
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (metodo == MetodoPago.PENDIENTE) "Pago pendiente" else "Pagado con ${metodo.label}",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                if (metodo == MetodoPago.PENDIENTE) "Desde pedido" else "Confirmado",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun DetailItemPremium(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp)
    }
}

@Composable
fun OrderListItemPremium(detalle: DetallePedido, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = detalle.imagenUrl,
            contentDescription = null,
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(detalle.nombre, fontWeight = FontWeight.Bold)
            Text("x${detalle.cantidad} • S/ ${"%.2f".format(detalle.precio)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Text("S/ ${"%.2f".format(detalle.subtotal)}", fontWeight = FontWeight.Black, color = OnSurface)
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
    }
}
