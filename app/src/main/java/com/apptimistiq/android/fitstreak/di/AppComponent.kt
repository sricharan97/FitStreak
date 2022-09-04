package com.apptimistiq.android.fitstreak.di

import android.content.Context
import com.apptimistiq.android.fitstreak.authentication.di.AuthenticationComponent
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.GoalDataSource
import com.apptimistiq.android.fitstreak.main.progressTrack.di.DailyProgressComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

//Definition of a Dagger component
@Singleton
@Component(
    modules = [
        AppModule::class,
        DatabaseModule::class,
        ViewModelBuilderModule::class,
        SubComponentsModule::class
    ]
)
interface AppComponent {

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }

    fun dailyProgressComponent(): DailyProgressComponent.Factory
    fun authenticationComponent(): AuthenticationComponent.Factory

    val activityDataSource: ActivityDataSource
    val goalDataSource: GoalDataSource

}

@Module(subcomponents = [DailyProgressComponent::class, AuthenticationComponent::class])
object SubComponentsModule