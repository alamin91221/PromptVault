// PromptVault Admin — Local Lightweight Development Server
// Zero dependencies: Uses pure Node.js standard libraries (http, fs, path, url)

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const REPO_ROOT = path.resolve(__dirname, '..');
const ADMIN_DIR = path.resolve(__dirname);
const IMAGES_DIR = path.join(REPO_ROOT, 'images');
const PROMPTS_FILE = path.join(REPO_ROOT, 'prompts.json');
const CONFIG_FILE = path.join(REPO_ROOT, 'app_config.json');

const DEFAULT_PORT = 3000;

// Ensure images directory exists
if (!fs.existsSync(IMAGES_DIR)) {
  fs.mkdirSync(IMAGES_DIR, { recursive: true });
}

const MIME_TYPES = {
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
  '.woff2': 'font/woff2',
  '.woff': 'font/woff'
};

function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    'Cache-Control': 'no-store, no-cache, must-revalidate'
  });
  res.end(JSON.stringify(data, null, 2));
}

function sendError(res, statusCode, message) {
  sendJson(res, statusCode, { error: true, message });
}

function parseJsonBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
      // 50MB safety limit for high-res images
      if (body.length > 50 * 1024 * 1024) {
        req.destroy();
        reject(new Error('Payload too large'));
      }
    });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (err) {
        reject(new Error('Invalid JSON format: ' + err.message));
      }
    });
    req.on('error', reject);
  });
}

function serveStaticFile(res, filePath) {
  if (!fs.existsSync(filePath)) {
    sendError(res, 404, 'File not found: ' + path.basename(filePath));
    return;
  }

  const stat = fs.statSync(filePath);
  if (stat.isDirectory()) {
    serveStaticFile(res, path.join(filePath, 'index.html'));
    return;
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME_TYPES[ext] || 'application/octet-stream';

  res.writeHead(200, {
    'Content-Type': contentType,
    'Content-Length': stat.size,
    'Access-Control-Allow-Origin': '*',
    'Cache-Control': ext === '.json' ? 'no-cache' : 'public, max-age=3600'
  });

  const stream = fs.createReadStream(filePath);
  stream.pipe(res);
}

const server = http.createServer(async (req, res) => {
  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400'
    });
    res.end();
    return;
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = parsedUrl.pathname;

  // -------------------------------------------------------------
  // API ENDPOINTS
  // -------------------------------------------------------------
  if (pathname.startsWith('/api/')) {
    // 1. Status API
    if (pathname === '/api/status' && req.method === 'GET') {
      let promptCount = 0;
      let categories = [];
      if (fs.existsSync(PROMPTS_FILE)) {
        try {
          const raw = JSON.parse(fs.readFileSync(PROMPTS_FILE, 'utf8'));
          promptCount = raw.items ? raw.items.length : 0;
          categories = raw.categories || [];
        } catch (_) {}
      }

      sendJson(res, 200, {
        status: 'ok',
        mode: 'local_server',
        projectDir: REPO_ROOT,
        hasPromptsFile: fs.existsSync(PROMPTS_FILE),
        hasConfigFile: fs.existsSync(CONFIG_FILE),
        promptCount,
        categoriesCount: categories.length,
        version: '1.0.0'
      });
      return;
    }

    // 2. Prompts API (GET / POST)
    if (pathname === '/api/prompts') {
      if (req.method === 'GET') {
        if (!fs.existsSync(PROMPTS_FILE)) {
          sendError(res, 404, 'prompts.json not found on disk');
          return;
        }
        try {
          const content = fs.readFileSync(PROMPTS_FILE, 'utf8');
          sendJson(res, 200, JSON.parse(content));
        } catch (err) {
          sendError(res, 500, 'Failed to read prompts.json: ' + err.message);
        }
        return;
      }

      if (req.method === 'POST') {
        try {
          const data = await parseJsonBody(req);
          if (!data || typeof data !== 'object') {
            sendError(res, 400, 'Invalid prompts payload');
            return;
          }

          // Automatically backup previous version
          if (fs.existsSync(PROMPTS_FILE)) {
            try {
              fs.copyFileSync(PROMPTS_FILE, PROMPTS_FILE + '.bak');
            } catch (_) {}
          }

          data.updated_at = new Date().toISOString();
          const formatted = JSON.stringify(data, null, 2);
          fs.writeFileSync(PROMPTS_FILE, formatted, 'utf8');

          console.log(`[Admin Server] Saved prompts.json (${(data.items || []).length} items)`);
          sendJson(res, 200, { success: true, message: 'prompts.json saved successfully to disk', updated_at: data.updated_at });
        } catch (err) {
          sendError(res, 500, 'Failed to save prompts.json: ' + err.message);
        }
        return;
      }
    }

    // 3. App Config API (GET / POST)
    if (pathname === '/api/config') {
      if (req.method === 'GET') {
        if (!fs.existsSync(CONFIG_FILE)) {
          sendError(res, 404, 'app_config.json not found on disk');
          return;
        }
        try {
          const content = fs.readFileSync(CONFIG_FILE, 'utf8');
          sendJson(res, 200, JSON.parse(content));
        } catch (err) {
          sendError(res, 500, 'Failed to read app_config.json: ' + err.message);
        }
        return;
      }

      if (req.method === 'POST') {
        try {
          const data = await parseJsonBody(req);
          if (!data || typeof data !== 'object') {
            sendError(res, 400, 'Invalid config payload');
            return;
          }

          // Backup
          if (fs.existsSync(CONFIG_FILE)) {
            try {
              fs.copyFileSync(CONFIG_FILE, CONFIG_FILE + '.bak');
            } catch (_) {}
          }

          const formatted = JSON.stringify(data, null, 2);
          fs.writeFileSync(CONFIG_FILE, formatted, 'utf8');

          console.log('[Admin Server] Saved app_config.json');
          sendJson(res, 200, { success: true, message: 'app_config.json saved successfully to disk' });
        } catch (err) {
          sendError(res, 500, 'Failed to save app_config.json: ' + err.message);
        }
        return;
      }
    }

    // 4. Image Upload API (POST /api/upload-image)
    if (pathname === '/api/upload-image' && req.method === 'POST') {
      try {
        const data = await parseJsonBody(req);
        if (!data.base64) {
          sendError(res, 400, 'Missing base64 image data');
          return;
        }

        const id = data.id || `img-${Date.now()}`;
        const filename = data.filename || `${id}.webp`;
        const sanitizedFilename = path.basename(filename);
        const targetPath = path.join(IMAGES_DIR, sanitizedFilename);

        const cleanBase64 = data.base64.replace(/^data:image\/\w+;base64,/, '');
        const buffer = Buffer.from(cleanBase64, 'base64');

        fs.writeFileSync(targetPath, buffer);
        console.log(`[Admin Server] Saved image: images/${sanitizedFilename} (${(buffer.length / 1024).toFixed(1)} KB)`);

        sendJson(res, 200, {
          success: true,
          filename: sanitizedFilename,
          relativePath: `images/${sanitizedFilename}`,
          url: `/images/${sanitizedFilename}`,
          sizeBytes: buffer.length
        });
      } catch (err) {
        sendError(res, 500, 'Failed to upload image: ' + err.message);
      }
      return;
    }

    sendError(res, 404, 'API endpoint not found');
    return;
  }

  // -------------------------------------------------------------
  // STATIC FILE SERVING
  // -------------------------------------------------------------
  // Root and Admin Panel
  if (pathname === '/' || pathname === '/admin' || pathname === '/admin/') {
    serveStaticFile(res, path.join(ADMIN_DIR, 'index.html'));
    return;
  }

  // Files inside images/ directory
  if (pathname.startsWith('/images/')) {
    const relFile = pathname.replace(/^\/images\//, '');
    serveStaticFile(res, path.join(IMAGES_DIR, path.normalize(relFile)));
    return;
  }

  // Direct root JSON access
  if (pathname === '/prompts.json') {
    serveStaticFile(res, PROMPTS_FILE);
    return;
  }
  if (pathname === '/app_config.json') {
    serveStaticFile(res, CONFIG_FILE);
    return;
  }

  // Admin assets (styles.css, app.js, or files inside /admin/...)
  let adminFilePath = path.join(ADMIN_DIR, pathname.replace(/^\/admin\//, '').replace(/^\//, ''));
  if (fs.existsSync(adminFilePath)) {
    serveStaticFile(res, adminFilePath);
    return;
  }

  // Try from repo root as fallback
  let repoFilePath = path.join(REPO_ROOT, pathname.replace(/^\//, ''));
  if (fs.existsSync(repoFilePath)) {
    serveStaticFile(res, repoFilePath);
    return;
  }

  sendError(res, 404, 'Not found: ' + pathname);
});

function startServer(port = DEFAULT_PORT) {
  server.listen(port, () => {
    console.log('===================================================');
    console.log('   PromptVault — Admin Local Backend Server');
    console.log('===================================================');
    console.log(` > Server running at: http://localhost:${port}/`);
    console.log(` > Project Root:      ${REPO_ROOT}`);
    console.log(` > prompts.json:      ${PROMPTS_FILE}`);
    console.log(` > app_config.json:   ${CONFIG_FILE}`);
    console.log(` > Images folder:     ${IMAGES_DIR}`);
    console.log('===================================================');
    console.log('Ready for requests from browser!');
  });

  server.on('error', err => {
    if (err.code === 'EADDRINUSE') {
      console.log(`Port ${port} is in use, trying port ${port + 1}...`);
      startServer(port + 1);
    } else {
      console.error('Server error:', err);
    }
  });
}

startServer();
