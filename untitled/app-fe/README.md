# AppFe

## Android przez Capacitor

Natywny katalog `android` jest generowany lokalnie i nie jest zapisywany w Git. Aby utworzyć go od nowa, w katalogu `app-fe` wykonaj:

```powershell
npm install
npm run build
npx cap add android
npx cap sync android
```

Następnie otwórz projekt w Android Studio:

```powershell
npx cap open android
```

Po późniejszych zmianach frontendu wystarczą:

```powershell
npm run build
npx cap sync android
```

Projekt Android korzysta z ustawień w `capacitor.config.ts` i z buildu znajdującego się w `dist/app-fe/browser`.
