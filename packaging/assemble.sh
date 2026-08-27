#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${1:-}"
WINSW_VERSION="2.12.0"
WINSW_URL="https://github.com/winsw/winsw/releases/download/v${WINSW_VERSION}/WinSW-x64.exe"

if [[ -z "${VERSION}" ]]; then
  VERSION="$(mvn -f "${ROOT}/pom.xml" -q -DforceStdout help:evaluate -Dexpression=project.version 2>/dev/null | tail -n 1)"
fi

JAR_SRC="${ROOT}/target/ultron-${VERSION}.jar"
if [[ ! -f "${JAR_SRC}" ]]; then
  echo "No se encontro ${JAR_SRC}. Ejecutar: mvn -B -DskipTests package"
  exit 1
fi

DIST_ROOT="${ROOT}/dist"
PKG_NAME="ultron-server-${VERSION}"
PKG_DIR="${DIST_ROOT}/${PKG_NAME}"

rm -rf "${DIST_ROOT}"
mkdir -p "${PKG_DIR}/systemd" "${PKG_DIR}/windows"

cp "${JAR_SRC}" "${PKG_DIR}/ultron.jar"
cp "${JAR_SRC}" "${DIST_ROOT}/ultron-${VERSION}.jar"

cp "${ROOT}/packaging/ultron.service" "${PKG_DIR}/systemd/ultron.service"
cp "${ROOT}/packaging/ultron-windows.xml" "${PKG_DIR}/windows/ultron.xml"
cp "${ROOT}/packaging/install-linux.sh" "${PKG_DIR}/install-linux.sh"
cp "${ROOT}/packaging/uninstall-linux.sh" "${PKG_DIR}/uninstall-linux.sh"
cp "${ROOT}/packaging/install-windows.ps1" "${PKG_DIR}/install-windows.ps1"
cp "${ROOT}/packaging/uninstall-windows.ps1" "${PKG_DIR}/uninstall-windows.ps1"
cp "${ROOT}/packaging/INSTALL.txt" "${PKG_DIR}/INSTALL.txt"

chmod +x "${PKG_DIR}/install-linux.sh" "${PKG_DIR}/uninstall-linux.sh"

echo "Descargando WinSW ${WINSW_VERSION}..."
curl -fsSL --retry 3 -o "${PKG_DIR}/windows/ultron.exe" "${WINSW_URL}"
chmod +x "${PKG_DIR}/windows/ultron.exe"

(
  cd "${DIST_ROOT}"
  zip -qr "${PKG_NAME}.zip" "${PKG_NAME}"
)

echo "Paquete listo:"
echo "  ${DIST_ROOT}/ultron-${VERSION}.jar"
echo "  ${DIST_ROOT}/${PKG_NAME}.zip"
