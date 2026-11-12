package com.nutridisco.app.domain.export

/**
 * Representa os campos e colunas que o usuário pode selecionar para incluir nos relatórios de exportação (PDF e CSV).
 */
enum class CampoExportacao(
    val rotulo: String,
    val descricao: String,
    val cabecalhoCsv: String,
    val cabecalhoPdf: String,
    val pesoLarguraPdf: Float
) {
    POLO_BASE(
        rotulo = "Polo Base",
        descricao = "Polo Base territorial de referência",
        cabecalhoCsv = "Polo Base",
        cabecalhoPdf = "POLO BASE",
        pesoLarguraPdf = 1.2f
    ),
    COMUNIDADE(
        rotulo = "Comunidade / Aldeia",
        descricao = "Aldeia ou comunidade atendida",
        cabecalhoCsv = "Comunidade",
        cabecalhoPdf = "COMUNIDADE",
        pesoLarguraPdf = 1.3f
    ),
    PROFISSIONAL(
        rotulo = "Profissional / COREN",
        descricao = "Nome e registro COREN do avaliador",
        cabecalhoCsv = "Profissional;COREN",
        cabecalhoPdf = "PROFISSIONAL",
        pesoLarguraPdf = 1.4f
    ),
    ID_CRIANCA(
        rotulo = "ID da Criança",
        descricao = "Identificador único do cadastro",
        cabecalhoCsv = "ID Crianca",
        cabecalhoPdf = "ID",
        pesoLarguraPdf = 0.6f
    ),
    NOME_CRIANCA(
        rotulo = "Nome da Criança",
        descricao = "Nome completo da criança",
        cabecalhoCsv = "Nome Crianca",
        cabecalhoPdf = "CRIANÇA",
        pesoLarguraPdf = 1.8f
    ),
    RESPONSAVEL(
        rotulo = "Nome do Responsável",
        descricao = "Mãe ou responsável legal",
        cabecalhoCsv = "Nome Responsavel",
        cabecalhoPdf = "RESPONSÁVEL",
        pesoLarguraPdf = 1.5f
    ),
    SEXO(
        rotulo = "Sexo Biológico",
        descricao = "Masculino ou Feminino",
        cabecalhoCsv = "Sexo",
        cabecalhoPdf = "SEXO",
        pesoLarguraPdf = 0.6f
    ),
    DATA_NASCIMENTO(
        rotulo = "Data de Nascimento",
        descricao = "Data de nascimento da criança",
        cabecalhoCsv = "Data Nascimento",
        cabecalhoPdf = "NASC.",
        pesoLarguraPdf = 1.0f
    ),
    DATA_AVALIACAO(
        rotulo = "Data da Avaliação",
        descricao = "Data e hora do atendimento clínico",
        cabecalhoCsv = "Data Avaliacao",
        cabecalhoPdf = "DATA",
        pesoLarguraPdf = 1.0f
    ),
    IDADE_MESES(
        rotulo = "Idade em Meses",
        descricao = "Idade em meses na consulta",
        cabecalhoCsv = "Idade (Meses)",
        cabecalhoPdf = "IDADE",
        pesoLarguraPdf = 0.7f
    ),
    PESO(
        rotulo = "Peso e Diagnóstico OMS",
        descricao = "Peso em kg e classificação Peso/Idade",
        cabecalhoCsv = "Peso (kg);Classificacao Peso",
        cabecalhoPdf = "PESO (KG)",
        pesoLarguraPdf = 1.5f
    ),
    ESTATURA(
        rotulo = "Estatura e Diagnóstico OMS",
        descricao = "Estatura em cm e classificação Estatura/Idade",
        cabecalhoCsv = "Estatura (cm);Classificacao Estatura",
        cabecalhoPdf = "ESTATURA (CM)",
        pesoLarguraPdf = 1.5f
    ),
    CNS(
        rotulo = "CNS",
        descricao = "Cartão Nacional de Saúde (15 dígitos)",
        cabecalhoCsv = "CNS",
        cabecalhoPdf = "CNS",
        pesoLarguraPdf = 1.2f
    ),
    OBSERVACOES(
        rotulo = "Observações",
        descricao = "Anotações e condutas clínicas",
        cabecalhoCsv = "Observacoes",
        cabecalhoPdf = "OBSERVAÇÕES",
        pesoLarguraPdf = 1.4f
    );

    companion object {
        val PADRAO: Set<CampoExportacao> = entries.toSet()
    }
}
