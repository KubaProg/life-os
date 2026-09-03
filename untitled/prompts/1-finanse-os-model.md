# Personal Finance OS - Project Plan

## 1. Cel projektu

Aplikacja ma być osobistym systemem finansowo-organizacyjnym.

Na początku skupiamy się wyłącznie na finansach osobistych i bardzo prostym zarządzaniu nimi.

Docelowo aplikacja ma posiadać również agenta AI, który:

- zna kontekst finansowy użytkownika,
- potrafi odczytywać dane z aplikacji,
- potrafi wykonywać akcje przez zdefiniowane toolsy,
- pomaga analizować decyzje finansowe,
- może działać przez chat,
- później może działać również głosowo,
- w kolejnych etapach może integrować się z Google Calendar i innymi źródłami danych.

Najważniejsza zasada architektoniczna:

> Logika biznesowa, kalkulacje i dane należą do aplikacji.  
> Agent AI jest tylko warstwą komunikacji i orkiestracji.

---

# 2. Stack technologiczny

## Frontend

- Angular
- PWA
- responsywny layout
- mobile-first

Docelowo możliwe opakowanie aplikacji przez Capacitor, jeśli PWA okaże się niewystarczające.

## Backend

- Java
- Spring Boot
- Spring Data JPA
- Spring Security - później
- Spring AI - później

## Baza danych

- PostgreSQL

## Infrastruktura na start

- Docker Compose
- lokalne środowisko

Docelowo możliwy deployment na:

- VPS
- AWS
- inny prosty serwer z domeną

---

# 3. Główne założenia UX

Aplikacja ma być prosta i wygodna głównie na telefonie.

## Ekran główny

Na początku ekran główny posiada 4 duże kafelki ułożone 2x2.

Przykład:

- Finanse
- Kalendarz
- Asystent
- Notatki

Na początku aktywnie rozwijana jest głównie sekcja:

- Finanse

Pozostałe mogą być placeholderami.

W przyszłości kafelki będą konfigurowalne.

---

# 4. Sekcja Finanse

Po wejściu w Finanse użytkownik powinien zobaczyć prosty dashboard.

## Dashboard finansowy

Powinien pokazywać między innymi:

- łączną wartość wszystkich kont,
- wartość środków per konto,
- podział kont według typu,
- ostatnie transakcje,
- sumę przychodów,
- sumę wydatków,
- podstawowe statystyki miesięczne.

Przykładowe konta:

- Konto osobiste
- XTB
- Saxo
- Gotówka PLN
- Gotówka EUR

Na V1 każde konto jest traktowane jako jedna zbiorcza wartość.

Nie modelujemy jeszcze:

- konkretnych ETF-ów,
- konkretnych akcji,
- liczby jednostek,
- pozycji inwestycyjnych,
- cen instrumentów.

To zostanie dodane później.

---

# 5. Model danych V1

Model ma być maksymalnie prosty.

Na początek tylko trzy główne encje:

- User
- FinancialAccount
- Transaction

Relacja:

User
|
+--- FinancialAccount
|
+--- Transaction

---

# 6. Encja User

Na początku bez pełnego security.

Encja istnieje po to, żeby model od początku wspierał wielu użytkowników.

## Pola

- id
- name
- email

Przykład:

```text
id: 1
name: Jakub
email: example@example.com

