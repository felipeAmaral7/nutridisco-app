# 📱 System Prompt & Architecture Context: NutriDisco

## 1. Visão Geral do Projeto
**Nome:** NutriDisco
**Plataforma:** Android Nativo (Kotlin)
**Objetivo:** Digitalizar o processo de avaliação nutricional de crianças (0 a 5 anos) em comunidades tradicionais e indígenas (DSEI Leste de Roraima). Substitui o uso de discos físicos de papel (padrão OMS/SISVAN 2006).
**Restrição Principal:** O aplicativo deve funcionar **100% OFFLINE**. Não há backend, API ou login em nuvem. Toda a persistência é local.

## 2. Arquitetura e Stack Recomendada
* **Linguagem:** Kotlin
* **Arquitetura:** MVVM (Model-View-ViewModel) + Clean Architecture.
* **Banco de Dados Local:** Room Database (SQLite).
* **Injeção de Dependência:** Hilt ou Koin.
* **UI:** Jetpack Compose ou XML (conforme as telas exportadas do Figma).
* **Assincronismo:** Kotlin Coroutines & Flow.

## 3. Regras de Negócio e Lógica de Cálculo (O Algoritmo)
O cálculo do Escore-Z **NÃO** deve ser feito com fórmulas matemáticas complexas no código. A aplicação utilizará a abordagem de **Lookup Table** (Tabela de Consulta).
O banco de dados contém uma tabela pré-populada (`referencias_antropometricas`) com os limites exatos da OMS para cada mês de vida.

### Fluxo de Classificação (ViewModel / Domain Layer):
1. Recebe: `idadeEmMeses`, `sexo` ('M' ou 'F'), `peso` e `estatura`.
2. Faz um `SELECT` no Room buscando a referência exata para aquela idade e sexo.
3. Aplica a seguinte lógica de classificação baseada nos valores retornados:

**Regra para ESTATURA/IDADE:**
* `< escore_menos_3` -> "Muito baixa estatura para a idade" (Vermelho)
* `>= escore_menos_3` e `< escore_menos_2` -> "Baixa estatura para a idade" (Amarelo)
* `>= escore_menos_2` -> "Estatura adequada para a idade" (Verde)

**Regra para PESO/IDADE:**
* `< escore_menos_3` -> "Muito baixo peso para a idade" (Vermelho)
* `>= escore_menos_3` e `< escore_menos_2` -> "Baixo peso para a idade" (Amarelo)
* `>= escore_menos_2` e `<= escore_mais_2` -> "Peso adequado para a idade" (Verde)
* `> escore_mais_2` -> "Peso Elevado para a idade" (Vermelho)

## 4. Modelagem de Dados (Room Entities)
O banco de dados foi desenhado para evitar duplicidade e manter rastreabilidade territorial. As datas DEVEM ser armazenadas como `Long` (Timestamp Unix) para facilitar cálculos de idade.

**Tabelas e Relações:**
1. **`ConfiguracaoDispositivo` (Singleton - ID=1 fixo):**
   * *Campos:* `id`, `nome_profissional`, `registro_coren`, `polo_base_id_padrao`.
   * *Papel:* Substitui o "login". Toda nova avaliação herda automaticamente a autoria do profissional configurado aqui.
2. **`PoloBase` (Seed Data pré-carregado):**
   * *Campos:* `id`, `nome`, `etnorregiao`.
3. **`Comunidade`:**
   * *Campos:* `id`, `polo_base_id` (FK), `nome`.
   * *Regra de UI:* O Autocomplete de comunidades no app deve sugerir nomes e permitir criar novas comunidades on-the-fly, vinculando ao Polo Base ativo.
4. **`Crianca`:**
   * *Campos:* `id`, `comunidade_id` (FK), `nome`, `sexo` (M/F), `data_nascimento` (Long), `nome_responsavel`, `numero_cns` (Opcional).
5. **`Avaliacao`:**
   * *Campos:* `id`, `crianca_id` (FK), `comunidade_atendimento_id` (FK), `data_atendimento` (Long), `idade_meses_momento`, `peso`, `estatura`, `classificacao_estatura`, `classificacao_peso`, `observacoes`, `nome_profissional`, `registro_coren`.
   * *Importante:* `nome_profissional` e `registro_coren` são salvos como *Snapshot* nesta tabela para fins de auditoria, mesmo que a configuração do dispositivo mude depois.
6. **`ReferenciaAntropometrica` (Seed Data estático da OMS):**
   * *Campos:* `id`, `tipo_parametro` ('PESO'/'ESTATURA'), `sexo`, `idade_meses`, `escore_menos_3`, `escore_menos_2`, `escore_mais_2`.

## 5. Casos de Uso Principais (Resumo Operacional)
* **Avaliação Rápida:** Tela que permite calcular os índices informando apenas sexo, idade, peso e estatura, sem exigir cadastro prévio.
* **Salvar Registro:** Transforma uma "Avaliação Rápida" em um registro no histórico, exigindo o cadastro da Criança e da Comunidade.
* **Acompanhamento (Linha do Tempo):** O perfil da criança mostra uma lista de avaliações passadas. O usuário pode adicionar uma *Nova Avaliação* a partir desta tela, pulando o preenchimento de dados pessoais e data de nascimento (o app calcula a idade no dia atual automaticamente).
* **Exportação CSV:** O app deve gerar um arquivo `.csv` tabular (usando `ContentResolver` / `MediaStore` ou `FileProvider`) contendo todas as avaliações com as colunas: Polo Base, Comunidade, Profissional, COREN, ID Criança, Sexo, Data Avaliação, Peso, Estatura e Classificações. O app deve acionar a Intent de compartilhamento do Android (ShareSheet) para enviar via Bluetooth, Drive ou WhatsApp.

## 6. Instruções para o AI Coding Agent
* **Contexto de UI:** Receberás os designs do Figma. Foca em criar componentes reutilizáveis, validadores de inputs (peso/altura com precisão decimal) e autocompletes eficientes.
* **Offline-first:** Trata todas as operações de banco local em Dispatchers.IO.
* **Seed Data:** Prepara a infraestrutura do RoomDatabase (Callback ou prepackaged `.db`) para aceitar os Polos Base e as Tabelas Antropométricas no momento da criação do banco.