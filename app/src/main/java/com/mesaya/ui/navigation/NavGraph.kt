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
import com.mesaya.ui.screens.detalle.DetalleReservaScreen
import com.mesaya.ui.screens.formulario.FormReservaScreen
import com.mesaya.ui.screens.menu.MenuScreen
import com.mesaya.ui.screens.perfil.PerfilScreen
import com.mesaya.ui.screens.reservas.ListaReservasScreen
import com.mesaya.viewmodel.MenuViewModel
import com.mesaya.viewmodel.PerfilViewModel
import com.mesaya.viewmodel.ReservaDetailViewModel
import com.mesaya.viewmodel.ReservaFormViewModel
import com.mesaya.viewmodel.ReservasViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as MesaYaApplication
    val reservaRepo = remember { app.reservaRepository }
    val menuRepo = remember { app.menuRepository }

    NavHost(
        navController = navController,
        startDestination = Screen.Reservas.route
    ) {
        composable(Screen.Reservas.route) {
            val vm: ReservasViewModel = viewModel(factory = ReservasViewModel.factory(reservaRepo))
            ListaReservasScreen(
                viewModel = vm,
                onNavigateToDetail = { id -> navController.navigate(Screen.Detalle.createRoute(id)) },
                onNavigateToForm = { id -> navController.navigate(Screen.Formulario.createRoute(id)) },
                onNavigateToPerfil = { navController.navigate(Screen.Perfil.route) }
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
            val vm: PerfilViewModel = viewModel(factory = PerfilViewModel.factory(reservaRepo))
            PerfilScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
