package com.mesaya.ui.screens.perfil

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mesaya.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()

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
                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.White)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text("Restaurante MesaYa", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("Administrador de Sede", color = Color.Gray)
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "DESEMPEÑO GENERAL",
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
                ProfileStatCard("Atendidas", "${stats.reservasAtendidas}", Icons.Default.CheckCircle, Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            
            ProfileStatCard(
                "Canceladas", 
                "${stats.reservasCanceladas}", 
                Icons.Default.Clear, 
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileOptionItem(Icons.Default.Settings, "Configuración del Sistema")
                    ProfileOptionItem(Icons.Default.Notifications, "Alertas de Reservas")
                    ProfileOptionItem(Icons.Default.Info, "Centro de Ayuda")
                    ProfileOptionItem(Icons.Default.ExitToApp, "Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                }
            }
        }
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
fun ProfileOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (color == Color.Unspecified) Color.Gray else color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontWeight = FontWeight.Bold, color = if (color == Color.Unspecified) Color.Unspecified else color)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowForward, null, tint = Color.LightGray)
    }
}
