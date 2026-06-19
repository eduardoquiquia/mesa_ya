package com.mesaya.ui.screens.formulario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mesaya.domain.model.EstadoReserva
import com.mesaya.domain.model.Reserva
import com.mesaya.utils.UiState
import com.mesaya.viewmodel.ReservaFormViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormReservaScreen(
    reservaId: Int? = null,
    viewModel: ReservaFormViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var clienteNombre by remember { mutableStateOf("") }
    var fechaTexto by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var numeroPersonas by remember { mutableStateOf(1) }
    var mesaId by remember { mutableStateOf(1) }
    var estadoSeleccionado by remember { mutableStateOf(EstadoReserva.PENDIENTE) }
    var showDatePicker by remember { mutableStateOf(false) }
    var estadoDropdownExpanded by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var clienteError by remember { mutableStateOf(false) }
    var fechaError by remember { mutableStateOf(false) }

    LaunchedEffect(reservaId) {
        reservaId?.let { viewModel.loadReserva(it) }
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is UiState.Success && state.data != null) {
            val r = state.data
            clienteNombre = r.clienteNombre
            fechaTexto = dateFormat.format(r.fecha)
            hora = r.hora
            numeroPersonas = r.numeroPersonas
            mesaId = r.mesaId
            estadoSeleccionado = EstadoReserva.fromValue(r.estado)
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaTexto = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Confirmar", fontWeight = FontWeight.Bold) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reservaId == null) "Nueva Reserva" else "Editar Reserva", fontWeight = FontWeight.Black) },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "COMPLETA LOS DATOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )

            FormTextFieldPremium(
                value = clienteNombre,
                onValueChange = { clienteNombre = it; clienteError = false },
                label = "Nombre del Cliente",
                icon = Icons.Default.Person,
                isError = clienteError
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FormTextFieldPremium(
                        value = fechaTexto,
                        onValueChange = {},
                        label = "Fecha",
                        icon = Icons.Default.DateRange,
                        isError = fechaError,
                        readOnly = true,
                        onClick = { showDatePicker = true }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FormTextFieldPremium(
                        value = hora,
                        onValueChange = { hora = it },
                        label = "Hora",
                        icon = Icons.Default.Notifications,
                        placeholder = "19:30"
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextFieldPremium(
                    value = numeroPersonas.toString(),
                    onValueChange = { numeroPersonas = it.toIntOrNull() ?: 1 },
                    label = "Personas",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                FormTextFieldPremium(
                    value = mesaId.toString(),
                    onValueChange = { mesaId = it.toIntOrNull() ?: 1 },
                    label = "Mesa #",
                    icon = Icons.Default.Place,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            ExposedDropdownMenuBox(
                expanded = estadoDropdownExpanded,
                onExpandedChange = { estadoDropdownExpanded = !estadoDropdownExpanded }
            ) {
                FormTextFieldPremium(
                    value = estadoSeleccionado.label,
                    onValueChange = {},
                    label = "Estado",
                    icon = Icons.Default.Info,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoDropdownExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = estadoDropdownExpanded,
                    onDismissRequest = { estadoDropdownExpanded = false }
                ) {
                    EstadoReserva.entries.forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.label) },
                            onClick = {
                                estadoSeleccionado = estado
                                estadoDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    clienteError = clienteNombre.isBlank()
                    fechaError = fechaTexto.isBlank()
                    if (clienteError || fechaError) return@Button

                    val fecha = try { dateFormat.parse(fechaTexto) ?: Date() } catch (_: Exception) { Date() }

                    viewModel.saveReserva(
                        Reserva(
                            id = reservaId ?: 0,
                            clienteNombre = clienteNombre.trim(),
                            fecha = fecha,
                            hora = hora.trim(),
                            numeroPersonas = numeroPersonas,
                            mesaId = mesaId,
                            estado = estadoSeleccionado.value
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(if (reservaId == null) "CONFIRMAR RESERVA" else "GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun FormTextFieldPremium(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    readOnly: Boolean = false,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.background(Color.Transparent).clickable { onClick() } else Modifier),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = trailingIcon,
            isError = isError,
            readOnly = readOnly,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            enabled = onClick == null || readOnly
        )
    }
}
