package com.github.kr328.clash.core

import java.net.InetSocketAddress
import java.net.Proxy

/**
 * Manager for lite proxy mode - provides proxy configuration
 * for application-level network requests without system VPN/TUN.
 *
 * Usage:
 *   val httpProxy = LiteProxyManager.getHttpProxy()
 *   val socksProxy = LiteProxyManager.getSocksProxy()
 *
 *   // OkHttp integration
 *   val client = OkHttpClient.Builder()
 *       .proxy(socksProxy)
 *       .build()
 *
 *   // JVM global proxy (affects HttpURLConnection)
 *   LiteProxyManager.applyJvmProxy()
 */
object LiteProxyManager {
    @Volatile
    var httpAddress: String? = null
        private set

    @Volatile
    var socksAddress: String? = null
        private set

    @Volatile
    var httpPort: Int = 7890
        private set

    @Volatile
    var socksPort: Int = 7891
        private set

    @Volatile
    var isRunning: Boolean = false
        private set

    fun updateAddresses(httpAddr: String?, socksAddr: String?) {
        if (httpAddr != null) {
            val parts = httpAddr.split(":")
            if (parts.size == 2) {
                httpAddress = parts[0]
                httpPort = parts[1].toIntOrNull() ?: 7890
            }
        } else {
            httpAddress = null
        }

        if (socksAddr != null) {
            val parts = socksAddr.split(":")
            if (parts.size == 2) {
                socksAddress = parts[0]
                socksPort = parts[1].toIntOrNull() ?: 7891
            }
        } else {
            socksAddress = null
        }

        isRunning = httpAddr != null || socksAddr != null
    }

    fun clear() {
        httpAddress = null
        socksAddress = null
        isRunning = false
    }

    /**
     * Get a Java Proxy object for HTTP proxy usage.
     * Returns Proxy.NO_PROXY if proxy is not running.
     */
    fun getHttpProxy(): Proxy {
        val addr = httpAddress ?: return Proxy.NO_PROXY
        return Proxy(Proxy.Type.HTTP, InetSocketAddress(addr, httpPort))
    }

    /**
     * Get a Java Proxy object for SOCKS5 proxy usage.
     * Returns Proxy.NO_PROXY if proxy is not running.
     */
    fun getSocksProxy(): Proxy {
        val addr = socksAddress ?: return Proxy.NO_PROXY
        return Proxy(Proxy.Type.SOCKS, InetSocketAddress(addr, socksPort))
    }

    /**
     * Apply HTTP proxy settings to JVM system properties.
     * This affects HttpURLConnection and other JVM HTTP clients.
     * Only affects the current process.
     */
    fun applyJvmProxy() {
        val addr = httpAddress
        if (addr != null) {
            System.setProperty("http.proxyHost", addr)
            System.setProperty("http.proxyPort", httpPort.toString())
            System.setProperty("https.proxyHost", addr)
            System.setProperty("https.proxyPort", httpPort.toString())
        }
    }

    /**
     * Clear JVM system proxy properties.
     */
    fun clearJvmProxy() {
        System.clearProperty("http.proxyHost")
        System.clearProperty("http.proxyPort")
        System.clearProperty("https.proxyHost")
        System.clearProperty("https.proxyPort")
    }
}
