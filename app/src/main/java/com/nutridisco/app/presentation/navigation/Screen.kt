package com.nutridisco.app.presentation.navigation

sealed class Screen(val route: String) {
    object Abertura : Screen("abertura")
    object AvaliacoesSalvas : Screen("avaliacoes_salvas")
    object SelecaoSexo : Screen("selecao_sexo")
    object Configuracao : Screen("configuracao")
    object AvaliacaoRapida : Screen("avaliacao_rapida")
    object Criancas : Screen("criancas")
    object CadastroCrianca : Screen("cadastro_crianca")
    object PerfilCrianca : Screen("perfil_crianca/{criancaId}") {
        fun createRoute(criancaId: Long) = "perfil_crianca/$criancaId"
    }
    object NovaAvaliacao : Screen("nova_avaliacao/{criancaId}") {
        fun createRoute(criancaId: Long) = "nova_avaliacao/$criancaId"
    }
    object Exportacao : Screen("exportacao")
}
