#!/usr/bin/env bash

# -----------------------------------------------------------------------------
# Konture GitHub Pages Local Server Utility Script (Untracked Sandbox)
# -----------------------------------------------------------------------------

set -euo pipefail

# Locate the root of the project (parent of private/ folder)
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Bold colors for logging
RED='\033[1;31m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

cleanup_leftover_servers() {
    log_info "Checking for any leftover Jekyll local servers on ports 4000 or 35729..."
    local ports=(4000 35729)
    for port in "${ports[@]}"; do
        if lsof -i :"$port" -t >/dev/null 2>&1; then
            local pids
            pids=$(lsof -i :"$port" -t)
            for pid in $pids; do
                local cmd
                cmd=$(ps -p "$pid" -o command= 2>/dev/null || true)
                # Check if the process is ruby or jekyll
                if [[ "$cmd" == *"jekyll"* || "$cmd" == *"ruby"* ]]; then
                    log_warn "Port $port is currently in use by a stale Jekyll/Ruby process (PID: $pid). Terminating it..."
                    kill "$pid" 2>/dev/null || true
                    sleep 0.5
                    if kill -0 "$pid" 2>/dev/null; then
                        log_warn "Process $pid did not exit. Forcing termination..."
                        kill -9 "$pid" 2>/dev/null || true
                    fi
                fi
            done
        fi
    done
}

WATCHER_PID=""

# Cleanup trap to stop watcher, save build cache, and remove temporary workspace
cleanup() {
    echo ""
    log_info "Stopping Jekyll local server and beginning cleanup..."
    
    # Kill the background python sync watcher
    if [ -n "${WATCHER_PID:-}" ]; then
        log_info "Stopping background file watcher..."
        kill "$WATCHER_PID" 2>/dev/null || true
    fi

    # Preserve Jekyll build cache for faster subsequent boots
    if [ -d ".docs_temp/.jekyll-cache" ]; then
        log_info "Preserving Jekyll build cache to docs/.jekyll-cache..."
        mkdir -p docs/.jekyll-cache
        cp -rf .docs_temp/.jekyll-cache/* docs/.jekyll-cache/ 2>/dev/null || true
    fi

    # Remove temporary workspace
    if [ -d ".docs_temp" ]; then
        log_info "Removing temporary workspace .docs_temp..."
        rm -rf .docs_temp
    fi

    log_success "Cleanup complete! Your git working tree remains 100% clean."
}

# Register cleanup trap
trap cleanup EXIT INT TERM

# Free up ports if they are blocked by leftover servers
cleanup_leftover_servers

# Check if Ruby is installed
if ! command -v ruby &> /dev/null; then
    log_error "Ruby is not installed. Please install Ruby to run GitHub Pages locally."
    exit 1
fi

# Print Ruby version
log_info "Using Ruby: $(ruby -v)"

# Check if Bundler is installed
if ! command -v bundle &> /dev/null; then
    log_warn "Bundler is not installed. Attempting to install Bundler..."
    gem install bundler
fi

# Print Bundler version
log_info "Using Bundler: $(bundle -v)"

# Regenerate KDocs (API Reference) via Gradle
# Note: Dokka is not compatible with Gradle configuration cache, so we disable it for this run.
log_info "Regenerating KDocs (API Reference) via Gradle..."
./gradlew dokkaHtmlMultiModule --no-configuration-cache

# Clean up any leftover temporary workspace from a crashed run
if [ -d ".docs_temp" ]; then
    rm -rf .docs_temp
fi

# Create the temporary workspace by copying docs directory
log_info "Setting up temporary untracked workspace at .docs_temp..."
cp -R docs .docs_temp

# Clean up temporary site output to ensure a clean build
rm -rf .docs_temp/_site

# Ensure API docs (KDoc) are copied to the temporary Jekyll build directory
log_info "Copying API docs (KDoc) to the temporary build destination..."
mkdir -p .docs_temp/_site
cp -rf docs/kdoc .docs_temp/_site/

# Prepend Jekyll front matter metadata to markdown files inside the temp workspace
log_info "Prepending Jekyll front matter metadata to temporary workspace..."
python3 script/add_front_matter.py .docs_temp

# Launch the background file synchronization watcher
log_info "Starting background sync watcher for docs/ changes..."
python3 private/.docs_temp_watcher.py &
WATCHER_PID=$!

# Navigate to the temporary workspace to run Jekyll
cd .docs_temp

# Check and install bundle dependencies if needed
log_info "Checking Ruby dependencies..."
if ! bundle check &> /dev/null; then
    log_info "Dependencies not met. Running 'bundle install'..."
    bundle install
else
    log_success "All Ruby dependencies are satisfied."
fi

# Start the local Jekyll server with livereload
log_info "Starting Jekyll local server with livereload..."
log_info "Navigate to http://127.0.0.1:4000/ to view the site."
bundle exec jekyll serve --livereload
