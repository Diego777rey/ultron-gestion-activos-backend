#!/usr/bin/env bash
set -euo pipefail

INSTALL_DIR="${ULTRON_INSTALL_DIR:-/opt/ultron}"
SERVICE_USER="${ULTRON_SERVICE_USER:-ultron-server}"
SERVICE_NAME="ultron"
MIN_JAVA=25
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Ejecutar como root: sudo $0"
  exit 1
fi

resolve_java() {
  local java_bin=""
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    java_bin="${JAVA_HOME}/bin/java"
  elif command -v java >/dev/null 2>&1; then
    java_bin="$(command -v java)"
  fi

  if [[ -z "${java_bin}" ]]; then
    echo "No se encontro Java ${MIN_JAVA}+."
    echo "Instalar Temurin ${MIN_JAVA}: https://adoptium.net/"
    exit 1
  fi

  if command -v readlink >/dev/null 2>&1; then
    java_bin="$(readlink -f "${java_bin}")"
  fi

  local version_line major
  version_line="$("${java_bin}" -version 2>&1 | head -n 1)"
  major="$(sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' <<< "${version_line}")"

  if [[ -z "${major}" || "${major}" -lt "${MIN_JAVA}" ]]; then
    echo "Se requiere Java ${MIN_JAVA}+. Encontrado: ${version_line}"
    echo "Instalar Temurin ${MIN_JAVA}: https://adoptium.net/"
    exit 1
  fi

  echo "${java_bin}"
}

echo "Instalando Ultron en ${INSTALL_DIR}"
JAVA_BIN="$(resolve_java)"
echo "Java: ${JAVA_BIN}"

if ! id "${SERVICE_USER}" >/dev/null 2>&1; then
  nologin_shell="/usr/sbin/nologin"
  if [[ ! -x "${nologin_shell}" ]]; then
    nologin_shell="/bin/false"
  fi
  useradd --system --home "${INSTALL_DIR}" --shell "${nologin_shell}" "${SERVICE_USER}"
  echo "Usuario de servicio creado: ${SERVICE_USER}"
fi

mkdir -p "${INSTALL_DIR}/logs"

cp "${SCRIPT_DIR}/ultron.jar" "${INSTALL_DIR}/ultron.jar"

chown -R "${SERVICE_USER}:${SERVICE_USER}" "${INSTALL_DIR}"
chmod 755 "${INSTALL_DIR}"
chmod 644 "${INSTALL_DIR}/ultron.jar"

UNIT_PATH="/etc/systemd/system/${SERVICE_NAME}.service"
sed "s|__JAVA_BIN__|${JAVA_BIN}|g; s|/opt/ultron|${INSTALL_DIR}|g; s|^User=ultron$|User=${SERVICE_USER}|; s|^Group=ultron$|Group=${SERVICE_USER}|" \
  "${SCRIPT_DIR}/systemd/ultron.service" > "${UNIT_PATH}"

systemctl daemon-reload
systemctl enable "${SERVICE_NAME}.service"
systemctl restart "${SERVICE_NAME}.service"

echo
echo "Ultron instalado y habilitado al iniciar el sistema."
echo "  Directorio: ${INSTALL_DIR}"
echo
echo "Comandos:"
echo "  sudo systemctl status ${SERVICE_NAME}"
echo "  sudo systemctl restart ${SERVICE_NAME}"
echo "  sudo journalctl -u ${SERVICE_NAME} -f"
echo "  logs en ${INSTALL_DIR}/logs"
