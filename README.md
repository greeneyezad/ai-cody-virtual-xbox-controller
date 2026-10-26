# AI Cody Virtual Xbox 360 Controller

Android (Kotlin) touchscreen controller + Windows companion prototype.

## What this build does
- Native Android app UI with Xbox-style buttons, sticks, triggers, profile selection, and editable button mappings.
- Controller state is sent as newline-delimited JSON over a transport abstraction.
- Bluetooth transport: Android RFCOMM client (SPP) to a compatible Bluetooth serial server on Windows.
- USB transport: TCP loopback tunnel using Android Debug Bridge `adb reverse`; this requires USB debugging and is not native USB gamepad enumeration.
- Windows companion accepts TCP JSON packets and maps them to an Xbox 360 virtual controller using ViGEmClient, if the ViGEmBus driver is installed.
- Mapping/profile models and unit tests for normalization and mapping logic.

## Honest limitations
1. A phone cannot become an authentic Microsoft Xbox 360 wired controller merely by plugging in USB. This USB mode uses ADB reverse + the Windows companion, so USB debugging must be enabled.
2. Windows does not expose a supported built-in API for arbitrary applications to create an XInput Xbox 360 device. This prototype uses ViGEmBus/ViGEmClient. ViGEmBus is retired/upstream archived; use only if you accept that dependency and verify its provenance. The included code isolates virtual output so it can be replaced.
3. Bluetooth RFCOMM requires a Windows Bluetooth SPP server. The included companion TCP listener does not itself register a Windows RFCOMM service. For the first working connection, use USB/ADB reverse or run the companion's TCP listener over the same trusted network. A Windows RFCOMM server adapter is a follow-up integration task.
4. Bluetooth HID Device on Android can make some phones present as a generic HID gamepad, but cannot guarantee Xbox 360 identity or XInput compatibility. MAC addresses are Bluetooth addresses, not identity proofs. “Bluetooth SHA ID” is not a standard Bluetooth address type; the app labels it as an optional user-defined identifier.
5. Firmware updating applies only to this app/protocol/companion. It cannot update the phone's Bluetooth firmware or a physical Xbox controller's firmware.
6. Xbox app/game compatibility varies. This does not spoof a genuine Microsoft device or bypass security/anti-cheat controls.

## Requirements
- Android Studio Hedgehog or newer, Android SDK 35, JDK 17.
- Windows 10/11, .NET 8 SDK.
- Optional: ViGEmBus driver and NuGet `Nefarius.ViGEm.Client` for virtual XInput output.
- Android phone with USB data cable; enable Developer Options and USB debugging for USB tunnel.

## Build Android
Open `android/` in Android Studio and sync Gradle, then Run. Or:
```bash
cd android
./gradlew assembleDebug
```
On Windows use `gradlew.bat assembleDebug`.

## Build Windows companion
```powershell
cd windows/AiCody.ControllerHost
dotnet restore
dotnet build
dotnet run
```
The companion listens on TCP port 26760 by default. It binds to `127.0.0.1` only.

## USB transport setup (prototype)
1. Install Android platform-tools on Windows and enable USB debugging on the phone.
2. Connect the phone by USB and accept the debugging authorization prompt.
3. Start the Windows companion.
4. Run:
```powershell
adb reverse tcp:26760 tcp:26760
```
5. In the Android app, choose USB / ADB tunnel and connect to `127.0.0.1:26760`.
This is a development bridge, not native USB controller enumeration. Do not expose the companion listener to public networks.

## Protocol
Each line is one JSON `ControllerState`:
```json
{"type":"state","profile":"Default","buttons":["A"],"leftX":0.0,"leftY":0.0,"rightX":0.0,"rightY":0.0,"leftTrigger":0.0,"rightTrigger":0.0,"timestamp":123}
```
The host resets all controls when it receives a malformed packet or a client disconnects.

## Tests
```bash
cd windows/AiCody.ControllerHost.Tests
dotnet test
```
Android mapping tests are in `android/app/src/test/`.

## Safety
Only connect to a computer you own or are authorized to control. The companion binds to loopback by default and resets the controller on disconnect.
