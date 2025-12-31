package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ro.cosminmihu.ktor.monitor.core.ClipboardManager
import ro.cosminmihu.ktor.monitor.core.ShareManager
import ro.cosminmihu.ktor.monitor.domain.ExportCallAsTextUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallRequestAsCurlUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallRequestAsWgetUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallUrlUseCase
import ro.cosminmihu.ktor.monitor.domain.GetCallUseCase
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.asString
import ro.cosminmihu.ktor.monitor.domain.model.contentType
import ro.cosminmihu.ktor.monitor.domain.model.durationAsText
import ro.cosminmihu.ktor.monitor.domain.model.encodedPathAndQuery
import ro.cosminmihu.ktor.monitor.domain.model.host
import ro.cosminmihu.ktor.monitor.domain.model.isError
import ro.cosminmihu.ktor.monitor.domain.model.isHttpError
import ro.cosminmihu.ktor.monitor.domain.model.isInProgress
import ro.cosminmihu.ktor.monitor.domain.model.isRedirect
import ro.cosminmihu.ktor.monitor.domain.model.isSecure
import ro.cosminmihu.ktor.monitor.domain.model.requestDateTimeAsText
import ro.cosminmihu.ktor.monitor.domain.model.requestTimeAsText
import ro.cosminmihu.ktor.monitor.domain.model.responseDateTimeAsText
import ro.cosminmihu.ktor.monitor.domain.model.sizeAsText
import ro.cosminmihu.ktor.monitor.domain.model.toBytesString
import ro.cosminmihu.ktor.monitor.domain.model.totalSizeAsText
import ro.cosminmihu.ktor.monitor.ui.detail.DetailUiState.Call
import ro.cosminmihu.ktor.monitor.ui.detail.DetailUiState.Request
import ro.cosminmihu.ktor.monitor.ui.detail.DetailUiState.Response
import ro.cosminmihu.ktor.monitor.ui.detail.formater.bodyImage
import kotlin.time.Duration.Companion.seconds

private const val NO_DATA = "-"
private const val SHARE_FILE_NAME = "ktormonitor.http"

internal class DetailViewModel(
    id: String,
    getCallUseCase: GetCallUseCase,
    private val exportCallUrlUseCase: ExportCallUrlUseCase,
    private val exportCallRequestAsCurlUseCase: ExportCallRequestAsCurlUseCase,
    private val exportCallRequestAsWgetUseCase: ExportCallRequestAsWgetUseCase,
    private val exportCallAsTextUseCase: ExportCallAsTextUseCase,
    private val clipboardManager: ClipboardManager,
    private val shareManager: ShareManager,
) : ViewModel() {

    private val call = getCallUseCase(id)
    val uiState = call
        .map { call ->
            call ?: return@map DetailUiState()

            DetailUiState(
                summary = DetailUiState.Summary(
                    url = call.url,
                    method = call.method,
                    protocol = call.protocol ?: NO_DATA,
                    requestTime = call.requestDateTimeAsText,
                    responseCode = call.responseCode?.toString() ?: NO_DATA,
                    responseTime = call.responseDateTimeAsText ?: NO_DATA,
                    duration = call.durationAsText ?: NO_DATA,
                    requestSize = call.requestContentLength.sizeAsText(),
                    responseSize = call.responseContentLength?.sizeAsText() ?: NO_DATA,
                    totalSize = call.totalSizeAsText ?: NO_DATA,
                    isLoading = call.isInProgress,
                    isRedirect = call.isRedirect,
                    isError = call.isError,
                    isHttpError = call.isHttpError,
                ),
                call = Call(
                    id = call.id,
                    isSecure = call.isSecure,
                    request = Request(
                        method = call.method,
                        host = call.host,
                        pathAndQuery = call.encodedPathAndQuery,
                        requestTime = call.requestTimeAsText,
                        headers = call.requestHeaders,
                        body = DetailUiState.Body(
                            bytes = call.requestBody?.toBytesString(),
                            raw = call.requestBody?.asString(),
                            image = checkBodyTruncated(call.isRequestBodyTruncated) {
                                bodyImage(call.requestContentType, call.requestBody)
                            },
                            isTrimmed = call.isRequestBodyTruncated == true,
                            contentFormat = checkBodyTruncated(call.isRequestBodyTruncated) {
                                call.requestContentType?.contentType?.contentFormat
                            }
                        ),
                    ),
                    response = Response(
                        responseCode = call.responseCode?.toString() ?: "",
                        contentType = call.responseContentType?.contentType ?: ContentType.UNKNOWN,
                        duration = call.durationAsText ?: "",
                        size = call.responseContentLength?.sizeAsText() ?: "",
                        error = call.error ?: "",
                        headers = call.responseHeaders ?: mapOf(),
                        body = DetailUiState.Body(
                            bytes = call.responseBody?.toBytesString(),
                            raw = call.responseBody?.asString(),
                            image = checkBodyTruncated(call.isResponseBodyTruncated) {
                                bodyImage(call.responseContentType, call.responseBody)
                            },
                            isTrimmed = call.isResponseBodyTruncated == true,
                            contentFormat = checkBodyTruncated(call.isResponseBodyTruncated) {
                                call.responseContentType?.contentType?.contentFormat
                            }
                        ),
                    )
                )
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            DetailUiState()
        )

    fun copy(type: DetailUiState.ClipboardCopyType) {
        viewModelScope.launch {
            val call = this@DetailViewModel.call.firstOrNull() ?: return@launch

            val copy = when (type) {
                DetailUiState.ClipboardCopyType.Url -> exportCallUrlUseCase(call)
                DetailUiState.ClipboardCopyType.Curl -> exportCallRequestAsCurlUseCase(call)
                DetailUiState.ClipboardCopyType.Wget -> exportCallRequestAsWgetUseCase(call)
                DetailUiState.ClipboardCopyType.Text -> exportCallAsTextUseCase(call)
            }

            clipboardManager.setText(copy)
        }
    }

    fun share(type: DetailUiState.FileShareType) {
        viewModelScope.launch {
            val call = this@DetailViewModel.call.firstOrNull() ?: return@launch

            val share = when (type) {
                DetailUiState.FileShareType.Text -> exportCallAsTextUseCase(call)
            }

            shareManager.shareAsFile(share, SHARE_FILE_NAME)
        }
    }
}


private fun <T> checkBodyTruncated(isTruncated: Boolean?, value: () -> T): T? {
    return when (isTruncated) {
        true -> null
        else -> value()
    }
}
