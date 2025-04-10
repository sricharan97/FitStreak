package com.apptimistiq.android.fitstreak.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

/**
 * A [ViewModelProvider.Factory] implementation that uses Dagger to create ViewModel instances.
 * 
 * This factory allows ViewModels to be created with dependencies provided by Dagger's dependency
 * injection system. It works with a map of ViewModel types to their Provider instances.
 */
class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given ViewModel class.
     * 
     * @param modelClass The class of the ViewModel to create an instance of
     * @return A new instance of the ViewModel
     * @throws IllegalArgumentException if the ViewModel class is not found in the creators map
     * @throws RuntimeException if there's an error during ViewModel instantiation
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Find the provider for the requested ViewModel class
        var creator: Provider<out ViewModel>? = creators[modelClass]
        
        // If not found directly, look for a compatible class
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        
        // Throw exception if no provider is found
        if (creator == null) {
            throw IllegalArgumentException("Unknown model class: $modelClass")
        }
        
        // Create and return the ViewModel instance
        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

/**
 * Dagger module that provides bindings for ViewModelFactory.
 * 
 * This module binds the custom ViewModelFactory to Dagger's dependency graph,
 * allowing it to be injected where a ViewModelProvider.Factory is required.
 */
@Module
abstract class ViewModelBuilderModule {

    /**
     * Binds the custom ViewModelFactory to be provided when ViewModelProvider.Factory is required.
     * 
     * @param factory The ViewModelFactory instance to be provided
     * @return A ViewModelProvider.Factory instance
     */
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

/**
 * Custom annotation for ViewModel key in multibinding.
 * 
 * This annotation is used in conjunction with Dagger's @IntoMap to bind ViewModel classes
 * to their providers, making them available through the ViewModelFactory.
 * 
 * @property value The ViewModel class that will be used as the key in the multibinding map
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
