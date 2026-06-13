### <img src="https://upload.wikimedia.org/wikipedia/commons/6/6b/Gradle_logo.svg" width="100"/>

=== "Ktor - Kotlin Multiplatform"

    ```kotlin hl_lines="4"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.14.1")
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
                implementation("ro.cosminmihu.ktor:ktor-monitor-logging-no-op:1.14.1")
            }
        }
    }
    ```

=== "Ktor - Android"
    
    ```kotlin hl_lines="2-3"
    dependencies {
        debugImplementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.14.1")
        releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-logging-no-op:1.14.1")
    }
    ```

    For ***Android minSdk < 26***, [Core Library Desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) is required.

=== "OkHttp - Android & JVM"

    ```kotlin hl_lines="2-3"
    dependencies {
        debugImplementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor:1.14.1")
        releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor-no-op:1.14.1")
    }
    ```

    For ***Android minSdk < 26***, [Core Library Desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) is required.

=== "http4k - Android & JVM"

    ```kotlin hl_lines="2-3"
    dependencies {
        debugImplementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter:1.14.1")
        releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter-no-op:1.14.1")
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
        .addNetworkInterceptor(
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
