#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status
set -eo pipefail

# ANSI color codes for beautiful, scannable output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}=======================================================================${NC}"
echo -e "${BLUE}                    Konture Local Quality Check                        ${NC}"
echo -e "${BLUE}=======================================================================${NC}"

# Ensure we are in the repository root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Step 1: Run unified Gradle checks (KtLint, Detekt, Tests, Coverage Thresholds)
echo -e "\n${BLUE}[1/4] Running unified Gradle checks (lint, detekt, tests, coverage)...${NC}"
if ./gradlew -q check --continue; then
    echo -e "${GREEN}[SUCCESS] All Gradle quality and coverage verification checks passed!${NC}"
else
    echo -e "${RED}[ERROR] Gradle checks or coverage thresholds failed. Run ./script/format.sh to auto-fix styling issues.${NC}"
    exit 1
fi

# Step 2: Run standalone konture-test module tests
echo -e "\n${BLUE}[2/4] Running standalone konture-test module tests...${NC}"
if ./gradlew -q :runKontureTest; then
    echo -e "${GREEN}[SUCCESS] Standalone konture-test module tests passed!${NC}"
else
    echo -e "${RED}[ERROR] Standalone konture-test module tests failed.${NC}"
    exit 1
fi

# Step 3: Build Gradle Subprojects
echo -e "\n${BLUE}[3/4] Building Gradle subprojects...${NC}"
if ./gradlew build -x test; then
    echo -e "${GREEN}[SUCCESS] Gradle build completed successfully!${NC}"
else
    echo -e "${RED}[ERROR] Gradle build failed.${NC}"
    exit 1
fi

# Step 4: Build Maven Plugin
echo -e "\n${BLUE}[4/4] Building Maven plugin...${NC}"
if ./gradlew -q :core:publishToMavenLocal && mvn clean compile -f plugin-maven/pom.xml; then
    echo -e "${GREEN}[SUCCESS] Maven plugin compilation passed!${NC}"
else
    echo -e "${RED}[ERROR] Maven plugin compilation failed.${NC}"
    exit 1
fi

echo -e "\n${GREEN}=======================================================================${NC}"
echo -e "${GREEN}             All Local Validation Checks Passed Successfully!          ${NC}"
echo -e "${GREEN}=======================================================================${NC}"
