# Life OS – zasady projektu

## Mobile-first i Capacitor

- Frontend pozostaje aplikacją Angular uruchamianą w przeglądarce oraz jako natywna aplikacja Android przez Capacitor.
- PWA, manifest PWA i Angular Service Worker nie są częścią aktualnej architektury. Nie dodawaj ich bez osobnej decyzji użytkownika.
- Projektuj każdy ekran najpierw dla szerokości 320–480 px, a następnie rozszerzaj układ breakpointami dla większych ekranów.
- Nie zakładaj kursora, hovera ani dużego ekranu. Interfejs musi działać dotykowo w Android WebView.
- Zapewniaj czytelną hierarchię na małym ekranie, pojedynczą kolumnę tam, gdzie to potrzebne, oraz wygodne strefy dotyku (co najmniej 44 × 44 px dla aktywnych kontrolek).
- Nie ukrywaj istotnych akcji wyłącznie pod hoverem. Zachowuj dostępność z klawiatury i semantyczny HTML.
- Przy zmianach UI projektuj responsywność dla 320 px, 480 px i desktopu oraz uwzględniaj Android WebView. Domyślnie oceniaj ją przez przegląd kodu; nie uruchamiaj weryfikacji w przeglądarce ani WebView bez wyraźnego polecenia użytkownika.
- Konfiguracja Capacitor znajduje się w `app-fe/capacitor.config.ts`, a natywny projekt w `app-fe/android`.
- Nigdy nie uruchamiaj automatycznie `cap sync`, `npx cap sync android`, `npm run android:sync`, `cap run`, `npm run android:run`, `cap open` ani `npx cap add android`. Synchronizację, uruchamianie i obsługę natywnego projektu Android wykonuje wyłącznie użytkownik ręcznie.
- Po zmianach frontendu można poinformować użytkownika, że powinien samodzielnie wykonać synchronizację Androida. Nie uruchamiaj automatycznie `npm run build` ani innych poleceń budowania.

## Domyślna weryfikacja zmian

- Domyślnie pisz kod i sprawdzaj jego poprawność przez przegląd źródeł: składnię, typy, importy, kontrakty i aktualność referencji.
- Nie uruchamiaj buildów, kompilacji, testów, serwerów podglądu ani aplikacji w celu weryfikacji bez wyraźnego polecenia użytkownika.
- Nie korzystaj z przeglądarki, screenshotów ani automatyzacji UI do sprawdzania zmian bez wyraźnego polecenia użytkownika.
- Nie proś rutynowo o zgodę na te czynności. Domyślnym zakończeniem zadania jest gotowy kod po przeglądzie źródeł.

## PrimeNG

- Preferuj komponenty PrimeNG, jeśli biblioteka ma odpowiedni komponent (np. Button, Card, Dialog, Drawer, Table, Tag, Menu, Input).
- Dla struktury dokumentu, prostych list, nagłówków i elementów stricte semantycznych używaj zwykłego HTML i CSS.
- Zachowuj spójność z istniejącym motywem Aura i konfiguracją PrimeNG; nie twórz zastępczego komponentu, jeśli PrimeNG oferuje właściwy element.

## Stan i komunikacja frontendu

- Rozdzielaj komunikację HTTP od przechowywania stanu. Serwisy API wykonują requesty i mapują kontrakty, ale nie przechowują stanu widoków.
- Dla danych biznesowych współdzielonych przez kilka ekranów lub modułów używaj lekkich store'ów opartych na natywnych Angular Signals, zwykle jako `@Injectable({ providedIn: 'root' })`.
- Nie dodawaj NgRx ani innej biblioteki do zarządzania stanem bez realnej potrzeby zaakceptowanej przez użytkownika.
- Zapisywalne sygnały store'a utrzymuj jako prywatne. Na zewnątrz udostępniaj sygnały tylko do odczytu oraz dane pochodne przez `computed()`.
- Nie używaj `effect()` do kopiowania lub propagowania stanu między sygnałami. Dane pochodne wyrażaj przez `computed()`.
- Stan czysto prezentacyjny pozostawiaj lokalnie w komponencie, np. otwarcie modala, wartości formularza, wybrana zakładka i błędy pól.
- Backend jest źródłem prawdy dla sald, sum, net worth i pozostałych obliczeń finansowych. Frontend nie powinien duplikować reguł domenowych.
- Nie twórz jednego globalnego store'a dla całego Life OS. Stosuj store'y według obszarów, np. `FinanceStore`, `GoalsStore` i `PlanningStore`, gdy pojawią się ich realni konsumenci.
- Dla dashboardu finansowego preferuj jeden spójny snapshot zawierający sumy według waluty, konta i historię transakcji. Po mutacji można podmienić cały snapshot odpowiedzią backendu.
- Nie sumuj bezpośrednio wartości w różnych walutach. Zwracaj i prezentuj agregaty osobno dla każdej waluty.

## Aktualny zakres backendu

- Aplikacja działa obecnie świadomie w trybie local single-user z użytkownikiem `id = 1`. Nie wdrażaj security, logowania ani tymczasowych abstrakcji użytkownika bez osobnej decyzji użytkownika.
- Publiczny URL backendu oraz komunikacja API z aplikacji zainstalowanej na telefonie są odłożone. Frontend przeglądarkowy nadal korzysta z relatywnego `/api` i proxy developerskiego.
- Tymczasowa konfiguracja CORS dla `/api/**` dopuszcza wszystkie originy, metody i nagłówki bez credentials. Przed publicznym udostępnieniem backendu należy ją zawęzić.
- Nie rozwijaj teraz CRUD kont ani security, jeżeli nie są wymagane przez wdrażany feature. Do pracy nad transakcjami można używać istniejących kont seedowanych w bazie.
- Aktualnym priorytetem jest dostarczanie kompletnych pionowych wycinków funkcjonalnych, w pierwszej kolejności transakcji, aktualnych sald i rzeczywistych danych dashboardu.

## Weryfikacja backendu

- Nie uruchamiaj poleceń Maven ani Maven Wrapper (`mvn`, `mvnw`, `mvnw.cmd`) w tym projekcie.
- Nie pobieraj zależności Maven i nie proś o dostęp sieciowy w celu uruchomienia testów backendu.
- Przy małych refaktorach Javy opieraj weryfikację na przeglądzie zmian, spójności pakietów i wyszukiwaniu nieaktualnych referencji.
