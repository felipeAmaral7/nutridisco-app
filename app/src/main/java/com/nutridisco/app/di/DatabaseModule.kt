package com.nutridisco.app.di

import android.content.Context
import androidx.room.Room
import com.nutridisco.app.data.local.AppDatabase
import com.nutridisco.app.data.local.dao.AvaliacaoDao
import com.nutridisco.app.data.local.dao.ComunidadeDao
import com.nutridisco.app.data.local.dao.ConfiguracaoDispositivoDao
import com.nutridisco.app.data.local.dao.CriancaDao
import com.nutridisco.app.data.local.dao.PoloBaseDao
import com.nutridisco.app.data.local.dao.ReferenciaAntropometricaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(com.nutridisco.app.data.local.seed.AppDatabaseCallback(context))
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideConfiguracaoDispositivoDao(database: AppDatabase): ConfiguracaoDispositivoDao =
        database.configuracaoDispositivoDao()

    @Provides
    fun providePoloBaseDao(database: AppDatabase): PoloBaseDao =
        database.poloBaseDao()

    @Provides
    fun provideComunidadeDao(database: AppDatabase): ComunidadeDao =
        database.comunidadeDao()

    @Provides
    fun provideCriancaDao(database: AppDatabase): CriancaDao =
        database.criancaDao()

    @Provides
    fun provideAvaliacaoDao(database: AppDatabase): AvaliacaoDao =
        database.avaliacaoDao()

    @Provides
    fun provideReferenciaAntropometricaDao(database: AppDatabase): ReferenciaAntropometricaDao =
        database.referenciaAntropometricaDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
