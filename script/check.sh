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

# Step 1: Run KtLint check
echo -e "\n${BLUE}[1/5] Running KtLint style audits...${NC}"
if ./gradlew ktlintCheck; then
    echo -e "${GREEN}[SUCCESS] KtLint checks passed!${NC}"
else
    echo -e "${RED}[ERROR] KtLint found style violations. Run ./gradlew ktlintFormat to auto-fix.${NC}"
    exit 1
fi

# Step 2: Run Detekt static analysis
echo -e "\n${BLUE}[2/5] Running Detekt static analysis...${NC}"
if ./gradlew detekt; then
    echo -e "${GREEN}[SUCCESS] Detekt static analysis passed!${NC}"
else
    echo -e "${RED}[ERROR] Detekt found quality violations.${NC}"
    exit 1
fi

# Step 3: Run Unit and Architecture Tests
echo -e "\n${BLUE}[3/5] Running Unit and Architecture tests...${NC}"
if ./gradlew test; then
    echo -e "${GREEN}[SUCCESS] All tests passed!${NC}"
else
    echo -e "${RED}[ERROR] Test execution failed.${NC}"
    exit 1
fi

# Step 4: Build Gradle Subprojects
echo -e "\n${BLUE}[4/5] Building Gradle subprojects...${NC}"
if ./gradlew build -x test; then
    echo -e "${GREEN}[SUCCESS] Gradle build completed successfully!${NC}"
else
    echo -e "${RED}[ERROR] Gradle build failed.${NC}"
    exit 1
fi

# Step 5: Build Maven Plugin
echo -e "\n${BLUE}[5/5] Building Maven plugin...${NC}"
if mvn clean compile -f plugin-maven/pom.xml; then
    echo -e "${GREEN}[SUCCESS] Maven plugin compilation passed!${NC}"
else
    echo -e "${RED}[ERROR] Maven plugin compilation failed.${NC}"
    exit 1
fi

echo -e "\n${GREEN}=======================================================================${NC}"
echo -e "${GREEN}             All Local Validation Checks Passed Successfully!          ${NC}"
echo -e "${GREEN}=======================================================================${NC}"
