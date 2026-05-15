package kwiktwik.ratewatch.app.di

import android.content.Context
import kwiktwik.ratewatch.app.data.remote.GoldSilverApi
import kwiktwik.ratewatch.app.data.remote.RetrofitClient
import kwiktwik.ratewatch.app.data.remote.StocksApi
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.util.LanguageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository = PreferencesRepository(context)

    @Provides
    @Singleton
    fun provideLanguageManager(
        prefs: PreferencesRepository
    ): LanguageManager = LanguageManager(prefs)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = RetrofitClient.buildOkHttpClient()

    @Provides
    @Singleton
    fun provideGoldSilverApi(client: OkHttpClient): GoldSilverApi =
        RetrofitClient.buildGoldSilverApi(client)

    @Provides
    @Singleton
    fun provideStocksApi(client: OkHttpClient): StocksApi =
        RetrofitClient.buildStocksApi(client)
}
