# QuickTilesRotate 🔄

Un'applicazione Android (compatibile con **Android 15 / API 35**) che aggiunge un **Tile nei Quick Settings** (Impostazioni Rapide) per forzare l'orientamento dello schermo tra tre modalità:
1. **OFF**: Orientamento predefinito gestito dal sistema (o rotazione automatica standard).
2. **ORIZZONTALE (Landscape)**: Forza la modalità orizzontale (utilizzando il sensore per consentire entrambe le direzioni landscape a 90° e 270°).
3. **VERTICALE (Portrait)**: Forza la modalità verticale bloccata.

---

## 🎯 Il Caso d'Uso: Instagram in Orizzontale
Alcune applicazioni (come **Instagram**) forzano `android:screenOrientation="portrait"` nel proprio manifest, ignorando qualsiasi impostazione di rotazione automatica di Android.

**QuickTilesRotate** risolve questo problema sfruttando l'API di overlay di sistema (`android.permission.SYSTEM_ALERT_WINDOW`). Creando una view trasparente di dimensione minima a livello di sistema (`TYPE_APPLICATION_OVERLAY`) con flag `FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE`, il `WindowManager` di Android applica la richiesta di orientamento a **tutto lo schermo**, sovrascrivendo i vincoli imposti da Instagram o da qualsiasi altra applicazione in primo piano, **senza richiedere i permessi di root** e senza interferire con tocchi o gesture dell'utente.

---

## ✨ Funzionalità

* ⚡ **Quick Settings Tile istantaneo**: Tocca il toggle nel menu a tendina per scorrere rapidamente tra gli stati:
  - `Off` ➡️ `Orizzontale (Landscape)` ➡️ `Verticale (Portrait)` ➡️ `Off`
* 📱 **Menu a Pressione Prolungata (Bottom Sheet)**: Tieni premuto a lungo sulla tile dei Quick Settings per aprire un elegante pannello Material 3 dal basso e selezionare direttamente la modalità desiderata con un tocco.
* 🛡️ **Nessun impatto sul touch**: I tocchi, gli swipe e la tastiera continuano a funzionare normalmente.
* 🛡️ **Piena conformità con Android 14 e 15 (API 35)**:
  - Utilizza `foregroundServiceType="specialUse"` conformemente alle direttive Google.
  - Notifica persistente a bassa priorità con pulsante rapido per disattivare l'overlay in qualsiasi momento.
  - Interfaccia Material 3 Edge-to-Edge con supporto a Dynamic Color (Material You).
* 💻 **Configurazione rapida via ADB**: Possibilità di abilitare i permessi in un secondo via comandi shell.

---

## 🚀 Installazione e Abilitazione Permessi

L'app richiede il permesso speciale **Visualizzazione sopra altre app** (`SYSTEM_ALERT_WINDOW`) e il permesso **Notifiche** (su Android 13+).

### Metodo 1: Dall'interfaccia dell'App
1. Apri l'app **QuickTilesRotate**.
2. Tocca i pulsanti per concedere il permesso di sovrapposizione e le notifiche.

### Metodo 2: Tramite ADB (Zero click su telefono)
Se hai il dispositivo collegato via cavo/Wi-Fi con debug USB attivo, esegui:

```bash
# Abilita il permesso di sovrapposizione
adb shell appops set com.cripeda.quicktilesrotate SYSTEM_ALERT_WINDOW allow

# Abilita le notifiche di sistema
adb shell pm grant com.cripeda.quicktilesrotate android.permission.POST_NOTIFICATIONS
```

---

## 📲 Come aggiungere il Quick Setting Tile

1. Scorri due volte dall'alto verso il basso dello schermo del tuo Pixel per espandere tutti i Quick Settings.
2. Tocca l'icona della **matita ✏️** (Modifica).
3. Scorri verso il basso nell'elenco dei tile disponibili e trova **Orientamento** (icona con rotazione).
4. Trascina la tile tra quelle attive in alto.
5. Fatto! Ora basta un singolo tap per girare Instagram in orizzontale.

---

## 🛠️ Compilazione e Sviluppo

Il progetto è configurato per **Android Studio Ladybug / Koala** o successivo con:
- **Min SDK**: `26` (Android 8.0)
- **Target / Compile SDK**: `35` (Android 15)
- **Linguaggio**: Kotlin 2.0.21
- **UI**: Jetpack Compose + Material 3
- **Build System**: Gradle 8.14.5 con Version Catalog (`libs.versions.toml`)

Per compilare l'APK di debug da riga di comando:
```bash
./gradlew assembleDebug
```
L'APK generato si troverà in `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 Licenza
Rilasciato sotto licenza MIT.
Sviluppato da [cripeda-dev](https://github.com/cripeda-dev).
