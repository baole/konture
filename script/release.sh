#!/usr/bin/env bash

# release.sh - Robust cross-platform automated version bumping and release verification script for Konture.
# Decouples, bumps, and verifies both main project and sample sandbox configurations cleanly.

set -euo pipefail

# ANSI color codes for premium console output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Print banner
echo -e "${CYAN}${BOLD}=======================================================================${NC}"
echo -e "${CYAN}${BOLD}                   Konture Automated Release System                    ${NC}"
echo -e "${CYAN}${BOLD}=======================================================================${NC}"

# Check for required commands
for cmd in perl git grep; do
    if ! command -v "$cmd" &> /dev/null; then
        echo -e "${RED}[ERROR] Required command '$cmd' is not installed. Exiting.${NC}" >&2
        exit 1
    fi
done

# Help / Usage info
show_usage() {
    echo -e "Usage: $0 <new-version> [--yes]"
    echo -e "Example: $0 0.6.2"
    echo -e "Options:"
    echo -e "  --yes, -y    Skip confirmation prompt and execute automatically."
}

if [[ $# -lt 1 ]]; then
    show_usage
    exit 1
fi

NEW_VERSION="$1"
AUTO_CONFIRM=false

if [[ $# -ge 2 ]]; then
    if [[ "$2" == "--yes" || "$2" == "-y" ]]; then
        AUTO_CONFIRM=true
    fi
fi

# Validate version format (e.g. 0.6.2 or 0.6.2-SNAPSHOT)
VERSION_REGEX="^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.]+)?$"
if [[ ! "$NEW_VERSION" =~ $VERSION_REGEX ]]; then
    echo -e "${RED}[ERROR] Invalid version format: '$NEW_VERSION'. Must match 'X.Y.Z' or 'X.Y.Z-SUFFIX'.${NC}" >&2
    exit 1
fi

# Locate the root of the project (parent of script/ folder)
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Extract current version from Version Catalog
VERSION_CATALOG="gradle/libs.versions.toml"
if [[ ! -f "$VERSION_CATALOG" ]]; then
    echo -e "${RED}[ERROR] Version catalog not found at '$VERSION_CATALOG'. Please run this from the project root.${NC}" >&2
    exit 1
fi

OLD_VERSION=$(grep 'konture =' "$VERSION_CATALOG" | head -n 1 | cut -d'"' -f2)

if [[ -z "$OLD_VERSION" ]]; then
    echo -e "${RED}[ERROR] Could not extract current konture version from '$VERSION_CATALOG'.${NC}" >&2
    exit 1
fi

echo -e "${BLUE}[INFO] Current detected version:${NC} ${BOLD}$OLD_VERSION${NC}"
echo -e "${BLUE}[INFO] Proposed target release version:${NC} ${BOLD}$NEW_VERSION${NC}"

if [[ "$OLD_VERSION" == "$NEW_VERSION" ]]; then
    echo -e "${YELLOW}[WARNING] Target version is identical to the current version. No updates needed.${NC}"
    exit 0
fi

# Prompt for confirmation if not auto-confirmed
if [ "$AUTO_CONFIRM" = false ]; then
    echo -ne "${YELLOW}${BOLD}Are you sure you want to bump version from $OLD_VERSION to $NEW_VERSION? (y/N): ${NC}"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Release process cancelled.${NC}"
        exit 0
    fi
fi

# List of files to modify and verify
FILES_TO_BUMP=(
    "gradle/libs.versions.toml"
    "build.gradle.kts"
    "build-logic/src/main/kotlin/konture.kotlin.gradle.kts"
    "plugin-gradle/build.gradle.kts"
    "docs/scripts.js"
    "showcases/sample-gradle/build.gradle.kts"
    "showcases/sample-gradle/konture-test/build.gradle.kts"
    "showcases/nowinandroid/build.gradle.kts"
    "showcases/nowinandroid/konture-test/build.gradle.kts"
    "showcases/kotlinconf-app/build.gradle.kts"
    "showcases/kotlinconf-app/konture-test/build.gradle.kts"
    "README.md"
    "CONTRIBUTING.md"
    "docs/contributing.md"
    "docs/installation.md"
    "docs/usage.md"
    "docs/configuration.md"
    "docs/baseline.md"
    "plugin-maven/pom.xml"
    "showcases/sample-maven/pom.xml"
)

# Backup files for rollback safety
echo -e "\n${BLUE}[1/3] Creating safety backups...${NC}"
BACKUP_DIR=$(mktemp -d -t konture-release-backup-XXXXXX)
trap 'rm -rf "$BACKUP_DIR"' EXIT

for f in "${FILES_TO_BUMP[@]}"; do
    if [[ -f "$f" ]]; then
        mkdir -p "$BACKUP_DIR/$(dirname "$f")"
        cp "$f" "$BACKUP_DIR/$f"
    fi
done

# Perform in-place search-and-replace using cross-platform perl
echo -e "${BLUE}[2/3] Bumping version coordinates in source and documentation files...${NC}"

# 1. Update Version Catalog
perl -pi -e "s/konture = \"\Q$OLD_VERSION\E\"/konture = \"$NEW_VERSION\"/g" gradle/libs.versions.toml

# 2. Update Docsify dynamic script
perl -pi -e "s/const latestVersion = '\Q$OLD_VERSION\E'/const latestVersion = '$NEW_VERSION'/g" docs/scripts.js

# 3. Update Showcase Root build plugins
perl -pi -e "s/id\(\"io.github.baole.konture\"\)\s+version\s+\"\Q$OLD_VERSION\E\"/id(\"io.github.baole.konture\") version \"$NEW_VERSION\"/g" showcases/sample-gradle/build.gradle.kts
perl -pi -e "s/id\(\"io.github.baole.konture\"\)\s+version\s+\"\Q$OLD_VERSION\E\"/id(\"io.github.baole.konture\") version \"$NEW_VERSION\"/g" showcases/nowinandroid/build.gradle.kts
perl -pi -e "s/id\(\"io.github.baole.konture\"\)\s+version\s+\"\Q$OLD_VERSION\E\"/id(\"io.github.baole.konture\") version \"$NEW_VERSION\"/g" showcases/kotlinconf-app/build.gradle.kts

# 4. Update Showcase konture-test module dependencies
perl -pi -e "s/testImplementation\(\"io.github.baole:konture:\Q$OLD_VERSION\E\"\)/testImplementation(\"io.github.baole:konture:$NEW_VERSION\")/g" showcases/sample-gradle/konture-test/build.gradle.kts
perl -pi -e "s/testImplementation\(\"io.github.baole:konture:\Q$OLD_VERSION\E\"\)/testImplementation(\"io.github.baole:konture:$NEW_VERSION\")/g" showcases/nowinandroid/konture-test/build.gradle.kts
perl -pi -e "s/testImplementation\(\"io.github.baole:konture:\Q$OLD_VERSION\E\"\)/testImplementation(\"io.github.baole:konture:$NEW_VERSION\")/g" showcases/kotlinconf-app/konture-test/build.gradle.kts

# 5. Update GitHub README.md references
perl -pi -e "s/\Q$OLD_VERSION\E/$NEW_VERSION/g" README.md

# 6. Update GitHub CONTRIBUTING.md references
perl -pi -e "s/\Q$OLD_VERSION\E/$NEW_VERSION/g" CONTRIBUTING.md

# 6b. Update docs/contributing.md references
perl -pi -e "s/\Q$OLD_VERSION\E/$NEW_VERSION/g" docs/contributing.md

# 6c. Update docs/installation.md references
perl -pi -e "s/\Q$OLD_VERSION\E/$NEW_VERSION/g" docs/installation.md

# 6d. Update docs/usage.md references
perl -pi -e "s/\Q$OLD_VERSION\E/$NEW_VERSION/g" docs/usage.md

# 6e. Update docs/configuration.md references
perl -pi -e "s/\Q$OLD_VERSION\E/$NEW_VERSION/g" docs/configuration.md

# 6f. Update docs/baseline.md references
perl -pi -e "s/\Q$OLD_VERSION\E/$NEW_VERSION/g" docs/baseline.md

# 7. Update Root buildscript classpath and project version
perl -pi -e "s/plugin-gradle:\Q$OLD_VERSION\E/plugin-gradle:$NEW_VERSION/g" build.gradle.kts
perl -pi -e "s/version = \"\Q$OLD_VERSION\E\"/version = \"$NEW_VERSION\"/g" build.gradle.kts
perl -pi -e "s/version = \"\Q$OLD_VERSION\E\"/version = \"$NEW_VERSION\"/g" build-logic/src/main/kotlin/konture.kotlin.gradle.kts

# 8. Update Gradle Plugin subproject version
perl -pi -e "s/version = \"\Q$OLD_VERSION\E\"/version = \"$NEW_VERSION\"/g" plugin-gradle/build.gradle.kts

# 8b. Update Maven Plugin and its konture-core dependency version
perl -pi -e "s/<version>\Q$OLD_VERSION\E<\/version>/<version>$NEW_VERSION<\/version>/g" plugin-maven/pom.xml

# 8c. Update Sample Maven Showcase konture version
perl -pi -e "s/<konture.version>\Q$OLD_VERSION\E<\/konture.version>/<konture.version>$NEW_VERSION<\/konture.version>/g" showcases/sample-maven/pom.xml

# 9. Commit and push changes inside submodules if they have dirty changes
echo -e "\n${BLUE}[3/3] Committing and pushing version bumps in git submodules...${NC}"
for submodule in showcases/*; do
    if [ -d "$submodule" ] && { [ -d "$submodule/.git" ] || [ -f "$submodule/.git" ]; }; then
        if (cd "$submodule" && git diff --quiet); then
            echo -e "${BLUE}No changes in submodule: $submodule${NC}"
        else
            echo -e "${YELLOW}Detected changes in submodule: $submodule. Committing and pushing...${NC}"
            (
                cd "$submodule"
                git add .
                git commit -m "Bump Konture version to $NEW_VERSION"
                echo -e "${BLUE}Pushing submodule $submodule to remote...${NC}"
                git push origin HEAD
            )
            echo -e "${GREEN}Submodule $submodule successfully updated and pushed.${NC}"
        fi
    fi
done

# Bumping complete
echo -e "${GREEN}[SUCCESS] Version bumped successfully in all coordinates!${NC}"


# Final success banner
echo -e "\n${GREEN}${BOLD}=======================================================================${NC}"
echo -e "${GREEN}${BOLD}             Version Bumped Successfully to v$NEW_VERSION!            ${NC}"
echo -e "${GREEN}${BOLD}=======================================================================${NC}"
echo -e "${BLUE}All files and documentation coordinates have been successfully updated.${NC}"
echo -e "\nTo verify the changes manually before committing, please run:"
echo -e "  1. Publish to local repository:"
echo -e "     ${CYAN}./gradlew publishToMavenLocal${NC}"
echo -e "  2. Run library unit and architecture tests:"
echo -e "     ${CYAN}./gradlew test${NC}"
echo -e "  3. Verify the nested standalone sample application:"
echo -e "     ${CYAN}./gradlew -p showcases/sample-gradle test${NC}"
echo -e "  4. Verify the nested standalone Maven sample application:"
echo -e "     ${CYAN}mvn clean test -f showcases/sample-maven/pom.xml${NC}"
echo -e "\nOnce verification passes, you can finalize the release by running:"
echo -e "  git add ."
echo -e "  git commit -m \"Release version $NEW_VERSION\""
echo -e "  git tag -a v$NEW_VERSION -m \"Version $NEW_VERSION\""
echo -e "  git push origin main --tags"
echo -e "${GREEN}${BOLD}=======================================================================${NC}"
