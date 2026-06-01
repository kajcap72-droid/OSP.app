# 🔨 Jak zbudować APK aplikacji OSP

Masz **3 sposoby** na uzyskanie pliku `.apk` — od najprostszego do najbardziej "developerskiego".

---

## ⭐ Opcja 1: GitHub Actions (REKOMENDOWANA)

**Najpewniejsza, darmowa, w chmurze. Nie potrzebujesz nic instalować na swoim komputerze.**

### Krok po kroku:

1. **Stwórz konto na GitHub** (jeśli jeszcze nie masz): https://github.com/signup

2. **Stwórz nowe puste repozytorium** (prywatne lub publiczne):
   - Wejdź na https://github.com/new
   - Nazwa np. `osp-app`
   - Kliknij **Create repository**

3. **Wgraj kod aplikacji** (z terminala, w katalogu `OSP/`):
   ```bash
   cd OSP
   git init
   git add .
   git commit -m "Initial commit - aplikacja OSP"
   git branch -M main
   git remote add origin https://github.com/TWÓJ_LOGIN/osp-app.git
   git push -u origin main
   ```

   **Lub przez przeglądarkę:** kliknij "uploading an existing file" na nowo stworzonym repo i przeciągnij całą zawartość katalogu `OSP/`.

4. **GitHub Actions zacznie automatycznie budować APK**
   - Idź do zakładki **Actions** w repozytorium
   - Zobaczysz workflow "Build OSP APK" w toku (5-12 minut)
   - Kiedy zakończy się zielonym ✓, kliknij na niego

5. **Pobierz APK**
   - Na dole strony workflow zobaczysz sekcję **Artifacts**
   - Kliknij **osp-debug-apk** → pobierze się ZIP z plikiem `.apk`
   - Rozpakuj ZIP, masz `app-debug.apk` ✅

6. **Zainstaluj na telefonie**
   - Przenieś plik na telefon (kabel USB, email, Drive...)
   - W ustawieniach Androida włącz: **Bezpieczeństwo → Nieznane źródła**
   - Otwórz plik na telefonie → zainstaluj

### 💡 Bonus: automatyczny GitHub Release

Aby przy każdym tagowaniu utworzył się release z gotowym APK do pobrania:
```bash
git tag v0.1.0
git push origin v0.1.0
```
Po kilkunastu minutach w zakładce **Releases** pojawi się gotowy do pobrania `.apk`.

---

## 🖥️ Opcja 2: Android Studio (lokalnie)

**Jeśli masz komputer z 8+ GB RAM i lubisz IDE.**

1. **Pobierz Android Studio** (~1 GB): https://developer.android.com/studio
2. Zainstaluj i uruchom
3. Wybierz **Open** → wskaż katalog `OSP/`
4. Poczekaj na Gradle sync (5-15 min przy pierwszym razie — pobierze SDK)
5. Górne menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
6. Po buildzie kliknij **locate** w powiadomieniu — znajdziesz APK w `app/build/outputs/apk/debug/`
7. Aby uruchomić od razu na emulatorze: zielony ▶ przycisk **Run**

---

## 💻 Opcja 3: Linia poleceń (zaawansowane)

**Wymaga JDK 17, Android SDK i 4+ GB wolnego RAM.**

```bash
# 1. Zainstaluj JDK 17 i Android SDK
# (zobacz https://developer.android.com/tools)

# 2. Ustaw zmienne środowiskowe
export ANDROID_HOME=/sciezka/do/Android/Sdk
export JAVA_HOME=/sciezka/do/jdk-17

# 3. Wejdź do katalogu projektu
cd OSP

# 4. Zbuduj debug APK
./gradlew assembleDebug

# 5. APK znajdziesz tutaj:
ls app/build/outputs/apk/debug/app-debug.apk
```

---

## ❓ Rozwiązywanie problemów

### "SDK location not found"
Stwórz plik `local.properties` w `OSP/`:
```
sdk.dir=/twoja/sciezka/do/Android/Sdk
```

### "OutOfMemoryError" podczas buildu
Zwiększ pamięć dla Gradle w pliku `OSP/gradle.properties`:
```
org.gradle.jvmargs=-Xmx6144m
```

### "Failed to install signed apk"
Pobrany APK jest **debug** (do testów). Aby zainstalować z innego źródła:
- Włącz "Instalowanie z nieznanych źródeł" dla aplikacji, którą używasz do otwarcia (np. Files / Drive)

### Build w GitHub Actions trwa długo
Przy pierwszym razie 8-12 min (pobiera ~500 MB zależności).
Kolejne ~3 min dzięki cache'owi.

---

## 📦 Co dostajesz

Po udanym buildzie masz:
- **`app-debug.apk`** (~25 MB) — instalowalna apka do testów
- (opcjonalnie) **`app-release-unsigned.apk`** — wersja produkcyjna, ale niepodpisana

Aby wgrać do Google Play, musisz dodatkowo **podpisać** release APK certyfikatem. To temat na osobną instrukcję — daj znać jak będziesz na tym etapie.
