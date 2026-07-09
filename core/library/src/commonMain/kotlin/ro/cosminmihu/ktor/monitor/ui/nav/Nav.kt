package ro.cosminmihu.ktor.monitor.ui.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
internal sealed interface Nav : NavKey {

    @Serializable
    data object List : Nav

    @Serializable
    data class Detail(val id: String) : Nav
}

@OptIn(ExperimentalSerializationApi::class)
internal val backStackConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Nav>()
        }
    }
}