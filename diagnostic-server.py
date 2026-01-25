#!/usr/bin/env python3
"""
Diagnostic server for Cat Jump Barrels web build
Shows what files are available and what's missing
"""

import http.server
import socketserver
import os
import sys
import json
from pathlib import Path
from urllib.parse import urlparse, unquote
import glob

PORT = 8080
BASE_DIR = os.getcwd()

class DiagnosticHandler(http.server.SimpleHTTPRequestHandler):
    """Diagnostic HTTP handler"""

    def do_GET(self):
        """Handle GET requests"""

        path = unquote(self.path).lstrip('/')

        if path == '' or path == '/':
            self.serve_diagnostic_page()
            return
        elif path == 'api/files-status':
            self.serve_files_status()
            return
        elif path == 'api/build-info':
            self.serve_build_info()
            return

        # Try to serve file normally
        self.serve_file(path)

    def serve_diagnostic_page(self):
        """Serve the diagnostic HTML page"""
        with open(os.path.join(BASE_DIR, 'test-index.html'), 'rb') as f:
            content = f.read()

        self.send_response(200)
        self.send_header('Content-Type', 'text/html')
        self.send_header('Content-Length', len(content))
        self.end_headers()
        self.wfile.write(content)

    def serve_files_status(self):
        """Serve JSON with file status"""
        status = {
            'timestamp': __import__('datetime').datetime.now().isoformat(),
            'base_dir': BASE_DIR,
            'files': {}
        }

        # Check key files
        files_to_check = {
            'package.json': 'build/wasm/packages/CatJumpsBarrels-webApp/package.json',
            'compiled.mjs': 'build/wasm/packages/CatJumpsBarrels-webApp/kotlin/CatJumpsBarrels-webApp.mjs',
            'skiko.wasm': 'build/wasm/packages_imported/skiko-js-wasm-runtime/0.9.37-3/skiko.wasm',
            'skiko.mjs': 'build/wasm/packages_imported/skiko-js-wasm-runtime/0.9.37-3/skiko.mjs',
            'index.html': 'webApp/build/processedResources/wasmJs/main/index.html',
            'test-index': 'test-index.html'
        }

        for key, filepath in files_to_check.items():
            full_path = os.path.join(BASE_DIR, filepath)
            exists = os.path.exists(full_path)
            size = os.path.getsize(full_path) if exists else 0
            status['files'][key] = {
                'path': filepath,
                'exists': exists,
                'size': size
            }

        # Find all .mjs files
        status['all_mjs_files'] = []
        for root, dirs, files in os.walk(BASE_DIR):
            # Skip node_modules and .gradle
            if 'node_modules' in root or '.gradle' in root:
                continue
            for file in files:
                if file.endswith('.mjs'):
                    rel_path = os.path.relpath(os.path.join(root, file), BASE_DIR)
                    status['all_mjs_files'].append(rel_path)

        # Find all .wasm files
        status['all_wasm_files'] = []
        for root, dirs, files in os.walk(BASE_DIR):
            if 'node_modules' in root or '.gradle' in root:
                continue
            for file in files:
                if file.endswith('.wasm'):
                    rel_path = os.path.relpath(os.path.join(root, file), BASE_DIR)
                    status['all_wasm_files'].append(rel_path)

        # Serve JSON
        json_data = json.dumps(status, indent=2).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', len(json_data))
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json_data)

    def serve_build_info(self):
        """Serve JSON with build information"""
        info = {
            'project': 'Cat Jump Barrels',
            'target': 'wasmJs',
            'kotlin_version': '2.3.0',
            'compose_version': '1.10.0',
            'build_status': self.get_build_status(),
            'base_directory': BASE_DIR
        }

        json_data = json.dumps(info, indent=2).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json_data)

    def get_build_status(self):
        """Get overall build status"""
        # Check if key files exist
        webapp_mjs = os.path.exists(os.path.join(BASE_DIR, 'build/wasm/packages/CatJumpsBarrels-webApp/kotlin/CatJumpsBarrels-webApp.mjs'))
        skiko_wasm = os.path.exists(os.path.join(BASE_DIR, 'build/wasm/packages_imported/skiko-js-wasm-runtime/0.9.37-3/skiko.wasm'))

        if webapp_mjs and skiko_wasm:
            return 'COMPLETE'
        elif skiko_wasm:
            return 'PARTIAL - Missing Kotlin WASM output'
        else:
            return 'INCOMPLETE'

    def serve_file(self, path):
        """Serve a regular file"""
        full_path = os.path.join(BASE_DIR, path)

        # Security check
        try:
            full_path = os.path.abspath(full_path)
            if not full_path.startswith(os.path.abspath(BASE_DIR)):
                self.send_error(403, "Access denied")
                return
        except:
            self.send_error(400, "Invalid path")
            return

        if not os.path.exists(full_path):
            print(f"[404] File not found: {path}")
            self.send_error(404, "File not found")
            return

        if os.path.isdir(full_path):
            # Try index.html in directory
            index_path = os.path.join(full_path, 'index.html')
            if os.path.exists(index_path):
                full_path = index_path
            else:
                self.send_error(403, "Directory listing not allowed")
                return

        # Get MIME type
        mime_type = 'application/octet-stream'
        if path.endswith('.mjs'):
            mime_type = 'application/javascript'
        elif path.endswith('.js'):
            mime_type = 'application/javascript'
        elif path.endswith('.wasm'):
            mime_type = 'application/wasm'
        elif path.endswith('.json'):
            mime_type = 'application/json'
        elif path.endswith('.html'):
            mime_type = 'text/html'
        elif path.endswith('.css'):
            mime_type = 'text/css'

        # Serve file
        with open(full_path, 'rb') as f:
            content = f.read()

        self.send_response(200)
        self.send_header('Content-Type', mime_type)
        self.send_header('Content-Length', len(content))
        if mime_type == 'application/wasm':
            self.send_header('Cross-Origin-Embedder-Policy', 'require-corp')
            self.send_header('Cross-Origin-Opener-Policy', 'same-origin')
        self.end_headers()
        self.wfile.write(content)

        print(f"[200] {path} ({mime_type}, {len(content)} bytes)")

    def log_message(self, format, *args):
        """Suppress default logging"""
        print(f"[{self.client_address[0]}] {format % args}")

def main():
    """Start the diagnostic server"""

    print("\n" + "="*70)
    print("  Cat Jump Barrels - Diagnostic Web Server")
    print("="*70)
    print(f"\nServer running at:")
    print(f"  http://localhost:{PORT}")
    print(f"  http://127.0.0.1:{PORT}\n")
    print(f"Base directory: {BASE_DIR}\n")
    print("Press Ctrl+C to stop\n")
    print("="*70 + "\n")

    with socketserver.TCPServer(("0.0.0.0", PORT), DiagnosticHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n\nServer stopped.")
            sys.exit(0)

if __name__ == '__main__':
    main()
