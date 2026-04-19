package com.github.kr328.clash.service.clash.module

import android.app.Service
import com.github.kr328.clash.core.Clash
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

class ProxyModule(service: Service) : Module<Unit>(service) {
    data class ProxyConfig(
        val httpListenAt: String,
        val socksListenAt: String,
    )

    data class ProxyAddresses(
        val httpAddress: String?,
        val socksAddress: String?,
    )

    private val close = Channel<Unit>(Channel.CONFLATED)

    @Volatile
    var addresses: ProxyAddresses = ProxyAddresses(null, null)
        private set

    override suspend fun run() {
        try {
            return close.receive()
        } finally {
            withContext(NonCancellable) {
                requestStop()
            }
        }
    }

    fun attach(config: ProxyConfig) {
        val httpAddr = Clash.startHttp(config.httpListenAt)
        val socksAddr = Clash.startSocks(config.socksListenAt)

        addresses = ProxyAddresses(
            httpAddress = httpAddr,
            socksAddress = socksAddr,
        )
    }

    suspend fun close() {
        close.send(Unit)
    }

    companion object {
        fun requestStop() {
            Clash.stopHttp()
            Clash.stopSocks()
        }
    }
}
