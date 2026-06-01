package pl.osp.app.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/** Typ zdarzenia. */
enum class AlarmType(val displayName: String, val icon: String) {
    FIRE("Pożar", "🔥"),
    LOCAL("Miejscowe zagrożenie", "⚠️"),
    EXERCISE("Ćwiczenia", "🎯"),
    FALSE_ALARM("Fałszywy alarm", "❓"),
    OTHER("Inne", "📍")
}

enum class AlarmStatus { ACTIVE, IN_PROGRESS, FINISHED, CANCELLED }

/** Status dyspozycyjności druha do konkretnego alarmu. */
enum class Disposition(val label: String) {
    GOING("Jadę"),
    NOT_GOING("Nie jadę"),
    LATER("Dojadę później"),
    NONE("Brak odpowiedzi")
}

data class Alarm(
    val id: String,
    val type: AlarmType,
    val title: String,
    val description: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val dispatchedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val status: AlarmStatus,
    val vehicles: List<String> = emptyList(),
    val responders: List<Responder> = emptyList(),
    val myDisposition: Disposition = Disposition.NONE
)

data class Responder(
    val memberId: String,
    val memberName: String,
    val disposition: Disposition,
    val respondedAt: LocalDateTime?
)

enum class MemberRank(val label: String) {
    DRUH("Druh"),
    STARSZY_DRUH("Starszy Druh"),
    RATOWNIK("Ratownik"),
    STARSZY_RATOWNIK("Starszy Ratownik"),
    DOWODCA_SEKCJI("Dowódca Sekcji"),
    NACZELNIK("Naczelnik"),
    PREZES("Prezes")
}

data class Member(
    val id: String,
    val firstName: String,
    val lastName: String,
    val rank: MemberRank,
    val phone: String,
    val email: String?,
    val birthDate: LocalDate,
    val joinDate: LocalDate,
    val active: Boolean,
    val qualifications: List<Qualification>,
    val medicalExamExpiry: LocalDate?,
    val photoUrl: String? = null
) {
    val fullName: String get() = "$firstName $lastName"
}

data class Qualification(
    val name: String,
    val acquiredAt: LocalDate,
    val expiresAt: LocalDate?
) {
    fun isExpiringSoon(threshold: Long = 60): Boolean =
        expiresAt?.let { it.isBefore(LocalDate.now().plusDays(threshold)) } ?: false
}

enum class EventType(val label: String) {
    DUTY("Dyżur"),
    TRAINING("Szkolenie"),
    MEETING("Zbiórka"),
    COMPETITION("Zawody"),
    MAINTENANCE("Konserwacja sprzętu"),
    SOCIAL("Wydarzenie integracyjne")
}

data class CalendarEvent(
    val id: String,
    val type: EventType,
    val title: String,
    val description: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val assignedMemberIds: List<String> = emptyList(),
    val location: String? = null
)

enum class EquipmentCategory(val label: String) {
    PPV("Aparat OUO"),
    HOSE("Wąż"),
    PUMP("Pompa"),
    UNIFORM("Umundurowanie"),
    RESCUE("Sprzęt ratowniczy"),
    LIGHTING("Oświetlenie"),
    HYDRAULIC("Sprzęt hydrauliczny"),
    OTHER("Inne")
}

data class Equipment(
    val id: String,
    val name: String,
    val category: EquipmentCategory,
    val serialNumber: String?,
    val purchaseDate: LocalDate?,
    val nextInspectionDate: LocalDate?,
    val location: String?,
    val notes: String?,
    val operational: Boolean
) {
    fun needsInspectionSoon(): Boolean =
        nextInspectionDate?.isBefore(LocalDate.now().plusDays(30)) == true
}

data class Vehicle(
    val id: String,
    val name: String,
    val registration: String,
    val model: String,
    val productionYear: Int,
    val technicalInspectionExpiry: LocalDate,
    val insuranceExpiry: LocalDate,
    val mileage: Int,
    val fuelLevel: Int,
    val operational: Boolean
)

data class Document(
    val id: String,
    val name: String,
    val category: String,
    val uploadedAt: LocalDateTime,
    val sizeBytes: Long,
    val mimeType: String,
    val url: String
)

data class YearStats(
    val year: Int,
    val totalAlarms: Int,
    val fires: Int,
    val localThreats: Int,
    val falseAlarms: Int,
    val exercises: Int,
    val averageResponseSeconds: Int,
    val topResponders: List<Pair<String, Int>>
)

data class UserProfile(
    val id: String,
    val name: String,
    val unit: String,
    val rank: MemberRank,
    val avatarUrl: String?
)
