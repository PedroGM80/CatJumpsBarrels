#!/usr/bin/env python3
"""
Simple HTTP server for Cat Jump Barrels web development
Usage: python3 dev-server.py
Then open: http://localhost:8000/dev-server.html
"""

import http.server
import socketserver
import os
import sys
from pathlib import Path

PORT = 8000

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        # Add headers to prevent caching during development
        self.send_header('Cache-Control', 'no-store, no-cache, must-revalidate, max-age=0')
        self.send_header('Pragma', 'no-cache')
        self.send_header('Expires', '0')
        super().end_headers()

    def log_message(self, format, *args):
        # Custom logging
        print(f"[{self.log_date_time_string()}] {format % args}")

def run_server():
    # Change to webApp directory
    script_dir = Path(__file__).parent
    os.chdir(script_dir)

    Handler = MyHTTPRequestHandler
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print()
        print("=" * 60)
        print("🎮 Cat Jump Barrels - Web Development Server")
        print("=" * 60)
        print()
        print(f"✓ Server running at: http://localhost:{PORT}")
        print(f"✓ Status page: http://localhost:{PORT}/dev-server.html")
        print()
        print("Press Ctrl+C to stop the server")
        print()
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n✓ Server stopped")
            sys.exit(0)

if __name__ == "__main__":
    run_server()
