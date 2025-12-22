package ro.cosminmihu.ktor.monitor.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal actual class ShareManager {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    internal actual suspend fun shareAsFile(
        content: String,
        name: String,
        title: String?,
    ) {
        val viewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return

        val tempDir = NSTemporaryDirectory()
        val filePath = tempDir + name
        val fileUrl = NSURL.fileURLWithPath(filePath)

        // Write text to file
        val nsString = NSString.create(string = content)
        nsString.writeToFile(
            filePath,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )

        // Present share sheet
        val activityViewController = UIActivityViewController(listOf(fileUrl), null)
        viewController.presentViewController(activityViewController, true, null)
    }
}