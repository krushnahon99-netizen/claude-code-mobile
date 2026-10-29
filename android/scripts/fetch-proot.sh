#!/usr/bin/env bash
# Downloads Termux's aarch64 proot and the libraries it links against, and stages them
# as jniLibs. On Android 10+ an app can only execute files from its nativeLibraryDir,
# and Android only extracts APK files named lib*.so there, so every file gets that shape:
#
#   proot                   -> libproot.so
#   libexec/proot/loader    -> libproot-loader.so
#   libtalloc.so.2          -> libtalloc.so (renamed inside libproot.so and itself too)
#   libandroid-shmem.so     -> libandroid-shmem.so
#
# Usage: scripts/fetch-proot.sh [output dir]   (default: app/src/main/jniLibs/arm64-v8a)
set -euo pipefail

REPO="https://packages.termux.dev/apt/termux-main"
INDEX="$REPO/dists/stable/main/binary-aarch64/Packages"
PREFIX="data/data/com.termux/files/usr"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="${1:-$SCRIPT_DIR/../app/src/main/jniLibs/arm64-v8a}"

for tool in curl ar tar sha256sum python3; do
  command -v "$tool" >/dev/null || { echo "fetch-proot: missing tool: $tool" >&2; exit 1; }
done

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

curl -fsSL --retry 3 -o "$WORK/Packages" "$INDEX"

# Prints "<Filename> <SHA256>" for one package of the index, so versions are never hard-coded.
package_info() {
  awk -v pkg="$1" '
    /^Package: / { found = ($2 == pkg); file = ""; sha = "" }
    found && /^Filename: / { file = $2 }
    found && /^SHA256: / { sha = $2 }
    found && file != "" && sha != "" { print file, sha; exit }
  ' "$WORK/Packages"
}

fetch_package() {
  local name="$1" info file sha
  info="$(package_info "$name")"
  if [ -z "$info" ]; then
    echo "fetch-proot: $name is missing from $INDEX" >&2
    exit 1
  fi
  read -r file sha <<<"$info"
  echo "fetch-proot: $name <- $file"
  curl -fsSL --retry 3 -o "$WORK/$name.deb" "$REPO/$file"
  echo "$sha  $WORK/$name.deb" | sha256sum --check --quiet -
  mkdir -p "$WORK/$name"
  (cd "$WORK/$name" && ar x "../$name.deb" && tar -xf data.tar.*)
}

fetch_package proot
fetch_package libtalloc
fetch_package libandroid-shmem

mkdir -p "$OUT"
install -m 755 "$WORK/proot/$PREFIX/bin/proot" "$OUT/libproot.so"
install -m 755 "$WORK/proot/$PREFIX/libexec/proot/loader" "$OUT/libproot-loader.so"
install -m 755 "$(readlink -f "$WORK/libtalloc/$PREFIX/lib/libtalloc.so.2")" "$OUT/libtalloc.so"
install -m 755 "$WORK/libandroid-shmem/$PREFIX/lib/libandroid-shmem.so" "$OUT/libandroid-shmem.so"

# Rename libtalloc.so.2 to libtalloc.so in the dynamic string tables (DT_NEEDED, SONAME and
# the symbol-version tables all point at the same string). The new name is shorter, so it's
# overwritten in place and padded with NUL: the ELF layout stays exactly as Termux built it,
# which is safer for Android's strict linker than rewriting the file with patchelf.
python3 - "$OUT/libproot.so" "$OUT/libtalloc.so" <<'EOF'
import sys

old = b"libtalloc.so.2\0"
new = b"libtalloc.so".ljust(len(old), b"\0")
for path in sys.argv[1:]:
    with open(path, "rb") as f:
        data = f.read()
    count = data.count(old)
    if count == 0:
        sys.exit(f"fetch-proot: libtalloc.so.2 not found in {path}")
    with open(path, "wb") as f:
        f.write(data.replace(old, new))
    print(f"fetch-proot: renamed libtalloc.so.2 in {path.rsplit('/', 1)[-1]} ({count}x)")
EOF

ls -l "$OUT"
