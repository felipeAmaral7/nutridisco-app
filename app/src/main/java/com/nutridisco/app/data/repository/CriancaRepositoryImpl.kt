package com.nutridisco.app.data.repository

import androidx.room.withTransaction
import com.nutridisco.app.data.local.AppDatabase
import com.nutridisco.app.data.local.dao.AvaliacaoDao
import com.nutridisco.app.data.local.dao.ComunidadeDao
import com.nutridisco.app.data.local.dao.ConfiguracaoDispositivoDao
import com.nutridisco.app.data.local.dao.CriancaDao
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.di.IoDispatcher
import com.nutridisco.app.domain.model.DadosNovaAvaliacao
import com.nutridisco.app.domain.repository.CriancaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CriancaRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: CriancaDao,
    private val comunidadeDao: ComunidadeDao,
    private val avaliacaoDao: AvaliacaoDao,
    private val configuracaoDispositivoDao: ConfiguracaoDispositivoDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CriancaRepository {

    override fun listarCriancas(): Flow<List<CriancaComComunidade>> =
        dao.listarTodasComComunidade()

    override fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?> =
        dao.buscarPorIdComComunidadeFlow(id)

    override suspend fun buscarCriancaPorId(id: Long): CriancaEntity? =
        withContext(ioDispatcher) {
            dao.buscarPorId(id)
        }

    override suspend fun buscarCriancaPorCns(cns: String): CriancaEntity? =
        withContext(ioDispatcher) {
            dao.buscarPorCns(cns)
        }

    override fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>> =
        dao.pesquisarPorNome(termo)

    override suspend fun salvarCrianca(crianca: CriancaEntity): Long =
        withContext(ioDispatcher) {
            dao.inserir(crianca)
        }

    override suspend fun cadastrarCriancaComAvaliacao(
        crianca: CriancaEntity,
        poloBaseId: Long,
        nomeComunidade: String,
        dadosAvaliacao: DadosNovaAvaliacao?
    ): Long = withContext(ioDispatcher) {
        database.withTransaction {
            // 1. Obter ou criar comunidade vinculada ao poloBaseId informado
            val nomeNormalizado = nomeComunidade.trim()
            val existente = comunidadeDao.buscarPorNomeEPolo(poloBaseId, nomeNormalizado)
            val comunidadeId = existente?.id ?: comunidadeDao.inserir(
                ComunidadeEntity(
                    poloBaseId = poloBaseId,
                    nome = nomeNormalizado
                )
            )

            // 2. Salvar Criança associada à comunidadeId
            val criancaId = dao.inserir(
                crianca.copy(comunidadeId = comunidadeId)
            )

            // 3. Garantir configuração padrão do dispositivo vinculada ao Polo Base
            val configAtual = configuracaoDispositivoDao.getConfiguracao()
            val config = if (configAtual == null) {
                val novaConfig = ConfiguracaoDispositivoEntity(
                    id = 1L,
                    nomeProfissional = "Profissional Local",
                    registroCoren = "Não informado",
                    poloBaseIdPadrao = poloBaseId
                )
                configuracaoDispositivoDao.salvarConfiguracao(novaConfig)
                novaConfig
            } else if (configAtual.poloBaseIdPadrao == null) {
                val configAtualizada = configAtual.copy(poloBaseIdPadrao = poloBaseId)
                configuracaoDispositivoDao.salvarConfiguracao(configAtualizada)
                configAtualizada
            } else {
                configAtual
            }

            // 4. Se houver dados de avaliação pendente, salvar avaliação associada atomicamente
            if (dadosAvaliacao != null) {
                avaliacaoDao.inserir(
                    AvaliacaoEntity(
                        criancaId = criancaId,
                        comunidadeAtendimentoId = comunidadeId,
                        dataAtendimento = dadosAvaliacao.dataAtendimento,
                        idadeMesesMomento = dadosAvaliacao.idadeMesesMomento,
                        peso = dadosAvaliacao.peso,
                        estatura = dadosAvaliacao.estatura,
                        classificacaoEstatura = dadosAvaliacao.classificacaoEstatura,
                        classificacaoPeso = dadosAvaliacao.classificacaoPeso,
                        observacoes = dadosAvaliacao.observacoes?.trim()?.ifEmpty { null },
                        nomeProfissional = config.nomeProfissional,
                        registroCoren = config.registroCoren
                    )
                )
            }

            criancaId
        }
    }

    override suspend fun atualizarCrianca(crianca: CriancaEntity) =
        withContext(ioDispatcher) {
            dao.atualizar(crianca)
        }

    override suspend fun deletarCrianca(crianca: CriancaEntity) =
        withContext(ioDispatcher) {
            dao.deletar(crianca)
        }
}
