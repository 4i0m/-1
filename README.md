# Keyboard Pro (كيبورد برو)

Flutter control app + native Android IME (Kotlin).

## Setup
```
flutter create --org com.keyboardpro --project-name keyboard_pro --platforms=android .
```
Then copy this folder's `lib/`, `pubspec.yaml`, `android/app/src/main/AndroidManifest.xml`,
`android/app/src/main/res/xml/method.xml` and the 5 Kotlin files over the generated ones
(package `com.keyboardpro.keyboard_pro`, so the generated MainActivity.kt must be replaced).

In `android/app/build.gradle(.kts)` set `minSdk = 24`, then:
```
flutter pub get && flutter run
```
Enable "كيبورد برو" in system settings, then select it as your keyboard.
