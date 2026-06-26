#!/bin/bash

# =============================================================================
# Medianet Core Banking — Deployment Script
# =============================================================================
# This script compiles the backend, runs tests, builds Docker images,
# and starts all services using docker-compose.
#
# Usage:
#   ./deploy.sh              # Full deployment (compile + test + docker)
#   ./deploy.sh --skip-tests # Skip tests during Maven build
#   ./deploy.sh --down       # Stop and remove all containers
#   ./deploy.sh --restart    # Restart all containers (no rebuild)
#   ./deploy.sh --logs       # Follow logs for all services
# =============================================================================

set -e

# --- Colors ---
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# --- Configuration ---
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"
BACKEND_DIR="${SCRIPT_DIR}/backend"
SKIP_TESTS=false

# --- Functions ---

print_banner() {
    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║           Medianet Core Banking — Deployment               ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_step() {
    echo -e "\n${GREEN}▸ [$1] $2${NC}"
}

print_info() {
    echo -e "  ${BLUE}ℹ $1${NC}"
}

print_warn() {
    echo -e "  ${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "  ${RED}✖ $1${NC}"
}

print_success() {
    echo -e "  ${GREEN}✔ $1${NC}"
}

check_prerequisites() {
    print_step "0/5" "Checking prerequisites..."

    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install JDK 17+."
        exit 1
    fi
    JAVA_VER=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}')
    print_info "Java version: ${JAVA_VER}"

    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.8+."
        exit 1
    fi
    MVN_VER=$(mvn --version 2>&1 | head -1)
    print_info "${MVN_VER}"

    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker Desktop."
        exit 1
    fi
    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running. Please start Docker Desktop."
        exit 1
    fi
    DOCKER_VER=$(docker --version)
    print_info "${DOCKER_VER}"

    # Check Docker Compose
    if ! docker compose version &> /dev/null; then
        print_error "Docker Compose V2 is not available."
        exit 1
    fi
    COMPOSE_VER=$(docker compose version --short)
    print_info "Docker Compose v${COMPOSE_VER}"

    print_success "All prerequisites satisfied."
}

compile_backend() {
    print_step "1/5" "Compiling Java Backend..."

    local MVN_FLAGS="-Dmaven.compiler.useIncrementalCompilation=false"

    if [ "$SKIP_TESTS" = true ]; then
        MVN_FLAGS="${MVN_FLAGS} -DskipTests"
        print_info "Tests will be SKIPPED (--skip-tests flag)"
    fi

    cd "${BACKEND_DIR}"
    mvn clean install ${MVN_FLAGS}

    if [ $? -ne 0 ]; then
        print_error "Maven build failed! Please check the output above."
        exit 1
    fi

    cd "${SCRIPT_DIR}"
    print_success "Backend compiled successfully."
}

stop_containers() {
    print_step "2/5" "Stopping previous containers (if any)..."
    docker compose -f "${COMPOSE_FILE}" down --remove-orphans 2>/dev/null || true
    print_success "Previous containers removed."
}

pull_images() {
    print_step "3/5" "Pre-fetching base Docker images..."
    # Logout from Docker Hub to avoid token issues
    docker logout 2>/dev/null || true
    docker pull eclipse-temurin:17-jre 2>/dev/null || print_warn "Could not pre-pull eclipse-temurin:17-jre (will use cache or pull during build)"
    docker pull postgres:15-alpine 2>/dev/null || print_warn "Could not pre-pull postgres:15-alpine"
    docker pull nginx:alpine 2>/dev/null || print_warn "Could not pre-pull nginx:alpine"
    print_success "Base images ready."
}

build_and_start() {
    print_step "4/5" "Building and starting all Docker services..."
    docker compose -f "${COMPOSE_FILE}" up --build -d

    if [ $? -ne 0 ]; then
        print_error "Docker Compose failed to start services!"
        exit 1
    fi
    print_success "All services are starting up."
}

show_status() {
    print_step "5/5" "Deployment Summary"

    echo ""
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║  Service              │  URL                               ║${NC}"
    echo -e "${CYAN}╠══════════════════════════════════════════════════════════════╣${NC}"
    echo -e "${CYAN}║${NC}  Frontend (Angular)   │  ${BOLD}http://localhost:4200${NC}              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  Composite API        │  ${BOLD}http://localhost:8080/swagger-ui.html${NC} ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  Client Service       │  ${BOLD}http://localhost:8081${NC}              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  Account Service      │  ${BOLD}http://localhost:8082${NC}              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  Kafka Connect        │  ${BOLD}http://localhost:8083${NC}              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  PostgreSQL           │  ${BOLD}localhost:5432${NC}                     ${CYAN}║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}Note: Services may take 1-3 minutes to become fully available.${NC}"
    echo -e "To view logs:        ${BOLD}docker compose logs -f${NC}"
    echo -e "To stop everything:  ${BOLD}./deploy.sh --down${NC}"
    echo -e "To check status:     ${BOLD}docker compose ps${NC}"
    echo ""
}

# --- Main ---

print_banner

# Parse arguments
case "${1:-}" in
    --down)
        print_step "1/1" "Stopping all containers..."
        docker compose -f "${COMPOSE_FILE}" down --remove-orphans
        print_success "All containers stopped and removed."
        exit 0
        ;;
    --restart)
        print_step "1/1" "Restarting all containers..."
        docker compose -f "${COMPOSE_FILE}" restart
        print_success "All containers restarted."
        exit 0
        ;;
    --logs)
        docker compose -f "${COMPOSE_FILE}" logs -f
        exit 0
        ;;
    --skip-tests)
        SKIP_TESTS=true
        ;;
    --help|-h)
        echo "Usage: ./deploy.sh [OPTION]"
        echo ""
        echo "Options:"
        echo "  (no args)       Full deployment: compile, test, build images, start"
        echo "  --skip-tests    Skip unit/integration tests during Maven build"
        echo "  --down          Stop and remove all containers"
        echo "  --restart       Restart all containers without rebuilding"
        echo "  --logs          Follow live logs from all services"
        echo "  --help          Show this help message"
        exit 0
        ;;
esac

check_prerequisites
compile_backend
stop_containers
pull_images
build_and_start
show_status
