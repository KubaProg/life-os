# Current focus

## Co robimy teraz

- Wracamy do rozwoju funkcji finansowych opisanych w [`prompts/3-plan-implementacji-stanu-obecnego.md`](prompts/3-plan-implementacji-stanu-obecnego.md).
- Istniejący frontend Angular działa już jako aplikacja Android przez Capacitor.
- PWA nie jest wymagane i obecnie nie jest częścią tego zadania.
- Szczegółowa instrukcja znajduje się w [`capacitor-android-impl.md`](capacitor-android-impl.md).
- Publiczny URL backendu odkładamy na później. Frontend nadal korzysta z relatywnego `/api`, dlatego wersja Android uruchamia interfejs, ale na telefonie nie łączy się jeszcze z lokalnym backendem.
- Backend posiada tymczasową globalną konfigurację CORS dla `/api/**`: wszystkie originy, metody i nagłówki, bez credentials.

## Najbliższa sesja — sugerowany start

1. Uruchomić ponownie backend z aktualnym kodem i ręcznie sprawdzić zapis przychodu oraz wydatku.
2. Dodać edycję istniejącej transakcji przez `PUT /api/finance/transactions/{transactionId}`.
3. Przy każdej transakcji na dashboardzie i w szczegółach konta dodać dostępny przycisk z ikoną edycji.
4. Otwierać ten sam modal co przy tworzeniu, ale w trybie edycji i z wypełnionymi danymi.
5. Po zapisie zaktualizować `FinanceStore`, sumy, salda i obie historie odpowiedzią backendu, bez przeładowania strony.
6. Sprawdzić tworzenie i edycję przy 320 px, 480 px, na desktopie oraz w Android WebView.

## Checkpoint Android — 2026-10-11

- Dodano `@capacitor/core`, `@capacitor/android` i `@capacitor/cli` w wersji `8.5.3`.
- Dodano `capacitor.config.ts` z `appId: 'pl.lifeos.app'`, nazwą `Life OS` i `webDir: 'dist/app-fe/browser'`.
- Dodano skrypty `android:sync`, `android:open` i `android:run`.
- Wygenerowano natywny projekt `app-fe/android` i zsynchronizowano z nim produkcyjny build Angulara.
- Aplikacja została uruchomiona w emulatorze Androida.
- Wygenerowano debug APK i potwierdzono jego instalację oraz uruchomienie na fizycznym telefonie.
- Konfiguracja publicznego URL API, HTTPS i działającej komunikacji telefonu z backendem pozostaje świadomie odłożona na później.

## Aktualny stan funkcji finansowych

Etap 1 dotyczący zarządzania kontami został świadomie zamrożony. Archiwizacja oraz formularze create/update kont nie blokują dalszego rozwoju; korzystamy z kont seedowanych i lokalnego użytkownika `id = 1`.

Aktualnie działa pierwszy pionowy wycinek Etapu 2:

1. zapis przychodu lub wydatku dla wybranego konta;
2. rzeczywiste saldo jako `openingBalance + przychody - wydatki`;
3. `GET /api/finance/overview` z sumami per waluta, kontami i wspólną historią;
4. `POST /api/finance/transactions` zwracający utworzoną transakcję i odświeżony snapshot;
5. `FinanceStore` oparty na Angular Signals;
6. przypięta szybka akcja i modal transakcji na dashboardzie;
7. historia wszystkich transakcji oraz historia konkretnego konta.

Dalsza kolejność:

1. **Etap 2 — następny wycinek:** edycja istniejących transakcji z poziomu obu list;
2. **Etap 2 — później:** usuwanie, filtry, korekty i transfery;
3. **Etap 3:** miesięczne przychody, wydatki, cashflow i savings rate;
4. **Etap 4:** aktywa oraz zobowiązania;
5. **Etap 5:** inwestycje, obligacje i lokaty;
6. **Etap 6:** historia net worth i wiarygodność wskaźników.

## Ważne ustalenia

- Frontend pozostaje w Angularze z PrimeNG; nie przepisujemy go na Kotlin ani Ionic.
- Capacitor jest natywną obudową tego samego buildu Angulara.
- Docelowo publicznie dostępny będzie backend, a frontend nadal będzie kopiowany do APK/AAB. Nie blokuje to obecnego rozwoju feature'ów.
- Po zmianach Angulara użytkownik samodzielnie wykonuje `npm run android:sync`; agent nie uruchamia poleceń Capacitor ani Android.
- Debug APK można budować poleceniem `app-fe/android/gradlew.bat assembleDebug` uruchomionym w katalogu `app-fe/android`.
- Projekt nadal korzysta z MySQL.
- Nie uruchamiać Mavena ani Maven Wrappera podczas weryfikacji backendu.
- Przy zmianach UI utrzymywać podejście mobile-first oraz kontrolki dotykowe minimum 44 × 44 px.
- Współdzielone dane finansowe przechowuje lekki `FinanceStore`; po pełnym odświeżeniu strony store pobiera świeży snapshot z backendu.
