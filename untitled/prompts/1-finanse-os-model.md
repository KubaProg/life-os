# Personal Finance OS - Project Plan

## Aktualizacja kierunku — 2026-10-19

**Aktualne ustalenia 2026-10-19:** wspólne `Account` / `accounts` zapewnia jeden zakres ID, właściciela, nazwę i walutę. `FinancialAccount` oraz `InvestmentAccount` są profilami ze wspólnym PK/FK przez `@MapsId`. `MoneyTransfer` ma tylko źródło i cel wskazujące `Account`; komenda przekazuje globalne ID rachunków. Przygotowano migrację V5 zachowującą zwykłe konta i przenoszącą ID inwestycji oraz ich referencje; starszego seeda V3 nie przepisujemy. Lokata powstaje z kwotą i pierwszym transferem atomowo, bez stałego konta finansującego w encji. Prototyp frontendu lokaty i transferów przywrócono na prośbę użytkownika do ręcznej oceny. Nie uruchamiano migracji, backendu, testów ani buildu. Szczegóły: [Current-focus.md](../Current-focus.md).


Dokument zachowuje wcześniejszy plan i opis bazowego modelu. Aktualne uzgodnienia MVP mają pierwszeństwo i znajdują się w [Current-focus.md](../Current-focus.md) oraz [planie stanu obecnego](3-plan-implementacji-stanu-obecnego.md):

- Podsumowanie miesięcznych transakcji jest wdrożone. Następny obszar: inwestycje (obligacje, lokaty i ETF), ich trwała historia naliczeń/wycen oraz rzeczywisty miesięczny zysk. Następnie cele i powiązane projekcje/scenariusze.
- Docelowo zwykłe konta i produkty inwestycyjne będą na jednej liście UI; typ pozycji otworzy historię zwykłego konta albo osobny dashboard inwestycji z konfiguracją i operacjami.
- Dashboard ma kompaktowe podsumowanie wybranego miesiąca: „Zysk z inwestycji”, przychody, wydatki i stopa oszczędzania. Nie ma osobnej karty dziennego balansu ani cashflow w UI. Backendowy kalkulator lokaty jest szkicem do przeglądu; frontend inwestycji nie jest jeszcze uzgodniony.
- Osobne aktywa rzeczowe, zobowiązania i pełny net worth oraz dalsze dopracowanie filtrów transakcji są odłożone; pomysły pozostają w dokumentacji.
- Model inwestycji rozdzieli konto, instrument, jednostki, cenę, operacje, bieżącą wartość, historię naliczeń/wycen i miesięczny zysk. Nowy miesiąc nie zeruje kapitału ani wcześniejszego zysku; kapitalizacja nie liczy go drugi raz. Wpisany procent jest parametrem prognozy albo oprocentowaniem zgodnym z warunkami produktu; nie zastępuje wyceny rynkowej ETF.
- API S&P 500 / ETF zostało rozpoznane w planie 3. Dostawca, ticker rzeczywistego ETF-u, podatki, opłaty i dokładne reguły produktu wymagają doprecyzowania przed ich wdrożeniem.
- Aktualny stack: Angular + PrimeNG, Android przez Capacitor, Spring Boot i MySQL; bez PWA i security, local single-user `id = 1`. Bez kursów walut agregaty pozostają osobno per waluta.
- W tej sesji aktualizujemy wyłącznie dokumentację. Model i kod nie są teraz rozszerzane.

---

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
id: 1f
name: Jakub
email: example@example.com

