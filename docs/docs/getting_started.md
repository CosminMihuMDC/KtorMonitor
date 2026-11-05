### <img src="https://upload.wikimedia.org/wikipedia/commons/6/6b/Gradle_logo.svg" width="100"/>

=== "Ktor - Kotlin Multiplatform"

    ```kotlin hl_lines="4"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.17.0")
            }
        }
    }
    ```
    
    **For Release Builds (No-Op)**
    
    To isolate KtorMonitor from release builds, use the `ktor-monitor-logging-no-op` variant. This ensures the monitor code is not included in production artifact.
    
    ```kotlin hl_lines="4"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("ro.cosminmihu.ktor:ktor-monitor-logging-no-op:1.17.0")
            }
        }
    }
    ```

=== "Ktor - Android"
    
    ```kotlin hl_lines="2-3"
    dependencies {
        debugImplementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.17.0")
        releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-logging-no-op:1.17.0")
    }
    ```

    For ***Android minSdk < 26***, [Core Library Desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) is required.

=== "OkHttp - Android & JVM"

    ```kotlin hl_lines="2-3"
    dependencies {
        debugImplementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor:1.17.0")
        releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor-no-op:1.17.0")
    }
    ```

    For ***Android minSdk < 26***, [Core Library Desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) is required.

=== "http4k - Android & JVM"

    ```kotlin hl_lines="2-3"
    dependencies {
        debugImplementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter:1.17.0")
        releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter-no-op:1.17.0")
    }
    ```

    For ***Android minSdk < 26***, [Core Library Desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) is required.


### 📦 Install Ktor Monitor

=== "Ktor Client Plugin"

    ```kotlin hl_lines="3-9"
    HttpClient {
        
        install(KtorMonitorLogging) {  
            sanitizeHeader { header -> header == "Authorization" }  
            filter { request -> !request.url.host.contains("cosminmihu.ro") }  
            showNotification = true  
            retentionPeriod = RetentionPeriod.OneHour
            maxContentLength = ContentLength.Default
        }
    }
    ```
    
    - ```sanitizeHeader``` - sanitize sensitive headers to avoid their values appearing in the logs
    - ```filter``` - filter logs for calls matching a predicate.
    - ```showNotification``` - Keep track of latest requests and responses into notification. Default is **true**. Android and iOS only. Notifications permission needs to be granted.
    - ```retentionPeriod``` - The retention period for the logs. Default is **1h**.
    - ```maxContentLength``` - The maximum length of the content that will be logged. After this, body will be truncated. Default is **250_000**. To log the entire body use ```ContentLength.Full```.

=== "OkHttp Interceptor"

    ```kotlin hl_lines="3-9"
    OkHttpClient.Builder()
        .addInterceptor(
            KtorMonitorInterceptor {
                sanitizeHeader { header -> header == "Authorization" }
                filter { request -> !request.url.host.contains("cosminmihu.ro") }
                showNotification = true
                retentionPeriod = RetentionPeriod.OneHour
                maxContentLength = ContentLength.Default
            }
        )
        .build()
    ```
    
    - ```sanitizeHeader``` - sanitize sensitive headers to avoid their values appearing in the logs
    - ```filter``` - filter logs for calls matching a predicate.
    - ```showNotification``` - Keep track of latest requests and responses into notification. Default is **true**. Android and iOS only. Notifications permission needs to be granted.
    - ```retentionPeriod``` - The retention period for the logs. Default is **1h**.
    - ```maxContentLength``` - The maximum length of the content that will be logged. After this, body will be truncated. Default is **250_000**. To log the entire body use ```ContentLength.Full```.

    !!! note
        Install it with `addInterceptor`, not `addNetworkInterceptor`. OkHttp does not run network interceptors for WebSocket calls, so a WebSocket handshake would not be logged. To also log WebSocket frames, open the socket with `client.newMonitoredWebSocket(request, listener)` instead of `client.newWebSocket(...)`. With `addInterceptor`, redirects are logged as a single call.

    !!! info "Why `newMonitoredWebSocket`?"
        An OkHttp interceptor only sees the WebSocket handshake (the `101` response). After the upgrade, frames no longer pass through `Interceptor.intercept`; OkHttp delivers received frames through `WebSocketListener` and sent frames through `WebSocket.send/close`. `newMonitoredWebSocket(request, listener)` wraps both the listener and the returned `WebSocket`, so it can:

        - record sent and received messages and the close/failure events;
        - link them to the `101` call already logged by the interceptor;
        - set the connection status (open, closed, failed) and the close code.

        Without it, only the `101` call is shown, with no messages. Ktor does not need this, because the plugin hooks directly into the WebSocket session.

        ```kotlin
        val webSocket = client.newMonitoredWebSocket(request, listener)
        ```

=== "http4k Filter"

    ```kotlin hl_lines="1-6"
    KtorMonitorFilter {
        sanitizeHeader { header -> header == "Authorization" }
        filter { request -> !request.uri.host.contains("cosminmihu.ro") }
        showNotification = true
        retentionPeriod = RetentionPeriod.OneHour
        maxContentLength = ContentLength.Default
    }.then(JavaHttpClient())
    ```
    
    - ```sanitizeHeader``` - sanitize sensitive headers to avoid their values appearing in the logs
    - ```filter``` - filter logs for calls matching a predicate.
    - ```showNotification``` - Keep track of latest requests and responses into notification. Default is **true**. Android only. Notifications permission needs to be granted.
    - ```retentionPeriod``` - The retention period for the logs. Default is **1h**.
    - ```maxContentLength``` - The maximum length of the content that will be logged. After this, body will be truncated. Default is **250_000**. To log the entire body use ```ContentLength.Full```.
