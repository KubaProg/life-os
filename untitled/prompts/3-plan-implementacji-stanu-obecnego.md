# Plan implementacji punktu 2.1 — stan obecny finansów

## 1. Cel dokumentu

Ten dokument konkretyzuje implementację punktu 2.1 z `personal_finance_os.md`:

> **Stan obecny — „gdzie jestem?”**

Po zakończeniu tego etapu aplikacja ma odpowiadać na cztery podstawowe pytania:

1. Ile wynosi mój majątek netto?
2. Gdzie znajdują się moje pieniądze i pozostałe składniki majątku?
3. Jakie mam zobowiązania?
4. Jak wyglądają moje przychody, wydatki, zysk z inwestycji i stopa oszczędzania w wybranym miesiącu?

Cele finansowe, projekcje i scenariusze pozostają następnymi obszarami szerszego MVP po inwestycjach. AI i integracje bankowe są poza obecnym zakresem. Pozyskiwanie cen rynkowych jest rozpoznawane jako część przyszłego pakietu inwestycji; dostawca nie został jeszcze wybrany.

### Aktualny kierunek obligacji — aktualizacja 2026-10-21

Ta aktualizacja i sekcja [Obligacje detaliczne: model, rozliczenia i źródła danych](#obligacje-detaliczne-model-rozliczenia-i-źródła-danych--2026-10-21) zastępują sprzeczne, wcześniejsze ustalenia dotyczące wspólnego `InvestmentAccount`, zakupu przy tworzeniu konta i konfiguracji produktu przez użytkownika. Starsze sekcje z datami zachowujemy jako historię decyzji.

- W kodzie wspólny `Account` ma osobne profile: `FinancialAccount`, `DepositAccount`, `BondAccount` i `BrokerageAccount`. `InvestmentAccount` został zastąpiony; migracja V8 rozdziela dotychczasowe profile i zachowuje identyfikatory. To opis źródeł, nie potwierdzenie wykonania migracji ani działania aplikacji.
- Konto obligacyjne powstaje bez kwoty. Dopłaty trafiają na jego saldo gotówki; zakup obligacji jest osobną operacją na tym koncie.
- MVP nie wymaga panelu samodzielnego ustalania reguł obligacji. Reguły pochodzą z warunków emisji państwowych; panel utrzymania katalogu oraz integracja Finwire są odłożone.
- Zaimplementowano źródła pierwszego produktu TOS1029 (4,40%): modele `BondIssue`, `Bond`, `BondOperation`, zakup, częściowy wykup i automatyczne rozliczenia. Szczegóły aktualnego zakresu są poniżej; wcześniejsze propozycje pozostają historią projektu.
- Weryfikacja agentem pozostaje przeglądem źródeł, bez buildów, testów, uruchamiania aplikacji, przeglądarki ani Mavena.

### Priorytet MVP — aktualizacja 2026-10-19

Miesięczne podsumowanie jest wdrożone. Inwestycje mają roboczy szkic backendu do przeglądu, bez potwierdzenia kompilacji i działania; obowiązuje kolejność prac uzgodniona 2026-10-19. Cashflow nie jest prezentowany w UI. Nie ma osobnej karty dziennego balansu.

Etap 5 realizujemy backendowo: najpierw InvestmentAccount, potem MoneyTransfer, następnie serwis i operacje zakładania lokaty, historia i overview. Frontend wymaga osobnego uzgodnienia. Później obligacje i ETF, z kolejnymi zakupami. Szczegółowa kolejność jest opisana w Etapie 5. Po inwestycjach cele finansowe oraz proste projekcje/scenariusze wymagające osobnych planów.

CRUD kont pozostaje zamrożony. Filtr kategorii, paginacja i filtrowanie API są odłożone. Transfery i korekty salda pozostają osobnymi funkcjami Finance Core; transfer kapitału konieczny dla inwestycji należy uwzględnić w odpowiednim przepływie. Aktywa rzeczowe, zobowiązania i pełny net worth pozostają poza obecnym MVP.

Ręczna weryfikacja mutacji i agregacji z aktualnym backendem/MySQL oraz Android WebView pozostaje otwarta. Na ostatnie polecenie użytkownika agent nie uruchamia testów, Playwrighta ani buildu; wymagania jakościowe poniżej opisują przyszłą weryfikację, nie zgodę na jej uruchomienie teraz. Nie uruchamiać Mavena ani poleceń Capacitor zgodnie z AGENTS.md.


### Wspólne Account i przywrócony szkic UI — aktualizacja 2026-10-19

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

### Model inwestycji i transferów — uzgodnienia 2026-10-19

#### Status i zakres tej sesji

- Na tym etapie porządkujemy plan backendu. W trakcie rozmowy powstał **roboczy szkic** encji, migracji V4, transferów, serwisów i obliczeń lokaty. Nie był kompilowany ani uruchamiany i nie oznacza ukończenia opisanych niżej etapów. Należy go przejrzeć etapami, zaczynając od modelu encji.
- Frontend lokat/transferów został ponownie przywrócony na wyraźną prośbę użytkownika jako szkic do ręcznej oceny; finalny projekt pozostaje do uzgodnienia.
- Nie uruchamiano testów, Playwrighta, buildu, Mavena, migracji ani poleceń Android/Capacitor. Zasady te pozostają aktualne do kolejnej decyzji użytkownika; AGENTS.md nadal zabrania Mavena i automatycznej obsługi Capacitor.

#### Przyjęty prosty model

- `FinancialAccount` pozostaje zwykłym kontem: bankowym, oszczędnościowym albo gotówką.
- Powstaje osobna encja **`InvestmentAccount`**, początkowo reprezentująca jedną inwestycję, z typem np. `DEPOSIT`, `BOND`, `ETF`, `OTHER`. Wartość enuma nie oznacza, że dany produkt ma już gotowy serwis lub kalkulator.
- Nie tworzymy teraz osobnych encji obligacji, ETF i lokaty ani osobnych tabel produktu i pozycji. Wspólne pola są w `InvestmentAccount`; pierwszą grupę warunków lokaty można osadzić jako `DepositTerms` przez `@Embedded`, w tej samej tabeli. Nieużywane pola mogą być puste, z walidacją zależną od typu.
- Przy wdrażaniu kolejnych zakupów obligacji/ETF wrócimy do rozdzielenia instrumentu, jednostek i partii/pozycji, jeśli będzie to potrzebne. Nie zastępować liczby jednostek i cen jedną nadpisywaną kwotą rachunku.
- Rachunek inwestycyjny **nie ma stałego konta finansującego**. Konto źródłowe wybierane przy założeniu lub zakupie jest parametrem operacji; ślad źródła pozostaje w transferze.
- Inwestycja jest tworzona **od razu na określoną kwotę**, z pierwszym finansowaniem/zakupem. Nie wprowadzamy widocznego dla użytkownika etapu pustej lokaty, którą trzeba dopiero zasilić.
- Lokata: jedna wpłata przy otwarciu, bez dopłat w pierwszym wariancie. Obligacje i ETF: pierwszy zakup przy tworzeniu, potem możliwość kolejnych zakupów jako osobnych operacji, również z innych kont.

#### Transaction, MoneyTransfer i operacje inwestycyjne

- `Transaction` pozostaje zapisem przychodu lub wydatku powiązanego ze zwykłym `FinancialAccount`. Kwota jest dodatnia, a `INCOME`/`EXPENSE` określa wpływ plus/minus na wyliczane saldo. Nie dodajemy drugiego opcjonalnego `investmentAccount` tylko po to, aby udawać transfer przychodem lub wydatkiem.
- **`MoneyTransfer`** jest osobnym wspólnym mechanizmem: źródło, cel, kwota, waluta i data. Obie strony wskazują wspólny `Account` przez globalne ID. Rodzina wynika z zapisanego rachunku, a nie z dodatkowej relacji w transferze.
- Transfer obciąża źródło i uznaje cel atomowo. Zwykły transfer kapitału nie jest przychodem, konsumpcyjnym wydatkiem ani zyskiem inwestycyjnym. Nie zapisujemy dodatkowej `Transaction` z tym samym obciążeniem.
- Reguły produktu ograniczają ogólny mechanizm: lokata nie przyjmuje dopłat i nie pozwala na częściowe wycofanie w pierwszym wariancie; zakup obligacji/ETF będzie wymagał osobnej operacji produktu.
- **Operacja inwestycyjna** opisuje sens zdarzenia (założenie lokaty, zakup jednostek, wypłata odsetek, zwrot kapitału) i może być powiązana z transferem. Nie powoduje ponownego obciążenia zwykłego konta. Naliczona wycena nie musi mieć transferu gotówki.
- Transfery walutowe z przewalutowaniem, odwracanie transferów i szersze reguły produktów są osobnym zakresem. Pierwszy mechanizm ograniczamy do tej samej waluty i lokalnego użytkownika `id = 1`.

#### Zysk miesięczny i trwała historia

- Wskaźnik na dashboardzie nazywa się **„Zysk z inwestycji”**, dotyczy wybranego miesiąca i ma plus przed dodatnią kwotą. Nie jest sumą sald, kapitału ani prognozą.
- Trwała historia operacji oraz naliczeń/wycen powstanie w osobnych tabelach. Samo pole bieżącej wartości albo jedna miesięczna suma nie wystarczy do wyjaśnienia wyniku i odtworzenia poprzednich okresów.
- Snapshoty narastające nie są dziennymi zyskami do dodawania. Miesięczny wynik wynika z granic okresu i zdarzeń w jego trakcie. Nowy miesiąc zmienia zakres raportu, **nie zeruje inwestycji ani wcześniejszego zysku**.
- Odczyt dashboardu może uzupełnić brakujące naliczenia, ale nie może przy każdym otwarciu ponownie dopisywać tego samego zysku do kapitału. Historia ma być odtwarzalna również za dni bez otwierania aplikacji.
- Kapitalizacja i wypłata nie tworzą drugi raz wcześniej naliczonego zysku. Wpłacony/wycofany kapitał nie jest zyskiem; wypłacony dochód wymaga rozdzielenia kapitału, odsetek, podatku i ewentualnych opłat.
- Dokładne warunki pierwszej lokaty (harmonogram kapitalizacji/wypłaty, konwencja dni, podatek i zaokrąglenia) muszą być jawne. Szkic używa stałej stopy, ACT/ACT 365/366 i wypłaty na koniec; nie traktować tych reguł jako uniwersalnych warunków każdej lokaty.
- Agregaty pozostają osobno per waluta. Rynkowa wycena ETF będzie wymagała cen ze źródłem i datą; brak danych nie oznacza wyniku zero.

### Stan realizacji

Ukończone:

- [x] `User` i `UserRepository` przeniesione do `com.example.appbe.user`,
- [x] klasy kont przeniesione do `com.example.appbe.finance.account`,
- [x] klasy transakcji przeniesione do `com.example.appbe.finance.transaction`,
- [x] importy zaktualizowane; brak referencji do starych pakietów `domain.user` i `domain.finance`,
- [x] aplikacja uruchamia się po refaktorze — potwierdzone lokalnie przez użytkownika,
- [x] refaktor nie zmienił zachowania domeny,
- [x] model konta używa `openingBalance` i `balanceStartDate`,
- [x] migracja Flyway V2 zachowuje dotychczasową wartość salda jako saldo początkowe,
- [x] dodane read-only API aktywnych kont: lista i szczegóły,
- [x] kontroler zwraca `AccountDto`, a nie encję JPA,
- [x] brak konta aktywnego zwraca `404 Not Found`,
- [x] pakiet `exception` zawiera bazowy `BusinessException`, wspólny `ApiError` i globalny `@RestControllerAdvice`,
- [x] pojedynczy handler obsługuje wszystkie wyjątki biznesowe na podstawie ich statusu i kodu,
- [x] błędy `@Valid` są mapowane do wspólnego `ApiError` z kodem `VALIDATION_FAILED`,
- [x] migracja Flyway V3 tworzy lokalnego użytkownika `id = 1` oraz przykładowe zwykłe konta,
- [x] dodane tworzenie konta przez `POST /api/finance/accounts`,
- [x] dodana częściowa edycja nazwy, typu i waluty przez `PATCH /api/finance/accounts/{accountId}`,
- [x] create i update akceptują wyłącznie `BANK_ACCOUNT`, `SAVINGS` i `CASH`,
- [x] mapowanie `FinancialAccount` → `AccountDto` zostało wydzielone do `AccountMapper`,
- [x] Angular posiada `AccountApiService` oraz modele kontraktu API,
- [x] dashboard i szczegóły konta pobierają rzeczywiste dane z backendu,
- [x] widoki odczytu obsługują loading, empty, error i `404`,
- [x] konta i transakcje korzystają z rzeczywistych danych; jedyny tymczasowy mock w podsumowaniu to jawnie oznaczony miesięczny zysk z inwestycji,
- [x] proxy Angular dev-servera przekazuje `/api` do backendu na porcie 8080,
- [x] responsywność widoków odczytu sprawdzona przy 320 px, 480 px i 1280 px,
- [x] frontend opakowany w natywną aplikację Android przez Capacitor 8,
- [x] aplikacja Android uruchomiona w emulatorze i zainstalowana na fizycznym telefonie z debug APK,
- [x] backend posiada tymczasowy globalny CORS `*` dla `/api/**` bez credentials,
- [x] dodane tworzenie przychodów i wydatków przez `POST /api/finance/transactions`,
- [x] bieżące saldo konta jest liczone z salda początkowego i transakcji,
- [x] `GET /api/finance/overview` zwraca sumy per waluta, konta i wspólną historię,
- [x] szczegóły konta zwracają konto wraz z jego historią transakcji,
- [x] Angular posiada lekki `FinanceStore` oparty na Signals,
- [x] dashboard posiada przypiętą akcję, modal dodawania transakcji i rzeczywistą historię.

Aktualny krok (2026-10-19):

- [x] dodać miesięczne agregacje `INCOME`/`EXPENSE`, cashflow w API oraz stopę oszczędzania, osobno per waluta,
- [x] wdrożyć cztery kompaktowe kafelki: zysk z inwestycji, przychody, wydatki i stopa oszczędzania, bez osobnej dziennej karty i bez cashflow w UI,
- [x] przekazywać miesiąc przy odczycie overview i mutacjach transakcji; odświeżać overview przy wejściu na dashboard,
- [ ] przejrzeć roboczy model inwestycji i kolejne elementy backendu według uzgodnionej kolejności; szkic w repo nie jest ukończonym etapem,
- [ ] domknąć backend pierwszej lokaty, osobno uzgodnić frontend, potem produkty obligacyjne i ETF (Etap 5),

- [ ] ręcznie zweryfikować pełny przepływ transakcji z uruchomionym backendem,
- [x] dodać rozsuwane filtry lokalne miesiąca i typu na obu listach oraz konta na dashboardzie,
- [x] dodać edycję istniejącej transakcji przez przycisk z ikoną i modal,
- [x] dodać usuwanie transakcji z potwierdzeniem i odświeżonym snapshotem na obu listach; frontend sprawdzony na izolowanych odpowiedziach API, backend do ręcznej weryfikacji,
- [x] zaimplementować aktualizację snapshotu, sald i historii po edycji bez przeładowania strony; ręczne sprawdzenie z aktualnym backendem pozostaje do wykonania.

### Checkpoint — koniec sesji 2026-09-30

Backend posiada działający szkielet feature'a kont:

```text
GET /api/finance/accounts
GET /api/finance/accounts/{accountId}
```

Odczyt działa przez `AccountController` → `AccountService` → `FinancialAccountRepository`. Publiczny kontrakt stanowi `AccountDto`; encja JPA nie jest zwracana przez API. Wyjątki biznesowe dziedziczą po `BusinessException` i są mapowane przez jeden `GlobalExceptionHandler` do `ApiError` zawierającego status HTTP, kod błędu, komunikat i ścieżkę.

Endpointy pozostają tymczasowo nieuwierzytelnione i zwracają aktywne konta bez filtrowania właściciela. Jest to świadomie zaakceptowane wyłącznie dla lokalnego trybu single-user. Następna sesja powinna rozpocząć się od `AccountRequest` oraz endpointu tworzenia konta, a następnie osobno dodać update i archive.

### Checkpoint — koniec sesji 2026-10-01

Backend kont obsługuje obecnie:

```text
GET   /api/finance/accounts
POST  /api/finance/accounts
GET   /api/finance/accounts/{accountId}
PATCH /api/finance/accounts/{accountId}
```

`AccountRequest` waliduje wymagane dane tworzonego konta, a `UpdateAccountRequest` pozwala częściowo zmienić nazwę, typ lub walutę. Pusty request aktualizacji jest dozwolonym no-op. Pierwszy zakres nadal akceptuje wyłącznie `BANK_ACCOUNT`, `SAVINGS` i `CASH`; pozostałe typy zwracają biznesowy błąd `UNSUPPORTED_ACCOUNT_TYPE`. Mapowanie encji na odpowiedź wykonuje osobny `AccountMapper`.

Lokalny tryb single-user używa użytkownika `id = 1`. Migracja `V3__seed_local_finance_data.sql` tworzy tego użytkownika oraz trzy przykładowe konta. Nie dodano profilu `local` ani tymczasowej abstrakcji bieżącego użytkownika.

Frontend posiada `AccountApiService` dla listy, szczegółów, create i update oraz modele TypeScript zgodne z kontraktem backendu. Dashboard i ekran szczegółów korzystają już z rzeczywistego read API. Salda na dashboardzie są grupowane według waluty bez ich przeliczania. Widoki obsługują loading, empty, error i `404`; sekcje zależne od transakcji są oznaczone jako niedostępne zamiast prezentowania mocków. Stary `FinanceDataService` pozostaje w repozytorium, ale nie jest już używany przez komponenty.

Angular dev-server korzysta z `proxy.conf.json`, który przekazuje `/api` do `http://localhost:8080`. Build frontendu zakończył się poprawnie, a layout sprawdzono przy 320 px, 480 px i 1280 px. Testów automatycznych nie dodawano zgodnie z decyzją użytkownika. Nie uruchamiano Mavena ani Maven Wrappera.

Następne małe paczki powinny objąć osobno archiwizację konta, frontendowy formularz create/update oraz podłączenie tych operacji do UI.

### Checkpoint — Android i powrót do feature'ów 2026-10-11

Frontend posiada zależności Capacitor `8.5.3`, konfigurację `capacitor.config.ts` oraz natywny projekt w `app-fe/android`. Produkcyjny build Angulara jest kopiowany z `dist/app-fe/browser`. Aplikacja została poprawnie uruchomiona w emulatorze, a wygenerowany debug APK został zainstalowany i uruchomiony na fizycznym telefonie.

`AccountApiService` nadal korzysta z relatywnego `/api`. Publiczny URL backendu i komunikacja z API zainstalowanej aplikacji zostały świadomie odłożone; nie blokują dalszej implementacji funkcji ani sprawdzania samego interfejsu na Androidzie. Backend ma tymczasową globalną konfigurację CORS dla `/api/**`, dopuszczającą wszystkie originy, metody i nagłówki bez credentials. Przed udostępnieniem backendu konfiguracja powinna zostać zawężona.

Od tego checkpointu ponownie skupiamy się na Etapie 1 planu finansowego: archiwizacji konta oraz formularzu create/update. Po zmianach frontendu wersję Android można odświeżyć przez `npm run android:sync`; przygotowanie publicznego HTTPS API pozostaje osobnym późniejszym zadaniem infrastrukturalnym.

### Checkpoint — pierwszy pionowy wycinek transakcji 2026-10-11

Zarządzanie kontami oraz security zostały świadomie odłożone. Aplikacja nadal działa jako local single-user z użytkownikiem `id = 1` i korzysta z kont seedowanych w bazie.

Backend obsługuje tworzenie `INCOME` i `EXPENSE`, waliduje dodatnią kwotę, aktywne konto oraz datę nie wcześniejszą niż `balanceStartDate`. Waluta transakcji wynika z konta. `currentBalance` jest liczony jako saldo początkowe plus wpływ transakcji. Endpoint overview zwraca jeden snapshot z sumami kont pogrupowanymi według waluty, aktualnymi kontami i historią wszystkich transakcji. Odpowiedź zapisu zawiera utworzoną transakcję i kompletny odświeżony snapshot.

Frontend używa `FinanceApiService` do HTTP oraz współdzielonego `FinanceStore` opartego na natywnych Angular Signals. Store żyje w pamięci instancji aplikacji; po pełnym odświeżeniu ponownie pobiera dane. Dashboard ma przypięty przycisk „Dodaj transakcję”, modal PrimeNG z Reactive Forms oraz wspólną historię. Szczegóły konta pobierają aktualne saldo i historię danego konta jednym requestem.

Build produkcyjny Angulara przechodzi. Backend nie był kompilowany Mavenem zgodnie z zasadami repozytorium; wymagane pozostaje ręczne sprawdzenie po ponownym uruchomieniu backendu.

Następnym zaakceptowanym wycinkiem jest edycja transakcji. Każda pozycja na wspólnej historii oraz w szczegółach konta ma otrzymać widoczny przycisk z ikoną edycji. Przycisk otwiera ten sam formularz PrimeNG co tworzenie, wypełniony aktualnymi danymi. Po zapisie odpowiedź backendu ma odświeżyć `FinanceStore`, sumy per waluta, salda kont oraz widoczne historie bez przeładowania strony.

---

## 2. Stan repozytorium na początku prac — opis historyczny

Stan poniżej dokumentuje punkt startowy; aktualny stan realizacji znajduje się w sekcji 1.

### Już istnieje

Backend:

- encje `User`, `FinancialAccount` i `Transaction`,
- enumy typów kont i transakcji,
- repozytoria Spring Data JPA,
- pierwsza migracja Flyway,
- podstawowe zapytania o konta, ostatnie transakcje i sumy miesięczne,
- lokalna baza uruchamiana przez Docker Compose.

Frontend:

- dashboard finansowy,
- widok szczegółów konta,
- responsywny kierunek interfejsu,
- komponenty PrimeNG i motyw Aura,
- mockowane konta i transakcje w `FinanceDataService`.

### Brakuje

- serwisów aplikacyjnych i logiki biznesowej backendu,
- kontrolerów REST oraz DTO,
- walidacji wejścia i spójnej obsługi błędów,
- połączenia Angulara z backendem,
- formularzy CRUD dla kont i transakcji,
- aktywów innych niż salda kont,
- zobowiązań,
- poprawnego modelu lokat, obligacji i pozycji inwestycyjnych,
- agregacji dashboardu po stronie backendu,
- net worth, cashflow i savings rate liczonych z rzeczywistych danych,
- historii wartości majątku,
- testów domeny, API i kluczowych przepływów UI.

### Przyjęta decyzja dotycząca bazy

Dokument koncepcyjny wskazuje PostgreSQL, ale aktualny projekt korzysta z MySQL:

- sterownik i zależności Flyway są dla MySQL,
- `application.properties` wskazuje MySQL,
- oba pliki Docker Compose uruchamiają MySQL,
- migracja używa składni MySQL (`AUTO_INCREMENT`, `DATETIME`).

Decyzja: pozostajemy przy obecnie zaimplementowanym MySQL. Kolejne migracje, testy integracyjne i konfiguracja środowisk powinny konsekwentnie używać MySQL; wzmianka o PostgreSQL w dokumencie koncepcyjnym nie jest aktualnym zadaniem implementacyjnym.

---

## 3. Zakres funkcjonalny punktu 2.1

### 3.1. Konta finansowe

Użytkownik może:

- zobaczyć listę aktywnych kont,
- dodać konto,
- edytować nazwę, typ i walutę konta,
- skorygować saldo,
- zarchiwizować konto bez usuwania historii,
- wejść w szczegóły konta i zobaczyć jego transakcje.

Minimalne typy:

- `BANK_ACCOUNT`,
- `SAVINGS`,
- `CASH`,
- `BROKERAGE`,
- `DEPOSIT`,
- `OTHER`.

### 3.2. Transakcje

Użytkownik może:

- dodać przychód lub wydatek,
- wskazać konto, datę, kwotę, kategorię i opis,
- przeglądać transakcje z paginacją,
- filtrować je po miesiącu, koncie, typie i kategorii,
- edytować oraz usuwać transakcję,
- wykonać transfer pomiędzy własnymi kontami bez zaliczania go do przychodu lub wydatku.

Wymagania UI dla edycji:

- każda transakcja na dashboardzie i w szczegółach konta ma widoczny, dostępny z klawiatury przycisk z ikoną edycji,
- aktywny obszar przycisku ma co najmniej 44 × 44 px i nie może być dostępny wyłącznie po hoverze,
- edycja wykorzystuje ten sam modal i formularz co tworzenie transakcji,
- formularz w trybie edycji jest wypełniony istniejącymi wartościami i pozwala zmienić konto, typ, kwotę, datę, kategorię oraz opis,
- zapis pokazuje stan oczekiwania, blokuje podwójne wysłanie i pozostawia modal otwarty przy błędzie,
- po sukcesie modal się zamyka, a sumy, salda i obie historie aktualizują się bez przeładowania strony.

Transakcja pozostaje centralną jednostką systemu, ponieważ w przyszłości ten sam przypadek użycia będzie wywoływany przez chat i voice.

### 3.3. Aktywa rzeczowe i pozostałe — odłożone poza obecne MVP

Użytkownik może ręcznie rejestrować aktywa, których nie reprezentuje saldo konta:

- samochód,
- nieruchomość,
- inne aktywo.

Minimalne dane aktywa:

- nazwa,
- typ,
- aktualna wartość,
- waluta,
- data wyceny,
- opcjonalny opis,
- status aktywne/archiwalne.

W tym etapie wartość jest aktualizowana ręcznie. Amortyzacja i historia wycen mogą zostać dodane jako osobny krok po działającym CRUD.

### 3.4. Zobowiązania — odłożone poza obecne MVP

Użytkownik może rejestrować:

- kredyt hipoteczny,
- pożyczkę,
- kartę kredytową,
- leasing,
- inne zobowiązanie.

Minimalne dane zobowiązania:

- nazwa,
- typ,
- aktualne saldo pozostałe do spłaty,
- waluta,
- data aktualizacji,
- opcjonalna rata miesięczna,
- status aktywne/spłacone.

Oprocentowanie, harmonogram rat i rozbicie raty na kapitał oraz odsetki nie są wymagane do pierwszego widoku stanu obecnego.

### 3.5. Inwestycje, obligacje i lokaty

Planowany pierwszy wariant lokaty (obecnie roboczy szkic) używa osobnej encji `InvestmentAccount` z typem `DEPOSIT`, `BOND`, `ETF` lub `OTHER`. Szkic serwisu obsługuje wyłącznie `DEPOSIT`; pozostałe typy wymagają własnej implementacji, a cały wycinek pozostaje do domknięcia.

Warunki lokaty (`DepositTerms`) są osadzone przez `@Embedded` w tej samej tabeli: roczny procent, podatek, daty i wypłata na koniec. Osobne tabele przechowują operacje, transfery i historię narastających wycen. Rachunek nie ma przypisanego stałego konta finansującego — źródło i cel są własnością każdego transferu.

Lokata jest tworzona z kwotą i pierwszym transferem w jednej transakcji bazodanowej. Kolejne dopłaty są zabronione w tym wariancie. Dla obligacji i ETF docelowo pierwszy zakup będzie częścią tworzenia, a kolejne zakupy będą odrębnymi operacjami, również z innych kont. Wtedy, jeśli będzie potrzebne rozdzielenie instrumentu i wielu pozycji/partii, model zostanie rozszerzony zamiast dodawania ich na zapas.

Dla ETF potrzebne będą instrument, liczba jednostek, operacje zakupu/sprzedaży i historyczna cena konkretnego funduszu, z datą i źródłem. Oczekiwana stopa zwrotu służy prognozie, nie zastępuje ceny. Model szczegółów obligacji zależy od wybranego typu/serii.

#### Prezentacja inwestycji i granice modelu

- Jedna lista pozycji na dashboardzie obejmuje konta zwykłe, obligacje, lokaty i ETF. Wspólna prezentacja nie oznacza identycznej logiki domenowej.
- Typ pozycji kieruje do historii zwykłego konta albo osobnego komponentu szczegółów inwestycji.
- Dashboard inwestycji: bieżąca wartość, kapitał/wpłaty netto, wynik, odsetki oraz wykres wzrostu; poniżej konfiguracja i historia wpłat/wypłat lub zakupów/sprzedaży jednostek. Zestaw wskaźników zależy od produktu.
- Lokata/obligacja: ustalić oprocentowanie i jego okres, kapitalizację, datę startu/wykupu, serię oraz warunki wcześniejszego wycofania środków. Dla ETF: ticker, giełda, waluta, jednostki i cena zakupu; uwzględnić opłaty i zdarzenia wpływające na pozycję.
- Podatki i opłaty liczy backend według ustalonego modelu produktu. Wycena bieżąca, naliczone odsetki, wartość możliwa do wypłaty i prognoza mają jednoznaczne osobne znaczenia.
- Do sumowania wybrać jedną bieżącą wartość pozycji. Odsetek albo niezrealizowanego zysku uwzględnionego już w tej wartości nie dodawać drugi raz.
- Zakup inwestycji finansowany z własnego konta przenosi wartość; nie tworzy nowego majątku ani konsumpcyjnego wydatku. Wpłat i wypłat nie mylić ze stopą zwrotu.
- Zwykły `FinancialAccount` i inwestycyjny `InvestmentAccount` pozostają osobnymi encjami. Docelowa wspólna lista UI będzie korzystać ze snapshotu overview i otwierać właściwy komponent szczegółów; kontrakt i UI pozostają do domknięcia w swoich etapach. Enum inwestycji nie oznacza gotowej obsługi każdego produktu.

#### Historia inwestycji w bazie — decyzja 2026-10-19

Trwała historia naliczeń i wycen jest wymagana do raportowania zysków za poprzednie miesiące. Nie wystarczy przechowywać jednej nadpisywanej wartości bieżącej ani wyłącznie miesięcznej sumy.

- Model rozdzieli produkt/instrument i jego warunki, pozycję/kapitał, operacje oraz historię naliczeń i wycen. Konkretne nazwy tabel i kolumn ustalimy przed migracją; obecnie tabel inwestycyjnych jeszcze nie dodano.
- Historia naliczeń/wycen powinna identyfikować inwestycję, datę/czas, walutę, wartość oraz naliczony wynik; dla danych rynkowych także źródło i czas notowania. Rozróżniamy kwoty za okres od wartości narastających, aby nie sumować kolejnych stanów jako kolejnych zysków.
- Operacje zachowują wpłaty, wypłaty, zakupy, sprzedaże, kapitalizacje, wypłaty odsetek, opłaty i podatki, zgodnie z regułami danego produktu. Wpłaty/wypłaty kapitału nie są zyskiem.
- Bieżący miesiąc obejmuje wynik do chwili aktualnego naliczenia lub dostępnej wyceny. Zakończony miesiąc korzysta z historycznych danych i granic okresu; nie z dzisiejszej ceny ani obecnego kapitału.
- Samo odejmowanie wartości końcowej i początkowej nie wystarczy, gdy w okresie były wpłaty, wypłaty lub wypłacone dochody. Backend uwzględnia te operacje według reguł produktu.
- Nowy miesiąc zmienia zakres raportu, bez zerowania kapitału lub usuwania wcześniejszego zysku. Niewypłacone odsetki pozostają częścią wartości inwestycji, a wypłacone środki są na koncie gotówkowym. Nie liczymy obu składników drugi raz.
- Odczyt overview może przeliczać wynik i uzupełniać brakujące dane historyczne w sposób powtarzalny, bez duplikatów; nie może przy każdym otwarciu dashboardu ponownie dopisywać zysku do kapitału. Kapitalizacja jest osobnym zdarzeniem uwzględnianym dokładnie raz.
- Obliczenia muszą odtworzyć brakujące okresy również wtedy, gdy aplikacja nie była otwierana. Nie polegamy na codziennym wejściu użytkownika; rekurencja nie jest wymaganiem.
- Dzienne snapshoty mogą stanowić historię wycen lub przyspieszać odczyt. Osobna tabela miesięcznych podsumowań jest opcjonalną optymalizacją, nie jedynym źródłem prawdy. Dla ETF brak historycznej ceny oznacza brak/niepełność danych, a nie wynik zero.
- Zysk inwestycyjny prezentujemy oddzielnie od przychodów z transakcji. Niezrealizowanej wyceny nie dopisujemy automatycznie do `INCOME` ani do stopy oszczędzania. Uznanie wypłaconego dochodu i podatków wymaga reguł produktu, bez podwójnego liczenia.

#### Obligacje detaliczne: model, rozliczenia i źródła danych — 2026-10-21

##### Zaimplementowany zakres TOS1029 — 2026-10-21

Opis źródeł po wdrożeniu; migracji i aplikacji nie uruchamiano. Ta podsekcja zastępuje określenie poniższych elementów jako wyłącznie proponowanych.

- V9 dodaje `bond_issues`, `bonds`, `bond_operations` i katalog jednej konkretnej emisji TOS1029. Zachowuje konta i transfery z V8. Emisji nie przedłużamy automatycznie na kolejne miesiące: dla nowej oferty trzeba dodać osobną emisję z jej warunkami. Panel katalogu i Finwire nadal są odłożone.
- Parametry emisji: sprzedaż 1–31.10.2026, PLN, 100 zł za sztukę, stałe 4,40% rocznie, trzy lata od zakupu, roczna kapitalizacja i odsetki płatne przy wykupie. Wartość jednej sztuki po trzech latach przed podatkiem: 113,79 zł.
- Jedna partia `Bond` reprezentuje jeden zakup. Zachowuje oryginalną liczbę sztuk, pozostałą liczbę i sztuki zarezerwowane do wykupu. Kapitał i bieżący zysk w UI dotyczą wyłącznie pozostałych sztuk. Po rozliczeniu 5 z 23 sztuk kapitał zmienia się z 2300 na 1800 zł; wypłacony zysk jest osobną pozycją historii i podsumowania.
- `BondCalculator` liczy wartość jednej sztuki wg załączników do listu emisyjnego, z ACT/ACT rocznego okresu odsetkowego. `Bond` przechowuje ostatni roczny stan kapitalizacji. Wycena bieżąca nie odtwarza codziennych zdarzeń od zakupu; historyczne miesięczne wyniki korzystają z trwałych operacji. Historycznych kwot wypłaty nie zastępujemy nowymi wyliczeniami.
- Podatek zwykłego rachunku: 19% od odsetek po pomniejszeniu o opłatę; kapitał nie jest opodatkowany. W MVP podstawą jest łączna wypłata odsetek w jednej operacji partii; podatek zaokrąglamy w górę do grosza. Wycena bieżąca pokazuje szacowany podatek, bez opłaty za hipotetyczny wcześniejszy wykup. Kapitalizacja nie wypłaca gotówki i nie pobiera podatku.
- Wcześniejszy wykup: dowolna dostępna liczba sztuk, po siedmiu dniach od zakupu, najpóźniej 20 dni przed zapadalnością. Opłata do 1 zł/szt., ograniczona narosłymi odsetkami. Dyspozycja rezerwuje sztuki i jest nieodwołalna. Uwzględniamy pięć kolejnych dni roboczych naliczania i wypłatę po ich upływie (następny dzień roboczy), zgodnie z pkt 24 i przykładem operatora; stosowany w kalkulatorze koniec naliczania jest wyłączny. Gotówka pojawia się dopiero w dniu rozliczenia. Przed rozliczeniem zarezerwowane sztuki nadal należą do portfela.
- Zapadalność: odsetki kończą się w terminie umownym. Jeżeli wypłata przypada w sobotę lub święto, gotówka trafia na konto w pierwszym kolejnym dniu roboczym. Kalendarz obejmuje polskie święta stałe i ruchome, w tym Wigilię od 2025 r.
- Zakup można zarejestrować z datą historyczną wewnątrz okresu sprzedaży emisji, jeśli saldo gotówki było wystarczające wtedy i po późniejszych zapisanych operacjach. Nowe dyspozycje wcześniejszego wykupu mają dzisiejszą datę. Oba formularze domyślnie wybierają dzisiaj, konto jest ustalone przez otwarty ekran.
- `BondAccountService` agreguje gotówkę i wartość papierów; `BondService` odpowiada za zakup, dyspozycje i rozliczenie; `BondCalculator` za matematykę. Ledger gotówki obejmuje transfery oraz wyłącznie rozliczone operacje obligacji. Zakup i wykup nie są sztucznymi transferami ani transakcjami wydatku/przychodu. Miesięczny cashflow dostaje tylko wypłacone odsetki netto.
- Rozliczenia uruchamia Spring `@Scheduled` co godzinę, pięć minut po pełnej godzinie, w strefie Europe/Warsaw; także start backendu i odczyt/operacja zależna od salda. Blokada lokalnego użytkownika i unikalne klucze zdarzeń/żądań chronią przed powtórnym zakupem lub zaksięgowaniem wypłaty. Po wyłączeniu aplikacji terminy zostają w bazie i są nadrabiane po uruchomieniu. W UI dodano odświeżanie salda i rozliczeń.
- Konto z otwartymi albo oczekującymi na wykup obligacjami nie może być usunięte. Po całkowitym wykupie można przenieść gotówkę i usunąć konto; partie i ich operacje pozostają w bazie z oryginalnym identyfikatorem i nazwą konta.
- To ewidencja w Life OS. Zakup, dyspozycja i wypłata nie wysyłają zleceń do banku. IKE/IKZE, zamiana, rynek wtórny, kolejne typy i integracja brokerska są poza tym zakresem.

Źródła reguł użyte w implementacji:

- [List emisyjny nr 97/2026 — TOS1029, załączniki 1 i 2](https://www.obligacjeskarbowe.pl/media_files/c15a26ae-de6e-4b89-a137-cb16687a7b0a.pdf).
- [Karta TOS1029 i przykład terminów wcześniejszego wykupu](https://www.obligacjeskarbowe.pl/oferta-obligacji/obligacje-3-letnie-tos/tos1029/).
- [Informator obligacyjny — podatek 19%](https://www.obligacjeskarbowe.pl/informator-obligacyjny/).
- [MF — zaokrąglanie podatku do groszy w górę, art. 63 § 1a](https://www.podatki.gov.pl/media/10514/broszura-do-pit-39-za-2024-r.pdf).
- [MRPiPS — Wigilia wolna od 2025 r.](https://www.gov.pl/web/rodzina/wolna-wigilia-od-przyszlego-roku).

##### Wcześniejszy projekt modelu i dalszy rozwój

**Zakres tej aktualizacji:** zapis decyzji o odłożeniu panelu oraz researchu Finwire; propozycja modelu i rozliczeń. Nie dodajemy teraz encji, migracji, kalkulatora, biblioteki schedulerów ani zadań działających w tle.

##### Proponowany model bez dziedziczenia encji

Nie tworzyć oddzielnych encji `TosBond`, `CoiBond`, `EdoBond` wyłącznie z powodu innego oprocentowania. Jeden model zakupionej partii `Bond` oraz typ `BondType` powinny wystarczyć. Rozdzielić wspólne warunki emisji od faktu posiadania obligacji:

| Element | Odpowiedzialność i przykładowe dane |
| --- | --- |
| `BondAccount` | Rachunek rejestrowy: wspólny `Account`, dostępna gotówka, posiadane partie. Dopłata nie jest zakupem. |
| `BondIssue` | Konkretna emisja: kod serii, `BondType`, okres sprzedaży, nominał i waluta, długość oszczędzania, reguła oprocentowania, kapitalizacji, wypłat, opłat, podatku i zaokrągleń, źródło warunków oraz wersja reguł. Cena zakupu jest odrębna od nominału. |
| `Bond` | Partia na `BondAccount`: referencja do emisji/niezmiennej wersji warunków, data zakupu, cena jednostkowa, liczba kupionych i pozostałych sztuk, termin zapadalności i status rozliczenia. Oddzielne zakupy tej samej emisji nie nadpisują wcześniejszej partii. |
| `BondOperation` | Trwała historia zakupu, kapitalizacji, wypłaty odsetek, dyspozycji i realizacji wykupu. Zdarzenie rozdziela liczbę sztuk, kapitał, odsetki brutto, opłatę, podatek i faktyczny wpływ na gotówkę. |
| Okresy odsetkowe — rozszerzenie | Dla przyszłych produktów zmiennoprocentowych: granice okresów, rzeczywiście obowiązujące stopy, wskaźniki i marże. Nie nadpisywać historii jedną bieżącą stopą. Nie wymagać tego rozszerzenia do pierwszej implementacji TOS. |

Reguły rachunkowe mogą być dobierane przez `BondType` i wersję kalkulatora, bez dziedziczenia JPA. Nie projektować na zapas uniwersalnego silnika dowolnych instrumentów. Zakup zachowuje wersję warunków; aktualizacja nowej emisji nie zmienia wcześniejszych zakupów. Brak wymaganej stopy dla okresu oznacza niekompletne dane, a nie stopę zero.

Formularz zakupu w kontekście otwartego konta: emisja, liczba sztuk, data zakupu (domyślnie dzisiaj); dane warunków prezentowane do odczytu. Konto jest przypięte do kontekstu. Koszt oraz dostępność gotówki weryfikuje backend. Pierwszy zakres: zwykły opodatkowany rachunek, bez IKE/IKZE, automatycznej zamiany i reinwestycji.

##### Proponowany podział serwisów

- `BondAccountService` pozostaje serwisem rachunku: utworzenie/odczyt, własność i saldo gotówki. Obecnie saldo pochodzi wyłącznie z transferów; dodanie zakupów i wykupów musi rozszerzyć jego obliczanie o operacje obligacyjne.
- `BondService` obsługuje przypadki użycia: zakup, przedterminowy wykup, wykup w terminie oraz rozliczanie należnych operacji. Nazwy klas są propozycją; nie rozbudowywać automatycznie wszystkiego wewnątrz `BondAccountService`.
- `BondCalculator` oblicza odsetki, kapitalizację, podatki, opłaty i daty zgodnie z obsługiwanym typem oraz wersją reguł emisji. Nie przechowuje salda rachunku.
- `MoneyTransfer` nadal oznacza przepływ między dwoma rachunkami. Zakup i wykup w obrębie jednego rachunku są operacjami `BondOperation`, a nie transferem konta do siebie ani fikcyjną transakcją `INCOME`/`EXPENSE`.
- Zakup atomowo zmniejsza dostępną gotówkę i zapisuje partię. Wykup atomowo zmniejsza pozostałą liczbę sztuk i uznaje gotówkę o należny kapitał oraz odsetki po podatku/opłatach. Nie księgować tych samych kwot drugi raz w osobnej tabeli salda.
- Naliczony zysk, wypłacony dochód i zwrócony kapitał są odrębne. Do przychodów z inwestycji trafia wypłacony dochód, nie cała kwota wykupu; kapitalizacja nie powoduje dopisania gotówki.
- Konto z otwartymi partiami nie może być usunięte przez sam zwrot jego gotówki. Najpierw rozliczyć posiadane obligacje według ich reguł, zachowując historię i rozdzielenie opłat/podatku.

##### Wykup w terminie i przed terminem

Dla rozważanego TOS oprocentowanie jest stałe, kapitalizacja roczna, a wykup następuje po trzech latach od zakupu. Sam kod emisji nie określa dnia wykupu konkretnej partii. Termin kontraktowy, data dyspozycji i data rozliczenia gotówki to różne pojęcia; kalendarz dni roboczych i zaokrąglenia ustalić na podstawie listu emisyjnego przed implementacją.

Wykup w terminie ma zamknąć partię i uznać rachunek rejestrowy kapitałem oraz odsetkami netto. Przedterminowy wykup wymaga osobnej dyspozycji i reguł opłaty oraz terminów rozliczenia z konkretnej emisji. Nie kopiować zasady zerwania lokaty „zwrot kapitału i skasowanie wszystkich odsetek” do obligacji. Szczegóły podatkowe zwykłego rachunku, sposób zaokrąglenia na sztukę/partię i naliczanie odsetek muszą być jawne przed kodowaniem.

Źródła: [TOS — opis produktu, kapitalizacji i wykupu](https://www.obligacjeskarbowe.pl/oferta-obligacji/obligacje-3-letnie-tos/tos1029/), [oficjalne listy emisyjne MF](https://www.gov.pl/web/finanse/biezaca-oferta2), [rozporządzenie — tekst jednolity z 2024 r.; punkt wyjścia do weryfikacji obowiązujących zasad](https://eli.gov.pl/api/acts/DU/2024/520/text.html). Nie traktować parametrów jednej emisji jako stałych dla wszystkich roczników.

##### Scheduler — rekomendacja na MVP, do uzgodnienia przed wdrożeniem

- Na obecny lokalny single-user proponujemy Spring `@Scheduled`, bez nowej biblioteki. Jeden okresowy proces wyszukuje należne, nierozliczone operacje z terminem `<= dzisiaj` w `Europe/Warsaw`; nie zakładać osobnego joba dla każdej sztuki obligacji.
- Scheduler jest tylko wyzwalaczem `BondService`, nie miejscem reguł finansowych. Zasilenie gotówki musi wynikać z trwałego zdarzenia rozliczenia, nie z samego upływu daty w UI.
- Ten sam mechanizm nadrabia zaległości przy starcie backendu i przed udostępnieniem aktualnego salda lub wykonaniem operacji zależnej od niego. Wyłączony backend nie wykonuje zadań; po powrocie przetwarza wszystkie należne zdarzenia z prawidłowymi datami finansowymi.
- Obsłużyć restart i powtórzenia: transakcja na pojedyncze rozliczenie, blokada partii i unikalny klucz należnego zdarzenia (np. partia + typ zdarzenia + okres) oraz osobna idempotencja poleceń zakupu/wykupu. Równoległy scheduler i odczyt konta nie mogą wypłacić tych samych pieniędzy dwa razy.
- W bazie zachować stan zakończenia oraz powiązanie księgowania ze zdarzeniem. Nie uznawać samej adnotacji ani biblioteki schedulerów za gwarancję jednokrotnego rozliczenia.
- Quartz rozważyć później, gdy będą potrzebne trwałe indywidualne harmonogramy, zarządzanie triggerami lub wiele instancji. Spring Boot wspiera Quartz i magazyn JDBC; na dziś nie dodajemy zależności ani tabel Quartz. Sam Quartz również nie zastępuje idempotencji rozliczeń.

Źródła: [Spring — Task Execution and Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html), [Spring Boot — Quartz Scheduler i magazyn JDBC](https://docs.spring.io/spring-boot/reference/io/quartz.html).

##### Finwire — źródło do przyszłego importu, nie bieżąca zależność MVP

Zachowane na przyszłość, research dokumentacji 2026-10-21:

- [Dokumentacja dostawcy](https://finwire.pl/dla-developerow), [OpenAPI](https://public-api.finwire.pl/openapi.json), [repozytorium dokumentacji](https://github.com/finwire-pl/api).
- Baza: `https://public-api.finwire.pl`; endpoint `GET /v1/bonds` jest opisany jako oprocentowanie detalicznych obligacji skarbowych na podstawie danych MF.
- Dostawca deklaruje publiczne REST/JSON bez klucza i rejestracji oraz licencję CC BY 4.0. Przed integracją potwierdzić aktualne warunki, wymagane wskazanie źródła i ograniczenia.
- Potwierdzono obecność endpointu w dokumentacji/specyfikacji, nie kompletność jego odpowiedzi ani dostępność produkcyjną. Sprawdzić pokrycie konkretnych emisji, historii, marż, opłat i okresów odsetkowych; nie zakładać, że sam endpoint daje kompletny model rozliczeń.
- Import przez backend do lokalnego, wersjonowanego katalogu, z datą pozyskania i źródłem. Nie pobierać stopy z internetu przy każdym naliczeniu ani nie nadpisywać warunków istniejących partii aktualną ofertą.
- Oficjalne listy emisyjne i tabele odsetkowe pozostają źródłem weryfikacji. Integracja Finwire oraz automatyczne aktualizacje są przyszłym zakresem.

##### Panel utrzymania emisji — odłożony punkt rozwoju

- [ ] Później: panel dodawania/importu i korekty danych emisji, wskazujący oficjalne źródło oraz historię zmian. Służy utrzymaniu odwzorowania państwowych warunków, nie dowolnemu projektowaniu obligacji przez użytkownika.
- [ ] Później: akceptacja danych z Finwire i obsługa niekompletnych danych historycznych.
- Na MVP użytkownik wybiera emisję i wpisuje zakup, bez konfigurowania kapitalizacji, podatku, opłaty lub zasad oprocentowania. Obsługiwany typ i emisje dostarczamy jako dane zgodne z dokumentami źródłowymi; panel nie jest warunkiem pierwszego zakupu.

#### Źródła wycen inwestycji — research 2026-10-13

Sprawdzono dokumentację, bez klucza użytkownika i bez potwierdzenia uprawnień konkretnego planu. Integracja nie została wdrożona.

| Dostawca | Dane | Endpoint / uwagi |
| --- | --- | --- |
| Alpha Vantage | Historia OHLC indeksu S&P 500: dzienna, tygodniowa lub miesięczna | `https://www.alphavantage.co/query?function=INDEX_DATA&symbol=SPX&interval=daily&apikey=YOUR_API_KEY`; dokumentacja oznacza ten endpoint jako premium. |
| Financial Modeling Prep | Notowania indeksów, w tym S&P 500; dostępne także endpointy historii indeksów | Index Quote API: `/stable/quote?symbol=%5EGSPC&apikey=YOUR_API_KEY`; historia EOD: `/stable/historical-price-eod/full?symbol=%5EGSPC&apikey=YOUR_API_KEY`. Oficjalna dokumentacja historii identyfikuje S&P 500 symbolem `^GSPC`; dostęp do tych endpointów w wybranym planie potwierdzić przed integracją. |

Źródła: [Alpha Vantage — S&P 500 / INDEX_DATA](https://www.alphavantage.co/documentation/), [FMP — Index Quote API](https://site.financialmodelingprep.com/developer/docs/stable/index-quote), [FMP — historia indeksu](https://site.financialmodelingprep.com/developer/docs/stable/index-historical-price-eod-full).

Indeks S&P 500 jest benchmarkiem. Do rzeczywistej wyceny posiadanych jednostek ETF pobieramy cenę konkretnego funduszu: `ilość jednostek × cena jednostki`. Na przykład SPY jest osobnym ETF-em śledzącym S&P 500 i ma własną cenę rynkową; jego symbol jest tu przykładem, nie wyborem instrumentu dla użytkownika. [Oficjalny opis SPY](https://www.ssga.com/us/en/individual/etfs/state-street-spdr-sp-500-etf-trust-spy).

Dla MVP do uzgodnienia: konkretny ETF (ticker/ISIN, giełda i waluta), częstotliwość wycen (np. zamknięcie dnia), dostępność w planie API oraz wycena walutowa. Backend pobiera i zapisuje cenę ze źródłem i czasem; przy błędzie zachowuje ostatnią znaną wycenę oznaczoną datą. Klucz API pozostaje po stronie backendu. Nie mnożymy liczby jednostek ETF przez poziom indeksu i nie używamy założonego przyszłego procentu jako aktualnego zysku.

### 3.6. Wskaźniki

Backend oblicza i zwraca:

- aktywa razem,
- zobowiązania razem,
- net worth,
- gotówkę i środki bankowe,
- inwestycje,
- przychody w miesiącu,
- wydatki w miesiącu,
- miesięczny zysk z inwestycji (aktualnie mock, rzeczywiste naliczenia w Etapie 5),
- miesięczny cashflow (pole API i wynik pośredni; bez kafelka w UI),
- savings rate.

Definicje (stopa oszczędzania może być ujemna; nie obcinamy jej do zera):

```text
cashflow = przychody - wydatki
net worth = suma aktywów - suma zobowiązań
savings rate = cashflow / przychody * 100%
```

Transfery, korekty salda i zakup aktywa za środki z istniejącego konta nie mogą sztucznie zwiększać przychodów ani wydatków konsumpcyjnych. Gdy przychód wynosi zero, savings rate powinien być zwracany jako `null`, a nie jako wartość nieskończona lub mylące `0%`.

---

## 4. Decyzje domenowe potrzebne przed kodowaniem API

### 4.1. Źródło prawdy dla salda konta

Przyjęta reguła dla V1:

- konto przechowuje `openingBalance` oraz `balanceStartDate`,
- bieżące saldo jest wyliczane jako wartość początkowa plus wpływ transakcji od daty początkowej,
- utworzenie, edycja lub usunięcie transakcji jest natychmiast widoczne w wyliczonym saldzie,
- saldo nie jest duplikowane w polu `currentBalance`, więc nie wymaga synchronizacji,
- reguły wpływu typów transakcji na saldo znajdują się wyłącznie w backendzie.

Wymaga to migracji obecnego `currentBalance` do `openingBalance`. Transfery i korekty muszą otrzymać jednoznaczne reguły kierunku, aby agregacja nie opierała się na domyśle.

### 4.2. Znak kwoty

W bazie `amount` powinno być wartością dodatnią, a kierunek przepływu powinien wynikać z `TransactionType`. Ułatwia to walidację i ogranicza przypadki typu `EXPENSE` z ujemną kwotą.

Frontend może prezentować wydatek ze znakiem minus, ale nie powinien przesyłać semantyki w dwóch miejscach naraz.

### 4.3. Transfery

Aktualny model: jeden rekord `MoneyTransfer` opisuje obie strony, kwotę, walutę i datę. Po stronie zwykłego konta wpływ na saldo wynika z kierunku transferu; po stronie inwestycji obowiązują reguły produktu. `InvestmentOperation` jest powiązana z transferem, ale nie powoduje drugiego obciążenia konta. Kapitał nie uczestniczy w przychodach/wydatkach; wypłacone odsetki są odrębnym przychodem.

### 4.4. Waluty

Nie wolno sumować PLN, EUR i USD bez przeliczenia. Decyzja dla V1: aplikacja przechowuje walutę konta i transakcji, ale nie przelicza wartości pomiędzy walutami. Salda oraz podsumowania są grupowane per waluta i nie tworzą jednej mylącej sumy.

W późniejszym etapie zostanie podłączone API kursów walut. Dopiero wtedy dashboard otrzyma walutę bazową, przeliczone agregacje, źródło kursu i czas jego aktualności.

### 4.5. Użytkownik V1

Do czasu wdrożenia logowania aplikacja działa świadomie jako local single-user. Nie dodajemy tymczasowego `CurrentUserProvider`; identyfikację użytkownika uporządkujemy podczas wdrażania Spring Security. Tymczasowe endpointy kont nie filtrują danych po właścicielu i nie mogą być udostępniane poza zaufanym środowiskiem lokalnym. Szczegóły etapu security znajdują się w głównym dokumencie `personal_finance_os.md`.

---

## 5. Modularny monolit i pierwszy lekki refaktor

Obecny podział `domain.user` i `domain.finance` zaczyna mieszać różne funkcjonalności w jednym pakiecie. Przed rozbudową API należy wykonać mały refaktor do układu package-by-feature, bez tworzenia od razu rozbudowanej hierarchii warstw.

Minimalny układ po refaktorze:

```text
com.example.appbe
|-- exception
|   |-- ApiError
|   |-- BusinessException
|   |-- AccountNotFoundException
|   `-- GlobalExceptionHandler
|-- user
|   |-- User
|   `-- UserRepository
`-- finance
    |-- account
    |   |-- FinancialAccount
    |   |-- FinancialAccountType
    |   |-- FinancialAccountRepository
    |   |-- AccountService
    |   |-- AccountController
    |   `-- AccountDto
    `-- transaction
        |-- Transaction
        |-- TransactionType
        `-- TransactionRepository
```

Na tym etapie:

- implementujemy funkcjonalność tylko w `finance.account`,
- istniejące klasy transakcji jedynie przenosimy do własnego feature package, aby projekt nadal się kompilował,
- nie tworzymy jeszcze pakietów `asset`, `liability`, `investment` ani `overview`,
- `exception` zawiera wspólny kontrakt błędu i globalny `@RestControllerAdvice`,
- nie dodajemy pustych pakietów `api`, `application`, `domain` i `infrastructure` dla małego feature'a.

Jeżeli `finance.account` urośnie, można później wydzielić warstwy wewnątrz tego feature'a:

```text
finance.account
|-- api
|-- application
`-- domain
```

Granica feature'a pozostaje wtedy bez zmian. Kontroler nie powinien zwracać encji JPA ani wykonywać logiki finansowej.

### Pierwszy zakres: wyłącznie zwykłe konta

Pierwszy działający pakiet obejmuje typy:

- `BANK_ACCOUNT`,
- `SAVINGS`,
- `CASH`.

Poza pierwszym zakresem pozostają konta maklerskie, lokaty, inwestycje, aktywa, zobowiązania, transakcje i agregaty dashboardu. Wartość prezentowana na koncie jest początkowo równa `openingBalance`; dopiero kolejny feature transakcji rozszerzy obliczenie o historię operacji.

---

## 6. Proponowane API REST

### Konta

```text
GET    /api/finance/accounts
POST   /api/finance/accounts
GET    /api/finance/accounts/{accountId}
PATCH  /api/finance/accounts/{accountId}
POST   /api/finance/accounts/{accountId}/archive
POST   /api/finance/accounts/{accountId}/balance-adjustments
```

### Transakcje

```text
GET    /api/finance/transactions?month=2026-09&accountId=&type=&category=&page=
POST   /api/finance/transactions
GET    /api/finance/transactions/{transactionId}
PUT    /api/finance/transactions/{transactionId}
DELETE /api/finance/transactions/{transactionId}
POST   /api/finance/transfers
```

`PUT /api/finance/transactions/{transactionId}` przyjmuje pełny kontrakt formularza transakcji. W trybie local single-user edytowana transakcja i wybrane konto muszą należeć do użytkownika `id = 1`. Odpowiedź powinna zawierać zaktualizowaną transakcję oraz kompletny `FinanceOverviewDto`, analogicznie do odpowiedzi create, aby frontend mógł atomowo podmienić snapshot.

### Aktywa i zobowiązania

```text
GET    /api/finance/assets
POST   /api/finance/assets
PATCH  /api/finance/assets/{assetId}
POST   /api/finance/assets/{assetId}/archive

GET    /api/finance/liabilities
POST   /api/finance/liabilities
PATCH  /api/finance/liabilities/{liabilityId}
POST   /api/finance/liabilities/{liabilityId}/close
```

### Inwestycje

```text
GET    /api/finance/investments/positions
POST   /api/finance/investments/positions
PATCH  /api/finance/investments/positions/{positionId}
POST   /api/finance/instruments/{instrumentId}/prices
```

### Dashboard — overview i robocze rozszerzenia 2026-10-19

```text
GET /api/finance/overview?month=2026-10
```

Dotychczasowy parametr `month` jest opcjonalny; domyślnie backend wybiera bieżący miesiąc w Europe/Warsaw. Pola `investments`, `investmentTotals`, `transfers` i wynik kalkulatora lokaty poniżej pochodzą z roboczego szkicu backendu; ich integracja nie jest oznaczona jako ukończony etap. Nie ma wdrożonego parametru `currency` ani przeliczenia na jedną walutę. Przykładowy kształt odpowiedzi (listy kont i transakcji skrócono):

```json
{
  "accountTotals": [{ "currency": "PLN", "amount": 82000.00 }],
  "accounts": [],
  "investments": [],
  "investmentTotals": [],
  "transfers": [],
  "transactions": [],
  "investmentProfit": {
    "month": "2026-10",
    "mocked": false,
    "totals": []
  },
  "monthlySummary": {
    "month": "2026-10",
    "totals": [{
      "currency": "PLN",
      "income": 9200.00,
      "expenses": 3240.82,
      "cashflow": 5959.18,
      "savingsRate": 64.77
    }]
  }
}
```

`investmentProfit` zastąpił dzienne `dailyResult` i dotyczy tego samego miesiąca co `monthlySummary`. Roboczy backend zawiera kalkulator lokaty zamiast mocka, ale nie oznacza to zatwierdzonego lub uruchomionego przepływu. Puste `totals` oznacza brak utworzonych inwestycji. Kod/migracja nie zostały jeszcze uruchomione. Brak rzeczywistej wyceny nie oznacza wyniku zero.

`POST`, `PUT` i `DELETE` transakcji przyjmują opcjonalny parametr `month` i zwracają odświeżone `overview` dla wybranego miesiąca w swoich odpowiedziach. Historia i salda pozostają pełne. Jeden endpoint agregujący ogranicza liczbę requestów na dashboardzie mobilnym. Szczegółowe ekrany korzystają z osobnych endpointów zasobów.

Aktywa, zobowiązania, pełny net worth oraz alokacja portfela są docelowymi rozszerzeniami, nie polami obecnego kontraktu.

---

## 7. Plan prac w kolejności

## Etap 0 — lekki refaktor package-by-feature — ukończony

1. [x] Przenieść `User` i `UserRepository` z `domain.user` do `user`.
2. [x] Przenieść klasy kont z `domain.finance` do `finance.account`.
3. [x] Przenieść klasy transakcji z `domain.finance` do `finance.transaction` bez rozwijania ich funkcjonalności.
4. [x] Poprawić importy i potwierdzić automatyczne skanowanie spod `com.example.appbe`.
5. [x] Nie zmieniać zachowania aplikacji w ramach samego przenoszenia pakietów.

Kryterium ukończenia: backend kompiluje się i uruchamia, migracja V1 nadal przechodzi, a klasy są pogrupowane według feature'ów.

## Etap 1 — najmniejszy vertical slice: zwykłe konta

1. Ujednolicić konfigurację wokół wybranego MySQL i jednego pliku Docker Compose.
2. Dodać profil `local` i testową konfigurację bazy.
3. [x] Zmienić model z `currentBalance` na `openingBalance` i `balanceStartDate` w nowej migracji Flyway.
4. [x] Dodać `AccountService`, `AccountDto`, read API oraz spójną obsługę wyjątków biznesowych.
5. **W toku:** lista, szczegóły, tworzenie i edycja są gotowe; pozostała archiwizacja kont.
6. **W toku:** backend ogranicza create/update do `BANK_ACCOUNT`, `SAVINGS` i `CASH`; pozostało zastosować to ograniczenie w formularzu.
7. Dodać testy serwisu i API kont.
8. [x] W Angularze dodać osobny `AccountApiService` i modele kontraktu.
9. **W toku:** lista i szczegóły korzystają z rzeczywistego API; pozostał formularz konta.
10. [x] Dodać stany loading, empty i error dla listy oraz szczegółów.
11. [x] Nie mieszać prawdziwych kont z mockowanymi metrykami — sekcje zależne od transakcji są wyraźnie oznaczone jako niedostępne.

Kryterium ukończenia: użytkownik może z UI utworzyć, odczytać, edytować i zarchiwizować zwykłe konto, a odświeżenie strony zachowuje dane w MySQL.

## Etap 2 — transakcje i rzeczywiste saldo konta

1. [x] Dodać `TransactionService`, DTO oraz endpoint tworzenia transakcji.
2. **W toku:** wspólna lista i lista konta mają rozsuwane filtry lokalne (miesiąc, typ oraz konto na dashboardzie). Paginacja, filtr kategorii i filtrowanie API pozostają do zrobienia.
3. [x] Dodać formularz transakcji przy użyciu Angular Reactive Forms i PrimeNG.
4. **Edycja zaimplementowana (2026-10-13):** `PUT`, wspólny formularz tworzenia/edycji i ikony na obu listach; ręczna weryfikacja zapisu pozostaje do wykonania. Usuwanie z potwierdzeniem zostało zaimplementowane 2026-10-13; zapis i usuwanie należy ręcznie sprawdzić z aktualnym backendem.
5. [x] Wyliczać saldo jako `openingBalance` plus wpływ transakcji.
6. Potwierdzić testami, że edycja i usunięcie transakcji natychmiast zmienia saldo.
7. Dodać korektę salda jako jawny przypadek użycia.
8. **Roboczy szkic 2026-10-19:** wspólne transfery; domknąć jako krok 2 planu backendu, przed serwisem lokaty.

Kryterium ukończenia: saldo konta oraz historia pozostają spójne po create/edit/delete/transfer, bez pola `currentBalance` jako drugiego źródła prawdy.

## Etap 3 — overview i podsumowanie dashboardu — wdrożone 2026-10-16/19

1. [x] Konta, sumy per waluta i pełna historia w jednym snapshotcie `FinanceOverviewService`.
2. [x] `GET /api/finance/overview?month=YYYY-MM` oraz opcjonalny miesiąc przy mutacjach transakcji.
3. [x] Rzeczywiste przychody i wydatki z `INCOME`/`EXPENSE`; transfery i korekty są wyłączone z agregacji.
4. [x] Cashflow oraz stopa oszczędzania liczone przez backend; `null` bez przychodów, procent zaokrąglany do dwóch miejsc, możliwe ujemne wartości.
5. [x] `FinanceStore` aktualizuje snapshot po mutacjach i chroni go przed spóźnionymi odczytami.
6. [x] Kompaktowy dashboard z miesięcznym wyborem niezależnym od filtrów historii. Cashflow usunięte z widoku; miesięczny zysk inwestycyjny nadal mockowany.
7. [ ] Ręczna weryfikacja z aktualnym Spring/MySQL i Android WebView. Build i sprawdzenia wykonane 2026-10-16 nie potwierdzają późniejszych zmian UI/kontraktu.

Kryterium tego pakietu: rzeczywiste podsumowanie transakcji i aktualizacja snapshotu. Usunięcie ostatniego mocka inwestycyjnego należy do Etapu 5, a nie do ponownej implementacji miesięcznych przychodów i wydatków.


## Etap 4 — aktywa i zobowiązania — odłożony poza obecne MVP

1. Dodać migracje oraz encje `Asset` i `Liability`.
2. Dodać serwisy, walidację, API i testy.
3. Dodać mobilne listy oraz formularze tworzenia i edycji.
4. Rozszerzyć overview o aktywa, zobowiązania i net worth.
5. Zapobiec podwójnemu liczeniu zakupu aktywa: transfer wartości z konta do aktywa nie jest wydatkiem konsumpcyjnym.

Kryterium ukończenia: net worth wynika z kont, aktywów i zobowiązań, a każdą składową można otworzyć i zweryfikować.

## Etap 5 — inwestycje, obligacje i lokaty — aktualizacja 2026-10-21

**Stan źródeł:** profile `DepositAccount`, `BondAccount` i `BrokerageAccount` zastąpiły wspólne `InvestmentAccount` (V8). V9 dodaje emisję TOS1029, partie i historię obligacji. Są formularze zakupu i częściowego wykupu, kalkulator, automatyczne rozliczenia i salda uwzględniające gotówkę oraz obligacje. Nie potwierdzamy tu wykonania migracji, buildów ani działania aplikacji.

1. Utrzymać wspólny `Account` z osobnymi profilami i wspólny `MoneyTransfer`. Tworzenie pustego konta i jego zasilenie są odrębne od zakupu papierów wartościowych.
2. Zachować reguły lokaty: jedna wpłata, naliczenia i historia; przy usuwaniu zwrot samego kapitału, bez niewypłaconych odsetek. Nie przenosić tych reguł na obligacje.
3. W źródłach: wspólny model partii `Bond`, emisja `BondIssue` i `BondOperation`, bez dziedziczenia encji per typ; pierwszy produkt TOS1029 4,40% według listu emisyjnego.
4. W źródłach: zakup z dostępnej gotówki konta i trwała historia. Formularz nie wymaga konfiguracji warunków emisji przez użytkownika.
5. W źródłach: naliczenia, podatek, opłata, częściowy i terminowy wykup do gotówki rachunku rejestrowego, z idempotencją i oddzieleniem kapitału od dochodu.
6. W źródłach: Spring `@Scheduled` oraz nadrabianie po starcie/na odczycie. Quartz odłożony.
7. W źródłach: overview obligacji i ich wynik, osobno gotówka, wartość papierów, naliczony zysk i wypłacony dochód.
8. Panel utrzymania emisji, import Finwire, kolejne typy obligacji i zakupy ETF pozostają dalszym rozwojem.

Szczegóły i źródła: [Obligacje detaliczne: model, rozliczenia i źródła danych](#obligacje-detaliczne-model-rozliczenia-i-źródła-danych--2026-10-21).

Zakaz uruchamiania testów, przeglądarki, buildów, Mavena i poleceń Capacitor przez agenta pozostaje aktualny. Wymagania jakościowe dokumentu są backlogiem przyszłej weryfikacji, a nie stwierdzeniem jej wykonania.

Po podstawowych inwestycjach: cele finansowe oraz proste projekcje/scenariusze w osobnych planach.

## Etap 6 — przekrojowa historia majątku i wiarygodność — późniejszy zakres

Historia pojedynczych inwestycji powstaje już w Etapie 5. Ten etap dotyczy szerszej historii całego majątku, po rozszerzeniu zakresu o net worth.

1. Dodać dzienne lub miesięczne snapshoty wartości majątku.
2. Udostępnić historię net worth.
3. Dodać zmianę miesiąc do miesiąca.
4. Dodać podział wydatków według kategorii.
5. Grupować wielowalutowe podsumowania bez ich przeliczania.
6. Pokazać datę aktualności ręcznych wycen aktywów, inwestycji i zobowiązań.

Kryterium ukończenia: dashboard potrafi pokazać nie tylko stan dzisiejszy, ale także wiarygodną zmianę w czasie i pochodzenie każdej głównej liczby.

---

## 8. Plan frontendu mobile-first

### Dashboard

Istniejący dashboard zachowuje kompaktowe podsumowanie miesiąca: „Zysk z inwestycji”, przychody, wydatki i stopę oszczędzania. Bez osobnej dużej karty dziennego balansu i bez kafelka cashflow. Miesiąc podsumowania jest niezależny od filtrów historii. Obliczenia i agregaty per waluta należą do backendu.

**Inwestycyjny frontend jest do uzgodnienia.** Potwierdzony kierunek: zwykłe i inwestycyjne rachunki na jednej liście, kliknięcie otwiera komponent właściwy dla typu rachunku. Do dogadania pozostają formularz tworzenia lokaty (kwota i źródło pieniędzy wymagane od razu), obsługa transferów, szczegóły warunków produktu, podatku, operacji i historii. Nie traktować pomysłów na modale, układ wskaźników lub wykres jako zatwierdzonego projektu UI.

Robocze komponenty lokaty i transferów zostały przywrócone na wyraźną prośbę użytkownika jako prototyp do ręcznego poklikania. Są podłączone do backendu; zapis nie jest atrapą. Finalny UX wymaga oceny. Wymagania mobile-first i PrimeNG pozostają aktualne; layout nie był automatycznie sprawdzany.

### Ekrany do utworzenia lub domknięcia

- dashboard finansowy,
- lista i szczegóły konta,
- formularz konta,
- lista transakcji z filtrami,
- formularz transakcji,
- formularz transferu,
- lista aktywów i formularz aktywa,
- lista zobowiązań i formularz zobowiązania,
- portfel inwestycyjny i ręczna aktualizacja ceny.

### Zasady interfejsu

- używać PrimeNG dla przycisków, formularzy, dialogów/drawerów, tabel/list, komunikatów i tagów,
- aktywne kontrolki mają co najmniej 44 × 44 px,
- główne akcje nie mogą być dostępne tylko przez hover,
- formularze na telefonie powinny być jednokolumnowe,
- dłuższe operacje muszą mieć czytelny stan oczekiwania,
- błędy walidacji mają być widoczne przy polach i zrozumiałe bez znajomości backendu,
- testować layout co najmniej dla 320 px, 480 px i desktopu,
- nie uzależniać podstawowej nawigacji od połączenia z siecią; cache i zapis offline w aplikacji Capacitor pozostają osobnym etapem.

---

## 9. Testy wymagane dla punktu 2.1 — plan przyszłej weryfikacji

Poniższa lista jest docelowym backlogiem jakościowym. Ostatnie polecenie użytkownika zabrania uruchamiania testów, Playwrighta i buildu przez agenta; ta aktualizacja dokumentacji go nie uchyla.

### Backend — testy jednostkowe

- przychód zwiększa saldo właściwego konta,
- wydatek zmniejsza saldo,
- edycja transakcji odwraca poprzedni wpływ i nakłada nowy,
- usunięcie transakcji odwraca jej wpływ,
- transfer zmienia oba salda, ale nie cashflow,
- operacja jest wycofywana w całości, jeżeli druga część transferu się nie powiedzie,
- nie można zmienić danych należących do innego użytkownika,
- net worth odejmuje zobowiązania,
- savings rate ma poprawne zachowanie dla zerowego przychodu,
- wartości pieniężne zachowują ustaloną precyzję i zasady zaokrąglania.

### Backend — testy integracyjne

- migracje uruchamiają się na pustej bazie,
- endpointy zwracają DTO, właściwe statusy HTTP i spójny format błędu,
- filtry oraz paginacja transakcji działają łącznie,
- operacje zapisu są transakcyjne,
- overview zgadza się z danymi źródłowymi.

Preferowane są Testcontainers z tym samym silnikiem bazy, który działa lokalnie i docelowo.

### Frontend

- serwis poprawnie mapuje kontrakt API,
- dashboard obsługuje success/loading/empty/error,
- formularze blokują niepoprawne kwoty i brak wymaganych pól,
- po zapisie widoki pokazują aktualne dane,
- podstawowe przepływy działają klawiaturą,
- layout nie ma poziomego przewijania przy 320 px i 480 px.

### Minimalny test end-to-end

```text
utwórz konto
  -> dodaj przychód
  -> dodaj wydatek
  -> wykonaj transfer
  -> dodaj aktywo i zobowiązanie
  -> otwórz dashboard
  -> zweryfikuj salda, cashflow, savings rate i net worth
```

---

## 10. Backlog w formie pierwszych ticketów

1. `FIN-001` — **ukończone** — refaktor `user`, `finance.account` i `finance.transaction` do package-by-feature.
2. `FIN-002` — ujednolicenie konfiguracji MySQL i profile środowiskowe.
3. `FIN-003` — **ukończone** — migracja `currentBalance` do `openingBalance` i `balanceStartDate`.
4. `FIN-004` — **ukończone** — wspólny format błędów API, błędów biznesowych i walidacji `@Valid`.
5. `FIN-005` — **w toku** — read, create i update API są gotowe; pozostała archiwizacja konta.
6. `FIN-006` — **w toku** — `AccountApiService`, lista i szczegóły są podłączone; pozostał formularz create/update.
7. `FIN-007` — **ukończone dla odczytu kont** — stany loading/empty/error oraz ręczna weryfikacja przy 320 px, 480 px i 1280 px.
8. `FIN-008` — TransactionService i read API.
9. `FIN-009` — **w toku** — create, edit, delete z potwierdzeniem oraz rozsuwane filtry lokalne są zaimplementowane; pozostają filtr kategorii, paginacja i filtrowanie API. Backend mutacji nadal wymaga ręcznej weryfikacji.
10. `FIN-010` — reguły wyliczania salda i korekty.
11. `FIN-011` — **szkic do przeglądu** — MoneyTransfer, krok 2 planu backendu; bez zatwierdzonego UI.
12. `FIN-012` — **wdrożone** — overview, miesięczne agregacje per waluta i odświeżony snapshot po mutacjach; integracja do ręcznej weryfikacji.
13. `FIN-013` — **częściowo wdrożone** — podsumowanie rzeczywistych transakcji działa; ostatni mock `investmentProfit` zostanie zastąpiony w pakiecie inwestycji.
14. `FIN-014` — aktywa: model, API i UI.
15. `FIN-015` — zobowiązania: model, API i UI.
16. `FIN-016` — net worth i rozszerzenie dashboardu.
17. `FIN-017` — instrumenty, pozycje, operacje i trwała historia cen/wycen; wspólna lista kont i inwestycji.
18. `FIN-018` — **plan i roboczy szkic** — serwis pierwszej lokaty po domknięciu encji oraz transferów; potem obligacje i ETF.
19. `FIN-019` — snapshoty i historia net worth.
20. `FIN-020` — integracja z API kursów walut i agregacje w walucie bazowej — etap późniejszy.
21. `FIN-021` — **zaimplementowane w źródłach, bez uruchomienia** — emisja `BondIssue`, partia `Bond` bez dziedziczenia encji i zakup za gotówkę konta; TOS1029 4,40%, migracja V9 i formularz.
22. `FIN-022` — **zaimplementowane w źródłach, bez uruchomienia** — naliczenia, częściowy i terminowy wykup, podatki i idempotentne rozliczenia; scheduler Spring oraz nadrabianie zaległości.
23. `FIN-023` — **odłożone** — panel utrzymania katalogu emisji; odwzorowanie warunków państwowych ze źródłem i wersjonowaniem.
24. `FIN-024` — **odłożone** — rozpoznanie i import `GET https://public-api.finwire.pl/v1/bonds`; sprawdzenie pełności danych i aktualnych warunków API.


Pierwszy milestone obejmuje wyłącznie `FIN-001`–`FIN-007`. Jego efektem jest mały, kompletny moduł zwykłych kont działający od MySQL przez Spring Boot do mobilnego UI. Transakcje i dashboard są osobnym kolejnym milestone'em.

---

## 11. Definition of Done całego punktu 2.1 — docelowy zakres, szerszy niż obecne MVP

Punkt 2.1 można uznać za ukończony, gdy:

- dashboard nie korzysta z mockowanych danych,
- użytkownik zarządza kontami i pojedynczymi transakcjami z UI,
- transfery nie zniekształcają cashflow,
- aktywa, inwestycje i zobowiązania są uwzględnione w net worth,
- lokaty i obligacje mają jawny, spójny model,
- dashboard pokazuje przychody, wydatki, zysk z inwestycji i stopę oszczędzania dla wybranego miesiąca; cashflow pozostaje obliczeniem backendu bez osobnego kafelka,
- wartości w różnych walutach nie są bezpośrednio sumowane,
- każda główna liczba jest liczona przez backend i możliwa do wyjaśnienia danymi źródłowymi,
- dostępna jest co najmniej miesięczna historia net worth,
- kluczowe reguły finansowe posiadają testy,
- UI jest używalne przy 320 px, 480 px i na desktopie,
- błędy, puste stany i oczekiwanie na dane są obsłużone,
- aplikacja uruchamia się lokalnie z udokumentowaną konfiguracją bazy.

---

## 12. Poza zakresem tego planu

- cele finansowe,
- Projection Engine,
- Scenario Engine i Decision Lab,
- agent AI, chat i voice,
- Google Calendar,
- integracje bankowe i automatyczny import,
- automatyczne notowania rynkowe,
- harmonogramy kredytów i zaawansowane naliczanie odsetek,
- customizowalny dashboard,
- pełne logowanie i zarządzanie użytkownikami,
- kompletna praca offline i synchronizacja konfliktów.

Te funkcje powinny korzystać z ustabilizowanego Finance Core, a nie powstawać równolegle z jego podstawowymi regułami.
