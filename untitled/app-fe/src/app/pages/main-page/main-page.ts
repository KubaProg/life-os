import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-main-page',
  imports: [ButtonModule, CardModule, RouterLink, TagModule],
  templateUrl: './main-page.html',
  styleUrl: './main-page.scss'
})
export class MainPage {
  protected readonly tiles = [
    { title: 'Finanse', description: 'Konta, transakcje i szybki obraz Twoich finansów.', icon: 'wallet', active: true, route: '/finanse' },
    { title: 'Kalendarz', description: 'Plany, terminy i ważne wydarzenia w jednym miejscu.', icon: 'calendar', active: false, route: null },
    { title: 'Asystent', description: 'Pomoc w analizie danych i codziennych decyzjach.', icon: 'assistant', active: false, route: null },
    { title: 'Notatki', description: 'Pomysły, listy i informacje, do których chcesz wrócić.', icon: 'notes', active: false, route: null }
  ];
}
