#!/usr/bin/env python3
"""
Simple HTTP server for serving the Cat Jump Barrels wasmJs web app
"""

import http.server
import socketserver
import os
import sys
import json
import socket
from pathlib import Path
from urllib.parse import urlparse, unquote

# Try different ports
PORTS = [8000, 3000, 5000, 8080, 9000, 7000]
BIND_ADDRESS = "127.0.0.1"

def find_free_port():
    """Find a free port from the list"""
    for port in PORTS:
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
                s.bind(('127.0.0.1', port))
                return port
        except OSError:
            continue
    return None

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    """Custom HTTP handler that properly serves WASM and ESM modules"""

    def do_GET(self):
        """Handle GET requests with proper MIME types"""

        # Parse the path
        parsed_path = urlparse(self.path)
        file_path = unquote(parsed_path.path).lstrip('/')

        # Log request
        print(f"[{self.client_address[0]}] GET {self.path}")

        # Handle root path
        if file_path == '' or file_path == '/':
            file_path = 'index.html'

        full_path = os.path.join(os.getcwd(), file_path)

        try:
            # Check if file exists
            if not os.path.exists(full_path):
                print(f"  -> File not found: {full_path}")
                self.send_error(404, f"File not found: {file_path}")
                return

            # Check if it's a directory
            if os.path.isdir(full_path):
                full_path = os.path.join(full_path, 'index.html')
                if not os.path.exists(full_path):
                    self.send_error(404, "index.html not found in directory")
                    return

            # Read file
            with open(full_path, 'rb') as f:
                content = f.read()

            # Determine content type
            if file_path.endswith('.mjs'):
                content_type = 'application/javascript'
            elif file_path.endswith('.js'):
                content_type = 'application/javascript'
            elif file_path.endswith('.wasm'):
                content_type = 'application/wasm'
            elif file_path.endswith('.json'):
                content_type = 'application/json'
            elif file_path.endswith('.html'):
                content_type = 'text/html'
            elif file_path.endswith('.css'):
                content_type = 'text/css'
            elif file_path.endswith('.png'):
                content_type = 'image/png'
            elif file_path.endswith('.jpg') or file_path.endswith('.jpeg'):
                content_type = 'image/jpeg'
            elif file_path.endswith('.gif'):
                content_type = 'image/gif'
            elif file_path.endswith('.svg'):
                content_type = 'image/svg+xml'
            else:
                content_type = 'application/octet-stream'

            # Send response
            self.send_response(200)
            self.send_header('Content-Type', content_type)
            self.send_header('Content-Length', len(content))
            # CORS headers for WASM
            self.send_header('Cross-Origin-Opener-Policy', 'same-origin')
            self.send_header('Cross-Origin-Embedder-Policy', 'require-corp')
            self.end_headers()

            self.wfile.write(content)
            print(f"  -> 200 OK ({len(content)} bytes, {content_type})")

        except Exception as e:
            print(f"  -> Error: {e}")
            self.send_error(500, str(e))

def start_server():
    """Start the HTTP server"""

    # Find a free port
    port = find_free_port()
    if port is None:
        print("ERROR: Could not find a free port!")
        print(f"Tried ports: {PORTS}")
        sys.exit(1)

    print(f"\n{'='*60}")
    print("Cat Jump Barrels - Web Server")
    print(f"{'='*60}")
    print(f"\nServer running at:")
    print(f"  http://localhost:{port}")
    print(f"  http://127.0.0.1:{port}")
    print(f"\nWorking directory: {os.getcwd()}")
    print(f"\nPress Ctrl+C to stop the server\n")

    # Create socket server
    handler = CustomHTTPRequestHandler
    socketserver.TCPServer.allow_reuse_address = True

    try:
        with socketserver.TCPServer((BIND_ADDRESS, port), handler) as httpd:
            print(f"Listening on {BIND_ADDRESS}:{port}...\n")
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n\nServer stopped.")
        sys.exit(0)
    except OSError as e:
        print(f"ERROR: {e}")
        print("Could not start server. The port might be in use.")
        sys.exit(1)

if __name__ == '__main__':
    start_server()
