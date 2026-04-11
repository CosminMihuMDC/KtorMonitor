package ro.cosminmihu.ktor.monitor.core

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import org.koin.dsl.module
import ro.cosminmihu.ktor.monitor.di.LibraryKoinContext

/**
 * Content provider used to initialize the [android.content.Context].
 */
internal class KtorMonitorInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context?.applicationContext ?: return false
        LibraryKoinContext.koin.loadModules(modules = listOf(module { single { context } }))
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(
        uri: Uri,
    ): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int = 0
}