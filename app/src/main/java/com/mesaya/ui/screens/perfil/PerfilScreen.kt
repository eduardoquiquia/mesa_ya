package com.mesaya.ui.screens.perfil

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mesaya.ui.components.MesaYaLogo
import com.mesaya.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val account by viewModel.account.collectAsState()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.updatePhoto(it)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesion", fontWeight = FontWeight.Black) },
            text = { Text("Tu cuenta quedara protegida y volveras al inicio de sesion.") },
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

    infoDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            title = { Text(dialog.first, fontWeight = FontWeight.Black) },
            text = { Text(dialog.second) },
            confirmButton = {
                Button(onClick = { infoDialog = null }) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (account.photoUri.isNullOrBlank()) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(58.dp),
                        tint = Color.White
                    )
                } else {
                    AsyncImage(
                        model = account.photoUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            TextButton(onClick = { photoPicker.launch(arrayOf("image/*")) }) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (account.photoUri.isNullOrBlank()) "Agregar foto" else "Cambiar foto")
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text("Cuenta MesaYa", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(
                account.email.ifBlank { "Sesion activa" },
                color = Color.Gray
            )
            Text(
                account.role.label,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "DESEMPENO GENERAL",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileStatCard("Total", "${stats.totalReservas}", Icons.Default.List, Modifier.weight(1f))
                ProfileStatCard("En prep.", "${stats.reservasEnPreparacion}", Icons.Default.Notifications, Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            
            ProfileStatCard(
                "Completadas", 
                "${stats.reservasCompletadas}", 
                Icons.Default.CheckCircle, 
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileSwitchItem(
                        icon = Icons.Default.Notifications,
                        label = "Alertas de reservas",
                        checked = account.alertsEnabled,
                        onCheckedChange = viewModel::setAlertsEnabled
                    )
                    ProfileOptionItem(
                        Icons.Default.Settings,
                        "Sesion y seguridad",
                        onClick = {
                            infoDialog = "Sesion recordada" to "Si cierras la app sin cerrar sesion, Firebase mantiene tu cuenta activa y MesaYa entra automaticamente al volver a abrir."
                        }
                    )
                    ProfileOptionItem(
                        Icons.Default.Info,
                        "Centro de ayuda",
                        onClick = {
                            infoDialog = "Ayuda" to "Flujo recomendado: crea una reserva, elige mesa disponible, arma tu pedido y revisa el metodo de pago antes de confirmar."
                        }
                    )
                    if (!account.photoUri.isNullOrBlank()) {
                        ProfileOptionItem(
                            Icons.Default.Delete,
                            "Quitar foto",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = viewModel::clearPhoto
                        )
                    }
                    ProfileOptionItem(
                        icon = Icons.Default.ExitToApp,
                        label = "Cerrar sesion",
                        color = MaterialTheme.colorScheme.error,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ProfileStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (color == Color.Unspecified) Color.Gray else color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontWeight = FontWeight.Bold, color = if (color == Color.Unspecified) Color.Unspecified else color)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowForward, null, tint = Color.LightGray)
    }
}
