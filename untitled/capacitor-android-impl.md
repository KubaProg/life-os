# Uruchomienie Life OS na Androidzie przez Capacitor

## Cel

Ta instrukcja opisuje dodanie Androida do istniejącego frontendu Angular w katalogu `app-fe`. PWA nie jest wymagane. Capacitor umieści produkcyjny build Angulara w natywnej aplikacji Android, która wyświetli go w systemowym WebView.

Docelowy przepływ:

```text
Angular (`app-fe`)
    -> `npm run build`
    -> `dist/app-fe/browser`
    -> `npx cap sync android`
    -> natywny projekt `app-fe/android`
    -> APK/AAB uruchamiane przez Androida
    -> publiczne API Spring Boot przez HTTPS
```

## Założenia

- Backend działa pod publicznym adresem HTTPS, np. `https://api.life-os.pl`.
- Frontend nie próbuje łączyć się z `localhost:8080` na telefonie.
- Polecenia wykonujemy w katalogu `app-fe`, chyba że krok mówi inaczej.
- Identyfikator aplikacji `pl.lifeos.app` jest przykładem. Przed publikacją należy wybrać własny, unikalny identyfikator. Po publikacji w Google Play nie powinno się go zmieniać.
- PWA, manifest oraz service worker nie są potrzebne do wykonania tej instrukcji.

## 1. Przygotuj środowisko Android

Zainstaluj:

1. Aktualne Node.js zgodne z używaną wersją Angulara i Capacitor.
2. Android Studio.
3. Android SDK wskazany przez aktualną wersję Capacitor.
4. Emulator Androida albo sterowniki i fizyczny telefon z włączonym debugowaniem USB.

Nie trzeba osobno konfigurować JDK, jeśli używany jest JDK dostarczony przez Android Studio.

Sprawdzenie Node i npm:

```powershell
node --version
npm --version
```

Dlaczego: Capacitor generuje zwykły projekt Android/Gradle, a Android Studio dostarcza SDK, emulator, kompilator i narzędzia do podpisywania aplikacji.

## 2. Skonfiguruj publiczny adres backendu

Obecnie serwis korzysta z relatywnego adresu:

```ts
private readonly accountsUrl = '/api/finance/accounts';
```

Proxy z `proxy.conf.json` działa tylko podczas lokalnego `ng serve`. Nie jest używane przez aplikację zainstalowaną na telefonie.

Przed testem na urządzeniu frontend powinien budować adres na podstawie produkcyjnego URL, np.:

```ts
export const environment = {
  apiUrl: 'https://api.life-os.pl'
};
```

```ts
private readonly accountsUrl =
  `${environment.apiUrl}/api/finance/accounts`;
```

Można użyć plików `environment.ts` i `environment.development.ts` z `fileReplacements` w `angular.json` albo własnego serwisu konfiguracji. Ważny jest efekt:

```text
przeglądarka developerska -> lokalny lub testowy backend
build produkcyjny Android -> publiczny backend HTTPS
```

Backend powinien akceptować żądania CORS z originu aplikacji Capacitor. Domyślnie Android używa lokalnego originu opartego o `https://localhost`. Nie ustawiaj w produkcji `server.url` na adres zdalnej strony — pliki Angulara powinny być spakowane wewnątrz aplikacji, a zdalne ma być tylko API.

Dlaczego: `localhost` na telefonie oznacza telefon, nie komputer deweloperski. HTTPS unika blokowania niezabezpieczonego ruchu przez Androida i problemów z mixed content.

## 3. Zainstaluj Capacitor

W katalogu `app-fe` wykonaj:

```powershell
npm install @capacitor/core @capacitor/android
npm install --save-dev @capacitor/cli
```

Pakiety `@capacitor/core`, `@capacitor/android` i `@capacitor/cli` powinny mieć ten sam numer głównej wersji. Można to sprawdzić:

```powershell
npm list @capacitor/core @capacitor/android @capacitor/cli
```

Dlaczego:

- `@capacitor/core` zapewnia most między kodem JavaScript a Androidem;
- `@capacitor/android` zawiera natywny runtime Androida;
- `@capacitor/cli` tworzy, synchronizuje i uruchamia projekt natywny.

## 4. Zainicjalizuj konfigurację Capacitor

Uruchom:

```powershell
npx cap init "Life OS" "pl.lifeos.app"
```

Następnie sprawdź wygenerowany plik `capacitor.config.ts`. Dla tego projektu powinien wyglądać podobnie do:

```ts
import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'pl.lifeos.app',
  appName: 'Life OS',
  webDir: 'dist/app-fe/browser'
};

export default config;
```

Najważniejsze pole to:

```ts
webDir: 'dist/app-fe/browser'
```

To rzeczywisty katalog, w którym obecna konfiguracja Angulara tworzy `index.html` i pliki aplikacji.

Dlaczego: podczas synchronizacji Capacitor kopiuje zawartość `webDir` do natywnego projektu. Nie uruchamia za nas `ng build`.

## 5. Zbuduj Angulara

```powershell
npm run build
```

Po poprawnym buildzie powinien istnieć plik:

```text
app-fe/dist/app-fe/browser/index.html
```

Dlaczego: aplikacja Android nie korzysta z plików TypeScript ani z serwera `ng serve`. Otrzymuje gotowe pliki JavaScript, CSS i HTML z produkcyjnego buildu.

## 6. Dodaj projekt Android

Ten krok wykonuje się tylko raz:

```powershell
npx cap add android
```

Powstanie katalog:

```text
app-fe/android
```

Jest to pełnoprawny projekt Android zarządzany przez Gradle i Android Studio. Powinien zostać zapisany w repozytorium, ponieważ będzie zawierał konfigurację aplikacji, ikony, ustawienia uprawnień oraz ewentualny kod natywny.

Dlaczego: Angular pozostaje źródłem interfejsu, natomiast katalog `android` jest natywną obudową potrzebną Androidowi.

## 7. Zsynchronizuj build z Androidem

```powershell
npx cap sync android
```

Polecenie:

1. kopiuje build z `dist/app-fe/browser` do projektu Android;
2. aktualizuje natywne zależności;
3. rejestruje zainstalowane pluginy Capacitor.

To polecenie trzeba powtarzać po zmianie pluginów. Po zwykłej zmianie Angulara również można używać `sync`; jest bezpiecznym domyślnym wyborem.

## 8. Sprawdź konfigurację

```powershell
npx cap doctor android
```

Jeśli używana wersja CLI nie przyjmuje argumentu platformy, wykonaj:

```powershell
npx cap doctor
```

Dlaczego: polecenie wykrywa typowe problemy z wersjami pakietów, SDK i konfiguracją projektu przed otwarciem Android Studio.

## 9. Otwórz projekt w Android Studio

```powershell
npx cap open android
```

Przy pierwszym otwarciu Android Studio wykona synchronizację Gradle. Po jej zakończeniu:

1. wybierz emulator lub podłączony telefon;
2. wybierz konfigurację `app`;
3. kliknij **Run**.

Alternatywnie można uruchomić aplikację bezpośrednio z terminala:

```powershell
npx cap run android
```

Jak to działa: Android uruchamia natywną aktywność Capacitor. Wewnątrz niej WebView ładuje lokalne pliki Angulara, a Angular wysyła żądania HTTPS do publicznego backendu.

## 10. Codzienny cykl pracy

Po zmianach tylko w kodzie Angulara:

```powershell
npm run build
npx cap sync android
npx cap run android
```

Albo ostatni krok można zastąpić otwartym Android Studio i ponownie kliknąć **Run**.

Warto później dodać do `package.json` skrypty:

```json
{
  "scripts": {
    "android:sync": "npm run build && cap sync android",
    "android:open": "cap open android",
    "android:run": "npm run build && cap run android"
  }
}
```

Po instalacji nowego pluginu:

```powershell
npm install NAZWA_PLUGINU
npx cap sync android
```

Samo `ng serve` nie aktualizuje aplikacji zainstalowanej na urządzeniu.

## 11. Sprawdź komunikację z API

Na urządzeniu zweryfikuj kolejno:

1. czy aplikacja uruchamia się bez białego ekranu;
2. czy główny routing Angulara działa;
3. czy żądanie listy kont dociera do publicznego API;
4. czy wejście bezpośrednio w szczegóły konta działa;
5. czy backend odpowiada poprawnymi nagłówkami CORS;
6. czy wyłączenie sieci pokazuje obsługiwany stan błędu zamiast zawieszenia interfejsu.

Jeżeli interfejs działa, ale każde żądanie HTTP kończy się błędem, najpierw sprawdź:

- czy URL API jest pełnym adresem HTTPS;
- czy certyfikat HTTPS jest zaufany i zawiera pełny łańcuch certyfikatów;
- czy backend dopuszcza origin Capacitor;
- czy API jest dostępne z sieci telefonu;
- czy w buildzie produkcyjnym została użyta właściwa konfiguracja środowiska.

Do diagnostyki WebView można podłączyć telefon i użyć `chrome://inspect` w desktopowym Chrome.

## 12. Dodaj ikonę i ekran startowy

Opcjonalne podczas pierwszego prototypu, wymagane przed publikacją:

```powershell
npm install --save-dev @capacitor/assets
```

Przygotuj pliki źródłowe, np.:

```text
app-fe/assets/
  icon-only.png
  icon-foreground.png
  icon-background.png
  splash.png
  splash-dark.png
```

Ikony powinny mieć co najmniej 1024 × 1024 px, a obrazy splash co najmniej 2732 × 2732 px. Następnie:

```powershell
npx capacitor-assets generate --android
```

Po wygenerowaniu zasobów uruchom ponownie:

```powershell
npx cap sync android
```

Dlaczego: Android potrzebuje wielu wariantów rozmiaru i formatu ikon, w tym adaptive icons oraz osobnego zachowania ekranu startowego w Androidzie 12+.

## 13. Przygotuj build do instalacji lub publikacji

Do testów lokalnych Android Studio może wygenerować debug APK. Do Google Play należy przygotować podpisany Android App Bundle (`.aab`):

1. otwórz projekt przez `npx cap open android`;
2. w Android Studio wybierz **Build > Generate Signed Bundle / APK**;
3. wybierz **Android App Bundle**;
4. utwórz lub wybierz własny keystore;
5. zapisz keystore i hasła w bezpiecznym miejscu poza repozytorium;
6. zbuduj wariant `release`;
7. przetestuj release przed wysłaniem do Google Play Console.

Utrata klucza podpisującego może uniemożliwić publikowanie aktualizacji poza mechanizmami odzyskiwania oferowanymi przez Google Play. Nie zapisuj plików `.jks` ani haseł w Git.

## 14. Minimalna checklista ukończenia

- [ ] Publiczny backend działa przez HTTPS.
- [ ] Produkcyjny build Angulara korzysta z publicznego URL API.
- [ ] CORS backendu dopuszcza żądania z aplikacji Capacitor.
- [ ] Pakiety Capacitor mają zgodne wersje główne.
- [ ] `capacitor.config.ts` ma `webDir: 'dist/app-fe/browser'`.
- [ ] `npm run build` kończy się powodzeniem.
- [ ] `npx cap sync android` kończy się powodzeniem.
- [ ] Aplikacja uruchamia się na emulatorze.
- [ ] Aplikacja uruchamia się na fizycznym telefonie.
- [ ] Routing i przycisk Wstecz zostały sprawdzone.
- [ ] Żądania do API działają na Wi-Fi i sieci komórkowej.
- [ ] Ikona, nazwa aplikacji i splash screen są poprawne.
- [ ] Release AAB jest podpisany i przetestowany.

## Czego nie trzeba robić

Do uruchomienia aplikacji przez Capacitor nie trzeba:

- dodawać PWA;
- dodawać Angular service workera;
- hostować frontendu w internecie;
- zamieniać PrimeNG na Ionic;
- przepisywać Angulara na Kotlin;
- uruchamiać Mavena podczas budowania aplikacji Android.

W internecie musi być dostępny backend. Sam frontend zostanie dołączony do APK/AAB podczas `cap sync` i kompilacji projektu Android.

## Przydatna dokumentacja

- [Instalacja Capacitor](https://capacitorjs.com/docs/getting-started)
- [Środowisko Android](https://capacitorjs.com/docs/getting-started/environment-setup)
- [Workflow build/sync/run](https://capacitorjs.com/docs/basics/workflow)
- [Capacitor Android](https://capacitorjs.com/docs/android)
- [Ikony i splash screen](https://capacitorjs.com/docs/guides/splash-screens-and-icons)
- [Publikacja w Google Play](https://capacitorjs.com/docs/android/deploying-to-google-play)
