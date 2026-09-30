# Plan implementacji modelu danych V1

## 1. Zakres

Ten plan dotyczy wyłącznie pierwszej wersji modelu danych dla sekcji finansowej aplikacji Personal Finance OS.

W V1 model danych ma być prosty i wspierać:

- wielu użytkowników,
- konta finansowe użytkownika,
- transakcje użytkownika,
- podstawowe agregacje pod dashboard finansowy.

Poza zakresem V1 zostają:

- security i pełne logowanie,
- agent AI,
- integracje z zewnętrznymi źródłami danych,
- konkretne instrumenty inwestycyjne,
- liczba jednostek ETF/akcji,
- ceny instrumentów,
- szczegółowy portfel inwestycyjny,
- przeliczanie wartości pomiędzy walutami i integracja z API kursów walut.

## 1.1. Przyjęte decyzje techniczne

- pozostajemy przy zaimplementowanej bazie MySQL,
- saldo konta jest wyliczane z wartości początkowej i transakcji, a nie przechowywane jako niezależne `currentBalance`,
- V1 może przechowywać konta i transakcje w różnych walutach, ale nie sumuje ich do jednej łącznej wartości,
- przeliczanie walut zostanie dodane później na podstawie zewnętrznego API kursów.

## 2. Encje V1

Na start implementujemy trzy główne encje:

- User
- FinancialAccount
- Transaction

Relacje:

```text
User
|-- FinancialAccount
|-- Transaction
```

Każde konto finansowe należy do jednego użytkownika.

Każda transakcja należy do jednego użytkownika.

Transakcja może opcjonalnie wskazywać konto finansowe, którego dotyczy.

## 3. User

Encja istnieje od początku, żeby aplikacja była gotowa na wielu użytkowników, mimo że pełne security zostanie dodane później.

Pola:

- id
- name
- email

Założenia implementacyjne:

- `id` jako klucz główny generowany przez bazę,
- `name` wymagane,
- `email` wymagany i unikalny,
- na start można utworzyć jednego użytkownika testowego przez migrację albo seed danych.

## 4. FinancialAccount

Encja reprezentuje jedno zbiorcze konto finansowe.

Przykłady:

- Konto osobiste
- XTB
- Saxo
- Gotówka PLN
- Gotówka EUR

Pola:

- id
- user
- name
- type
- currency
- openingBalance
- balanceStartDate
- active
- createdAt
- updatedAt

Sugerowane typy kont:

- BANK_ACCOUNT
- BROKERAGE
- CASH
- SAVINGS
- OTHER

Założenia implementacyjne:

- konto zawsze należy do użytkownika,
- `name` jest wymagane,
- `type` powinien być enumem,
- `currency` można przechowywać jako trzyliterowy kod waluty, np. `PLN`, `EUR`, `USD`,
- `openingBalance` powinien używać `BigDecimal` i oznacza saldo początkowe, od którego aplikacja zaczyna prowadzić historię,
- `balanceStartDate` określa dzień, od którego transakcje wpływają na wyliczane saldo,
- bieżące saldo nie jest osobnym źródłem prawdy; aplikacja wylicza je jako `openingBalance` plus wpływ transakcji od `balanceStartDate`,
- edycja lub usunięcie wcześniejszej transakcji automatycznie zmienia wyliczone saldo bez dodatkowej synchronizacji pola na koncie,
- `active` pozwala ukrywać stare konta bez usuwania historii,
- w V1 konto przechowuje jedną zbiorczą wartość, bez rozbijania na instrumenty.

## 5. Transaction

Encja reprezentuje pojedynczy przychód, wydatek albo korektę finansową.

Pola:

- id
- user
- financialAccount
- type
- amount
- currency
- transactionDate
- description
- category
- createdAt
- updatedAt

Sugerowane typy transakcji:

- INCOME
- EXPENSE
- TRANSFER
- ADJUSTMENT

Założenia implementacyjne:

- transakcja zawsze należy do użytkownika,
- `financialAccount` może być opcjonalne, ale dla większości transakcji powinno być ustawione,
- `amount` powinien używać `BigDecimal`,
- `currency` powinna być zgodna z kontem, jeśli konto jest ustawione,
- transakcja wpływająca na saldo musi wskazywać konto finansowe,
- wpływ na saldo powinien być jednoznaczny: `INCOME` zwiększa saldo, a `EXPENSE` je zmniejsza,
- `transactionDate` oznacza datę faktycznej transakcji,
- `createdAt` i `updatedAt` oznaczają daty techniczne rekordu,
- `category` na start może być zwykłym tekstem, bez osobnej encji.

## 6. Baza danych

Używana baza danych:

- MySQL

Projekt pozostaje przy obecnie zaimplementowanym silniku. Migracje Flyway i testy integracyjne powinny używać składni oraz obrazu MySQL zgodnych z wersją uruchamianą przez Docker Compose.

Tabele V1:

- users
- financial_accounts
- transactions

Indeksy:

- `users.email`
- `financial_accounts.user_id`
- `financial_accounts.type`
- `transactions.user_id`
- `transactions.financial_account_id`
- `transactions.transaction_date`
- `transactions.type`

Ograniczenia:

- email użytkownika powinien być unikalny,
- kwoty nie powinny być null,
- saldo początkowe i data początkowa konta nie powinny być null,
- typy enumów nie powinny być null,
- waluta nie powinna być null,
- nazwa konta nie powinna być pusta.

## 7. Spring Boot - warstwa implementacji

Pakiety sugerowane dla modelu danych:

```text
domain
|-- user
|-- finance
```

Minimalne elementy do utworzenia:

- encja `User`,
- encja `FinancialAccount`,
- encja `Transaction`,
- enum `FinancialAccountType`,
- enum `TransactionType`,
- repozytorium `UserRepository`,
- repozytorium `FinancialAccountRepository`,
- repozytorium `TransactionRepository`.

Na tym etapie nie trzeba jeszcze tworzyć rozbudowanej logiki domenowej. Wystarczy poprawny model, relacje, repozytoria i możliwość wykonywania podstawowych zapytań.

## 8. Zapytania potrzebne pod dashboard finansowy

Model danych powinien umożliwiać:

- pobranie wszystkich aktywnych kont użytkownika,
- wyliczenie salda każdego konta jako wartości początkowej skorygowanej o transakcje,
- policzenie sumy wartości kont użytkownika osobno dla każdej waluty,
- pobranie wartości środków per konto,
- pogrupowanie kont według typu,
- pobranie ostatnich transakcji użytkownika,
- policzenie sumy przychodów w miesiącu,
- policzenie sumy wydatków w miesiącu,
- policzenie podstawowego bilansu miesięcznego.

Te agregacje powinny należeć do aplikacji, nie do agenta AI.

Przykładowa reguła dla podstawowych typów:

```text
saldo konta = openingBalance
             + suma(INCOME od balanceStartDate)
             - suma(EXPENSE od balanceStartDate)
```

Transfery i korekty wymagają osobnych, jawnych reguł wpływu na saldo. Do czasu ich ustalenia nie powinny być uwzględniane przez domysł oparty wyłącznie na znaku kwoty.

## 9. Kolejność implementacji

1. Zachować obecną konfigurację Spring Data JPA, Flyway i MySQL.
2. Ujednolicić konfigurację MySQL pomiędzy aplikacją i plikami Docker Compose.
3. Zmienić model konta z `currentBalance` na `openingBalance` i `balanceStartDate` w nowej migracji Flyway.
4. Zaktualizować encję `FinancialAccount` i dane developerskie.
5. Dodać zapytania wyliczające wpływ transakcji na saldo konta.
6. Dodać serwis zwracający wyliczone saldo zamiast wystawiać encję JPA bezpośrednio.
7. Dodać agregacje dashboardu osobno dla każdej waluty.
8. Dodać testy edycji i usunięcia transakcji potwierdzające natychmiastową zmianę salda.
9. Dodać testy repozytoriów i testy integracyjne na MySQL.

## 10. Decyzje przyjęte i odłożone

Decyzje przyjęte:

- baza danych: MySQL,
- źródło prawdy salda: `openingBalance` oraz transakcje,
- brak przeliczania walut w V1,
- agregacje wielu walut są zwracane oddzielnie, bez tworzenia mylącej sumy łącznej.

Na późniejsze etapy zostają:

- czy kategorie transakcji będą osobną encją,
- czy transfer będzie jedną transakcją czy parą transakcji,
- wybór API kursów walut, przechowywanie kursów i przeliczanie do waluty bazowej,
- jak modelować inwestycje i instrumenty finansowe,
- czy historia salda konta będzie osobną tabelą,
- jak agent AI będzie wywoływał operacje na danych przez toolsy.

## 11. Kryterium gotowości V1

Model danych V1 jest gotowy, gdy aplikacja potrafi zapisać w MySQL:

- użytkownika,
- kilka kont finansowych użytkownika,
- transakcje przypisane do użytkownika i opcjonalnie konta,
- dane wystarczające do zbudowania prostego dashboardu finansowego,
- wartość początkową konta i datę rozpoczęcia historii,
- dane pozwalające wyliczyć aktualne saldo po dodaniu, edycji albo usunięciu transakcji,
- podsumowania rozdzielone według waluty, bez automatycznego przeliczania.
