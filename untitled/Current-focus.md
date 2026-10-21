# Current focus — 2026-10-21

## Ostatnio zrobione

- Osobne profile kont: zwykłe, lokaty, obligacje i rachunki maklerskie. Tworzenie, transfery i usuwanie z zachowaniem historii.
- TOS1029 4,40%: zakup z gotówki konta, dzienne naliczanie wartości, roczna kapitalizacja, częściowy wykup, podatek i opłata. Automatyczne rozliczenia oraz nadrabianie po starcie; migracja V9.
- Po wykupie kapitał i bieżący zysk dotyczą pozostałych sztuk; wypłacony zysk jest osobno. Dashboard sumuje miesięczny zysk netto lokat i obligacji, osobno według waluty.

## Teraz

- Kończymy etap TOS. Obecny katalog zawiera tylko TOS1029; panel emisji i integracja Finwire są odłożone.
- Weryfikacja przez agenta: przegląd źródeł, bez uruchamiania aplikacji, migracji, buildów i testów.

## Następne: ETF

- Kontynuować moduł rachunku maklerskiego: istnieje już profil `BrokerageAccount` i obsługa gotówki, brakuje zakupów oraz pozycji ETF.
- Ustalić model instrumentu i partii zakupu, potem wdrożyć formularz zakupu jednostek z gotówki bieżącego konta oraz historię. Uzgodnić źródło cen do wyceny i liczenia wyniku.

Szczegóły i źródła: [plan finansów](prompts/3-plan-implementacji-stanu-obecnego.md). Zasady pracy: [AGENTS.md](AGENTS.md).
