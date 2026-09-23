import { Component } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-main-page',
  imports: [ButtonModule, CardModule, TagModule],
  templateUrl: './main-page.html',
  styleUrl: './main-page.scss'
})
export class MainPage {
  protected readonly tiles = [
    { title: 'Finanse', description: 'Konta, transakcje i szybki obraz Twoich finansów.', icon: 'wallet', active: true },
    { title: 'Kalendarz', description: 'Plany, terminy i ważne wydarzenia w jednym miejscu.', icon: 'calendar', active: false },
    { title: 'Asystent', description: 'Pomoc w analizie danych i codziennych decyzjach.', icon: 'assistant', active: false },
    { title: 'Notatki', description: 'Pomysły, listy i informacje, do których chcesz wrócić.', icon: 'notes', active: false }
  ];
}
