# Current focus

## Co robimy teraz

- Wracamy do rozwoju funkcji finansowych opisanych w [`prompts/3-plan-implementacji-stanu-obecnego.md`](prompts/3-plan-implementacji-stanu-obecnego.md).
- Istniejący frontend Angular działa już jako aplikacja Android przez Capacitor.
- PWA nie jest wymagane i obecnie nie jest częścią tego zadania.
- Szczegółowa instrukcja znajduje się w [`capacitor-android-impl.md`](capacitor-android-impl.md).
- Publiczny URL backendu odkładamy na później. Frontend nadal korzysta z relatywnego `/api`, dlatego wersja Android uruchamia interfejs, ale na telefonie nie łączy się jeszcze z lokalnym backendem.
- Backend posiada tymczasową globalną konfigurację CORS dla `/api/**`: wszystkie originy, metody i nagłówki, bez credentials.

## Wspólne Account i przywrócony szkic UI — aktualizacja 2026-10-19

Najnowsza decyzja zastępuje wcześniejszy wariant z oddzielnymi zakresami ID oraz czterema relacjami w transferze. Użytkownik zaakceptował wdrożenie wspólnego modelu i przywrócenie szkicu frontendu do ręcznego poklikania.

- **`accounts` / `Account`** ma globalne ID rachunku, właściciela, nazwę, walutę, rodzinę `AccountKind` (`ORDINARY`, `INVESTMENT`), aktywność i daty techniczne. Nie ma kolumn wskazujących kolejne typy profili.
- **`financial_accounts` / `FinancialAccount`** i **`investment_accounts` / `InvestmentAccount`** mają jednostronne `@OneToOne @MapsId` do `Account`. Kolumna `id` profilu jest jednocześnie jego PK i FK do `accounts.id`; generator ID jest tylko w `Account`.
- Rodzina rachunku nie jest typem produktu: `InvestmentType` nadal rozróżnia lokatę, obligacje, ETF itd. Dodanie nowej rodziny wymaga jej profilu i reguł operacji, ale nie kolejnych kolumn źródła/celu w transferze.
- **`MoneyTransfer`** ma dokładnie dwie relacje do `Account`: `sourceAccount` i `destinationAccount`. Komenda API przesyła `sourceAccountId`, `destinationAccountId`, kwotę, datę i UUID próby. Backend odczytuje rodzinę z bazy; nie ufa typowi przysłanemu przez klienta. Metadane `kind` w odpowiedzi służą m.in. nawigacji UI.
- Zaktualizowano profile, relację użytkownika, zapytania repozytoriów, obsługę transferów, zakładanie lokaty i kontrakty frontendu. Typ i warunki produktu nadal narzucają dozwolone operacje.
- **Migracja V5** `V5__introduce_shared_account_identity.sql` przenosi wspólne dane do `accounts`, zachowuje ID zwykłych kont i nadaje inwestycjom ID ponad ich zakresem. Przemapowuje istniejące transfery, operacje i wyceny, przebudowuje FK i usuwa zbędne kolumny. Istniejące ID inwestycji mogą się zmienić.
- Starsze migracje V1–V4 pozostają bez zmian. Znaleziony seed V3 jest historyczną migracją Flyway: jego rekordy również zostaną przeniesione przez V5, bez ponownego seedowania i bez zmiany checksum.
- **Szkic UI przywrócono na prośbę użytkownika:** formularz lokaty z kwotą i źródłem pieniędzy, formularz transferu, wspólna lista kont/inwestycji, osobne szczegóły lokaty z warunkami, historią i wykresem, historia transferów na dashboardzie i w szczegółach zwykłego konta. To prototyp do ręcznej oceny, nie zatwierdzony finalny projekt UI.
- Lokata nadal jest zakładana z kwotą i pierwszym transferem atomowo, bez stałego konta finansującego w encji. Późniejsze zakupy obligacji i ETF pozostają kolejnymi produktami; nie są zaimplementowane przez sam enum.
- Wykonano wyłącznie przegląd kodu, relacji, SQL i referencji. **Nie uruchamiano migracji, backendu, testów, Playwrighta, Mavena ani buildu frontendu.** Działanie i kompilacja pozostają niepotwierdzone; agent nie uruchamia poleceń Capacitor/Android.

## Model inwestycji i transferów — uzgodnienia 2026-10-19

### Status i zakres tej sesji

- Na tym etapie porządkujemy plan backendu. W trakcie rozmowy powstał **roboczy szkic** encji, migracji V4, transferów, serwisów i obliczeń lokaty. Nie był kompilowany ani uruchamiany i nie oznacza ukończenia opisanych niżej etapów. Należy go przejrzeć etapami, zaczynając od modelu encji.
- Szkic frontendu lokat/transferów został ponownie przywrócony na wyraźną prośbę użytkownika (patrz najnowszy checkpoint). Dalszy wygląd i zachowanie UI pozostają do dopracowania po ręcznej ocenie.
- Nie uruchamiano testów, Playwrighta, buildu, Mavena, migracji ani poleceń Android/Capacitor. Zasady te pozostają aktualne do kolejnej decyzji użytkownika; AGENTS.md nadal zabrania Mavena i automatycznej obsługi Capacitor.

### Przyjęty prosty model

- Wspólny `Account` przechowuje tożsamość rachunku; `FinancialAccount` jest profilem zwykłego konta: bankowego, oszczędnościowego albo gotówki.
- Osobna encja-profil **`InvestmentAccount`** współdzieli ID z `Account`, początkowo reprezentuje jedną inwestycję, z typem np. `DEPOSIT`, `BOND`, `ETF`, `OTHER`. Wartość enuma nie oznacza, że dany produkt ma już gotowy serwis lub kalkulator.
- Nie tworzymy teraz osobnych encji obligacji, ETF i lokaty ani osobnych tabel produktu i pozycji. Wspólne pola są w `InvestmentAccount`; pierwszą grupę warunków lokaty można osadzić jako `DepositTerms` przez `@Embedded`, w tej samej tabeli. Nieużywane pola mogą być puste, z walidacją zależną od typu.
- Przy wdrażaniu kolejnych zakupów obligacji/ETF wrócimy do rozdzielenia instrumentu, jednostek i partii/pozycji, jeśli będzie to potrzebne. Nie zastępować liczby jednostek i cen jedną nadpisywaną kwotą rachunku.
- Rachunek inwestycyjny **nie ma stałego konta finansującego**. Konto źródłowe wybierane przy założeniu lub zakupie jest parametrem operacji; ślad źródła pozostaje w transferze.
- Inwestycja jest tworzona **od razu na określoną kwotę**, z pierwszym finansowaniem/zakupem. Nie wprowadzamy widocznego dla użytkownika etapu pustej lokaty, którą trzeba dopiero zasilić.
- Lokata: jedna wpłata przy otwarciu, bez dopłat w pierwszym wariancie. Obligacje i ETF: pierwszy zakup przy tworzeniu, potem możliwość kolejnych zakupów jako osobnych operacji, również z innych kont.

### Transaction, MoneyTransfer i operacje inwestycyjne

- `Transaction` pozostaje zapisem przychodu lub wydatku powiązanego ze zwykłym `FinancialAccount`. Kwota jest dodatnia, a `INCOME`/`EXPENSE` określa wpływ plus/minus na wyliczane saldo. Nie dodajemy drugiego opcjonalnego `investmentAccount` tylko po to, aby udawać transfer przychodem lub wydatkiem.
- **`MoneyTransfer`** jest osobnym wspólnym mechanizmem: źródło, cel, kwota, waluta i data. Obie strony wskazują wspólny `Account` przez globalne ID; rodzina rachunku jest odczytywana z bazy. Nie ma już dwóch osobnych zakresów ID ani czterech opcjonalnych relacji.
- Transfer obciąża źródło i uznaje cel atomowo. Zwykły transfer kapitału nie jest przychodem, konsumpcyjnym wydatkiem ani zyskiem inwestycyjnym. Nie zapisujemy dodatkowej `Transaction` z tym samym obciążeniem.
- Reguły produktu ograniczają ogólny mechanizm: lokata nie przyjmuje dopłat i nie pozwala na częściowe wycofanie w pierwszym wariancie; zakup obligacji/ETF będzie wymagał osobnej operacji produktu.
- **Operacja inwestycyjna** opisuje sens zdarzenia (założenie lokaty, zakup jednostek, wypłata odsetek, zwrot kapitału) i może być powiązana z transferem. Nie powoduje ponownego obciążenia zwykłego konta. Naliczona wycena nie musi mieć transferu gotówki.
- Transfery walutowe z przewalutowaniem, odwracanie transferów i szersze reguły produktów są osobnym zakresem. Pierwszy mechanizm ograniczamy do tej samej waluty i lokalnego użytkownika `id = 1`.

### Zysk miesięczny i trwała historia

- Wskaźnik na dashboardzie nazywa się **„Zysk z inwestycji”**, dotyczy wybranego miesiąca i ma plus przed dodatnią kwotą. Nie jest sumą sald, kapitału ani prognozą.
- Trwała historia operacji oraz naliczeń/wycen powstanie w osobnych tabelach. Samo pole bieżącej wartości albo jedna miesięczna suma nie wystarczy do wyjaśnienia wyniku i odtworzenia poprzednich okresów.
- Snapshoty narastające nie są dziennymi zyskami do dodawania. Miesięczny wynik wynika z granic okresu i zdarzeń w jego trakcie. Nowy miesiąc zmienia zakres raportu, **nie zeruje inwestycji ani wcześniejszego zysku**.
- Odczyt dashboardu może uzupełnić brakujące naliczenia, ale nie może przy każdym otwarciu ponownie dopisywać tego samego zysku do kapitału. Historia ma być odtwarzalna również za dni bez otwierania aplikacji.
- Kapitalizacja i wypłata nie tworzą drugi raz wcześniej naliczonego zysku. Wpłacony/wycofany kapitał nie jest zyskiem; wypłacony dochód wymaga rozdzielenia kapitału, odsetek, podatku i ewentualnych opłat.
- Dokładne warunki pierwszej lokaty (harmonogram kapitalizacji/wypłaty, konwencja dni, podatek i zaokrąglenia) muszą być jawne. Szkic używa stałej stopy, ACT/ACT 365/366 i wypłaty na koniec; nie traktować tych reguł jako uniwersalnych warunków każdej lokaty.
- Agregaty pozostają osobno per waluta. Rynkowa wycena ETF będzie wymagała cen ze źródłem i datą; brak danych nie oznacza wyniku zero.

## Kierunek MVP — aktualizacja 2026-10-19

- Istnieją zwykłe konta, podstawowe transakcje i miesięczne podsumowanie dashboardu. Ręczna weryfikacja z aktualnym backendem/MySQL pozostaje otwarta.
- Bieżący priorytet: uporządkowany backend inwestycji w kolejności encja → transfery → serwis i operacje lokaty → historia → overview. Roboczy kod w repo nie oznacza ukończenia tych etapów.
- Na obecnym dashboardzie pozostają cztery wskaźniki: zysk z inwestycji, przychody, wydatki i stopa oszczędzania. Cashflow nie jest osobnym kafelkiem; miesiąc podsumowania i filtry historii są niezależne.
- Szkic frontendu lokaty i transferów jest przywrócony do ręcznej oceny; finalny UI pozostaje do uzgodnienia. Bez automatycznego testowania i buildu.
- Po podstawowych inwestycjach: cele i proste projekcje/scenariusze. Aktywa rzeczowe, zobowiązania i pełny net worth pozostają później.
- PWA, security, AI, integracje bankowe i infrastruktura publicznego backendu pozostają poza bieżącym zadaniem.

## Checkpoint historyczny — mini dashboard 2026-10-16

- `GET /api/finance/overview?month=YYYY-MM` zwraca dodatkowo `dailyResult` oraz `monthlySummary`. Bez parametru wybierany jest bieżący miesiąc w Europe/Warsaw.
- `monthlySummary.totals` zawiera osobne waluty: `income`, `expenses`, `cashflow` i `savingsRate` w punktach procentowych (`75` oznacza 75%). Bez przychodów procent jest `null`.
- Backend sumuje wyłącznie `INCOME` i `EXPENSE`. Istniejące `TRANSFER` i `ADJUSTMENT` są pomijane; nie było potrzeby zmiany enuma ani schematu bazy. CRUD transferów i inwestycje pozostają osobnymi pakietami.
- Obliczenia są w `MonthlySummaryCalculator`; używają BigDecimal i zaokrąglają procent HALF_UP do dwóch miejsc. Historia i salda nie są filtrowane miesiącem podsumowania.
- Mutacje transakcji przyjmują opcjonalny `month` i zwracają snapshot dla wybranego podsumowania. FinanceStore przekazuje ten parametr i chroni snapshot przed spóźnionymi odczytami.
- Główna karta prezentuje „Dzisiejszy balans”, bez licznika kont. Tymczasowy wynik `12,50 PLN` ma flagę `mocked: true` i wyraźną etykietę danych przykładowych; nie jest wyliczany z sald.
- Podsumowanie ma niezależny wybór miesiąca PrimeNG i układ 1/2/4 kolumny przy szerokościach poniżej 480 / od 480 / od 960 px.
- Wykonano `npm run build` oraz niezależne od Mavena sprawdzenie kalkulatora przez javac/java: granice miesiąca, transfery, korekty, waluty, pusty miesiąc, brak przychodów, ujemny wynik i zaokrąglenia.
- Przed poleceniem zakończenia testów sprawdzono frontend na izolowanych odpowiedziach API przy 320 px, 480 px i desktopie oraz zmianę miesiąca. Nie zweryfikowano integracji Spring/MySQL, odświeżenia po mutacji w przeglądarce ani Android WebView. Na prośbę użytkownika zakończono dalsze testowanie.
- Nie uruchamiano Mavena ani poleceń Capacitor/Android.

## Najbliższe prace — kolejność backendu

### 1. Wspólny Account, profile rachunków i migracja — przygotowane w kodzie

- Przejrzeć wspólną encję `Account`, `AccountKind` i profile ze wspólnym PK przez `@MapsId`. Zmiana jest przygotowana w kodzie, ale nie została uruchomiona.
- Ustalić, które pola są wspólne, a które należą do osadzonej konfiguracji lokaty; walidować je według typu.
- Nie dodawać stałego konta finansującego ani osobnych encji produktu/pozycji na zapas.
- Przygotować/docelowo zaakceptować migrację i repozytorium. W repo istnieją V4 (pierwszy model lokaty) oraz V5 (wspólna tożsamość rachunku); agent ich nie uruchamiał.

Efekt: uzgodniony model i zapis rachunku inwestycyjnego; to nie oznacza jeszcze ukończonego przepływu zakładania lokaty.

### 2. MoneyTransfer — wspólny mechanizm

- Domknąć model źródła i celu dla zwykłego/inwestycyjnego konta, dodatnią kwotę, walutę i datę.
- Wdrożyć atomowy zapis i wpływ na salda. Zweryfikować właściciela, aktywność rachunków, dostępne środki, różne źródło/cel i zgodność walut.
- Zapewnić, że ponowienie tego samego żądania nie tworzy drugiego przelewu, a błąd wycofuje całą operację.
- Zostawić `Transaction` dla przychodów/wydatków; nie liczyć transferu kapitału w miesięcznych przychodach, wydatkach ani zysku inwestycji.
- Najpierw zwykłe konto → zwykłe konto; następnie użycie tego samego mechanizmu przez operacje lokaty, z ograniczeniami produktu.

Efekt: wspólna warstwa przenoszenia pieniędzy, niezależna od formularzy i konkretnego produktu.

### 3. Serwis zakładania lokaty i pierwsze operacje

- Ustalić jawne warunki obsługiwanego wariantu lokaty: daty, procent roczny, podatek, kapitalizację/wypłatę i zasady zaokrąglania.
- Komenda zakładania wymaga od razu kwoty oraz źródła pieniędzy. `InvestmentService`/serwis lokaty tworzy rachunek i wywołuje transfer w jednej transakcji bazodanowej.
- Zapisać operację założenia lokaty i powiązać ją z transferem. Nie pozostawiać pustej lokaty ani częściowego finansowania po błędzie.
- Zablokować późniejsze dopłaty do pierwszego wariantu lokaty. Ustalić rozliczenie na koniec: pełny kapitał i należne odsetki po uwzględnieniu konfiguracji podatku, na wybrane konto w tej samej walucie.

Efekt: pierwszy kompletny przepływ backendowy założenia i finansowania lokaty.

### 4. Naliczenia i historia wycen lokaty

- Dodać/domknąć tabelę operacji i historię naliczeń/wycen z datą oraz wersją reguł obliczeń.
- Liczyć odsetki w backendzie, uzupełniać brakujące okresy i zachowywać wynik historycznych miesięcy.
- Rozdzielić kapitał, zysk brutto, podatek i wartość netto. Uwzględniać kapitalizację oraz wypłatę dokładnie raz.
- Nowy miesiąc nie zeruje wartości inwestycji; historia jest źródłem raportu, a nie zapisem ponawianego zwiększania kapitału.

### 5. Podłączenie do overview i miesięcznego podsumowania

- Zwracać zwykłe konta i inwestycje w jednym snapshotcie, z odrębnymi kontraktami i identyfikacją typu rachunku.
- Zastąpić przykładowy zysk rzeczywistym wynikiem dla wybranego miesiąca; nie duplikować reguł finansowych w Angularze.
- Rozdzielić transfer kapitału od faktycznie wypłaconych odsetek przy agregacji przychodów. Nie dublować zysku w wartości majątku.
- Oznaczyć brak/starość danych i zachować agregaty per waluta. Szkic tych zmian jest w repo, ale nie został potwierdzony kompilacją ani uruchomieniem.

### 6. Frontend — przywrócony szkic do ręcznej oceny

Użytkownik poprosił o przywrócenie szkicu formularzy i szczegółów; zostało to wykonane. Nadal nie jest to zatwierdzony finalny projekt. Uzgodniony kierunek: jedna lista zwykłych i inwestycyjnych kont oraz inny komponent szczegółów zależnie od typu. Do dopracowania po poklikaniu pozostają formularz tworzenia lokaty, wybór źródła pieniędzy, prezentacja warunków/podatku, szczegóły produktu, operacje i obsługa transferów. Nie traktować wcześniejszych pomysłów na konkretne modale i wykres jako zatwierdzonego projektu UI.

### 7. Kolejne produkty i dalsze MVP

- Obligacje: konkretny typ/seria, pierwszy zakup przy tworzeniu, następne zakupy i ewentualne rozdzielenie partii/pozycji.
- ETF: instrument, jednostki, zakupy z różnych kont, sprzedaż oraz historia cen.
- Następnie cele finansowe, proste projekcje i scenariusze.

Backlog Finance Core: korekty salda, odwracanie/korygowanie transferów, filtr kategorii, paginacja i filtrowanie API. CRUD kont pozostaje zamrożony; aktywa rzeczowe, zobowiązania i pełny net worth są poza obecnym MVP. Zaległa ręczna weryfikacja istniejących transakcji z MySQL i Android WebView nie została zakończona.

## Checkpoint — rozsuwane filtry historii 2026-10-13

- Dodano wspólny komponent `TransactionHistoryFilters` z filtrem miesiąca i typu na obu listach oraz filtrem konta na dashboardzie.
- Sekcja PrimeNG Fieldset jest domyślnie zwinięta. Rozwijanie i zwijanie działa klawiaturą; zwijanie zachowuje wybrane filtry, a nagłówek pokazuje liczbę aktywnych filtrów.
- Licznik wyników pozostaje widoczny. Dodano reset oraz osobny komunikat o braku pasujących transakcji.
- Filtrowanie jest lokalne, oparte na `computed()` i pełnym snapshotcie; nie zmienia sald ani sum, nie wykonuje dodatkowych requestów i automatycznie uwzględnia snapshot po mutacji.
- Miesiąc jest porównywany jako lokalne `YYYY-MM`, bez konwersji przez UTC. Uzupełniono polskie nazwy miesięcy i etykiety nawigacji kalendarza PrimeNG.
- Sprawdzono układ przy 320 px, 480 px i na desktopie, rozwijanie klawiaturą, reset, brak wyników oraz łączenie filtrów.
- Na izolowanych odpowiedziach API sprawdzono zachowanie aktywnych filtrów po zmianie typu transakcji i po usunięciu oraz osobne sumy PLN/EUR. Dane użytkownika nie były modyfikowane.
- `npm run build` przeszedł. Backend, paginacja, filtr kategorii i Android WebView pozostają poza zakresem tego pakietu.

## Checkpoint — usuwanie transakcji 2026-10-13

- Dodano `DELETE /api/finance/transactions/{transactionId}` dla przychodów i wydatków lokalnego użytkownika `id = 1`.
- Usuwanie oraz obliczenie odświeżonego snapshotu odbywają się w jednej transakcji backendu; po `delete()` wykonywany jest `flush()` przed pobraniem overview.
- Obie listy mają przycisk kosza i wspólny mały dialog PrimeNG z kategorią, kontem, datą, kwotą, anulowaniem i potwierdzeniem.
- Podczas usuwania przyciski są blokowane; błąd pozostawia dialog otwarty i pozwala ponowić operację.
- Po sukcesie snapshot aktualizuje `FinanceStore`, salda, sumy per waluta i obie historie bez przeładowania.
- Sprawdzono układ potwierdzenia przy 320 px, 480 px i na desktopie oraz anulowanie na rzeczywistym UI bez usuwania danych użytkownika.
- Na izolowanym serwerze z danymi testowymi sprawdzono błąd i ponowienie, zmianę salda po usunięciu wydatku, aktualizację sum po usunięciu przychodu oraz pustą historię po usunięciu ostatniej transakcji. To weryfikacja frontendu z testowymi odpowiedziami API; backend wymaga ręcznej weryfikacji z MySQL.
- `npm run build` zakończył się poprawnie. Nie uruchamiano Mavena ani poleceń Capacitor/Android.

## Checkpoint — edycja transakcji 2026-10-13

- Dodano `PUT /api/finance/transactions/{transactionId}` z pełnym formularzem oraz odpowiedzią `updatedTransaction` i odświeżonym `overview`.
- Edytowana transakcja i wybrane aktywne konto muszą należeć do lokalnego użytkownika `id = 1`; obowiązuje walidacja typu, kwoty i daty.
- Dashboard i szczegóły konta mają widoczne przyciski edycji z ikoną oraz obszarem dotyku minimum 44 × 44 px.
- Wspólny komponent `TransactionDialog` obsługuje tworzenie i edycję; w edycji wczytuje wszystkie pola transakcji.
- Po sukcesie `FinanceStore` podmienia snapshot backendu, z którego szczegóły konta pobierają aktualne saldo i historię. Starszy request overview nie nadpisuje wyniku zapisu.
- Sprawdzono w przeglądarce otwieranie edycji na obu listach, reset formularza przy tworzeniu oraz walidację pustej kategorii. Sprawdzenie zapisu i sald z aktualnym backendem pozostaje do wykonania ręcznie.
- Zmiana sposobu prezentacji modala na telefonie oraz weryfikacja Android WebView zostały odłożone zgodnie z decyzją użytkownika.

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

1. **Etap 2 — zaimplementowane, do sprawdzenia z backendem:** edycja i usuwanie transakcji z poziomu obu list;
2. **Etap 2 — filtry lokalne gotowe; później:** filtr kategorii, paginacja i filtrowanie API, korekty oraz transfery;
3. **Etap 3 — wdrożony 2026-10-16:** miesięczne przychody, wydatki, cashflow i savings rate; integracja z MySQL do ręcznej weryfikacji;
4. **Etap 4 — odłożony poza obecne MVP:** aktywa rzeczowe i zobowiązania; zachować pomysły rozwojowe;
5. **Etap 5 — plan backendu:** InvestmentAccount → MoneyTransfer → zakładanie lokaty → historia → overview; roboczy szkic do przeglądu, frontend do uzgodnienia;
6. **Po inwestycjach:** cele i proste projekcje/scenariusze; przekrojowa historia net worth z Etapu 6 pozostaje późniejszym zakresem.

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
