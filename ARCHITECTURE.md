# Architecture notes

## Components
- Android UI: Compose touchscreen layout, profile selector, dead-zone and sensitivity tuning.
- Transport: `ControllerTransport` interface; USB/ADB TCP tunnel and Bluetooth SPP client.
- Protocol: one JSON object per line; explicit allow-list and normalized numeric values.
- Windows host: loopback TCP receiver; `VigemOutput` translates state to a virtual Xbox 360 controller.
- Tests: Android model tests and .NET validation tests.

## Planned next integrations
1. Implement a native Windows Bluetooth RFCOMM/SPP server using Windows Bluetooth APIs, with explicit pairing and reconnect UX.
2. Add draggable, remappable button-layout editor and persistent profile storage (DataStore/JSON).
3. Replace the directional-button stick prototype with a real analog joystick surface and pointer capture.
4. Add signed update manifests and rollback-safe updater for the app/host only.
5. Evaluate a maintained virtual gamepad driver or a supported vendor-specific solution before distributing ViGEmBus.
6. Add controller diagnostics and latency telemetry.

## Identity and privacy
The SHA field is descriptive metadata only. It does not authenticate a device. Production pairing should use a random pairing token and authenticated encrypted transport. Never trust a client based only on a MAC address.
