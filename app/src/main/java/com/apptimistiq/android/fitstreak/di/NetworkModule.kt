package com.apptimistiq.android.fitstreak.di

import com.apptimistiq.android.fitstreak.network.SpoonacularApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

//Base URL to interact with the Spoonacular Web service
private const val BASE_URL = "https://api.spoonacular.com//"

@Module
object NetworkModule {

    // @Provides tell Dagger how to create instances of the type that this function
    // returns (i.e. SpoonacularApiService).
    // Function parameters are the dependencies of this type
    @JvmStatic
    @Singleton
    @Provides
    fun provideSpoonacularRetrofitService(moshi: Moshi): SpoonacularApiService {

        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()
            .create(SpoonacularApiService::class.java)


    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideConverterFactory(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
}