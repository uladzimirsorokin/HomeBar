package com.sorokinuladzimir.homebarassistant.core.di

import com.sorokinuladzimir.homebarassistant.ui.utils.ImageHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideImageHandler(): ImageHandler = ImageHandler()
}
