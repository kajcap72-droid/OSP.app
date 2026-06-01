package pl.osp.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Szkielet pod realne API. Endpointy oparte na strukturze sugerowanej przez
 * https://github.com/kapi2289/eremiza-api (2019) — UWAGA: stare i prawdopodobnie
 * nieaktualne. Do uzupełnienia po uzyskaniu legalnego dostępu do aktualnego API
 * własnej jednostki / dostawcy systemu.
 */
interface EremizaApi {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("alarms")
    suspend fun getAlarms(
        @Query("count") count: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<AlarmDto>
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val userId: String)
data class AlarmDto(
    val id: String,
    val type: String,
    val title: String,
    val description: String?,
    val address: String?,
    val lat: Double?,
    val lon: Double?,
    val dispatchedAt: String,
    val finishedAt: String?,
    val status: String
)
