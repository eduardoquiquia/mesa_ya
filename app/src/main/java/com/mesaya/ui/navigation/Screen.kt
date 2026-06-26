package com.mesaya.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Rol : Screen("rol")
    object Reservas : Screen("reservas")
    object AdminReservas : Screen("admin_reservas")
    object Formulario : Screen("formulario?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "formulario?id=$id" else "formulario"
    }
    object Detalle : Screen("detalle/{id}") {
        fun createRoute(id: Int) = "detalle/$id"
    }
    object Menu : Screen("menu/{id}") {
        fun createRoute(id: Int) = "menu/$id"
    }
    object Perfil : Screen("perfil")
}
