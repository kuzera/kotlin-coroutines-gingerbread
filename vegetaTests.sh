#!/bin/bash

if ! [ -x "$(command -v vegeta)" ]; then
  echo 'Error: vegeta is not installed.' >&2
  exit 1
fi

singleTestDuration=60s
rateForBlocking=60
rateForNonBlocking=500
sleepAfterTestGroup=15s
numberOfSingleTestRepetitions=10

runSingleVegetaTest() {
  path=$1
  rate=$2
  echo "GET http://localhost:8080/gingerbread/${path}" | vegeta attack -timeout=5s -duration=${singleTestDuration} -rate ${rate} | vegeta report | egrep "^Requests|^Latencies|^Success"
  sleep ${sleepAfterTestGroup}
}

runVegeta() {
  path=$1
  rate=$2

  for i in $(seq 1 $numberOfSingleTestRepetitions); do
    runSingleVegetaTest ${path} ${rate}
  done
}

for path in blockingRestTemplate suspendingPureCoroutines; do
  echo ${path}
  runVegeta ${path} ${rateForBlocking} | sort -r | cut -c 42- | sed 's/ms//g; s/s//g; s/%//g; s/,//g'
done

for path in suspendingFuelCoroutines webfluxPureReactive webfluxReactiveCoroutines; do
  echo ${path}
  runVegeta ${path} ${rateForNonBlocking} | sort -r | cut -c 42- | sed 's/ms//g; s/s//g; s/%//g; s/,//g'
done
