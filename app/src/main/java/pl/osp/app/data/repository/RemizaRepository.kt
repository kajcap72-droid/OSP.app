package pl.osp.app.data.repository

import kotlinx.coroutines.flow.Flow
import pl.osp.app.data.model.*

/**
 * Główna abstrakcja danych.
 *
 * Ma dwie implementacje:
 *  - [MockRemizaRepository]   — działa od razu, bez backendu (dane przykładowe)
 *  - [RemoteRemizaRepository] — szkielet pod realne API (Retrofit). Do podpięcia po
 *    uzyskaniu legalnego dostępu do API jednostki / e-remiza.
 *
 * Przełączasz w [pl.osp.app.di.RepositoryModule].
 */
interface RemizaRepository {

    // Sesja
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun logout()
    fun currentUser(): Flow<UserProfile?>

    // Alarmy
    fun observeAlarms(): Flow<List<Alarm>>
    suspend fun refreshAlarms()
    suspend fun getAlarm(id: String): Alarm?
    suspend fun setMyDisposition(alarmId: String, disposition: Disposition)

    // Członkowie
    fun observeMembers(): Flow<List<Member>>
    suspend fun getMember(id: String): Member?

    // Kalendarz
    fun observeEvents(): Flow<List<CalendarEvent>>

    // Sprzęt
    fun observeEquipment(): Flow<List<Equipment>>

    // Pojazdy
    fun observeVehicles(): Flow<List<Vehicle>>

    // Dokumenty
    fun observeDocuments(): Flow<List<Document>>

    // Statystyki
    suspend fun getYearStats(year: Int): YearStats
}
