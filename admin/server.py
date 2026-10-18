#!/usr/bin/env python3
"""
PromptVault Admin — Python Fallback Server
Zero dependencies: Uses standard library only (http.server, json, os, urllib)
"""

import http.server
import socketserver
import os
import sys
import json
import base64
import shutil
import urllib.parse
from datetime import datetime

ADMIN_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.abspath(os.path.join(ADMIN_DIR, '..'))
IMAGES_DIR = os.path.join(REPO_ROOT, 'images')
PROMPTS_FILE = os.path.join(REPO_ROOT, 'prompts.json')
CONFIG_FILE = os.path.join(REPO_ROOT, 'app_config.json')

DEFAULT_PORT = 3000

os.makedirs(IMAGES_DIR, exist_ok=True)

MIME_TYPES = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.js': 'application/javascript; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.webp': 'image/webp',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
}

class PromptVaultHandler(http.server.BaseHTTPRequestHandler):
    def send_cors_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors_headers()
        self.end_headers()

    def send_json(self, status_code, data):
        body = json.dumps(data, indent=2, ensure_ascii=False).encode('utf-8')
        self.send_response(status_code)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Content-Length', str(len(body)))
        self.send_header('Cache-Control', 'no-store, no-cache, must-revalidate')
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(body)

    def send_error_json(self, status_code, message):
        self.send_json(status_code, {"error": True, "message": message})

    def parse_json_body(self):
        content_length = int(self.headers.get('Content-Length', 0))
        if content_length == 0:
            return {}
        raw = self.rfile.read(content_length).decode('utf-8')
        return json.loads(raw)

    def serve_file(self, file_path):
        if not os.path.exists(file_path):
            self.send_error_json(404, f"File not found: {os.path.basename(file_path)}")
            return
        if os.path.isdir(file_path):
            file_path = os.path.join(file_path, 'index.html')
            if not os.path.exists(file_path):
                self.send_error_json(404, "Index file not found")
                return

        ext = os.path.splitext(file_path)[1].lower()
        content_type = MIME_TYPES.get(ext, 'application/octet-stream')

        try:
            with open(file_path, 'rb') as f:
                data = f.read()
            self.send_response(200)
            self.send_header('Content-Type', content_type)
            self.send_header('Content-Length', str(len(data)))
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(data)
        except Exception as e:
            self.send_error_json(500, f"Error reading file: {str(e)}")

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        pathname = parsed.path

        if pathname == '/api/status':
            prompt_count = 0
            cat_count = 0
            if os.path.exists(PROMPTS_FILE):
                try:
                    with open(PROMPTS_FILE, 'r', encoding='utf-8') as f:
                        d = json.load(f)
                        prompt_count = len(d.get('items', []))
                        cat_count = len(d.get('categories', []))
                except Exception:
                    pass
            self.send_json(200, {
                "status": "ok",
                "mode": "local_server",
                "projectDir": REPO_ROOT,
                "hasPromptsFile": os.path.exists(PROMPTS_FILE),
                "hasConfigFile": os.path.exists(CONFIG_FILE),
                "promptCount": prompt_count,
                "categoriesCount": cat_count,
                "version": "1.0.0"
            })
            return

        if pathname == '/api/prompts':
            if not os.path.exists(PROMPTS_FILE):
                self.send_error_json(404, "prompts.json not found")
                return
            try:
                with open(PROMPTS_FILE, 'r', encoding='utf-8') as f:
                    self.send_json(200, json.load(f))
            except Exception as e:
                self.send_error_json(500, str(e))
            return

        if pathname == '/api/config':
            if not os.path.exists(CONFIG_FILE):
                self.send_error_json(404, "app_config.json not found")
                return
            try:
                with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
                    self.send_json(200, json.load(f))
            except Exception as e:
                self.send_error_json(500, str(e))
            return

        if pathname in ('/', '/admin', '/admin/'):
            self.serve_file(os.path.join(ADMIN_DIR, 'index.html'))
            return

        if pathname.startswith('/images/'):
            rel = pathname.replace('/images/', '').lstrip('/')
            self.serve_file(os.path.join(IMAGES_DIR, rel))
            return

        if pathname == '/prompts.json':
            self.serve_file(PROMPTS_FILE)
            return

        if pathname == '/app_config.json':
            self.serve_file(CONFIG_FILE)
            return

        admin_path = os.path.join(ADMIN_DIR, pathname.replace('/admin/', '').lstrip('/'))
        if os.path.exists(admin_path):
            self.serve_file(admin_path)
            return

        repo_path = os.path.join(REPO_ROOT, pathname.lstrip('/'))
        if os.path.exists(repo_path):
            self.serve_file(repo_path)
            return

        self.send_error_json(404, f"Path not found: {pathname}")

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        pathname = parsed.path

        if pathname == '/api/prompts':
            try:
                data = self.parse_json_body()
                if os.path.exists(PROMPTS_FILE):
                    try:
                        shutil.copyfile(PROMPTS_FILE, PROMPTS_FILE + '.bak')
                    except Exception:
                        pass
                data['updated_at'] = datetime.utcnow().isoformat() + "Z"
                with open(PROMPTS_FILE, 'w', encoding='utf-8') as f:
                    json.dump(data, f, indent=2, ensure_ascii=False)
                print(f"[Admin Server] Saved prompts.json ({len(data.get('items', []))} items)")
                self.send_json(200, {"success": True, "message": "prompts.json saved to disk", "updated_at": data['updated_at']})
            except Exception as e:
                self.send_error_json(500, str(e))
            return

        if pathname == '/api/config':
            try:
                data = self.parse_json_body()
                if os.path.exists(CONFIG_FILE):
                    try:
                        shutil.copyfile(CONFIG_FILE, CONFIG_FILE + '.bak')
                    except Exception:
                        pass
                with open(CONFIG_FILE, 'w', encoding='utf-8') as f:
                    json.dump(data, f, indent=2, ensure_ascii=False)
                print("[Admin Server] Saved app_config.json")
                self.send_json(200, {"success": True, "message": "app_config.json saved to disk"})
            except Exception as e:
                self.send_error_json(500, str(e))
            return

        if pathname == '/api/upload-image':
            try:
                data = self.parse_json_body()
                b64 = data.get('base64', '')
                if not b64:
                    self.send_error_json(400, "Missing image base64")
                    return
                if ',' in b64:
                    b64 = b64.split(',', 1)[1]
                img_bytes = base64.b64decode(b64)
                item_id = data.get('id', f"img-{int(datetime.utcnow().timestamp())}")
                filename = data.get('filename', f"{item_id}.webp")
                target = os.path.join(IMAGES_DIR, os.path.basename(filename))
                with open(target, 'wb') as f:
                    f.write(img_bytes)
                print(f"[Admin Server] Saved image: images/{os.path.basename(filename)} ({len(img_bytes)/1024:.1f} KB)")
                self.send_json(200, {
                    "success": True,
                    "filename": os.path.basename(filename),
                    "relativePath": f"images/{os.path.basename(filename)}",
                    "url": f"/images/{os.path.basename(filename)}",
                    "sizeBytes": len(img_bytes)
                })
            except Exception as e:
                self.send_error_json(500, str(e))
            return

        self.send_error_json(404, f"API endpoint not found: {pathname}")

def run(port=DEFAULT_PORT):
    server_address = ('', port)
    while port < 3020:
        try:
            httpd = socketserver.TCPServer(('', port), PromptVaultHandler)
            print("===================================================")
            print("   PromptVault — Admin Python Backend Server")
            print("===================================================")
            print(f" > Server running at: http://localhost:{port}/")
            print(f" > Project Root:      {REPO_ROOT}")
            print(f" > prompts.json:      {PROMPTS_FILE}")
            print(f" > app_config.json:   {CONFIG_FILE}")
            print(f" > Images folder:     {IMAGES_DIR}")
            print("===================================================")
            print("Ready for requests from browser!")
            httpd.serve_forever()
            break
        except OSError as e:
            if e.errno == 98 or e.errno == 10048:
                print(f"Port {port} in use, trying {port+1}...")
                port += 1
            else:
                raise e

if __name__ == '__main__':
    run()
