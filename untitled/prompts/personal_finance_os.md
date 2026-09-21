# Personal Finance OS

## Koncepcja osobistego asystenta finansowo-organizacyjnego

**Status:** koncepcja + plan implementacyjny  
**Cel:** aplikacja przede wszystkim do użytku własnego, z możliwością późniejszego rozwinięcia w produkt  
**Preferowany stack:** Angular + Spring Boot + PostgreSQL + Docker Compose, później Spring AI / LLM, Google Calendar, voice i ewentualnie n8n

---

## 1. Wizja produktu

Aplikacja ma być prywatnym **Personal Finance OS** - jednym miejscem, które łączy aktualny stan finansów, historię transakcji, portfel inwestycyjny, cele finansowe i symulacje decyzji.

Docelowo nad tym systemem działa agent AI, z którym można rozmawiać tekstowo i głosowo. Agent nie jest źródłem prawdy ani kalkulatorem finansowym. Jego rolą jest:

- rozumienie intencji użytkownika,
- pobieranie aktualnego kontekstu z systemu,
- wywoływanie narzędzi backendowych,
- uruchamianie deterministycznych symulacji,
- interpretacja wyników,
- wykonywanie zaakceptowanych akcji, np. dodania transakcji lub wydarzenia do kalendarza.

Najważniejsza idea produktu:

> **System ma rozumieć aktualną sytuację finansową użytkownika i pokazywać konsekwencje decyzji zanim zostaną podjęte.**

---

## 2. Główne filary systemu

### 2.1. Stan obecny - "gdzie jestem?"

System powinien przechowywać i prezentować:

- konta finansowe,
- gotówkę,
- inwestycje,
- obligacje i lokaty,
- samochody i inne aktywa,
- zobowiązania,
- pojedyncze transakcje,
- miesięczny cashflow,
- net worth,
- savings rate.

### 2.2. Cele - "dokąd zmierzam?"

Przykładowe cele:

- uzbieranie kapitału na mieszkanie lub dom,
- konkretny poziom majątku,
- zakup samochodu,
- zbudowanie poduszki finansowej,
- zgromadzenie określonej kwoty inwestycji.

Każdy cel powinien mieć m.in.:

- nazwę,
- kwotę docelową,
- termin opcjonalny,
- priorytet,
- aktualny postęp,
- miesięczną kontrybucję,
- projekcję daty osiągnięcia.

### 2.3. Symulacje - "co jeśli?"

System powinien pozwalać porównywać rzeczywistość bazową z hipotetycznym scenariuszem.

Przykłady:

- zakup auta za 100-200 tys. PLN,
- sprzedaż obecnego auta,
- droższe utrzymanie samochodu,
- wzrost lub spadek miesięcznych kosztów,
- zmiana pracy i dochodu,
- kredyt hipoteczny,
- zakup mieszkania,
- dodatkowe inwestowanie,
- jednorazowy duży wydatek,
- zmiana oczekiwanej stopy zwrotu.

### 2.4. Agent - "porozmawiaj z moimi danymi"

Docelowo agent powinien umieć odpowiadać m.in. na pytania:

- "Ile mam teraz pieniędzy i gdzie?"
- "Na co wydałem najwięcej w tym miesiącu?"
- "Jak zakup auta za 150 tys. wpłynie na mój cel mieszkaniowy?"
- "Porównaj zakup BMW za 150 tys. z Porsche za 250 tys."
- "Dodaj 180 zł za paliwo."
- "Co mam jutro w kalendarzu?"
- "Dodaj mi wymianę oleju na środę o 17."

---

## 3. Zasada architektoniczna: AI nie liczy finansów

Jedna z najważniejszych decyzji projektowych:

> **Wszystkie istotne obliczenia finansowe wykonuje backend. LLM tylko wybiera narzędzia i interpretuje wyniki.**

Przykład:

```text
Użytkownik:
"Czy stać mnie na auto za 180k?"

Agent:
1. getFinancialOverview()
2. getGoals()
3. simulateScenario(...)
4. interpretuje wynik
```

Backend może zwrócić:

```json
{
  "baselineGoalDate": "2031-08",
  "scenarioGoalDate": "2033-02",
  "delayMonths": 18,
  "liquidityAfterPurchase": 70000,
  "estimatedFiveYearCost": 218000
}
```

AI może następnie powiedzieć, co wynik oznacza, ale nie powinno samodzielnie tworzyć tych liczb.

Korzyści:

- powtarzalność,
- testowalność,
- możliwość audytu,
- mniej halucynacji,
- łatwe porównywanie scenariuszy,
- niezależność od wybranego modelu LLM.

---

## 4. Zakres V1 - bez AI

V1 powinno działać całkowicie bez LLM, Google Calendar i integracji bankowych.

### Cel V1

> **Jedno miejsce do obserwowania całego majątku, pojedynczych transakcji i celów finansowych oraz prostego analizowania wpływu dużych decyzji.**

### Funkcjonalności V1

#### Finanse

- konta,
- aktywa,
- zobowiązania,
- pojedyncze transakcje,
- przychody i koszty,
- kategorie transakcji,
- net worth,
- cashflow,
- savings rate.

#### Inwestycje

- instrument,
- liczba jednostek,
- średnia cena zakupu,
- waluta,
- aktualna cena,
- aktualna wartość pozycji,
- oczekiwana średnia stopa zwrotu do projekcji.

W pierwszej wersji ceny mogą być aktualizowane ręcznie. Później źródło danych można podmienić na Market Data API.

#### Cele finansowe

- nazwa,
- target amount,
- target date,
- priorytet,
- aktualny postęp,
- szacowana data osiągnięcia.

#### Dashboard

Pierwszy dashboard może być sztywny zamiast customizowalny.

Powinien pokazywać przede wszystkim:

- net worth,
- zmianę miesiąc do miesiąca,
- wartość gotówki,
- wartość inwestycji,
- zobowiązania,
- cashflow miesięczny,
- savings rate,
- strukturę majątku,
- postęp celów.

Customizacja dashboardu powinna zostać odłożona na później.

---

## 5. Pojedyncze transakcje jako centralny element modelu

Ponieważ docelowo transakcje mają być dodawane także głosowo, pojedyncza transakcja powinna być podstawową jednostką systemu.

Przykładowy model:

```text
Transaction

id
accountId
date
amount
currency
type
category
description
source
```

Przykład:

```text
-142.37 PLN
2026-08-10
FOOD
"Lidl"
MANUAL
```

W przyszłości agent będzie korzystać z dokładnie tego samego API:

```text
"Zapłaciłem 180 zł za paliwo."
        v
createTransaction(...)
```

Dzięki temu voice i chat są tylko nowymi interfejsami do istniejącego modelu domenowego.

---

## 6. Model inwestycji

Nie warto przechowywać wyłącznie wartości typu:

```text
ETF = 100 000 PLN
```

Lepszy model:

```text
InvestmentPosition

instrumentId
quantity
averagePurchasePrice
currency
```

oraz osobno:

```text
InstrumentPrice

instrumentId
timestamp
price
currency
source
```

Pozwala to rozdzielić:

- bieżącą wartość rynkową,
- historię ceny,
- cenę zakupu,
- oczekiwaną stopę zwrotu używaną w projekcjach.

To ważne, ponieważ:

> **Aktualna cena rynkowa nie jest tym samym co założona przyszła stopa zwrotu.**

---

## 7. Projection Engine

Projection Engine jest jednym z głównych elementów domenowych aplikacji.

W V1 może być bardzo prosty i deterministyczny - praktycznie odpowiednik dobrze zrobionego arkusza Excel.

### Podejście

Symulacja działa miesiąc po miesiącu.

```text
for each month:
    cash += income
    cash -= expenses
    investments *= monthlyReturn
    cash += plannedCashflows
```

Przykładowy wynik:

```text
Projection

startDate
months[]
```

```text
ProjectionMonth

month
cash
investments
liabilities
income
expenses
netWorth
```

Dzięki temu system buduje finansową linię czasu, którą później można łatwo porównywać pomiędzy scenariuszami.

---

## 8. Scenario Engine

Scenario Engine powinien być możliwie generyczny.

Zamiast tworzyć osobne kalkulatory:

```text
CarPurchaseSimulator
HouseSimulator
VacationSimulator
JobChangeSimulator
```

lepiej zbudować:

```text
Scenario
  +-- ScenarioChange[]
```

### Przykład: zakup samochodu

```text
Scenario: BMW M2

ONE_TIME_EXPENSE      130 000 PLN
RECURRING_EXPENSE       1 300 PLN / month
ASSET_CREATE          Vehicle BMW M2
ASSET_DEPRECIATION     10% / year
```

### Przykład: zmiana pracy

```text
RECURRING_INCOME       +4 000 PLN / month
```

### Przykład: zakup mieszkania

```text
ONE_TIME_EXPENSE       wkład własny
ASSET_CREATE           mieszkanie
LIABILITY_CREATE       kredyt hipoteczny
RECURRING_EXPENSE      rata
RECURRING_EXPENSE      czynsz / utrzymanie
```

Najważniejszy efekt:

> **Scenariusz staje się zestawem zmian na osi czasu, a nie osobnym kalkulatorem dla każdego rodzaju decyzji.**

Specjalistyczny formularz, np. "zakup samochodu", może po prostu wygenerować odpowiednie `ScenarioChange`.

---

## 9. Decision Lab - główny feature produktowy

Decision Lab powinien być miejscem, w którym użytkownik porównuje:

```text
BASELINE
vs.
SCENARIO
```

Przykład:

```text
Stan bazowy:
kapitał:              200 000 PLN
oszczędności/mies.:     8 000 PLN
cel:                   500 000 PLN

Baseline:
cel osiągnięty za ~38 miesięcy

Scenariusz:
zakup auta:           -100 000 PLN
dodatkowy koszt:       -1 200 PLN / mies.

Wynik:
cel osiągnięty za ~59 miesięcy

Wpływ decyzji:
+21 miesięcy
```

Dla samochodów warto docelowo uwzględniać:

- cenę zakupu,
- sprzedaż obecnego auta,
- finansowanie,
- koszt kapitału,
- ubezpieczenie,
- paliwo,
- serwis,
- opony,
- podatki i opłaty,
- amortyzację / utratę wartości,
- wartość końcową,
- koszt alternatywny niewydanego kapitału.

Celem jest maksymalnie obiektywna analiza całkowitego wpływu decyzji.

---

## 10. Dashboard - pierwszy układ

### Overview

```text
Net worth                237 400 PLN

Gotówka                   82 000
ETF                        91 500
Obligacje                  45 000
Lokaty                     30 000
Samochód                   55 000
-------------------------------
Aktywa                    303 500
Zobowiązania              -66 100
```

### Dodatkowe elementy

- wykres net worth w czasie,
- cashflow w bieżącym miesiącu,
- top kategorie wydatków,
- alokacja aktywów,
- progres celów,
- najbliższe duże zaplanowane przepływy,
- skrót do ostatnio analizowanych scenariuszy.

---

## 11. Proponowany model domenowy V1

```text
User
|
+-- FinancialAccount
|     +-- CASH
|     +-- BANK
|     +-- BROKERAGE
|     +-- SAVINGS
|     +-- DEPOSIT
|
+-- Transaction
|
+-- Asset
|     +-- VEHICLE
|     +-- REAL_ESTATE
|     +-- OTHER
|
+-- InvestmentPosition
|
+-- Instrument
|
+-- InstrumentPrice
|
+-- Liability
|
+-- FinancialGoal
|
+-- Scenario
|     +-- ScenarioChange
|
+-- Projection
      +-- ProjectionMonth
```

W V1 system może być single-user, ale warto nie projektować modelu tak, aby uniemożliwiał późniejsze dodanie `userId`.

---

## 12. Proponowany stack

### Frontend

- Angular,
- Angular Material lub PrimeNG,
- Chart.js lub Apache ECharts,
- responsywny layout.

### Backend

- Java,
- Spring Boot,
- Spring Security,
- Spring Data JPA,
- później Spring AI.

### Baza

- PostgreSQL,
- później opcjonalnie pgvector dla pamięci semantycznej.

### Infrastruktura - start

```text
Docker Compose
+-- frontend
+-- backend
+-- postgres
```

Później:

- prosty VPS lub serwer z domeną,
- ewentualnie AWS po ustabilizowaniu projektu.

### Styl architektury

**Modular monolith**, nie mikroserwisy.

Przykład modułów:

```text
com.personalfinance

finance
transactions
portfolio
goals
projection
scenario
agent
calendar
notes
auth
integration
```

---

## 13. Architektura docelowa

```text
                       +-----------------+
                       |     Angular     |
                       |                 |
                       | Dashboard       |
                       | Goals           |
                       | Scenarios       |
                       | Chat / Voice    |
                       +--------+--------+
                                |
                              REST
                                |
                     +----------v----------+
                     |    Spring Boot      |
                     |  PERSONAL FINANCE   |
                     |        OS API       |
                     +----------+----------+
                                |
          +---------------------+---------------------+
          |                     |                     |
    Finance Module        Life Module           Agent Module
          |                     |                     |
      Accounts              Calendar              Spring AI
      Assets                Events                    |
      Expenses              Notes                     v
      Goals                                         LLM
      Scenarios                                      |
          |                                           |
          +-------------- Tools <---------------------+
                                |
                +---------------+--------------+
                v               v              v
           PostgreSQL      Google APIs         n8n
```

---

## 14. V2 - Agent AI

Po ustabilizowaniu Finance Core i Scenario Engine można dodać agenta.

### Założenie

Agent korzysta wyłącznie z bezpiecznego Tool Layer, a nie bezpośrednio z JPA/SQL.

```text
Agent
  |
  v
Agent Tools
  |
  +-- FinanceFacade
  +-- ScenarioFacade
  +-- CalendarFacade
        |
        v
      Domain
```

### Pierwsze narzędzia READ

```text
getFinancialOverview()
getTransactions(...)
getPortfolio()
getGoals()
getGoalProjection(...)
simulateScenario(...)
```

### Później WRITE

```text
createTransaction(...)
createGoal(...)
createScenario(...)
updateAsset(...)
```

Ten sam serwis domenowy powinien być wykorzystywany przez REST API Angulara i przez tool agenta.

---

## 15. Uprawnienia agenta

Warto od początku myśleć o trzech klasach operacji.

### READ

Bez dodatkowego potwierdzenia:

- odczyt portfela,
- odczyt transakcji,
- odczyt celów,
- symulacje,
- odczyt kalendarza.

### WRITE

Agent pokazuje zamiar i prosi o potwierdzenie lub korzysta z ustalonej polityki:

- utworzenie transakcji,
- utworzenie celu,
- utworzenie scenariusza,
- wydarzenie w kalendarzu.

### SENSITIVE

Zawsze jawne potwierdzenie:

- usuwanie danych,
- operacje finansowe na realnych kontach,
- inwestycje,
- przelewy,
- wysyłanie wiadomości.

Jeśli system kiedyś uzyska możliwość wykonywania realnych operacji finansowych, autoryzacja powinna znajdować się całkowicie poza LLM.

---

## 16. Pamięć i kontekst agenta

Agent nie powinien dostawać całej historii życia w każdym promptcie.

### Structured Memory

Najważniejsze informacje pozostają w PostgreSQL:

- finanse,
- cele,
- aktywa,
- scenariusze,
- kalendarz,
- decyzje.

### Conversation Memory

```text
Conversation
+-- Message
    +-- userMessage
    +-- toolCall
    +-- toolResult
    +-- assistantMessage
```

### Semantic Memory - później

Dla miękkich informacji typu:

- "Dom jest ważniejszy niż drogie auto."
- "Nie chcę brać kredytu samochodowego."
- "Chcę utrzymywać minimum 50k poduszki."

Możliwy kierunek: PostgreSQL + pgvector.

### LifeContext

Przydatna może być generowana projekcja najważniejszych danych:

```text
LifeContext

income
monthlyExpenses
liquidAssets
netWorth
emergencyFund
currentCar
housingSituation
financialGoals
plannedExpenses
importantUpcomingEvents
```

Agent może pobierać taki snapshot jednym toolem, a po szczegóły sięgać dopiero w razie potrzeby.

---

## 17. Google Calendar

Integrację kalendarza należy dodać dopiero po działającym agencie tekstowym.

### Etap 1 - read only

- pobieranie wydarzeń,
- pytania typu "co mam jutro?",
- uwzględnianie ważnych wydarzeń w kontekście finansowym.

### Etap 2 - write

- tworzenie wydarzeń,
- aktualizacja wydarzeń,
- zarządzanie terminami.

Przykład:

```text
"Dodaj mi w środę o 17 wymianę oleju."
      v
createCalendarEvent(...)
```

Operacje zapisujące powinny korzystać z warstwy potwierdzeń.

---

## 18. Voice

Voice nie powinien być osobnym agentem.

Docelowy przepływ:

```text
mikrofon
   v
speech-to-text
   v
ten sam Agent
   v
tools
   v
backend
   v
text-to-speech (opcjonalnie)
```

Najbardziej praktyczny use case:

```text
"120 zł, Biedronka, zakupy"
        v
createTransaction(...)
```

Dzięki temu rejestrowanie pojedynczych transakcji staje się szybkie i mało uciążliwe.

---

## 19. Rola n8n

n8n warto traktować jako **automation engine**, a nie główny mózg aplikacji.

Dobre zastosowania:

- cykliczne joby,
- synchronizacje,
- webhooki,
- powiadomienia,
- automatyczne importy,
- integracje z zewnętrznymi usługami.

Główny agent powinien być zbudowany w kodzie, ponieważ projekt ma też służyć nauce współczesnego agent engineeringu.

---

## 20. Mobile

Na początku aplikacja może być responsywną aplikacją webową.

Sensowna ścieżka:

```text
Angular Web App
      v
Responsive / PWA
      v
Capacitor
   /       \
Android   iOS
```

Nie ma potrzeby budowania osobnego Fluttera lub React Native przed potwierdzeniem, że aplikacja naprawdę tego potrzebuje.

---

## 21. Roadmapa

### V1 - Finance Core

- Accounts,
- Transactions,
- Assets,
- Investments,
- Liabilities,
- Dashboard,
- Cashflow,
- Net Worth,
- Financial Goals,
- Projection Engine,
- Scenario Engine.

**Ręczne dane. Zero AI. Zero Google.**

### V1.5 - dokładniejsze symulacje

- średnia stopa zwrotu,
- inflacja,
- wzrost dochodu,
- wzrost kosztów,
- amortyzacja aktywów,
- recurring expenses,
- one-time expenses,
- kredyt hipoteczny,
- oprocentowanie.

### V2 - Agent

- Spring AI,
- LLM,
- chat,
- tool calling,
- pierwsze READ tools,
- później WRITE tools.

### V2.5 - Context / Memory

- historia rozmów,
- trwałe preferencje,
- plany,
- decision history,
- semantic memory.

### V3 - Google Calendar

- read events,
- create events,
- update events,
- approval layer.

### V4 - Voice

- speech-to-text,
- ten sam agent,
- voice transaction capture,
- opcjonalnie text-to-speech.

### V5 - zaawansowane symulacje

- Monte Carlo,
- rozkłady stóp zwrotu,
- zmienna inflacja,
- zmienny dochód,
- nieplanowane wydatki,
- scenariusze optimistic / base / pessimistic,
- probabilistyczna data osiągnięcia celu.

Przykład:

```text
Prawdopodobieństwo osiągnięcia celu:

do 2030   22%
do 2031   61%
do 2032   84%
```

AI nadal jedynie interpretuje wynik; symulację wykonuje kod.

---

## 22. Pierwszy naprawdę wartościowy vertical slice

Pierwszy duży milestone po V1:

> **"Mogę zapytać aplikację, czy mogę kupić samochód za 120 tys. PLN, jeśli chcę mieć 500 tys. PLN na mieszkanie, a agent sam pobierze moje finanse i przeprowadzi symulację."**

Przepływ:

```text
Angular Chat
     v
POST /assistant
     v
Spring AI
     v
LLM
     v
getFinancialOverview()
     v
getGoal()
     v
simulateScenario()
     v
LLM interpretuje wynik
```

To jest punkt, w którym system zaczyna być czymś znacznie ciekawszym niż standardowy tracker finansów.

---

## 23. Observability agenta

Każde wywołanie narzędzia powinno być logowane.

```text
AgentRun

id
conversationId
userMessage
model
finalResponse
```

```text
ToolExecution

toolName
input
output
duration
success
```

Pozwala to odpowiedzieć na pytanie:

> "Dlaczego agent stwierdził, że mam 73 tys.?"

zamiast zgadywać, skąd pochodziła liczba.

---

## 24. Pomysły odłożone na później

Nie powinny wejść do V1, ale warto je zachować jako parking pomysłów:

- customizowalny dashboard drag & drop,
- Notes,
- Tasks,
- Decision Journal,
- dokumenty,
- wishlisty,
- planowanie podróży,
- cele osobiste poza finansami,
- zdrowie / sport,
- email,
- dodatkowe integracje,
- bank API,
- automatyczny import transakcji,
- analiza dokumentów / faktur,
- rozbudowane automatyzacje n8n.

### Decision Journal - szczególnie ciekawy pomysł na później

Dla dużej decyzji można zapisać:

```text
Decision

"Zakup BMW M2"

data
alternatives
expectedImpact
reasoning
confidence
```

Po roku system może porównać oczekiwania z rzeczywistością, np. rzeczywisty koszt auta vs założony koszt.

To tworzy feedback loop do poprawy przyszłych decyzji.

---

## 25. Najważniejsze zasady projektu

1. **Najpierw poprawny model finansowy, później AI.**
2. **AI nie wykonuje istotnych obliczeń finansowych.**
3. **Agent komunikuje się z systemem przez jawne tools/facades.**
4. **Ten sam domain layer obsługuje REST API i agenta.**
5. **Pojedyncza transakcja jest podstawową jednostką danych.**
6. **Scenario Engine powinien być generyczny.**
7. **V1 powinno być małe i użyteczne bez integracji zewnętrznych.**
8. **Modular monolith jest wystarczający i preferowany.**
9. **Operacje WRITE/SENSITIVE wymagają kontroli i potwierdzeń.**
10. **Agent musi być obserwowalny i audytowalny.**

---

## 26. Następny krok implementacyjny

Po tym dokumencie kolejnym sensownym krokiem jest zaprojektowanie **V1 na poziomie implementacyjnym**, czyli:

1. encje i relacje PostgreSQL,
2. moduły i pakiety Spring Boot,
3. DTO i endpointy REST,
4. model `ProjectionEngine`,
5. model `Scenario` / `ScenarioChange`,
6. sposób liczenia pierwszej prostej projekcji,
7. ekran Dashboard w Angularze,
8. ekran Transactions,
9. ekran Goals,
10. ekran Decision Lab,
11. backlog implementacyjny w kolejności prac,
12. testy jednostkowe dla silnika projekcji.

To powinno być pierwszym realnym blueprintem do rozpoczęcia kodowania.

---

## Podsumowanie

Rdzeniem projektu nie jest dashboard ani sam agent AI. Największą wartością ma być połączenie:

```text
rzeczywiste dane finansowe
        +
cele użytkownika
        +
silnik projekcji
        +
symulacje decyzji
        +
agent jako naturalny interfejs
```

Dzięki temu aplikacja może docelowo odpowiadać nie tylko na pytanie **"ile mam pieniędzy?"**, ale przede wszystkim:

> **"Jak decyzja, którą rozważam dzisiaj, wpłynie na życie i cele, które chcę osiągnąć za kilka lat?"**
