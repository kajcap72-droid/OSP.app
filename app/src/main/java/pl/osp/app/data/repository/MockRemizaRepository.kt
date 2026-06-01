package pl.osp.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import pl.osp.app.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Implementacja repozytorium oparta o dane przykładowe.
 * Pozwala uruchomić aplikację bez backendu.
 */
@Singleton
class MockRemizaRepository @Inject constructor() : RemizaRepository {

    private val user = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "me",
            name = "Jan Kowalski",
            unit = "OSP Demo",
            rank = MemberRank.RATOWNIK,
            avatarUrl = null
        )
    )

    private val alarms = MutableStateFlow(MockData.alarms())
    private val members = MutableStateFlow(MockData.members())
    private val events = MutableStateFlow(MockData.events())
    private val equipment = MutableStateFlow(MockData.equipment())
    private val vehicles = MutableStateFlow(MockData.vehicles())
    private val documents = MutableStateFlow(MockData.documents())

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        val profile = UserProfile("me", "Jan Kowalski", "OSP Demo", MemberRank.RATOWNIK, null)
        user.value = profile
        return Result.success(profile)
    }

    override suspend fun logout() { user.value = null }
    override fun currentUser(): StateFlow<UserProfile?> = user

    override fun observeAlarms(): StateFlow<List<Alarm>> = alarms
    override suspend fun refreshAlarms() { /* no-op in mock */ }
    override suspend fun getAlarm(id: String): Alarm? = alarms.value.find { it.id == id }

    override suspend fun setMyDisposition(alarmId: String, disposition: Disposition) {
        alarms.update { list ->
            list.map { a ->
                if (a.id == alarmId) a.copy(myDisposition = disposition) else a
            }
        }
    }

    /**
     * Wstrzykuje świeży alarm na początek listy (używane przez przycisk
     * „Symuluj alarm" do testu powiadomień push).
     */
    fun pushFakeAlarm(): Alarm {
        val templates = MockData.alarmTemplates()
        val template = templates[Random.nextInt(templates.size)]
        val alarm = template.copy(
            id = "sim-${System.currentTimeMillis()}",
            dispatchedAt = LocalDateTime.now(),
            status = AlarmStatus.ACTIVE,
            myDisposition = Disposition.NONE
        )
        alarms.update { listOf(alarm) + it }
        return alarm
    }

    override fun observeMembers(): StateFlow<List<Member>> = members
    override suspend fun getMember(id: String): Member? = members.value.find { it.id == id }
    override fun observeEvents(): StateFlow<List<CalendarEvent>> = events
    override fun observeEquipment(): StateFlow<List<Equipment>> = equipment
    override fun observeVehicles(): StateFlow<List<Vehicle>> = vehicles
    override fun observeDocuments(): StateFlow<List<Document>> = documents

    override suspend fun getYearStats(year: Int): YearStats {
        val list = alarms.value
        return YearStats(
            year = year,
            totalAlarms = list.size,
            fires = list.count { it.type == AlarmType.FIRE },
            localThreats = list.count { it.type == AlarmType.LOCAL },
            falseAlarms = list.count { it.type == AlarmType.FALSE_ALARM },
            exercises = list.count { it.type == AlarmType.EXERCISE },
            averageResponseSeconds = 285,
            topResponders = listOf(
                "Jan Kowalski" to 47,
                "Piotr Nowak" to 41,
                "Adam Wiśniewski" to 38,
                "Krzysztof Wójcik" to 35,
                "Tomasz Lewandowski" to 29
            )
        )
    }
}

/** Statyczne dane przykładowe. */
private object MockData {

    /** Współrzędne remizy (centrum mapy). */
    val STATION_LAT = 52.2297
    val STATION_LON = 21.0122

    fun alarmTemplates(): List<Alarm> = listOf(
        Alarm(
            id = "tpl1",
            type = AlarmType.FIRE,
            title = "Pożar samochodu osobowego",
            description = "Pożar pojazdu w wyniku zwarcia instalacji elektrycznej. " +
                "Zagrożenie rozprzestrzenienia na sąsiednie pojazdy na parkingu.",
            address = "Parking ul. Targowa 15",
            latitude = 52.2350, longitude = 21.0205,
            dispatchedAt = LocalDateTime.now(),
            finishedAt = null,
            status = AlarmStatus.ACTIVE,
            vehicles = listOf("GBA 2,5/16 MAN")
        ),
        Alarm(
            id = "tpl2",
            type = AlarmType.LOCAL,
            title = "Kolizja drogowa",
            description = "Zderzenie dwóch pojazdów. Jedna osoba poszkodowana zakleszczona w aucie.",
            address = "DK 7, km 24+100",
            latitude = 52.2120, longitude = 20.9890,
            dispatchedAt = LocalDateTime.now(),
            finishedAt = null,
            status = AlarmStatus.ACTIVE,
            vehicles = listOf("GBA 2,5/16 MAN", "SLOp Ford Ranger")
        ),
        Alarm(
            id = "tpl3",
            type = AlarmType.LOCAL,
            title = "Podtopienia po ulewie",
            description = "Zalana piwnica domu jednorodzinnego. Potrzebne wypompowywanie wody.",
            address = "ul. Łąkowa 8",
            latitude = 52.2455, longitude = 21.0380,
            dispatchedAt = LocalDateTime.now(),
            finishedAt = null,
            status = AlarmStatus.ACTIVE,
            vehicles = listOf("GBA 2,5/16 MAN")
        )
    )

    fun alarms(): List<Alarm> {
        val now = LocalDateTime.now()
        return listOf(
            Alarm(
                id = "a1",
                type = AlarmType.FIRE,
                title = "Pożar budynku gospodarczego",
                description = "Pożar stodoły, zagrożenie rozprzestrzenienia na budynek mieszkalny.",
                address = "ul. Wiejska 12, Demo",
                latitude = 52.2297, longitude = 21.0122,
                dispatchedAt = now.minusHours(2),
                finishedAt = null,
                status = AlarmStatus.IN_PROGRESS,
                vehicles = listOf("GBA 2,5/16 MAN", "SLOp Ford Ranger"),
                responders = listOf(
                    Responder("m1", "Jan Kowalski", Disposition.GOING, now.minusHours(2).plusMinutes(1)),
                    Responder("m2", "Piotr Nowak", Disposition.GOING, now.minusHours(2).plusMinutes(2)),
                    Responder("m3", "Adam Wiśniewski", Disposition.LATER, now.minusHours(2).plusMinutes(4)),
                    Responder("m4", "Krzysztof Wójcik", Disposition.NOT_GOING, now.minusHours(2).plusMinutes(3))
                ),
                myDisposition = Disposition.NONE
            ),
            Alarm(
                id = "a2",
                type = AlarmType.LOCAL,
                title = "Drzewo na drodze",
                description = "Powalone drzewo blokuje pas ruchu po wczorajszej burzy.",
                address = "DW 719, km 12+400",
                latitude = 52.2010, longitude = 20.9500,
                dispatchedAt = now.minusDays(1).withHour(7).withMinute(15),
                finishedAt = now.minusDays(1).withHour(8).withMinute(40),
                status = AlarmStatus.FINISHED,
                vehicles = listOf("GBA 2,5/16 MAN"),
                responders = emptyList()
            ),
            Alarm(
                id = "a3",
                type = AlarmType.LOCAL,
                title = "Wyciek substancji ropopochodnej",
                description = "Plama oleju po kolizji drogowej.",
                address = "Skrzyżowanie ul. Kościuszki / ul. Polna",
                latitude = 52.2500, longitude = 21.0500,
                dispatchedAt = now.minusDays(3),
                finishedAt = now.minusDays(3).plusHours(2),
                status = AlarmStatus.FINISHED,
                vehicles = listOf("GBA 2,5/16 MAN")
            ),
            Alarm(
                id = "a4",
                type = AlarmType.EXERCISE,
                title = "Ćwiczenia z PSP",
                description = "Ćwiczenia zgrywające z JRG nr 3.",
                address = "Plac ćwiczeń JRG 3",
                latitude = null, longitude = null,
                dispatchedAt = now.minusDays(7),
                finishedAt = now.minusDays(7).plusHours(4),
                status = AlarmStatus.FINISHED,
                vehicles = listOf("GBA 2,5/16 MAN", "SLOp Ford Ranger")
            ),
            Alarm(
                id = "a5",
                type = AlarmType.FALSE_ALARM,
                title = "Alarm fałszywy w dobrej wierze",
                description = "Wezwanie do pożaru – okazało się, że było to ognisko ogrodowe.",
                address = "ul. Sosnowa 4",
                latitude = 52.2700, longitude = 21.0800,
                dispatchedAt = now.minusDays(10),
                finishedAt = now.minusDays(10).plusMinutes(45),
                status = AlarmStatus.FINISHED
            )
        )
    }

    fun members(): List<Member> = listOf(
        Member("m1", "Jan", "Kowalski", MemberRank.RATOWNIK, "+48 600 100 200", "jan@osp.pl",
            LocalDate.of(1988, 4, 12), LocalDate.of(2010, 5, 1), true,
            listOf(
                Qualification("KPP", LocalDate.of(2023, 3, 1), LocalDate.of(2026, 3, 1)),
                Qualification("Kurs podstawowy", LocalDate.of(2011, 6, 10), null),
                Qualification("Prawo jazdy kat. C", LocalDate.of(2012, 9, 1), LocalDate.of(2027, 9, 1))
            ),
            LocalDate.now().plusMonths(2)
        ),
        Member("m2", "Piotr", "Nowak", MemberRank.DOWODCA_SEKCJI, "+48 600 100 201", "piotr@osp.pl",
            LocalDate.of(1985, 1, 30), LocalDate.of(2005, 9, 1), true,
            listOf(
                Qualification("Dowódcy", LocalDate.of(2018, 10, 1), null),
                Qualification("KPP", LocalDate.of(2022, 11, 1), LocalDate.of(2025, 11, 1))
            ),
            LocalDate.now().plusMonths(5)
        ),
        Member("m3", "Adam", "Wiśniewski", MemberRank.STARSZY_RATOWNIK, "+48 600 100 202", null,
            LocalDate.of(1992, 7, 22), LocalDate.of(2014, 3, 1), true,
            listOf(Qualification("Kurs podstawowy", LocalDate.of(2014, 12, 1), null)),
            LocalDate.now().plusDays(20)
        ),
        Member("m4", "Krzysztof", "Wójcik", MemberRank.DRUH, "+48 600 100 203", null,
            LocalDate.of(2002, 11, 5), LocalDate.of(2022, 1, 1), true,
            emptyList(), null
        ),
        Member("m5", "Tomasz", "Lewandowski", MemberRank.NACZELNIK, "+48 600 100 204", "naczelnik@osp.pl",
            LocalDate.of(1978, 2, 18), LocalDate.of(1998, 5, 1), true,
            listOf(
                Qualification("Naczelnik OSP", LocalDate.of(2015, 4, 1), null),
                Qualification("KPP", LocalDate.of(2024, 1, 10), LocalDate.of(2027, 1, 10))
            ),
            LocalDate.now().plusYears(1)
        ),
        Member("m6", "Marek", "Kamiński", MemberRank.PREZES, "+48 600 100 205", "prezes@osp.pl",
            LocalDate.of(1972, 6, 1), LocalDate.of(1995, 3, 1), true, emptyList(), null)
    )

    fun events(): List<CalendarEvent> {
        val today = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).withNano(0)
        return listOf(
            CalendarEvent("e1", EventType.TRAINING, "Szkolenie z KPP",
                "Powtórka zasad pierwszej pomocy. Obowiązkowe dla ratowników.",
                today.plusDays(2), today.plusDays(2).plusHours(3),
                listOf("m1", "m2", "m3", "m5"), "Remiza – sala szkoleniowa"),
            CalendarEvent("e2", EventType.MEETING, "Zbiórka miesięczna",
                "Omówienie spraw bieżących.", today.plusDays(7), today.plusDays(7).plusHours(2),
                emptyList(), "Remiza"),
            CalendarEvent("e3", EventType.MAINTENANCE, "Przegląd aparatów ODO",
                "Cykliczna kontrola aparatów powietrznych.",
                today.plusDays(14), today.plusDays(14).plusHours(4),
                listOf("m2", "m5"), "Remiza – garaż"),
            CalendarEvent("e4", EventType.DUTY, "Dyżur weekendowy",
                "Pełnienie dyżuru bojowego w remizie.",
                today.plusDays(5).withHour(8), today.plusDays(6).withHour(8),
                listOf("m1", "m3"), "Remiza"),
            CalendarEvent("e5", EventType.COMPETITION, "Zawody gminne",
                "Reprezentujemy jednostkę.",
                today.plusDays(30).withHour(10), today.plusDays(30).withHour(16),
                listOf("m1", "m2", "m3", "m4"), "Stadion w Demowicach")
        )
    }

    fun equipment(): List<Equipment> = listOf(
        Equipment("eq1", "Aparat ODO Drager #1", EquipmentCategory.PPV, "DR-2019-001",
            LocalDate.of(2019, 5, 1), LocalDate.now().plusMonths(3), "Garaż – szafa A", null, true),
        Equipment("eq2", "Aparat ODO Drager #2", EquipmentCategory.PPV, "DR-2019-002",
            LocalDate.of(2019, 5, 1), LocalDate.now().plusDays(15), "Garaż – szafa A",
            "Wkrótce przegląd!", true),
        Equipment("eq3", "Wąż W-75 #4", EquipmentCategory.HOSE, null,
            LocalDate.of(2021, 3, 1), LocalDate.now().plusMonths(8), "Garaż – regał B", null, true),
        Equipment("eq4", "Pompa szlamowa Honda", EquipmentCategory.PUMP, "HD-2020-77",
            LocalDate.of(2020, 7, 12), LocalDate.now().plusMonths(2), "Garaż – stanowisko 3",
            "Wymienić olej po następnym wyjeździe", true),
        Equipment("eq5", "Zestaw narzędzi hydraulicznych Lukas",
            EquipmentCategory.HYDRAULIC, "LK-2022-01",
            LocalDate.of(2022, 11, 1), LocalDate.now().plusMonths(6), "GBA – skrytka 4", null, true),
        Equipment("eq6", "Maszt oświetleniowy", EquipmentCategory.LIGHTING, "ML-2018-22",
            LocalDate.of(2018, 1, 1), LocalDate.now().minusDays(10), "Garaż", "PRZEGLĄD ZALEGŁY", false)
    )

    fun vehicles(): List<Vehicle> = listOf(
        Vehicle("v1", "GBA 2,5/16", "WX 12345", "MAN TGM 13.290", 2019,
            LocalDate.now().plusMonths(5), LocalDate.now().plusMonths(3),
            45_120, 90, true),
        Vehicle("v2", "SLOp", "WX 54321", "Ford Ranger 3.2 TDCi", 2017,
            LocalDate.now().plusMonths(8), LocalDate.now().plusMonths(7),
            87_500, 75, true)
    )

    fun documents(): List<Document> = listOf(
        Document("d1", "Statut OSP.pdf", "Dokumenty założycielskie",
            LocalDateTime.now().minusMonths(6), 245_000L, "application/pdf", ""),
        Document("d2", "Regulamin organizacyjny.pdf", "Regulaminy",
            LocalDateTime.now().minusMonths(3), 180_000L, "application/pdf", ""),
        Document("d3", "Plan szkoleń 2026.xlsx", "Szkolenia",
            LocalDateTime.now().minusDays(20), 32_000L,
            "application/vnd.openxmlformats", ""),
        Document("d4", "Sprawozdanie roczne 2025.pdf", "Sprawozdania",
            LocalDateTime.now().minusDays(60), 1_240_000L, "application/pdf", "")
    )
}
