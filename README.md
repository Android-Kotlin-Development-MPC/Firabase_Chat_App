# Firebase Chat App (exp13)

A Jetpack Compose Android chat app using Firebase Realtime Database, demonstrating:

1. **Multi-Channel Chat Rooms** — A spinner (dropdown) lets you pick between
   *General Chat* (`/messages/general`) and *MPC Lab Study* (`/messages/mpc`).
   The Firebase reference updates dynamically so reads/writes target the
   selected channel.
2. **Real-time Message Deletion** — Long-press a message bubble. If the message
   belongs to the current user, a confirmation dialog appears and deletes the
   message from Firebase.

## Features

- Channel selector via `ExposedDropdownMenuBox` (`ChatScreen.kt`)
- Real-time message streaming, switching channels resubscribes to the new node
  (`ChatViewModel` uses `flatMapLatest`)
- Own messages shown right-aligned, others left-aligned
- Long-press own bubbles → delete dialog → removes from Firebase
- Stable per-device user id and editable display name (`UserSession.kt`)

## Setup

1. **Create a Firebase project** at https://console.firebase.google.com.
2. Add an **Android app** with package name `com.example.firebasechatapp`.
3. Download the generated **`google-services.json`** and replace the placeholder
   at `app/google-services.json`.
4. In the Firebase **Realtime Database**, set the rules to (warning: open rules
   for a course demo — tighten for production):

   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

5. Open the project in Android Studio and run it on a device/emulator.

## Project structure

```
app/src/main/java/com/example/firebasechatapp/
├── MainActivity.kt            # Entry point (Compose host)
├── data/
│   ├── ChatChannel.kt         # Channel enum -> /messages/{key}
│   ├── ChatMessage.kt         # Message model + ownership check
│   └── UserSession.kt         # Stable user id + display name (SharedPreferences)
├── repository/
│   └── ChatRepository.kt      # Firebase reads/writes/deletes per channel
├── ui/
│   ├── ChatScreen.kt          # Channel dropdown, bubbles, delete dialog, composer
│   └── theme/Theme.kt         # Material 3 theme
└── viewmodel/
    └── ChatViewModel.kt       # Channel state, message stream, send/delete
```

## Key dependencies

- Firebase BOM `32.7.4` + `firebase-database-ktx`
- Jetpack Compose BOM `2024.01.00` / Kotlin `1.9.22` / AGP `8.2.2`
