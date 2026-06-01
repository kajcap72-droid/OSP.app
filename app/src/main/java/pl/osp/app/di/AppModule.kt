package pl.osp.app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.osp.app.data.remote.EremizaApi
import pl.osp.app.data.repository.MockRemizaRepository
import pl.osp.app.data.repository.RemizaRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Domyślnie aplikacja używa Mocka. Aby przełączyć na realne API:
     * zmień `MockRemizaRepository` na `RemoteRemizaRepository`.
     */
    @Binds @Singleton
    abstract fun bindRepository(impl: MockRemizaRepository): RemizaRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        // PODMIEŃ na URL realnego API jednostki/dostawcy:
        .baseUrl("https://example.invalid/api/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides @Singleton
    fun provideApi(retrofit: Retrofit): EremizaApi = retrofit.create(EremizaApi::class.java)
}
