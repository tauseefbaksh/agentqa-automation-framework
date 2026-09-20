#!/bin/bash

mvn -pl agentqa-eval exec:java -Dexec.mainClass="com.agentqa.eval.EvalRunner" -Dexec.args="--deterministic"
