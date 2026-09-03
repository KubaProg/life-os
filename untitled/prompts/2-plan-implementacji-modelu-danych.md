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
- szczegółowy portfel inwestycyjny.

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
- currentBalance
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
- `currentBalance` powinien używać `BigDecimal`,
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
- `transactionDate` oznacza datę faktycznej transakcji,
- `createdAt` i `updatedAt` oznaczają daty techniczne rekordu,
- `category` na start może być zwykłym tekstem, bez osobnej encji.

## 6. Baza danych

Docelowa baza danych:

- PostgreSQL

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
- policzenie sumy wartości kont użytkownika,
- pobranie wartości środków per konto,
- pogrupowanie kont według typu,
- pobranie ostatnich transakcji użytkownika,
- policzenie sumy przychodów w miesiącu,
- policzenie sumy wydatków w miesiącu,
- policzenie podstawowego bilansu miesięcznego.

Te agregacje powinny należeć do aplikacji, nie do agenta AI.

## 9. Kolejność implementacji

1. Sprawdzić aktualną strukturę backendu Spring Boot.
2. Dodać zależności dla Spring Data JPA i PostgreSQL, jeśli ich brakuje.
3. Skonfigurować połączenie z PostgreSQL w lokalnym środowisku.
4. Dodać encję `User`.
5. Dodać enum i encję `FinancialAccount`.
6. Dodać enum i encję `Transaction`.
7. Dodać repozytoria JPA.
8. Dodać migrację tworzącą tabele.
9. Dodać przykładowe dane startowe dla jednego użytkownika i kilku kont.
10. Dodać podstawowe testy repozytoriów albo test kontekstu JPA.

## 10. Decyzje do odłożenia

Na późniejsze etapy zostają:

- czy kategorie transakcji będą osobną encją,
- czy transfer będzie jedną transakcją czy parą transakcji,
- jak obsługiwać wiele walut w łącznej wartości majątku,
- jak modelować inwestycje i instrumenty finansowe,
- jak aktualizować `currentBalance` konta na podstawie transakcji,
- czy historia salda konta będzie osobną tabelą,
- jak agent AI będzie wywoływał operacje na danych przez toolsy.

## 11. Kryterium gotowości V1

Model danych V1 jest gotowy, gdy aplikacja potrafi zapisać w PostgreSQL:

- użytkownika,
- kilka kont finansowych użytkownika,
- transakcje przypisane do użytkownika i opcjonalnie konta,
- dane wystarczające do zbudowania prostego dashboardu finansowego.
