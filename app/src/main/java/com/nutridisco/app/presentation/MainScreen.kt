package com.nutridisco.app.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nutridisco.app.presentation.avaliacaorapida.AvaliacaoRapidaScreen
import com.nutridisco.app.presentation.avaliacaorapida.AvaliacaoRapidaViewModel
import com.nutridisco.app.presentation.avaliacoessalvas.AvaliacoesSalvasViewModel
import com.nutridisco.app.presentation.cadastro.CadastroCriancaScreen
import com.nutridisco.app.presentation.cadastro.CadastroCriancaViewModel
import com.nutridisco.app.presentation.configuracao.ConfiguracaoScreen
import com.nutridisco.app.presentation.configuracao.ConfiguracaoViewModel
import com.nutridisco.app.presentation.criancas.CriancasScreen
import com.nutridisco.app.presentation.criancas.CriancasViewModel
import com.nutridisco.app.presentation.exportacao.ExportacaoScreen
import com.nutridisco.app.presentation.exportacao.ExportacaoViewModel
import com.nutridisco.app.presentation.navigation.Screen
import com.nutridisco.app.presentation.novaavaliacao.NovaAvaliacaoScreen
import com.nutridisco.app.presentation.novaavaliacao.NovaAvaliacaoViewModel
import com.nutridisco.app.presentation.perfil.PerfilCriancaScreen
import com.nutridisco.app.presentation.perfil.PerfilCriancaViewModel
import com.nutridisco.app.presentation.resultado.ResultadoScreen
import com.nutridisco.app.presentation.ui.AberturaScreen
import com.nutridisco.app.presentation.ui.AvaliacoesSalvasScreen
import com.nutridisco.app.presentation.ui.SelecaoSexoScreen

private val ROTA_AVALIACAO_RAPIDA = "${Screen.AvaliacaoRapida.route}?sexo={sexo}"

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Abas principais que exibem a barra inferior
    val bottomNavItems = listOf(
        Screen.AvaliacoesSalvas to ("Início" to Icons.Default.Home),
        Screen.SelecaoSexo to ("Avaliação" to Icons.Default.Calculate),
        Screen.Criancas to ("Crianças" to Icons.Default.ChildCare),
        Screen.Exportacao to ("Exportar" to Icons.Default.FileDownload),
        Screen.Configuracao to ("Ajustes" to Icons.Default.Settings)
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.first.route } ||
        currentRoute?.startsWith(Screen.AvaliacaoRapida.route) == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (screen, info) ->
                        val (label, icon) = info
                        val isSelected = currentRoute == screen.route ||
                            (screen == Screen.SelecaoSexo && currentRoute?.startsWith(Screen.AvaliacaoRapida.route) == true)

                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Abertura.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Abertura.route) {
                AberturaScreen(
                    onIniciarAvaliacaoClick = {
                        navController.navigate(Screen.AvaliacoesSalvas.route) {
                            popUpTo(Screen.Abertura.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AvaliacoesSalvas.route) {
                val viewModel: AvaliacoesSalvasViewModel = hiltViewModel()
                AvaliacoesSalvasScreen(
                    viewModel = viewModel,
                    onIniciarNovaAvaliacaoClick = {
                        navController.navigate(Screen.SelecaoSexo.route)
                    },
                    onCriancaClick = { criancaId ->
                        navController.navigate(Screen.PerfilCrianca.createRoute(criancaId))
                    }
                )
            }

            composable(Screen.SelecaoSexo.route) {
                SelecaoSexoScreen(
                    onVoltarClick = { navController.popBackStack() },
                    onProximoClick = { sexoOpcao ->
                        navController.navigate("${Screen.AvaliacaoRapida.route}?sexo=${sexoOpcao.valorDb}")
                    }
                )
            }

            composable(
                route = ROTA_AVALIACAO_RAPIDA,
                arguments = listOf(
                    navArgument("sexo") { type = NavType.StringType; defaultValue = "M" }
                )
            ) { backStackEntry ->
                val viewModel: AvaliacaoRapidaViewModel = hiltViewModel()
                val sexo = backStackEntry.arguments?.getString("sexo") ?: "M"
                LaunchedEffect(sexo) {
                    viewModel.onSexoChange(sexo)
                }
                AvaliacaoRapidaScreen(
                    viewModel = viewModel,
                    onVoltar = { navController.popBackStack() },
                    onResultadoCalculado = { navController.navigate(Screen.Resultado.route) }
                )
            }

            composable(Screen.Resultado.route) { backStackEntry ->
                // Compartilha o ViewModel da Avaliação Rápida, que já contém o resultado calculado.
                val avaliacaoEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(ROTA_AVALIACAO_RAPIDA)
                }
                val viewModel: AvaliacaoRapidaViewModel = hiltViewModel(avaliacaoEntry)
                ResultadoScreen(
                    viewModel = viewModel,
                    onVoltar = { navController.popBackStack() },
                    onNovaAvaliacao = {
                        navController.navigate(Screen.SelecaoSexo.route) {
                            popUpTo(Screen.SelecaoSexo.route) { inclusive = true }
                        }
                    },
                    onSalvarRegistro = { res ->
                        navController.navigate(
                            "${Screen.CadastroCrianca.route}?sexo=${res.sexo}&idade=${res.idadeMeses}" +
                                "&peso=${res.peso}&estatura=${res.estatura}"
                        )
                    }
                )
            }

            composable(Screen.Criancas.route) {
                val viewModel: CriancasViewModel = hiltViewModel()
                CriancasScreen(
                    viewModel = viewModel,
                    onCriancaClick = { criancaId ->
                        navController.navigate(Screen.PerfilCrianca.createRoute(criancaId))
                    },
                    onNovaCriancaClick = {
                        navController.navigate(Screen.CadastroCrianca.route)
                    }
                )
            }

            composable(
                route = "${Screen.CadastroCrianca.route}?sexo={sexo}&idade={idade}&peso={peso}&estatura={estatura}",
                arguments = listOf(
                    navArgument("sexo") { type = NavType.StringType; defaultValue = "M" },
                    navArgument("idade") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("peso") { type = NavType.FloatType; defaultValue = -1f },
                    navArgument("estatura") { type = NavType.FloatType; defaultValue = -1f }
                )
            ) { backStackEntry ->
                val viewModel: CadastroCriancaViewModel = hiltViewModel()
                val sexo = backStackEntry.arguments?.getString("sexo") ?: "M"
                val idade = backStackEntry.arguments?.getInt("idade") ?: 0
                val peso = backStackEntry.arguments?.getFloat("peso")?.toDouble() ?: -1.0
                val estatura = backStackEntry.arguments?.getFloat("estatura")?.toDouble() ?: -1.0

                val dadosPendente = if (peso > 0 && estatura > 0) Pair(peso, estatura) else null

                viewModel.preencherDadosIniciais(sexo, idade)

                CadastroCriancaScreen(
                    viewModel = viewModel,
                    dadosAvaliacaoPendente = dadosPendente,
                    onVoltar = { navController.popBackStack() },
                    onSalvoComSucesso = { criancaId ->
                        navController.navigate(Screen.PerfilCrianca.createRoute(criancaId)) {
                            popUpTo(Screen.Criancas.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.PerfilCrianca.route,
                arguments = listOf(
                    navArgument("criancaId") { type = NavType.LongType }
                )
            ) {
                val viewModel: PerfilCriancaViewModel = hiltViewModel()
                PerfilCriancaScreen(
                    viewModel = viewModel,
                    onVoltar = { navController.popBackStack() },
                    onNovaAvaliacaoClick = { criancaId ->
                        navController.navigate(Screen.NovaAvaliacao.createRoute(criancaId))
                    }
                )
            }

            composable(
                route = Screen.NovaAvaliacao.route,
                arguments = listOf(
                    navArgument("criancaId") { type = NavType.LongType }
                )
            ) {
                val viewModel: NovaAvaliacaoViewModel = hiltViewModel()
                NovaAvaliacaoScreen(
                    viewModel = viewModel,
                    onVoltar = { navController.popBackStack() },
                    onSalvo = { navController.popBackStack() }
                )
            }

            composable(Screen.Exportacao.route) {
                val viewModel: ExportacaoViewModel = hiltViewModel()
                ExportacaoScreen(viewModel = viewModel)
            }

            composable(Screen.Configuracao.route) {
                val viewModel: ConfiguracaoViewModel = hiltViewModel()
                ConfiguracaoScreen(
                    viewModel = viewModel,
                    onSalvo = {
                        navController.navigate(Screen.SelecaoSexo.route) {
                            popUpTo(Screen.Configuracao.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
