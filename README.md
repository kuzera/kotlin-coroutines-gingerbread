# Spring Boot gingerbread service

This project contains example Controller serving a few endpoints.

Each endpoint returns a Gingerbread data and uses specific request handler:

* /blockingRestTemplate - standard Spring [Rest Template](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-resttemplate.html) (blocking)
* /suspendingPureCoroutines - uses Rest Template but wrapped into [coroutine](https://kotlinlang.org/docs/reference/coroutines-overview.html) (still blocking)
* /suspendingFuelCoroutines - uses suspendable client and coroutines: [Fuel](https://github.com/kittinunf/fuel/tree/master/fuel-coroutines) (non-blocking)
* /webfluxPureReactive - uses Spring [WebClient](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-webclient.html) underneath
* /webfluxReactiveCoroutines - the same Spring WebClient but wrapped into [mono](https://github.com/Kotlin/kotlinx.coroutines/tree/master/reactive/kotlinx-coroutines-reactor) coroutine builder

Assumes that request handlers call some external micro-service (auxiliary [kotlin-coroutines-server](https://github.com/kuzera/kotlin-coroutines-server)) exposed on `http://localhost:8087/`

To run a server (by default uses netty on port 8080):
```bash
$ ./gradlew bootRun
```
To set the number of netty server workers to 64 (by default netty uses number of CPU cores, but at least 2):
```bash
$ ./gradlew bootRun -Dreactor.netty.ioWorkerCount=64
```
To run performance test against local server on port 8080:
```bash
$ ./vegetaTests.sh
```