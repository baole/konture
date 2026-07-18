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
echo -e "${BLUE}                    Konture Auto-Fix & Formatting                      ${NC}"
echo -e "${BLUE}=======================================================================${NC}"

# Ensure we are in the repository root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Step 1: Run Spotless to apply license headers and general formatting
echo -e "\n${BLUE}[1/3] Applying Spotless formatting & license headers...${NC}"
if ./gradlew -q spotlessApply; then
    echo -e "${GREEN}[SUCCESS] Spotless formatting completed successfully!${NC}"
else
    echo -e "${RED}[ERROR] Spotless formatting failed.${NC}"
    exit 1
fi

# Step 2: Run KtLint format
echo -e "\n${BLUE}[2/3] Running KtLint formatting...${NC}"
if ./gradlew -q ktlintFormat; then
    echo -e "${GREEN}[SUCCESS] KtLint formatting completed successfully!${NC}"
else
    echo -e "${RED}[ERROR] KtLint formatting failed.${NC}"
    exit 1
fi

# Step 3: Run Detekt auto-correct
echo -e "\n${BLUE}[3/3] Running Detekt auto-correct...${NC}"
# Detekt --auto-correct might exit with non-zero if there are non-correctable issues remaining.
# We run without -e for detekt so we don't prematurely abort the script on non-correctable issues.
set +e
./gradlew -q detekt --auto-correct
DETEKT_STATUS=$?
set -e

if [ $DETEKT_STATUS -eq 0 ]; then
    echo -e "${GREEN}[SUCCESS] Detekt auto-correct completed with zero issues!${NC}"
else
    echo -e "${YELLOW}[WARNING] Detekt finished. Some non-correctable issues may still remain.${NC}"
fi

echo -e "\n${GREEN}=======================================================================${NC}"
echo -e "${GREEN}             Auto-fix and formatting run completed!                    ${NC}"
echo -e "${GREEN}=======================================================================${NC}"
