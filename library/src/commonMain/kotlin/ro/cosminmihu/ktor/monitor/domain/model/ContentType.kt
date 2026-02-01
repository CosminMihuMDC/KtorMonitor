package ro.cosminmihu.ktor.monitor.domain.model

import androidx.compose.ui.graphics.Color

internal enum class ContentType(
    val contentType: String,
    val contentName: String,
    val color: Long,
) {
    // Application Types
    APPLICATION_ANY("application/*", "APP", 0xFFFFD700),
    APPLICATION_ATOM("application/atom+xml", "ATOM", 0xFFFFD700),
    APPLICATION_CBOR("application/cbor", "CBOR", 0xFFFFD700),
    APPLICATION_JSON("application/json", "JSON", 0xFFFF69B4),
    APPLICATION_HAL_JSON("application/hal+json", "HAL", 0xFFFF69B4),
    APPLICATION_JAVASCRIPT("application/javascript", "JS", 0xFFADD8E6),
    APPLICATION_OCTET_STREAM("application/octet-stream", "BIN", 0xFFA0522D),
    APPLICATION_RSS("application/rss+xml", "RSS", 0xFFFFD700),
    APPLICATION_SOAP("application/soap+xml", "SOAP", 0xFFFFD700),
    APPLICATION_XML("application/xml", "XML", 0xFFFFD700),
    APPLICATION_XML_DTD("application/xml-dtd", "DTD", 0xFFFFD700),
    APPLICATION_XAML("application/xaml+xml", "XAML", 0xFFFFD700),
    APPLICATION_ZIP("application/zip", "ZIP", 0xFF708090),
    APPLICATION_GZIP("application/gzip", "GZ", 0xFF778899),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded", "FORM", 0xFFDA70D6),
    APPLICATION_PDF("application/pdf", "PDF", 0xFFFF6347),
    APPLICATION_PROTOBUF("application/protobuf", "PROTO", 0xFFFFD700),
    APPLICATION_WASM("application/wasm", "WASM", 0xFF008080),
    APPLICATION_PROBLEM_JSON("application/problem+json", "PROB", 0xFFFF69B4),
    APPLICATION_PROBLEM_XML("application/problem+xml", "PROB", 0xFFFFD700),
    APPLICATION_VND_API_JSON("application/vnd.api+json", "API", 0xFFFF1493),

    // Microsoft Office Word
    APPLICATION_MSWORD("application/msword", "DOC", 0xFF1E90FF),
    APPLICATION_WORD_OPENXML(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "DOCX",
        0xFF00008B
    ),
    APPLICATION_WORD_MACRO("application/vnd.ms-word.document.macroEnabled.12", "DOCM", 0xFF8A2BE2),
    APPLICATION_WORD_TEMPLATE(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
        "DOTX",
        0xFF9932CC
    ),
    APPLICATION_WORD_TEMPLATE_MACRO(
        "application/vnd.ms-word.template.macroEnabled.12",
        "DOTM",
        0xFF9400D3
    ),

    // Microsoft Office Excel
    APPLICATION_EXCEL("application/vnd.ms-excel", "XLS", 0xFF32CD32),
    APPLICATION_EXCEL_OPENXML(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "XLSX",
        0xFF228B22
    ),
    APPLICATION_EXCEL_MACRO("application/vnd.ms-excel.sheet.macroEnabled.12", "XLSM", 0xFF006400),
    APPLICATION_EXCEL_TEMPLATE(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
        "XLTX",
        0xFF3CB371
    ),
    APPLICATION_EXCEL_TEMPLATE_MACRO(
        "application/vnd.ms-excel.template.macroEnabled.12",
        "XLTM",
        0xFF2E8B57
    ),
    APPLICATION_EXCEL_BINARY(
        "application/vnd.ms-excel.sheet.binary.macroEnabled.12",
        "XLSB",
        0xFF008000
    ),

    // Microsoft Office PowerPoint
    APPLICATION_POWERPOINT("application/vnd.ms-powerpoint", "PPT", 0xFFFF8C00),
    APPLICATION_POWERPOINT_OPENXML(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "PPTX",
        0xFFFF4500
    ),
    APPLICATION_POWERPOINT_MACRO(
        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
        "PPTM",
        0xFFDC143C
    ),
    APPLICATION_POWERPOINT_TEMPLATE(
        "application/vnd.openxmlformats-officedocument.presentationml.template",
        "POTX",
        0xFFFF6347
    ),
    APPLICATION_POWERPOINT_TEMPLATE_MACRO(
        "application/vnd.ms-powerpoint.template.macroEnabled.12",
        "POTM",
        0xFFB22222
    ),
    APPLICATION_POWERPOINT_SLIDESHOW(
        "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
        "PPSX",
        0xFFCD5C5C
    ),
    APPLICATION_POWERPOINT_SLIDESHOW_MACRO(
        "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
        "PPSM",
        0xFFA52A2A
    ),

    // Microsoft Office Access
    APPLICATION_ACCESS("application/vnd.ms-access", "MDB", 0xFF800000),
    APPLICATION_ACCESS_MODERN("application/vnd.ms-access", "ACCDB", 0xFF8B0000),

    // Microsoft Visio
    APPLICATION_VISIO("application/vnd.visio", "VSD", 0xFF4682B4),
    APPLICATION_VISIO_OPENXML("application/vnd.ms-visio.drawing", "VSDX", 0xFF5F9EA0),
    APPLICATION_VISIO_MACRO("application/vnd.ms-visio.drawing.macroEnabled.12", "VSDM", 0xFF6495ED),

    // Audio Types
    AUDIO_ANY("audio/*", "AUDIO", 0xFF87CEFA),
    AUDIO_MPEG("audio/mpeg", "MP3", 0xFF87CEFA),
    AUDIO_OGG("audio/ogg", "OGG", 0xFFB0E0E6),
    AUDIO_WAV("audio/wav", "WAV", 0xFF4682B4),
    AUDIO_WEBM("audio/webm", "WEBM", 0xFF00CED1),
    AUDIO_FLAC("audio/flac", "FLAC", 0xFF8B0000),
    AUDIO_X_WAV("audio/x-wav", "XWAV", 0xFF4682B4),

    // Image Types
    IMAGE_ANY("image/*", "IMG", 0xFFFFB6C1),
    IMAGE_GIF("image/gif", "GIF", 0xFFFFD700),
    IMAGE_JPEG("image/jpeg", "JPEG", 0xFFFFB6C1),
    IMAGE_PNG("image/png", "PNG", 0xFF98FB98),
    IMAGE_WEBP("image/webp", "WEBP", 0xFF20B2AA),
    IMAGE_SVG("image/svg+xml", "SVG", 0xFFDA70D6),
    IMAGE_X_ICON("image/x-icon", "ICO", 0xFFFF8C00),
    IMAGE_BMP("image/bmp", "BMP", 0xFFFFD700),

    // Text Types
    TEXT_ANY("text/*", "TXT", 0xFFFFA07A),
    TEXT_PLAIN("text/plain", "TXT", 0xFFFFA07A),
    TEXT_CSS("text/css", "CSS", 0xFF90EE90),
    TEXT_CSV("text/csv", "CSV", 0xFF90EE90),
    TEXT_HTML("text/html", "HTML", 0xFFD3D3D3),
    TEXT_JAVASCRIPT("text/javascript", "JS", 0xFFADD8E6),
    TEXT_VCARD("text/vcard", "VCARD", 0xFFFFA07A),
    TEXT_XML("text/xml", "XML", 0xFFFFD700),
    TEXT_EVENT_STREAM("text/event-stream", "SSE", 0xFF2DD4BF),

    // Video Types
    VIDEO_ANY("video/*", "MP4", 0xFFFFDAB9),
    VIDEO_MP4("video/mp4", "MP4", 0xFFFFDAB9),
    VIDEO_MPEG("video/mpeg", "MPEG", 0xFFDAA520),
    VIDEO_OGG("video/ogg", "OGV", 0xFFF0E68C),
    VIDEO_WEBM("video/webm", "WEBM", 0xFFADFF2F),
    VIDEO_QUICKTIME("video/quicktime", "QTFF", 0xFFFFDAB9),
    VIDEO_X_MS_WMV("video/x-ms-wmv", "WMV", 0xFFDAA520),

    // Font Types
    FONT_ANY("font/*", "FONT", 0xFFFFDAB9),
    FONT_COLLECTION("font/collection", "TTC", 0xFFFFDAB9),
    FONT_OTF("font/otf", "OTF", 0xFFFFDAB9),
    FONT_SFNT("font/sfnt", "SFNT", 0xFFFFDAB9),
    FONT_WOFF("font/woff", "WOFF", 0xFFFFDAB9),
    FONT_WOFF2("font/woff2", "WOFF2", 0xFFFFDAB9),
    FONT_EOT("font/eot", "EOT", 0xFFFFDAB9),

    // Multipart Types
    MULTIPART_ANY("multipart/*", "PART", 0xFFFF4500),
    MULTIPART_MIXED("multipart/mixed", "MIX", 0xFFFF69B4),
    MULTIPART_ALTERNATIVE("multipart/alternative", "ALT", 0xFFFF69B4),
    MULTIPART_RELATED("multipart/related", "RLT", 0xFFFF69B4),
    MULTIPART_FORM_DATA("multipart/form-data", "FORM", 0xFFFF4500),
    MULTIPART_SIGNED("multipart/signed", "SIGN", 0xFFFF4500),
    MULTIPART_ENCRYPTED("multipart/encrypted", "CRYPT", 0xFFFF4500),
    MULTIPART_BYTE_RANGES("multipart/byteranges", "BYTES", 0xFFDAA520),

    // Web Sockets
    WEB_SOCKET("", "WS", 0xFF3A7A03),

    UNKNOWN("", "?", 0xFF808080);
}

internal val String.contentType
    get() = ContentType.entries
        .firstOrNull { this.contains(it.contentType, true) }
        ?: ContentType.UNKNOWN

internal val ContentType.asColor
    get() = Color(color)
