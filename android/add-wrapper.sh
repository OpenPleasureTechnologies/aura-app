#!/bin/bash
# This script creates the gradle-wrapper.jar

mkdir -p gradle/wrapper
cd gradle/wrapper

# Download the official wrapper jar
curl -L -o gradle-wrapper.jar https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar

echo "gradle-wrapper.jar created successfully"
