# biometric-app-lock

Xposed module to put any app behind a biometric lock. WIP

![Android 11+](https://img.shields.io/badge/Android-11%2B-1B5E20?style=flat-square)
![libxposed API 101](https://img.shields.io/badge/libxposed-API_101-ff69b4?style=flat-square)

## Requirements

- Android 11+
- Xposed manager with libxposed API 101 support
- Enrolled biometric on the device

## Install

- Grab the APK from [Releases](../../releases)
- Enable the module in your Xposed manager
- Pick apps in the Apps tab
- Force-stop each app you added, then launch it again

## What it does

- Gates any app behind a biometric prompt on launch.
- Blocks screenshots and recents until you authenticate.
- Stays unlocked until you leave the app.
- Configurable grace period before relocking, globally and per-app.

## What it doesn't

- Lock state is in-memory only, so root and ADB still reach app files and databases.
- Hooks the main process only, so apps with multiple processes get only partial coverage.
- Multi-window and PiP not covered.
- Apps with root or Xposed detection (banking apps, etc.) may crash or refuse to open.

## License

[![GPLv3](https://img.shields.io/badge/License-GPLv3-blue?style=flat-square)](LICENSE)

GPLv3, see [LICENSE](LICENSE)
