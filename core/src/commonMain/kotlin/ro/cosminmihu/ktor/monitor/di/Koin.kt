package ro.cosminmihu.ktor.monitor.di

import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier

internal inline fun <reified T : Any> LibraryKoinContext.get(
    qualifier: Qualifier? = null,
    noinline parameters: (() -> ParametersHolder)? = null,
) = koin.get<T>(qualifier, parameters)

internal inline fun <reified T : Any> LibraryKoinContext.inject(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    noinline parameters: (() -> ParametersHolder)? = null,
) = koin.inject<T>(qualifier, mode, parameters)
