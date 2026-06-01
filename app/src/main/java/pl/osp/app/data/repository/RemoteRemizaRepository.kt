package pl.osp.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.osp.app.data.model.*
import pl.osp.app.data.remote.EremizaApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Szkielet zdalnego repozytorium. Wszystkie metody są oznaczone TODO — gdy
 * uzyskasz legalny dostęp do realnego API jednostki, uzupełnij implementację
 * i podmień w [pl.osp.app.di.RepositoryModule.provideRepository].
 *
 * NIE używaj do reverse-engineeringu serwisów, do których nie masz uprawnień.
 */
@Singleton
class RemoteRemizaRepository @Inject constructor(
    private val api: EremizaApi
) : RemizaRepository {

    override suspend fun login(email: String, password: String): Result<UserProfile> =
        runCatching { TODO("Mapuj odpowiedź api.login(...) na UserProfile") }

    override suspend fun logout() { /* TODO clear token */ }
    override fun currentUser(): Flow<UserProfile?> = flowOf(null)

    override fun observeAlarms(): Flow<List<Alarm>> = flowOf(emptyList())
    override suspend fun refreshAlarms() { TODO() }
    override suspend fun getAlarm(id: String): Alarm? = TODO()
    override suspend fun setMyDisposition(alarmId: String, disposition: Disposition) { TODO() }

    override fun observeMembers(): Flow<List<Member>> = flowOf(emptyList())
    override suspend fun getMember(id: String): Member? = null
    override fun observeEvents(): Flow<List<CalendarEvent>> = flowOf(emptyList())
    override fun observeEquipment(): Flow<List<Equipment>> = flowOf(emptyList())
    override fun observeVehicles(): Flow<List<Vehicle>> = flowOf(emptyList())
    override fun observeDocuments(): Flow<List<Document>> = flowOf(emptyList())

    override suspend fun getYearStats(year: Int): YearStats = TODO()
}
