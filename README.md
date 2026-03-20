# Neomart Admin Dashboard

![img.png](media/screenshot-2.png)

I use this as a gateway to upload apk(s) to the Neostore app.
The backend is still underway and actively developed (when I have the time).

The goal of this project is to provide a universal application repository that can be easily accessible in a form of online store (mimicking Google Play Store/Apple App Store) for legacy devices.

The targeting apps that will be distributed via Neostore will be of those that supports legacy devices (Android 1.x - 4.x).

## Features
- Secure Authentication: JWT-based login system with secure local token storage.
- Release management: API Integration to publish new app versions, track version codes, and manage changelogs.

## Tech Stacks
- UI Frameworks: Compose Multiplatform targeting Desktop/JVM.
- Architecture: MVVM with Unidirectional data flow.
- Dependency Injection: Koin.
- Networking: Ktor Client,
- Config Management: Gmazzo BuildConfig Plugin.