# NutriDisco 📱🌿

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-Offline_First-47854F?style=for-the-badge&logo=sqlite&logoColor=white)
![Hilt](https://img.shields.io/badge/DI-Dagger_Hilt_2.51.1-EA4335?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-103%20passed-success?style=for-the-badge&logo=checkmarx&logoColor=white)

**Avaliação Nutricional Antropométrica Infantil em Territórios Indígenas e Comunidades Tradicionais**  
*Substituição digital e offline do disco físico da OMS para crianças de 0 a 59 meses*

**Universidade Federal do Amazonas (UFAM)** • **DSEI Leste de Roraima**

</div>

---

## 📌 Sumário
- [Sobre o Projeto](#-sobre-o-projeto)
- [Contexto e Problema](#-contexto-e-problema)
- [Principais Funcionalidades](#-principais-funcionalidades)
- [Regras de Negócio e Metodologia OMS](#-regras-de-negócio-e-metodologia-oms)
- [Arquitetura de Software](#-arquitetura-de-software)
- [Stack Tecnológica](#-stack-tecnológica)
- [Design System e Interface](#-design-system-e-interface)
- [Testes e Garantia de Qualidade](#-testes-e-garantia-de-qualidade)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Como Executar o Projeto](#-como-executar-o-projeto)
- [Exportação de Dados](#-exportação-de-dados)
- [Licença e Créditos](#-licença-e-créditos)

---

## 📖 Sobre o Projeto

O **NutriDisco** é uma aplicação móvel Android nativa projetada para profissionais de saúde (enfermeiros, técnicos e agentes de saúde indígena) que atuam em áreas de difícil acesso, com foco no **Distrito Sanitário Especial Indígena (DSEI) Leste de Roraima**.

O aplicativo substitui o tradicional **disco físico de papelão da Organização Mundial da Saúde (OMS)** por uma ferramenta digital moderna, rápida, à prova d'água e **100% offline**, garantindo a precisão no cálculo das curvas de crescimento infantil e mitigando erros de interpretação em campo.

---

## 🌍 Contexto e Problema

Nos territórios indígenas e comunidades ribeirinhas da Amazônia:
1. **Ausência de Conectividade:** Não há cobertura contínua de internet (3G/4G/Wi-Fi). Sistemas dependentes de nuvem tornam-se inoperantes.
2. **Desgaste do Disco Físico:** O disco impresso da OMS sofre danos severos por umidade, chuva e atrito durante deslocamentos fluviais e terrestres, borrando números e escalas.
3. **Cálculo de Idade Complexo:** Calcular com precisão a idade em meses a partir da data de nascimento sob estresse de campo pode gerar erros de corte nas curvas de escore-Z.
4. **Perda de Histórico:** O registro manuscrito em fichas de papel frequentemente é extraviado ou danificado antes de ser tabulado no SISVAN.

O **NutriDisco** elimina essas barreiras ao executar localmente todas as avaliações com dados persistidos em SQLite/Room, gerando relatórios em **PDF** e planilhas **CSV** para sincronização ou entrega nos polos base.

---

## ✨ Principais Funcionalidades

### ⚡ 1. Avaliação Rápida em 2 Passos
* **Etapa 1:** Seleção intuitiva do sexo biológico (Menina / Menino) com cartões tácteis de alta legibilidade.
* **Etapa 2:** Entrada da data de nascimento com máscara dinâmica (`dd/mm/aaaa`), cálculo imediato da idade em meses completos e inserção de peso (kg) e estatura (cm) com suporte flexível a vírgula ou ponto.
* **Validações Instantâneas:** Bloqueio de datas futuras e validação de limites antropométricos.

### 📊 2. Motor de Classificação Nutricional da OMS
* Cálculo em tempo real dos índices antropométricos:
  * **Estatura para Idade (E/I):** Muito baixa estatura, Baixa estatura, Estatura adequada.
  * **Peso para Idade (P/I):** Muito baixo peso, Baixo peso, Peso adequado, Peso elevado.
* Alertas visuais semânticos de risco (**Verde**, **Amarelo**, **Alto Risco / Vermelho**).
* Visualização detalhada das referências esperadas (cortes de -3, -2 e +2 desvios padrão).

### 📝 3. Cadastro Atômico e Gestão de Crianças
* Possibilidade de salvar o atendimento gerando a ficha da criança (Nome, CNS, Responsável, Polo Base e Comunidade).
* **Transação Atômica:** Cadastro da criança e sua avaliação inicial são persistidos juntos (`database.withTransaction`). Em caso de falha ou CNS duplicado, nada é gravado em estado incompleto.
* Criação de comunidades dinâmicas (*on-the-fly*): o profissional pode selecionar aldeias existentes ou cadastrar novas comunidades vinculadas ao Polo Base atual.

### 📈 4. Linha do Tempo e Perfil Clínico
* Histórico longitudinal de avaliações por criança, permitindo monitorar a recuperação nutricional ou a desaceleração de ganho ponderal.
* Realização de novas avaliações subsequentes com reaproveitamento automático dos dados cadastrais.
* Edição retroativa de dados e registros com recálculo automático da classificação.
* Proteção contra registros órfãos: caso a única avaliação de uma criança seja excluída, o cadastro também é higienizado.

### 📑 5. Exportação Flexível de Dados (PDF & CSV)
* **Relatórios PDF em A4:** Geração local de documentos paginados com cabeçalho institucional, tabela organizada e quebras automáticas de página.
* **Planilhas CSV:** Exportação tabular delimitada por `;`, com encoding UTF-8 (compatível com Excel, LibreOffice Calc e importadores SISVAN).
* **Seleção Modular:** Escolha personalizada de quais colunas exportar (Polo, Comunidade, Profissional, COREN, CNS, Índices, Classificações, etc.).
* **Compartilhamento Nativo:** Integração com Android Share Sheet (Bluetooth, WhatsApp, Google Drive, cabo USB, etc.).

### ⚙️ 6. Identificação Profissional sem Login
* O profissional configura uma única vez seus dados (Nome, COREN, Polo Base ativo).
* Todas as avaliações recebem um carimbo de auditoria com os dados do profissional e território vigentes na data da coleta.

---

## 📏 Regras de Negócio e Metodologia OMS

1. **Faixa Etária Estrita (0 a 59 meses completos):**
   * O padrão antropométrico infantil internacional da OMS destina-se a menores de 5 anos (< 60 meses).
   * A camada de domínio bloqueia estritamente qualquer cálculo ou registro para `>= 60 meses`, lançando `IdadeLimiteExcedidaException`.
2. **Cálculo de Meses:**
   * Utiliza cálculo calendárico exato via `ChronoUnit.MONTHS` a partir da data de nascimento em relação à data do atendimento, garantindo precisão absoluta no corte das curvas.
3. **Proteção contra Falsos-Negativos:**
   * Caso a tabela de referências não contenha o registro exato para o sexo e idade (ex.: banco corrompido), o app dispara `ReferenciaNaoEncontradaException` e avisa o usuário, impedindo classificar inadvertidamente uma criança desnutrida como "adequada".
4. **Seed Pré-carregado:**
   * Contém **240 curvas oficiais da OMS** (60 meses × 2 sexos × 2 parâmetros) e todos os **34 Polos Base e aldeias** mapeadas no DSEI Leste de Roraima.

---

## 🏛 Arquitetura de Software

O NutriDisco adota a **Clean Architecture** combinada com **MVVM (Model-View-ViewModel)** e **UDF (Unidirectional Data Flow)**:

```
com.nutridisco.app
├── data
│   ├── local
│   │   ├── dao             # Room DAOs (Avaliacao, Crianca, Territorio, etc.)
│   │   ├── entity          # Entidades SQLite estruturadas
│   │   ├── migrations      # Migrações manuais versionadas (v1 -> v4)
│   │   ├── model           # Modelos relacionais (@Embedded e @Relation)
│   │   └── seed            # Seed idempotente (seed_nutridisco.sql)
│   └── repository          # Implementações dos repositórios locais
├── domain
│   ├── export              # Gerenciadores de PDF (PdfDocument) e CSV
│   ├── model               # Enums de classificação, riscos e DTOs de negócio
│   ├── repository          # Contratos e interfaces de repositórios
│   ├── usecase             # Casos de uso isolados (ex: CalcularClassificacaoNutricional)
│   └── util                # Cálculos de idade e formatação de datas
├── presentation
│   ├── avaliacaorapida     # Fluxo do disco (Etapas 1 e 2)
│   ├── avaliacoessalvas    # Tela inicial com busca e listagem com badges
│   ├── components          # TopBarPilula, StatusBadge, Máscaras de texto
│   ├── configuracao        # Perfil profissional e território
│   ├── exportacao          # Seletor de colunas e geração de arquivos
│   ├── navigation          # Rotas e grafo com Jetpack Navigation Compose
│   ├── perfil              # Ficha clínica da criança e histórico
│   └── theme               # Cores institucionais, tipografia e Material 3
└── di                      # Módulos de injeção Dagger Hilt
```

### Princípios Chave:
* **Independência de Framework:** A camada `domain` é agnóstica de bibliotecas Android externas, contendo apenas Kotlin puro e lógica médica/antropométrica.
* **Persistência Reativa:** DAOs expõem `Flow<List<T>>`, emitindo atualizações instantâneas na interface com Jetpack Compose.
* **Coroutines & Concorrência:** Operações de banco e arquivos executam exclusivamente em `Dispatchers.IO`.

---

## 🛠 Stack Tecnológica

| Componente | Tecnologia | Versão | Propósito |
| :--- | :--- | :--- | :--- |
| **Linguagem** | Kotlin | `2.0.21` | Linguagem base moderna e concisa |
| **Plataforma** | Android SDK | `minSdk 26` / `targetSdk 34` | Compatibilidade com Android 8.0+ |
| **Interface** | Jetpack Compose | BOM `2024.10.00` | UI declarativa reativa |
| **Design System** | Material 3 | Oficial | Componentes modernos acessíveis |
| **Injeção de Dependência** | Dagger Hilt | `2.51.1` | Injeção robusta com escopo de ViewModel |
| **Banco de Dados** | Room Database | `2.6.1` (KSP) | Abstração SQLite offline com migrations |
| **Navegação** | Navigation Compose | `2.8.3` | Gerenciamento de rotas e pilha de telas |
| **Concorrência** | Coroutines & Flow | `1.9.0` | Processamento assíncrono não-bloqueante |
| **Documentos** | Android PdfDocument | Nativo | Geração de relatórios gráficos vetoriais |
| **Testes Unitários** | JUnit 4 + Coroutines Test | `4.13.2` / `1.9.0` | Validação de domínio e regras médicas |

---

## 🎨 Design System e Interface

O projeto segue fielmente o protótipo desenvolvido no **Figma**, priorizando ergonomia para uso em campo sob luz solar e telas de diferentes densidades:

* **Paleta Institucional:**
  * Fundo da Aplicação: Bege Acolhedor (`#F7F5EE`)
  * Ações Principais e Botões: Verde Floresta (`#2D7A32` / `#26732B`)
  * Verde Institucional: `#1E6B24`
  * Textos: Alto contraste (`#2B2B2B` e `#555555`)
* **TopBar em Pílula (`TopBarPilula`):** Barra de navegação flutuante com cantos curvos (`24.dp`), fundo verde e botões contextuais.
* **Badges de Risco Semântico (`StatusBadge`):** Cores padronizadas para identificação imediata do estado da criança:
  * 🟢 **Adequado** (Verde)
  * 🟡 **Atenção** (Amarelo)
  * 🔴 **Alto Risco** (Vermelho / Alerta de desnutrição ou peso elevado)

---

## 🧪 Testes e Garantia de Qualidade

A integridade clínica e a robustez do banco de dados são validadas por uma suíte abrangente:

* **103 Testes Unitários:**
  * `CalcularClassificacaoNutricionalUseCaseTest`: Avalia os cortes limítrofes exatos de -3, -2 e +2 desvios padrão para ambos os sexos, além do bloqueio para `>= 60 meses`.
  * `IdadeUtilsTest` e `DataUtilsTest`: Cobertura de cálculos bissextos, viradas de mês, fusos e máscaras.
  * `PdfExportManagerTest` e `CsvExportManagerTest`: Validação da geração de cabeçalhos, filtros de colunas e paginação.
  * `AppDatabaseCallbackTest` e `MigrationsTest`: Testes de migração entre versões do banco e integridade do seed SQL.
  * Testes unitários para todos os `ViewModels` da aplicação.
* **Testes Instrumentados:**
  * Validação em SQLite real de transações atômicas com rollback em caso de falha.
  * `MigrationTestHelper` testando a atualização real de esquemas entre versões (v1 a v4).

### Executar os testes:
```bash
# Executar todos os testes unitários
./gradlew test

# Executar testes com relatório detalhado
./gradlew testDebugUnitTest --info
```

---

## 📁 Estrutura do Projeto

```
nutridisco/
├── app/
│   ├── schemas/                    # Histórico versionado dos schemas do Room (1.json a 4.json)
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/database/    # seed_nutridisco.sql (curvas OMS e territórios DSEI)
│   │   │   ├── java/com/nutridisco/app/
│   │   │   │   ├── data/           # Camada de Dados (Local, DAOs, Entidades, Migrações)
│   │   │   │   ├── di/             # Módulos Dagger Hilt
│   │   │   │   ├── domain/         # Camada de Domínio (Modelos, Casos de Uso, Exportadores)
│   │   │   │   └── presentation/   # Camada de UI (Compose, ViewModels, Telas, Componentes)
│   │   │   └── res/                # Recursos, strings, ícones e ilustrações
│   │   ├── test/                   # Suíte de 103 testes unitários
│   │   └── androidTest/            # Testes instrumentados de banco de dados e transações
├── gradle/
│   └── libs.versions.toml          # Version Catalog centralizado do Gradle
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos:
* **Android Studio:** Ladybug (2024.2.1+) ou versão superior.
* **JDK:** Java 17 ou superior configurado no ambiente.
* **Android SDK:** API Level 34 instalada.
* **Dispositivo/Emulador:** Android 8.0 (API 26) ou superior.

### Passo a passo:

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/nutridisco.git
   cd nutridisco
   ```

2. **Abra no Android Studio:**
   * Selecione `Open...` e aponte para a pasta raiz do projeto.
   * Aguarde a sincronização do Gradle e o download das dependências.

3. **Compilar e Executar:**
   * Conecte um dispositivo físico ou inicie um emulador Android.
   * Clique no botão **Run ▶** no Android Studio ou execute via terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📤 Exportação de Dados

O NutriDisco oferece opções avançadas de exportação desenhadas para a rotina de postos de saúde:

```
[Avaliações Salvas] ──> [Filtros / Seleção de Colunas] ──> [Exportar]
                                                              │
                     ┌────────────────────────────────────────┴────────────────────────────────────────┐
                     ▼                                                                                 ▼
             [Relatório em PDF]                                                                [Planilha CSV]
       • Formato A4 Paginado                                                             • Padrão delimitado por ";"
       • Cabeçalho com Polo e Equipe                                                     • Codificação UTF-8
       • Indicadores Semânticos                                                          • Importável no Excel e SISVAN
```

Os arquivos gerados são salvos no cache seguro do aplicativo e compartilhados através do `FileProvider` oficial do Android, garantindo sigilo e compatibilidade com as políticas de privacidade do sistema operacional.

---

## 👥 Licença e Créditos

Projeto desenvolvido com foco na melhoria da atenção à saúde nutricional das populações indígenas e tradicionais.

* **Instituição de Ensino e Pesquisa:** [Universidade Federal do Amazonas (UFAM)](https://ufam.edu.br)
* **Parceria e Território de Aplicação:** Distrito Sanitário Especial Indígena (DSEI) Leste de Roraima.
* **Padrões de Referência:** Curvas de Crescimento Infantil da [Organização Mundial da Saúde (OMS)](https://www.who.int/tools/child-growth-standards).

---

<div align="center">
Desenvolvido com dedicação para a saúde pública infantil na Amazônia. 💚🌱
</div>
