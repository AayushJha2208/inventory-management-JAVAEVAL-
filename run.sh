#!/bin/bash
cd "$(dirname "$0")"

# Ensure write permissions on required folders
chmod -R u+w data logs reports 2>/dev/null || true

# Locate Java executable
JAVA_BIN=""
if command -v java >/dev/null 2>&1 && java -version >/dev/null 2>&1; then
    JAVA_BIN="java"
elif [ -x "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home/bin/java" ]; then
    JAVA_BIN="/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home/bin/java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
else
    for candidate in /Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java; do
        if [ -x "$candidate" ]; then
            JAVA_BIN="$candidate"
            break
        fi
    done
fi

if [ -z "$JAVA_BIN" ]; then
    echo "Error: Java was not found. Please install Java."
    exit 1
fi

# Locate SQLite JDBC jar
SQLITE_JAR="$HOME/.m2/repository/org/xerial/sqlite-jdbc/3.47.2.0/sqlite-jdbc-3.47.2.0.jar"
CP="target/classes:$SQLITE_JAR"

if command -v mvn >/dev/null 2>&1; then
    mvn -q compile exec:java
else
    "$JAVA_BIN" --enable-native-access=ALL-UNNAMED -cp "$CP" com.thapar.ims.App "$@"
fi
