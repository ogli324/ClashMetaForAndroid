package proxy

import (
	"sync"

	"github.com/metacubex/mihomo/listener/socks"
	"github.com/metacubex/mihomo/tunnel"
)

var socksListener *socks.Listener
var socksLock sync.Mutex

func StartSocks(listen string) (listenAt string, err error) {
	socksLock.Lock()
	defer socksLock.Unlock()

	stopSocksLocked()

	socksListener, err = socks.New(listen, tunnel.Tunnel)
	if err == nil {
		listenAt = socksListener.Address()
	}

	return
}

func StopSocks() {
	socksLock.Lock()
	defer socksLock.Unlock()

	stopSocksLocked()
}

func stopSocksLocked() {
	if socksListener != nil {
		socksListener.Close()
	}

	socksListener = nil
}
