#!/bin/bash

mvn -pl agentqa-baseline exec:java -Dexec.mainClass="com.agentqa.baseline.BaselineRunner" -Dexec.args="--deterministic"
