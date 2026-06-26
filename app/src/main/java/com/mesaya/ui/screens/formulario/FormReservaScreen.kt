package com.mesaya.ui.screens.formulario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val mesasOcupadas by viewModel.mesasOcupadas.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var clienteNombre by remember { mutableStateOf("") }
    var fechaTexto by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var numeroPersonasTexto by remember { mutableStateOf("") }
    var mesaId by remember { mutableStateOf(0) }
    var estadoSeleccionado by remember { mutableStateOf(EstadoReserva.PENDIENTE) }
    var reservaActual by remember { mutableStateOf<Reserva?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var clienteError by remember { mutableStateOf(false) }
    var fechaError by remember { mutableStateOf(false) }
    var horaError by remember { mutableStateOf(false) }
    var personasError by remember { mutableStateOf(false) }
    var mesaError by remember { mutableStateOf(false) }
    val fechaSeleccionada = remember(fechaTexto) {
        runCatching { dateFormat.parse(fechaTexto) }.getOrNull()
    }
    val horaNormalizada = remember(hora) { normalizeHoraOrNull(hora) }

    LaunchedEffect(reservaId) {
        reservaId?.let { viewModel.loadReserva(it) }
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is UiState.Success && state.data != null) {
            val r = state.data
            reservaActual = r
            clienteNombre = r.clienteNombre
            fechaTexto = dateFormat.format(r.fecha)
            hora = r.hora
            numeroPersonasTexto = r.numeroPersonas.toString()
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

    LaunchedEffect(fechaSeleccionada?.time, horaNormalizada, reservaId) {
        viewModel.observarMesasOcupadas(fechaSeleccionada, horaNormalizada.orEmpty(), reservaId ?: 0)
    }

    LaunchedEffect(mesasOcupadas, mesaId) {
        if (mesaId in mesasOcupadas) {
            mesaId = 0
            mesaError = true
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaTexto = dateFormat.format(Date(millis))
                        fechaError = false
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
                "DATOS DE TU RESERVA",
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
                placeholder = "Ej. Carlos Ramirez",
                isError = clienteError
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FormTextFieldPremium(
                        value = fechaTexto,
                        onValueChange = {},
                        label = "Fecha",
                        icon = Icons.Default.DateRange,
                        placeholder = "Elige una fecha",
                        isError = fechaError,
                        readOnly = true,
                        onClick = { showDatePicker = true }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FormTextFieldPremium(
                        value = hora,
                        onValueChange = {
                            hora = sanitizeHoraInput(it)
                            horaError = false
                        },
                        label = "Hora",
                        icon = Icons.Default.Notifications,
                        placeholder = "Ej. 12 o 12:00",
                        isError = horaError,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        onFocusLost = {
                            normalizeHoraOrNull(hora)?.let { hora = it }
                        }
                    )
                }
            }

            HoraRapidaSelector(
                selectedHora = horaNormalizada,
                onHoraSelected = {
                    hora = it
                    horaError = false
                }
            )

            FormTextFieldPremium(
                value = numeroPersonasTexto,
                onValueChange = {
                    numeroPersonasTexto = it.filter { char -> char.isDigit() }.take(2)
                    personasError = false
                },
                label = "Personas",
                icon = Icons.Default.Person,
                placeholder = "Ej. 2",
                isError = personasError,
                keyboardType = KeyboardType.Number
            )

            MesaSelector(
                selectedMesa = mesaId,
                mesasOcupadas = mesasOcupadas,
                enabled = fechaSeleccionada != null && horaNormalizada != null,
                hasError = mesaError,
                onMesaSelected = {
                    mesaId = it
                    mesaError = false
                }
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    clienteError = clienteNombre.isBlank()
                    fechaError = fechaTexto.isBlank()
                    val horaFinal = normalizeHoraOrNull(hora)
                    val personas = numeroPersonasTexto.toIntOrNull()?.takeIf { it > 0 }
                    horaError = horaFinal == null
                    personasError = personas == null
                    mesaError = mesaId == 0 || mesaId in mesasOcupadas
                    if (clienteError || fechaError || horaError || personasError || mesaError) return@Button
                    val horaReserva = horaFinal ?: return@Button
                    val cantidadPersonas = personas ?: return@Button

                    val fecha = try { dateFormat.parse(fechaTexto) ?: Date() } catch (_: Exception) { Date() }
                    val reservaBase = reservaActual

                    viewModel.saveReserva(
                        (reservaBase ?: Reserva(
                            id = reservaId ?: 0,
                            clienteNombre = clienteNombre.trim(),
                            fecha = fecha,
                            hora = horaReserva,
                            numeroPersonas = cantidadPersonas,
                            mesaId = mesaId
                        )).copy(
                            id = reservaId ?: reservaBase?.id ?: 0,
                            clienteNombre = clienteNombre.trim(),
                            fecha = fecha,
                            hora = horaReserva,
                            numeroPersonas = cantidadPersonas,
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
private fun HoraRapidaSelector(
    selectedHora: String?,
    onHoraSelected: (String) -> Unit
) {
    val horas = remember { listOf("12:00", "13:00", "18:00", "19:00", "20:00", "21:00") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Horarios rapidos",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            horas.take(3).forEach { hora ->
                HoraChip(
                    hora = hora,
                    selected = selectedHora == hora,
                    modifier = Modifier.weight(1f),
                    onClick = { onHoraSelected(hora) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            horas.drop(3).forEach { hora ->
                HoraChip(
                    hora = hora,
                    selected = selectedHora == hora,
                    modifier = Modifier.weight(1f),
                    onClick = { onHoraSelected(hora) }
                )
            }
        }
    }
}

@Composable
private fun HoraChip(
    hora: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                shape = shape
            )
            .clickable { onClick() },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            hora,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MesaSelector(
    selectedMesa: Int,
    mesasOcupadas: Set<Int>,
    enabled: Boolean,
    hasError: Boolean,
    onMesaSelected: (Int) -> Unit
) {
    val mesas = remember { (1..12).toList() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Mesa",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (hasError) MaterialTheme.colorScheme.error else Color.Gray
            )
            Text(
                if (enabled) "Toca una mesa libre" else "Primero fecha y hora",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }

        Text(
            if (enabled) "${12 - mesasOcupadas.size} mesas disponibles para ese horario" else "Las mesas se activan cuando la fecha y hora son validas.",
            style = MaterialTheme.typography.bodySmall,
            color = if (hasError) MaterialTheme.colorScheme.error else Color.Gray,
            fontWeight = FontWeight.SemiBold
        )

        mesas.chunked(3).forEach { rowMesas ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowMesas.forEach { mesa ->
                    val ocupada = mesa in mesasOcupadas
                    val selected = mesa == selectedMesa
                    MesaChip(
                        mesa = mesa,
                        ocupada = ocupada,
                        selected = selected,
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onMesaSelected(mesa) }
                    )
                }
            }
        }

        if (hasError) {
            Text(
                "Selecciona una mesa libre para continuar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MesaChip(
    mesa: Int,
    ocupada: Boolean,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val background = when {
        !enabled -> Color(0xFFF7F7F7)
        selected -> MaterialTheme.colorScheme.primary
        ocupada -> Color(0xFFF1F1F1)
        else -> Color.White
    }
    val borderColor = when {
        !enabled -> Color(0xFFE8E8E8)
        selected -> MaterialTheme.colorScheme.primary
        ocupada -> Color(0xFFE0E0E0)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    }
    val textColor = when {
        !enabled -> Color(0xFF9A9A9A)
        selected -> Color.White
        ocupada -> Color(0xFF8A8A8A)
        else -> Color(0xFF222222)
    }
    val label = when {
        !enabled -> "Bloqueada"
        ocupada -> "Ocupada"
        selected -> "Elegida"
        else -> "Tocar"
    }

    Column(
        modifier = modifier
            .height(72.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(enabled = enabled && !ocupada) { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Mesa $mesa",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = textColor
        )
        Text(
            label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = textColor.copy(alpha = 0.8f)
        )
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
    imeAction: ImeAction = ImeAction.Default,
    onClick: (() -> Unit)? = null,
    onFocusLost: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) onFocusLost?.invoke()
                    },
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = trailingIcon,
                isError = isError,
                readOnly = readOnly,
                placeholder = { Text(placeholder, color = Color.LightGray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onFocusLost?.invoke() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    disabledTextColor = Color.Black,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                    disabledLabelColor = Color.Gray,
                    disabledPlaceholderColor = Color.LightGray
                ),
                enabled = onClick == null
            )
            
            // Capa invisible para capturar el clic si existe una acción
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onClick() }
                )
            }
        }
    }
}

private fun sanitizeHoraInput(raw: String): String =
    raw.filter { it.isDigit() || it == ':' }.take(5)

private fun normalizeHoraOrNull(raw: String): String? {
    val clean = raw.trim()
    if (clean.isBlank()) return null

    val hour: Int
    val minute: Int
    if (clean.contains(":")) {
        val parts = clean.split(":")
        if (parts.size != 2) return null
        hour = parts[0].toIntOrNull() ?: return null
        minute = when (parts[1].length) {
            0 -> 0
            1 -> (parts[1].toIntOrNull() ?: return null) * 10
            else -> parts[1].take(2).toIntOrNull() ?: return null
        }
    } else {
        val digits = clean.filter { it.isDigit() }
        when (digits.length) {
            1, 2 -> {
                hour = digits.toInt()
                minute = 0
            }
            3 -> {
                hour = digits.take(1).toInt()
                minute = digits.drop(1).toInt()
            }
            4 -> {
                hour = digits.take(2).toInt()
                minute = digits.drop(2).toInt()
            }
            else -> return null
        }
    }

    if (hour !in 0..23 || minute !in 0..59) return null
    return "%02d:%02d".format(Locale.US, hour, minute)
}
