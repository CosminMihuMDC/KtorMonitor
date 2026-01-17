# <img src="images/logo-icon.svg" width="35"/> KtorMonitor

[![Maven Central](https://img.shields.io/maven-central/v/ro.cosminmihu.ktor/ktor-monitor-logging?logo=apachemaven&label=Maven%20Central&link=https://search.maven.org/artifact/ro.cosminmihu.ktor/ktor-monitor-logging/)](https://search.maven.org/artifact/ro.cosminmihu.ktor/ktor-monitor-logging)
[![License](https://img.shields.io/github/license/CosminMihuMDC/KtorMonitor?label=License&logo=lintcode&logoColor=white&color=#3DA639)](https://github.com/CosminMihuMDC/KtorMonitor/blob/main/LICENSE)
![Platforms](https://img.shields.io/badge/Platforms-Android%20+%20iOS%20+%20JVM%20+%20Wasm%20+%20Js-brightgreen?logo=bambulab&logoColor=white&color=8d69e0)
[![JetBrains Klibs](https://img.shields.io/badge/JetBrains-klibs-4284F3?logo=jetbrains&logoColor=white)](https://klibs.io/project/CosminMihuMDC/KtorMonitor)
[![Documentation](https://img.shields.io/badge/Docs-html-brightgreen?logo=readthedocs&logoColor=white)](https://cosminmihumdc.github.io/KtorMonitor/html)
<br>
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-F05032.svg?logo=git&logoColor=white)](http://makeapullrequest.com)
[![GitHub stars](https://img.shields.io/github/stars/CosminMihuMDC/KtorMonitor)](https://github.com/CosminMihuMDC/KtorMonitor)
[![GitHub forks](https://img.shields.io/github/forks/CosminMihuMDC/KtorMonitor)](https://github.com/CosminMihuMDC/KtorMonitor/fork)

Powerful tool to monitor [Ktor Client](https://ktor.io/) requests and responses, making it easier to debug and analyze network communication.

## ✨ Features

*   **Comprehensive Logging**: Inspect detailed request and response information including headers, body, and duration.
*   **Platform Support**: Native support for **Android**, **iOS**, **Desktop (JVM)**, **Wasm**, and **JS**.
*   **Configurable**: Customize retention periods, content length limits, and notification behavior.
*   **Security**: redact sensitive headers (e.g., Authorization) automatically.
*   **No-Op Artifact**: Easily disable monitoring in release builds without code changes.

By default, **```KtorMonitor```**:
- **android** -> is enabled for ```debug``` builds and disabled for ```release``` builds
- **ios** -> is enabled for ```debug``` builds and disabled for ```release``` builds
- **desktop** -> is enabled for all builds
- **wasm** -> is enabled for all builds
- **js** -> is enabled for all builds