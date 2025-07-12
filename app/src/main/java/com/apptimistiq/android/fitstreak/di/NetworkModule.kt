package com.apptimistiq.android.fitstreak.di

import com.apptimistiq.android.fitstreak.network.SpoonacularApiService
import com.google.firebase.auth.FirebaseAuth
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

/**
 * Base URL for the Spoonacular API
 * Used to configure the Retrofit instance for network requests
 */
private const val BASE_URL = "https://api.spoonacular.com/"

/**
 * Dagger module that provides network-related dependencies for the application.
 * This includes Retrofit service instances and JSON parsing configurations.
 */
@Module
object NetworkModule {

    /**
     * Provides the Moshi instance used for JSON parsing.
     * 
     * @return A configured [Moshi] instance with Kotlin adapter support
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideConverterFactory(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Provides a configured Retrofit service implementation for Spoonacular API.
     * 
     * @param moshi The Moshi instance for JSON serialization/deserialization
     * @return An implementation of [SpoonacularApiService] interface
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideSpoonacularRetrofitService(moshi: Moshi): SpoonacularApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpoonacularApiService::class.java)
    }

    // Add the Firebase Auth provider directly here
    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
