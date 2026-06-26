package com.mesaya.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mesaya.domain.model.AppUser
import com.mesaya.domain.model.EstadoReserva
import com.mesaya.domain.model.MetodoPago
import com.mesaya.domain.model.Reserva
import com.mesaya.domain.model.UserRole
import com.mesaya.ui.components.MesaYaLogo
import com.mesaya.ui.screens.reservas.StatusBadge
import com.mesaya.ui.screens.reservas.estadoColor
import com.mesaya.utils.UiState
import com.mesaya.viewmodel.AdminUsersViewModel
import com.mesaya.viewmodel.ReservasViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReservasScreen(
    viewModel: ReservasViewModel,
    usersViewModel: AdminUsersViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val usersState by usersViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesion", fontWeight = FontWeight.Black) },
            text = { Text("Volveras a la pantalla de seleccion de rol.") },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Salir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MesaYaLogo(size = 32.dp, backgroundColor = Color.White.copy(alpha = 0.96f))
                        Spacer(Modifier.size(8.dp))
                        Text("Administracion", fontWeight = FontWeight.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesion")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Reservas", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Usuarios", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                when (val state = uiState) {
                    is UiState.Loading -> LoadingAdmin()
                    is UiState.Error -> ErrorAdmin(state.message)
                    is UiState.Success -> {
                        AdminReservasContent(
                            reservas = state.data,
                            onCambiarEstado = { reserva, estado -> viewModel.cambiarEstado(reserva, estado) }
                        )
                    }
                }
            } else {
                when (val state = usersState) {
                    is UiState.Loading -> LoadingAdmin()
                    is UiState.Error -> ErrorAdmin(state.message)
                    is UiState.Success -> {
                        AdminUsersContent(
                            users = state.data,
                            onMakeAdmin = { usersViewModel.makeAdmin(it) },
                            onMakeCliente = { usersViewModel.makeCliente(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingAdmin() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorAdmin(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun AdminReservasContent(
    reservas: List<Reserva>,
    onCambiarEstado: (Reserva, EstadoReserva) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AdminMetricCard(
                    label = "Ventas",
                    value = "S/ ${"%.0f".format(reservas.sumOf { it.total })}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(EstadoReserva.entries) { estado ->
                AdminMetricCard(
                    label = estado.label,
                    value = reservas.count { EstadoReserva.fromValue(it.estado) == estado }.toString(),
                    color = estadoColor(estado)
                )
            }
        }

        if (reservas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay reservas registradas", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(reservas, key = { it.id }) { reserva ->
                    AdminReservaCard(reserva = reserva, onCambiarEstado = onCambiarEstado)
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun AdminUsersContent(
    users: List<AppUser>,
    onMakeAdmin: (AppUser) -> Unit,
    onMakeCliente: (AppUser) -> Unit
) {
    if (users.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay usuarios registrados", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users, key = { it.uid }) { user ->
            AdminUserCard(
                user = user,
                onMakeAdmin = { onMakeAdmin(user) },
                onMakeCliente = { onMakeCliente(user) }
            )
        }
    }
}

@Composable
private fun AdminUserCard(
    user: AppUser,
    onMakeAdmin: () -> Unit,
    onMakeCliente: () -> Unit
) {
    val isBootstrapAdmin = user.email.equals("admin@mesaya.com", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(user.email, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text(user.role.label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            Button(
                onClick = if (user.role == UserRole.ADMIN) onMakeCliente else onMakeAdmin,
                shape = RoundedCornerShape(12.dp),
                enabled = !isBootstrapAdmin
            ) {
                Icon(
                    if (user.role == UserRole.ADMIN) Icons.Default.Person else Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    when {
                        isBootstrapAdmin -> "Principal"
                        user.role == UserRole.ADMIN -> "Cliente"
                        else -> "Admin"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AdminMetricCard(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.size(width = 150.dp, height = 86.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

@Composable
private fun AdminReservaCard(
    reserva: Reserva,
    onCambiarEstado: (Reserva, EstadoReserva) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(reserva.clienteNombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    StatusBadge(reserva.estado)
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Cambiar estado")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        EstadoReserva.entries.forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado.label, color = estadoColor(estado), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    onCambiarEstado(reserva, estado)
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(estadoColor(estado))
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminDetail(Icons.Default.DateRange, dateFormat.format(reserva.fecha))
                AdminDetail(Icons.Default.Notifications, reserva.hora.ifBlank { "--:--" })
                AdminDetail(Icons.Default.Place, "Mesa ${reserva.mesaId}")
                AdminDetail(Icons.Default.Person, "${reserva.numeroPersonas}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminPagoBadge(MetodoPago.fromValue(reserva.metodoPago))
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

@Composable
private fun AdminPagoBadge(metodo: MetodoPago) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (metodo) {
                    MetodoPago.PENDIENTE -> Icons.Default.Info
                    MetodoPago.YAPE -> Icons.Default.Phone
                    MetodoPago.PLIN -> Icons.Default.Send
                },
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(6.dp))
            Text(
                metodo.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun AdminDetail(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(Modifier.size(5.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}
