![Biometric App Lock](https://socialify.git.ci/hxreborn/biometric-app-lock/image?description=1&font=Inter&forks=1&issues=1&logo=https%3A%2F%2Fraw.githubusercontent.com%2Fhxreborn%2Fbiometric-app-lock%2Frefs%2Fheads%2Fmain%2Fassets%2Ficon.png&name=1&pattern=Floating%20Cogs&pulls=1&stargazers=1&theme=Auto)

<p align="center">
  Xposed module that intercepts the launches of a user-defined list of apps at the System Framework level. Those activities are never created until you authenticate via fingerprint or face unlock.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-11%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 11+">
  <img src="https://img.shields.io/badge/libxposed-API_101-ff69b4?style=for-the-badge" alt="libxposed API 101">
  <img src="https://img.shields.io/github/downloads/hxreborn/biometric-app-lock/total?style=for-the-badge&logo=github&label=Downloads" alt="Downloads">
</p>

> [!WARNING]
> Pre-1.0 releases (0.x.x) and any build tagged `-test` are experimental.

> [!NOTE]
> For stock AOSP and Pixel ROMs. Other ROMs may work but are untested, use at your own risk. If you're on HyperOS, OxygenOS or ColorOS, your ROM already ships a native app lock and you probably don't need this.

## About this module

Stock Android never had a native per-app lock, and [Private Space](https://source.android.com/docs/security/features/private-space) (available since Android 15 Beta 2) is a secondary isolated profile where apps run as separate installs with their own data.

This module intercepts activity launches at the system framework level before the target app starts. The Activity is never created until auth succeeds. Tapping a locked app from the recents screen is intercepted too.

A reboot is required after install or update because framework hooks only load at boot (for now 😉). Changing which apps are locked takes effect immediately.

## Requirements

- Android 11+ with an enrolled biometric
- Xposed manager with libxposed API 101 support

## Install

1. Install APK from [Releases](../../releases)
2. Enable module in your Xposed manager with System Framework scope
3. Reboot
4. Select apps to lock in the Apps tab

## License

[![GPL-3.0-only](https://img.shields.io/badge/LICENSE-GPL--3.0--only-%23A42E2B?style=for-the-badge&logo=gnu&logoColor=white&logoPosition=right)](LICENSE)
