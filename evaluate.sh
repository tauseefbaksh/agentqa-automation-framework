#!/usr/bin/env bash
set -e

if command -v cygpath >/dev/null 2>&1 && [ -n "${JAVA_HOME:-}" ]; then
  export JAVA_HOME="$(cygpath -u "$JAVA_HOME")"
elif [[ "${JAVA_HOME:-}" =~ ^[A-Za-z]:\\ ]]; then
  java_drive="${JAVA_HOME:0:1}"
  java_rest="${JAVA_HOME:2}"
  java_rest="${java_rest//\\//}"
  export JAVA_HOME="/${java_drive,,}${java_rest%/}"
elif [ -d "/mnt/d/Java/JDK 17" ]; then
  export JAVA_HOME="/mnt/d/Java/JDK 17"
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$repo_root/core-engine"

if command -v java >/dev/null 2>&1; then
  mvn -q -pl agentqa-eval -am compile
  mvn -q -pl agentqa-eval \
    org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
    -Dexec.mainClass=com.agentqa.eval.EvalCli \
    -Dexec.args="--deterministic --append-changelog"
else
  cmd.exe /c "set \"JAVA_HOME=D:\Java\JDK 17\"&& mvn -q -pl agentqa-eval -am compile"
  cmd.exe /c "set \"JAVA_HOME=D:\Java\JDK 17\"&& mvn -q -pl agentqa-eval org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.agentqa.eval.EvalCli -Dexec.args=--deterministic -Dagentqa.appendChangelog=true"
fi
