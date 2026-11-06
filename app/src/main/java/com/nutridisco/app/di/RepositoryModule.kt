package com.nutridisco.app.di

import com.nutridisco.app.data.repository.AvaliacaoRepositoryImpl
import com.nutridisco.app.data.repository.ConfiguracaoRepositoryImpl
import com.nutridisco.app.data.repository.CriancaRepositoryImpl
import com.nutridisco.app.data.repository.ReferenciaAntropometricaRepositoryImpl
import com.nutridisco.app.data.repository.TerritorioRepositoryImpl
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import com.nutridisco.app.domain.repository.TerritorioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConfiguracaoRepository(
        impl: ConfiguracaoRepositoryImpl
    ): ConfiguracaoRepository

    @Binds
    @Singleton
    abstract fun bindTerritorioRepository(
        impl: TerritorioRepositoryImpl
    ): TerritorioRepository

    @Binds
    @Singleton
    abstract fun bindCriancaRepository(
        impl: CriancaRepositoryImpl
    ): CriancaRepository

    @Binds
    @Singleton
    abstract fun bindAvaliacaoRepository(
        impl: AvaliacaoRepositoryImpl
    ): AvaliacaoRepository

    @Binds
    @Singleton
    abstract fun bindReferenciaAntropometricaRepository(
        impl: ReferenciaAntropometricaRepositoryImpl
    ): ReferenciaAntropometricaRepository
}
