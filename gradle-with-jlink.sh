#!/usr/bin/env bash
# Uso: ./gradle-with-jlink.sh installDebug
# Fuerza JAVA_HOME para esta invocación (Cursor/IDE suelen pisar org.gradle.java.home).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
STUDIO_JBR="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
ZULU_17="/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"
if [[ -x "$STUDIO_JBR/bin/jlink" ]]; then
  export JAVA_HOME="$STUDIO_JBR"
elif [[ -x "$ZULU_17/bin/jlink" ]]; then
  export JAVA_HOME="$ZULU_17"
else
  echo "No se encontró jlink en Studio JBR ni Zulu 17. Instalá un JDK completo o editá este script." >&2
  exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"
cd "$ROOT"
exec ./gradlew "$@"
