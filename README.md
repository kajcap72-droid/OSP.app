# OSP — Aplikacja dla Ochotniczych Straży Pożarnych

Aplikacja Android dla jednostek OSP. Inspirowana e-remizą, ale rozszerzona o dodatkowe funkcje przydatne w codziennej pracy jednostki.

## Funkcje (MVP)

- 🔥 **Alarmy** — lista zdarzeń, szczegóły, mini-mapa miejsca zdarzenia, potwierdzanie „jadę / nie jadę"
- 🔔 **Powiadomienia alarmowe** — dźwięk syreny, długa wibracja, full-screen intent
  (wybudza ekran nawet z lockscreena), akcje **Jadę / Nie jadę** prosto z notyfikacji,
  klik w powiadomienie otwiera szczegóły alarmu (deep link).
  **FAB „Symuluj alarm"** na ekranie Alarmów do testu bez backendu.
  Klasa FCM gotowa do podpięcia Firebase (instrukcja w `OspFirebaseMessagingService.kt`).
- 🗺️ **Mapa zdarzeń (osmdroid / OpenStreetMap)** — interaktywna mapa, kolorowe pinki
  wg typu zdarzenia, pin remizy, klik w pin pokazuje kartę z przyciskiem
  **„Nawiguj"** (otwiera Google Maps / dowolną apkę nawigacyjną przez intent geo:),
  legenda u góry, FAB „wycentruj na remizę". **Bez kluczy API.**
- 👥 **Członkowie** — ewidencja druhów, kontakt, uprawnienia, badania (z przypomnieniami o terminach)
- 📅 **Dyżury / kalendarz** — grafik dyżurów, szkolenia, zbiórki, wydarzenia
- 🧰 **Sprzęt** — ewidencja sprzętu z terminami legalizacji / przeglądów
- 🚒 **Pojazdy** — karty drogowe, przeglądy, OC, paliwo
- 📄 **Dokumenty** — repozytorium plików jednostki
- 📊 **Statystyki** — wyjazdy w roku, podział na typy, top druhowie, średni czas zadysponowania
- ⚙️ **Ustawienia** — profil druha, powiadomienia, motyw

## Jak przetestować powiadomienie alarmowe

1. Uruchom aplikację, wejdź w ekran **Alarmy**.
2. Kliknij FAB **„Symuluj alarm"** (na Androidzie 13+ zaakceptuj prośbę o powiadomienia).
3. **Zminimalizuj apkę** (np. przyciskiem home) — masz 1,5 s.
4. Po chwili usłyszysz syrenę, telefon zawibruje, pojawi się powiadomienie z akcjami
   **Jadę / Nie jadę**. Można też kliknąć powiadomienie — otworzy szczegóły alarmu.

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Hilt** — DI
- **Room** — lokalna baza
- **Retrofit + Moshi** — sieć (warstwa przygotowana)
- **Coroutines + Flow** — asynchroniczność
- **Coil** — obrazy
- **Navigation Compose** — nawigacja
- **DataStore** — preferencje
- **WorkManager** — przypomnienia o terminach

## Architektura

```
ui (Compose Screens + ViewModels)
        ↓
domain / repository (interface)
        ↓
   ┌────┴─────┐
data/local   data/remote
 (Room)      (Retrofit)
```

`RemizaRepository` ma dwie implementacje:
- `MockRemizaRepository` — działa od razu, dane przykładowe
- `RemoteRemizaRepository` — szkielet pod realne API (do uzupełnienia, gdy uzyskasz legalny dostęp do oficjalnego API)

## ⚠️ Uwaga o API e-remiza

Repozytorium [`kapi2289/eremiza-api`](https://github.com/kapi2289/eremiza-api) z 2019 r. jest nieaktualne. Obecny e-remiza nie udostępnia publicznego API. Ta aplikacja jest **samodzielna** — może być używana jako oddzielne narzędzie albo (po legalnym uzgodnieniu z dostawcą e-remiza) zintegrowana przez podmianę `RemoteRemizaRepository`.

## Jak uruchomić

1. Otwórz folder `OSP/` w Android Studio (Hedgehog lub nowszy)
2. Sync Gradle
3. Run na emulatorze lub urządzeniu (min. Android 8.0 / API 26)

Aplikacja domyślnie używa `MockRemizaRepository`, więc działa **bez backendu** — od razu widać alarmy, druhów, sprzęt itp.

## Struktura projektu

```
OSP/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/pl/osp/app/
│       │   ├── OspApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── model/        ← modele domenowe
│       │   │   ├── local/        ← Room
│       │   │   ├── remote/       ← Retrofit (szkielet)
│       │   │   └── repository/   ← repozytoria
│       │   ├── di/               ← Hilt modules
│       │   ├── ui/
│       │   │   ├── theme/
│       │   │   ├── navigation/
│       │   │   └── screens/      ← ekrany Compose
│       │   └── util/
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```
