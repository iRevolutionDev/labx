package software.revolution.labx.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import software.revolution.labx.data.repository.EditorPreferencesRepository
import software.revolution.labx.data.repository.EditorPreferencesRepositoryImpl
import software.revolution.labx.data.repository.FileRepository
import software.revolution.labx.data.repository.FileRepositoryImpl
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("app_preferences")
        }
    }

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun provideEditorPreferencesRepository(dataStore: DataStore<Preferences>): EditorPreferencesRepository {
        return EditorPreferencesRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideFileRepository(@IoDispatcher dispatcher: CoroutineDispatcher): FileRepository {
        return FileRepositoryImpl(dispatcher)
    }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher