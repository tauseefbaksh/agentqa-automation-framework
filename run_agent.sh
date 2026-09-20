#!/bin/bash

mvn -pl agentqa-agent-core exec:java -Dexec.mainClass="com.agentqa.agent.core.AgentRunner" -Dexec.args="--deterministic"
