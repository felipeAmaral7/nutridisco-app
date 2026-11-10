package com.nutridisco.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/**
 * Fonte única da data/hora "atual" usada no cálculo de idade.
 * Injetada para permitir testes com data fixa (e, futuramente, a validação do relógio do aparelho).
 */
@Module
@InstallIn(SingletonComponent::class)
object TempoModule {

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
