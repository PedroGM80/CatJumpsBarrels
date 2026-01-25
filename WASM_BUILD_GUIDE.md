# Cat Jump Barrels - wasmJs Build Guide

## Problem: GitHub is Blocked

Your ISP blocks GitHub, which prevents downloading:
- **Yarn** (package manager) - ✅ SOLVED: Using npm instead
- **Binaryen** (WebAssembly toolchain) - ⚠️ NEEDS SOLUTION

## Solutions

### Option 1: Download on Another Network (RECOMMENDED)

On a computer with unrestricted internet:

```bash
# Download Binaryen directly
# For Windows x86_64:
https://github.com/WebAssembly/binaryen/releases/download/version_123/binaryen-version_123-x86_64-windows.tar.gz

# For macOS:
https://github.com/WebAssembly/binaryen/releases/download/version_123/binaryen-version_123-macos.tar.gz

# For Linux:
https://github.com/WebAssembly/binaryen/releases/download/version_123/binaryen-version_123-x86_64-linux.tar.gz
```

Then:
1. Extract to your project: `.gradle/binaryen/`
2. Copy back to restricted network machine
3. Gradle will use the cached version

### Option 2: Use a VPN/Proxy

Temporarily enable a VPN or HTTP proxy that doesn't block GitHub:
- **Mullvad VPN** (free, open source)
- **Cloudflare Warp** (free)
- **ProtonVPN** (free tier)

Then run:
```bash
./gradlew wasmJsBrowserProductionWebpack
```

### Option 3: Pre-compiled Approach

If you want to deploy immediately without full wasmJs build:

```bash
# Use development build (faster, no optimization)
./gradlew wasmJsBrowserDevelopmentWebpack
```

## Current Configuration ✅

```
✓ Kotlin 2.3.0
✓ Compose Multiplatform 1.10.0
✓ Node.js v22.16.0 + npm 11.6.2
✓ Configured to use npm (not Yarn)
✓ JVM version compiles successfully
```

## Build Commands

### Build Web Version (needs Binaryen)
```bash
./gradlew wasmJsBrowserProductionWebpack
```

### Output Location
```
webApp/build/dist/wasmJs/productionWebpack/
```

### Serve Locally
```bash
cd webApp/build/dist/wasmJs/productionWebpack/
python3 -m http.server 8000
# Open http://localhost:8000
```

## Binaryen Cache Location

Once downloaded, place at:
```
.gradle/binaryen/version_123/
```

Gradle will detect and use it automatically.

## Alternative: Docker

You could also build inside Docker with unrestricted internet:

```dockerfile
FROM openjdk:21
RUN apt-get update && apt-get install -y nodejs npm
COPY . /app
WORKDIR /app
RUN ./gradlew wasmJsBrowserProductionWebpack
```

## Support

Check Kotlin issue tracker for alternative Binaryen sources:
- https://github.com/JetBrains/kotlin/issues

## Next Steps

1. **Choose your solution** from options above
2. **Run the build** when ready
3. **Deploy** the compiled files in `webApp/build/dist/wasmJs/productionWebpack/`
