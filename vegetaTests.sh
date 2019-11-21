#!/bin/bash

if ! [ -x "$(command -v vegeta)" ]; then
  echo 'Error: vegeta is not installed.' >&2
  exit 1
fi

singleTestDuration=90s
rate=500
sleepAfterTestGroup=15s
numberOfSingleTestRepetitions=3

runSingleVegetaTest() {
  path=$1
  echo "GET http://localhost:8080/gingerbread/${path}" | vegeta attack -timeout=5s -duration=${singleTestDuration} -rate ${rate} | vegeta report | egrep "^Requests|^Latencies|^Success"
  sleep ${sleepAfterTestGroup}
}

runVegeta() {
  path=$1

  for i in $(seq 1 $numberOfSingleTestRepetitions); do
    runSingleVegetaTest ${path}
  done
}

for path in blockingRestTemplate suspendingPureCoroutines suspendingFuelCoroutines webfluxPureReactive webfluxReactiveCoroutines; do
  echo ${path}
  runVegeta ${path} ${rateForNonBlocking} | sort -r | cut -c 42- | sed 's/ms//g; s/s//g; s/%//g; s/,//g'
done
