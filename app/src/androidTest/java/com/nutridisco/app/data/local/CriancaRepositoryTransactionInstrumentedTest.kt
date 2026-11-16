package com.nutridisco.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.data.repository.CriancaRepositoryImpl
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.model.DadosNovaAvaliacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CriancaRepositoryTransactionInstrumentedTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: CriancaRepositoryImpl

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = CriancaRepositoryImpl(
            database = database,
            dao = database.criancaDao(),
            comunidadeDao = database.comunidadeDao(),
            avaliacaoDao = database.avaliacaoDao(),
            configuracaoDispositivoDao = database.configuracaoDispositivoDao(),
            ioDispatcher = Dispatchers.Unconfined
        )

        // Seed mínimo de um polo base para vincular comunidades
        runBlocking {
            database.poloBaseDao().inserirTodos(
                listOf(PoloBaseEntity(id = 1L, nome = "Surumu", etnorregiao = "Serras"))
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun cadastrarCriancaComAvaliacao_sucesso_persisteAtomicamenteTodosOsDados() = runBlocking {
        val crianca = CriancaEntity(
            comunidadeId = 0L,
            nome = "Criança Teste",
            sexo = "F",
            dataNascimento = 1600000000000L,
            nomeResponsavel = "Mãe Teste",
            numeroCns = "898000000000001"
        )
        val dadosAvaliacao = DadosNovaAvaliacao(
            peso = 10.5,
            estatura = 82.0,
            idadeMesesMomento = 20,
            classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
            classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
            observacoes = "Primeira consulta de rotina"
        )

        val criancaId = repository.cadastrarCriancaComAvaliacao(
            crianca = crianca,
            poloBaseId = 1L,
            nomeComunidade = "Nova Esperança",
            dadosAvaliacao = dadosAvaliacao
        )

        assertTrue(criancaId > 0)

        // Verifica que a comunidade foi criada e vinculada ao polo 1
        val comunidade = database.comunidadeDao().buscarPorNomeEPolo(1L, "Nova Esperança")
        assertNotNull(comunidade)
        assertEquals("Nova Esperança", comunidade!!.nome)

        // Verifica que a criança foi salva com o id da comunidade correto
        val criancaSalva = database.criancaDao().buscarPorId(criancaId)
        assertNotNull(criancaSalva)
        assertEquals("Criança Teste", criancaSalva!!.nome)
        assertEquals(comunidade.id, criancaSalva.comunidadeId)
        assertEquals("898000000000001", criancaSalva.numeroCns)

        // Verifica que a avaliação foi salva associada à criança e à comunidade
        val avaliacoes = database.avaliacaoDao().listarPorCrianca(criancaId).first()
        assertEquals(1, avaliacoes.size)
        val av = avaliacoes.first()
        assertEquals(criancaId, av.criancaId)
        assertEquals(comunidade.id, av.comunidadeAtendimentoId)
        assertEquals(10.5, av.peso, 0.0)
        assertEquals(82.0, av.estatura, 0.0)
        assertEquals(ClassificacaoEstatura.ESTATURA_ADEQUADA, av.classificacaoEstatura)
        assertEquals(ClassificacaoPeso.PESO_ADEQUADO, av.classificacaoPeso)
        assertEquals("Primeira consulta de rotina", av.observacoes)

        // Verifica que a configuração padrão recebeu o polo base
        val config = database.configuracaoDispositivoDao().getConfiguracao()
        assertNotNull(config)
        assertEquals(1L, config!!.poloBaseIdPadrao)
    }

    @Test
    fun cadastrarCriancaComAvaliacao_violacaoDeCnsUnico_fazRollbackSemSalvarCriancaNemAvaliacao() = runBlocking {
        // Cadastra uma criança com CNS prévio
        val cnsDuplicado = "898000000000002"
        val criancaOriginal = CriancaEntity(
            comunidadeId = 0L,
            nome = "Primeira Criança",
            sexo = "M",
            dataNascimento = 1600000000000L,
            nomeResponsavel = "Responsável 1",
            numeroCns = cnsDuplicado
        )
        repository.cadastrarCriancaComAvaliacao(
            crianca = criancaOriginal,
            poloBaseId = 1L,
            nomeComunidade = "Comunidade Existente",
            dadosAvaliacao = null
        )

        // Tentativa de cadastrar segunda criança com o MESMO CNS
        val criancaComMesmoCns = CriancaEntity(
            comunidadeId = 0L,
            nome = "Segunda Criança Invalida",
            sexo = "F",
            dataNascimento = 1600000000000L,
            nomeResponsavel = "Responsável 2",
            numeroCns = cnsDuplicado
        )
        val avaliacaoPendente = DadosNovaAvaliacao(
            peso = 12.0,
            estatura = 85.0,
            idadeMesesMomento = 22,
            classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
            classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO
        )

        try {
            repository.cadastrarCriancaComAvaliacao(
                crianca = criancaComMesmoCns,
                poloBaseId = 1L,
                nomeComunidade = "Comunidade Invalida",
                dadosAvaliacao = avaliacaoPendente
            )
            fail("Deveria ter lançado SQLiteConstraintException devido ao índice único de CNS.")
        } catch (e: SQLiteConstraintException) {
            // Sucesso esperado: erro de integridade capturado
        }

        // Verifica que apenas a primeira criança existe no banco
        val todasCriancas = database.criancaDao().listarTodasComComunidade().first()
        assertEquals(1, todasCriancas.size)
        assertEquals("Primeira Criança", todasCriancas.first().nome)

        // Verifica que nenhuma avaliação foi persistida para a segunda criança
        val todasAvaliacoes = database.avaliacaoDao().listarTodasDetalhadasParaExportacao()
        assertEquals(0, todasAvaliacoes.size)
    }

    @Test
    fun cadastrarCriancaComAvaliacao_semAvaliacao_persisteSomenteCriancaEComunidade() = runBlocking {
        val crianca = CriancaEntity(
            comunidadeId = 0L,
            nome = "Criança Apenas Cadastro",
            sexo = "M",
            dataNascimento = 1600000000000L,
            nomeResponsavel = "Pai Teste",
            numeroCns = null
        )

        val criancaId = repository.cadastrarCriancaComAvaliacao(
            crianca = crianca,
            poloBaseId = 1L,
            nomeComunidade = "Comunidade Isolada",
            dadosAvaliacao = null
        )

        assertTrue(criancaId > 0)
        val criancaSalva = database.criancaDao().buscarPorId(criancaId)
        assertNotNull(criancaSalva)
        assertEquals("Criança Apenas Cadastro", criancaSalva!!.nome)

        // Avaliações continuam vazias
        val avaliacoes = database.avaliacaoDao().listarPorCrianca(criancaId).first()
        assertTrue(avaliacoes.isEmpty())
    }
}
