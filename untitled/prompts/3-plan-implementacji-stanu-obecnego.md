# Plan implementacji punktu 2.1 — stan obecny finansów

## 1. Cel dokumentu

Ten dokument konkretyzuje implementację punktu 2.1 z `personal_finance_os.md`:

> **Stan obecny — „gdzie jestem?”**

Po zakończeniu tego etapu aplikacja ma odpowiadać na cztery podstawowe pytania:

1. Ile wynosi mój majątek netto?
2. Gdzie znajdują się moje pieniądze i pozostałe składniki majątku?
3. Jakie mam zobowiązania?
4. Jak wygląda mój cashflow i savings rate w wybranym miesiącu?

Plan celowo nie obejmuje jeszcze celów finansowych, projekcji, scenariuszy, AI, integracji bankowych ani automatycznych cen rynkowych.

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
- [x] pojedynczy handler obsługuje wszystkie wyjątki biznesowe na podstawie ich statusu i kodu.

Aktualny krok:

- [ ] przygotowanie zapisu zwykłych kont: create, update i archive.

### Checkpoint — koniec sesji 2026-09-30

Backend posiada działający szkielet feature'a kont:

```text
GET /api/finance/accounts
GET /api/finance/accounts/{accountId}
```

Odczyt działa przez `AccountController` → `AccountService` → `FinancialAccountRepository`. Publiczny kontrakt stanowi `AccountDto`; encja JPA nie jest zwracana przez API. Wyjątki biznesowe dziedziczą po `BusinessException` i są mapowane przez jeden `GlobalExceptionHandler` do `ApiError` zawierającego status HTTP, kod błędu, komunikat i ścieżkę.

Endpointy pozostają tymczasowo nieuwierzytelnione i zwracają aktywne konta bez filtrowania właściciela. Jest to świadomie zaakceptowane wyłącznie dla lokalnego trybu single-user. Następna sesja powinna rozpocząć się od `AccountRequest` oraz endpointu tworzenia konta, a następnie osobno dodać update i archive.

---

## 2. Stan repozytorium na początku prac

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

Transakcja pozostaje centralną jednostką systemu, ponieważ w przyszłości ten sam przypadek użycia będzie wywoływany przez chat i voice.

### 3.3. Aktywa rzeczowe i pozostałe

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

### 3.4. Zobowiązania

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

Nie należy przechowywać wyłącznie jednej ręcznie wpisanej wartości typu „ETF = 100 000 PLN”. Docelowy minimalny model powinien rozróżniać:

- `Instrument` — opis instrumentu,
- `InvestmentPosition` — liczba jednostek i średnia cena zakupu,
- `InstrumentPrice` — cena na konkretny moment,
- `FinancialAccount` typu `BROKERAGE` — rachunek, na którym znajduje się pozycja.

Wyceny w pierwszej wersji są ręczne. Aktualna wartość pozycji wynika z liczby jednostek i ostatniej ceny.

Obligacje i lokaty można wdrożyć po podstawowych kontach i transakcjach. Przed implementacją trzeba zdecydować, czy będą specjalizowanymi instrumentami, czy prostszymi produktami terminowymi. Nie należy blokować pierwszego działającego vertical slice'a tą decyzją.

### 3.6. Wskaźniki

Backend oblicza i zwraca:

- aktywa razem,
- zobowiązania razem,
- net worth,
- gotówkę i środki bankowe,
- inwestycje,
- przychody w miesiącu,
- wydatki w miesiącu,
- miesięczny cashflow,
- savings rate.

Definicje V1:

```text
cashflow = przychody - wydatki
net worth = suma aktywów - suma zobowiązań
savings rate = max(cashflow, 0) / przychody * 100%
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

Rekomendacja: osobny przypadek użycia `transferMoney`, który atomowo tworzy dwie powiązane operacje — obciążenie konta źródłowego i uznanie docelowego. Obie strony transferu posiadają wspólny `transferId` i nie uczestniczą w cashflow.

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

### Dashboard

```text
GET /api/finance/overview?month=2026-09&currency=PLN
```

Przykładowa odpowiedź dashboardu:

```json
{
  "asOfDate": "2026-09-30",
  "currency": "PLN",
  "assetsTotal": 303500.00,
  "liabilitiesTotal": 66100.00,
  "netWorth": 237400.00,
  "cash": 82000.00,
  "investments": 136500.00,
  "monthlyIncome": 9200.00,
  "monthlyExpenses": 3240.82,
  "monthlyCashflow": 5959.18,
  "savingsRate": 64.77,
  "accounts": [],
  "assetAllocation": [],
  "recentTransactions": []
}
```

Jeden endpoint agregujący ogranicza liczbę requestów na dashboardzie mobilnym. Szczegółowe ekrany nadal korzystają z osobnych endpointów zasobów.

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
5. **W toku:** lista i szczegóły są gotowe; pozostało tworzenie, edycja i archiwizacja kont.
6. Ograniczyć pierwszy formularz do `BANK_ACCOUNT`, `SAVINGS` i `CASH`.
7. Dodać testy serwisu i API kont.
8. W Angularze dodać osobny `AccountApiService` i modele kontraktu.
9. Podłączyć listę, szczegóły oraz formularz kont do rzeczywistego API.
10. Dodać stany loading, empty i error.
11. Nie mieszać prawdziwych kont z mockowanymi metrykami — sekcje zależne od transakcji pozostawić wyłączone lub wyraźnie oznaczone jako niedostępne.

Kryterium ukończenia: użytkownik może z UI utworzyć, odczytać, edytować i zarchiwizować zwykłe konto, a odświeżenie strony zachowuje dane w MySQL.

## Etap 2 — transakcje i rzeczywiste saldo konta

1. Dodać `TransactionService`, DTO i endpointy transakcji.
2. Dodać listę transakcji z paginacją i filtrami.
3. Dodać formularz transakcji przy użyciu Angular Reactive Forms i PrimeNG.
4. Dodać edycję oraz usuwanie transakcji.
5. Wyliczać saldo jako `openingBalance` plus wpływ transakcji.
6. Potwierdzić testami, że edycja i usunięcie transakcji natychmiast zmienia saldo.
7. Dodać korektę salda jako jawny przypadek użycia.
8. Dodać transfer pomiędzy kontami.

Kryterium ukończenia: saldo konta oraz historia pozostają spójne po create/edit/delete/transfer, bez pola `currentBalance` jako drugiego źródła prawdy.

## Etap 3 — overview i pełne podłączenie dashboardu

1. Dodać `FinanceOverviewService` liczący miesięczne agregacje.
2. Dodać `GET /api/finance/overview`.
3. Zastąpić pozostałe dane z `FinanceDataService` wywołaniami HTTP.
4. Podłączyć rzeczywiste przychody, wydatki, cashflow i ostatnie transakcje.
5. Grupować podsumowania według waluty bez ich przeliczania.

Kryterium ukończenia: dashboard nie zawiera produkcyjnych mocków, a wszystkie widoczne liczby pochodzą z backendu.

## Etap 4 — aktywa i zobowiązania

1. Dodać migracje oraz encje `Asset` i `Liability`.
2. Dodać serwisy, walidację, API i testy.
3. Dodać mobilne listy oraz formularze tworzenia i edycji.
4. Rozszerzyć overview o aktywa, zobowiązania i net worth.
5. Zapobiec podwójnemu liczeniu zakupu aktywa: transfer wartości z konta do aktywa nie jest wydatkiem konsumpcyjnym.

Kryterium ukończenia: net worth wynika z kont, aktywów i zobowiązań, a każdą składową można otworzyć i zweryfikować.

## Etap 5 — inwestycje, obligacje i lokaty

1. Dodać `Instrument`, `InvestmentPosition` i `InstrumentPrice`.
2. Dodać ręczne tworzenie instrumentu, pozycji i aktualizacji ceny.
3. Obliczać wartość pozycji po stronie backendu.
4. Pokazać portfel i jego udział w alokacji aktywów.
5. Wybrać oraz wdrożyć model obligacji i lokat.
6. Dodać testy precyzji obliczeń i zachowania przy braku ceny.

Kryterium ukończenia: użytkownik widzi bieżącą wartość pozycji oraz łączną wartość inwestycji bez ręcznego duplikowania sum na dashboardzie.

## Etap 6 — historia i wiarygodność wskaźników

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

Na szerokości 320–480 px kolejność treści powinna być następująca:

1. net worth,
2. aktywa i zobowiązania,
3. cashflow oraz savings rate,
4. szybka akcja „Dodaj transakcję”,
5. konta,
6. alokacja majątku,
7. ostatnie transakcje.

Na desktopie sekcje mogą układać się w kolumny, ale kolejność semantyczna w HTML powinna pozostać logiczna.

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
- nie uzależniać podstawowej nawigacji od połączenia z siecią; PWA/offline write queue pozostaje osobnym etapem.

---

## 9. Testy wymagane dla punktu 2.1

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
4. `FIN-004` — **ukończone** — wspólny format błędów API i globalny handler.
5. `FIN-005` — **w toku** — AccountService, DTO i CRUD API zwykłych kont; read API jest gotowe.
6. `FIN-006` — Angular AccountApiService, lista, szczegóły i formularz konta.
7. `FIN-007` — stany loading/empty/error i testy responsywności kont.
8. `FIN-008` — TransactionService i read API.
9. `FIN-009` — CRUD transakcji, paginacja i filtry.
10. `FIN-010` — reguły wyliczania salda i korekty.
11. `FIN-011` — transfer pomiędzy kontami.
12. `FIN-012` — FinanceOverviewService i endpoint dashboardu.
13. `FIN-013` — pełne podłączenie dashboardu i usunięcie mocków.
14. `FIN-014` — aktywa: model, API i UI.
15. `FIN-015` — zobowiązania: model, API i UI.
16. `FIN-016` — net worth i rozszerzenie dashboardu.
17. `FIN-017` — instrumenty, pozycje i ręczne ceny.
18. `FIN-018` — obligacje i lokaty.
19. `FIN-019` — snapshoty i historia net worth.
20. `FIN-020` — integracja z API kursów walut i agregacje w walucie bazowej — etap późniejszy.

Pierwszy milestone obejmuje wyłącznie `FIN-001`–`FIN-007`. Jego efektem jest mały, kompletny moduł zwykłych kont działający od MySQL przez Spring Boot do mobilnego UI. Transakcje i dashboard są osobnym kolejnym milestone'em.

---

## 11. Definition of Done całego punktu 2.1

Punkt 2.1 można uznać za ukończony, gdy:

- dashboard nie korzysta z mockowanych danych,
- użytkownik zarządza kontami i pojedynczymi transakcjami z UI,
- transfery nie zniekształcają cashflow,
- aktywa, inwestycje i zobowiązania są uwzględnione w net worth,
- lokaty i obligacje mają jawny, spójny model,
- dashboard pokazuje cashflow oraz savings rate dla wybranego miesiąca,
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
