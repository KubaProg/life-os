# Life OS – zasady interfejsu

## Mobile-first i PWA

- Projektuj każdy ekran najpierw dla szerokości 320–480 px, a następnie rozszerzaj układ breakpointami dla większych ekranów.
- Interfejs musi nadawać się do użycia jako PWA: nie zakładaj kursora, hovera, dużego ekranu ani stałego połączenia z siecią.
- Zapewniaj czytelną hierarchię na małym ekranie, pojedynczą kolumnę tam, gdzie to potrzebne, oraz wygodne strefy dotyku (co najmniej 44 × 44 px dla aktywnych kontrolek).
- Nie ukrywaj istotnych akcji wyłącznie pod hoverem. Zachowuj dostępność z klawiatury i semantyczny HTML.
- Przy zmianach UI sprawdzaj responsywność przynajmniej dla 320 px, 480 px i desktopu.

## PrimeNG

- Preferuj komponenty PrimeNG, jeśli biblioteka ma odpowiedni komponent (np. Button, Card, Dialog, Drawer, Table, Tag, Menu, Input).
- Dla struktury dokumentu, prostych list, nagłówków i elementów stricte semantycznych używaj zwykłego HTML i CSS.
- Zachowuj spójność z istniejącym motywem Aura i konfiguracją PrimeNG; nie twórz zastępczego komponentu, jeśli PrimeNG oferuje właściwy element.
