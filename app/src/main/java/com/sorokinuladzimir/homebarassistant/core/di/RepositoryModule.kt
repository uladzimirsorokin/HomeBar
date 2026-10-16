package com.sorokinuladzimir.homebarassistant.core.di

import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.BarDataRepository
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
    abstract fun bindBarData(repository: BarDataRepository): BarData
}
