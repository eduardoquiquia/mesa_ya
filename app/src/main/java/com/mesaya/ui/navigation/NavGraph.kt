package com.mesaya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mesaya.MesaYaApplication
import com.mesaya.domain.model.UserRole
import com.mesaya.ui.screens.admin.AdminReservasScreen
import com.mesaya.ui.screens.auth.AuthScreen
import com.mesaya.ui.screens.detalle.DetalleReservaScreen
import com.mesaya.ui.screens.formulario.FormReservaScreen
import com.mesaya.ui.screens.menu.MenuScreen
import com.mesaya.ui.screens.perfil.PerfilScreen
import com.mesaya.ui.screens.reservas.ListaReservasScreen
import com.mesaya.ui.screens.rol.RolScreen
import com.mesaya.viewmodel.AuthViewModel
import com.mesaya.viewmodel.AdminUsersViewModel
import com.mesaya.viewmodel.MenuViewModel
import com.mesaya.viewmodel.PerfilViewModel
import com.mesaya.viewmodel.ReservaDetailViewModel
import com.mesaya.viewmodel.ReservaFormViewModel
import com.mesaya.viewmodel.ReservasViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as MesaYaApplication
    val authRepo = remember { app.authRepository }

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            val vm: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepo))
            AuthScreen(
                viewModel = vm,
                onAuthenticated = { role ->
                    val destination = if (role == UserRole.ADMIN) {
                        Screen.AdminReservas.route
                    } else {
                        Screen.Reservas.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Rol.route) {
            RolScreen(
                onClienteClick = { navController.navigate(Screen.Reservas.route) },
                onAdminClick = { navController.navigate(Screen.AdminReservas.route) }
            )
        }

        composable(Screen.Reservas.route) {
            val reservaRepo = remember { app.reservaRepository }
            val vm: ReservasViewModel = viewModel(factory = ReservasViewModel.factory(reservaRepo))
            ListaReservasScreen(
                viewModel = vm,
                onNavigateToDetail = { id -> navController.navigate(Screen.Detalle.createRoute(id)) },
                onNavigateToForm = { id -> navController.navigate(Screen.Formulario.createRoute(id)) },
                onNavigateToPerfil = { navController.navigate(Screen.Perfil.route) }
            )
        }

        composable(Screen.AdminReservas.route) {
            val reservaRepo = remember { app.reservaRepository }
            val userAdminRepo = remember { app.userAdminRepository }
            val vm: ReservasViewModel = viewModel(factory = ReservasViewModel.factory(reservaRepo))
            val usersVm: AdminUsersViewModel = viewModel(factory = AdminUsersViewModel.factory(userAdminRepo))
            AdminReservasScreen(
                viewModel = vm,
                usersViewModel = usersVm,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authRepo.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.AdminReservas.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.Formulario.route,
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val reservaRepo = remember { app.reservaRepository }
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            val vm: ReservaFormViewModel = viewModel(factory = ReservaFormViewModel.factory(reservaRepo))
            FormReservaScreen(
                reservaId = id,
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Detalle.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val reservaRepo = remember { app.reservaRepository }
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val vm: ReservaDetailViewModel = viewModel(factory = ReservaDetailViewModel.factory(reservaRepo))
            DetalleReservaScreen(
                reservaId = id,
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMenu = { resId -> navController.navigate(Screen.Menu.createRoute(resId)) }
            )
        }

        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val reservaRepo = remember { app.reservaRepository }
            val menuRepo = remember { app.menuRepository }
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val vm: MenuViewModel = viewModel(
                factory = MenuViewModel.factory(reservaRepo, menuRepo)
            )
            MenuScreen(
                reservaId = id,
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Perfil.route) {
            val reservaRepo = remember { app.reservaRepository }
            val vm: PerfilViewModel = viewModel(factory = PerfilViewModel.factory(context, reservaRepo))
            PerfilScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authRepo.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Reservas.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
