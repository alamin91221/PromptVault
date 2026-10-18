// PromptVault Studio — Admin Dashboard & Project Controller
// Multi-Mode: Local Server (Live Disk Sync), GitHub Cloud API, and Offline Standalone

const STORAGE_KEYS = {
  OWNER: 'pv_gh_owner',
  REPO: 'pv_gh_repo',
  BRANCH: 'pv_gh_branch',
  TOKEN: 'pv_gh_token',
  THEME: 'pv_admin_theme',
  LOCAL_PROMPTS: 'pv_local_prompts',
  LOCAL_CONFIG: 'pv_local_config'
};

const DEFAULT_PROMPTS = {
  schema_version: 1,
  updated_at: new Date().toISOString(),
  categories: ["Portrait", "Anime", "Nature", "Cyberpunk", "3D", "Fantasy"],
  items: [
    {
      id: "pv-001",
      title: "Ethereal Beauty",
      image_url: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=900&q=80",
      prompt: "Ultra detailed portrait of a beautiful woman with blue hair, flowers and butterflies, soft cinematic lighting, dreamy atmosphere, shallow depth of field, 8k, photorealistic, studio lighting, highly detailed, ornate, fantasy art.",
      negative_prompt: "low quality, blurry, bad anatomy, extra fingers, text, watermark, signature",
      model: "Midjourney v7",
      category: "Portrait",
      tags: ["portrait", "flowers", "butterflies", "ethereal", "fantasy"],
      is_premium: true,
      created_at: "2026-10-01T10:00:00Z"
    },
    {
      id: "pv-002",
      title: "Neon Samurai 2099",
      image_url: "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=900&q=80",
      prompt: "Cyberpunk samurai warrior standing in rain-slicked Tokyo street, glowing magenta and cyan neon katana, holographic armor reflections, volumetric fog, hyperdetailed cinematic unreal engine 5 render, anamorphic lens flare.",
      negative_prompt: "cartoon, oversaturated, deformed hands, low resolution, bad lighting",
      model: "Stable Diffusion 3.5",
      category: "Cyberpunk",
      tags: ["cyberpunk", "samurai", "neon", "katana", "tokyo"],
      is_premium: false,
      created_at: "2026-10-05T14:30:00Z"
    },
    {
      id: "pv-003",
      title: "Anime Starlight Sorceress",
      image_url: "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=900&q=80",
      prompt: "Vibrant anime girl sorceress casting celestial magic in a cosmic night sky, pastel pink flowing hair, glowing star particles, Makoto Shinkai style, masterwork illustration, crisp lines, rich colors, 4k wallpaper.",
      negative_prompt: "bad hands, missing limbs, dark blurry background, sketch",
      model: "Niji 6",
      category: "Anime",
      tags: ["anime", "magic", "stars", "celestial", "illustration"],
      is_premium: false,
      created_at: "2026-10-08T09:15:00Z"
    },
    {
      id: "pv-004",
      title: "Astronaut in Cosmic Wilderness",
      image_url: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=900&q=80",
      prompt: "A lone futuristic astronaut standing on an alien cliff overlooking a swirling purple galaxy and glowing twin moons, bioluminescent alien flora, cinematic sci-fi film still, NASA realism mixed with surreal wonder, 8k resolution.",
      negative_prompt: "earth, standard astronaut suit, blurry, low res, watermark",
      model: "DALL-E 3",
      category: "Nature",
      tags: ["astronaut", "space", "galaxy", "sci-fi", "alien"],
      is_premium: true,
      created_at: "2026-10-10T16:45:00Z"
    },
    {
      id: "pv-005",
      title: "Cyberpunk Synthwave Cat",
      image_url: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=80",
      prompt: "A cute fluffy cat wearing glowing futuristic neon pink sunglasses, sitting on a cyberpunk rooftop, retro 80s synthwave aesthetic, vaporwave neon palette, sharp focus, detailed fur texture, octane render.",
      negative_prompt: "ugly, deformed eyes, washed out, low contrast, text",
      model: "Midjourney v7",
      category: "Cyberpunk",
      tags: ["cat", "cyberpunk", "synthwave", "glasses", "neon"],
      is_premium: false,
      created_at: "2026-10-12T11:20:00Z"
    },
    {
      id: "pv-006",
      title: "Mystic Pagoda in Mountain Fog",
      image_url: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=900&q=80",
      prompt: "Ancient oriental pagoda nestled amidst jagged misty mountain peaks at dawn, cherry blossom petals drifting on breeze, golden sunlight piercing through morning fog, traditional watercolor style with modern digital precision, tranquil atmosphere.",
      negative_prompt: "modern buildings, roads, electric poles, saturated unnatural colors",
      model: "Flux.1 Dev",
      category: "Nature",
      tags: ["nature", "pagoda", "mountains", "dawn", "peaceful"],
      is_premium: false,
      created_at: "2026-10-15T08:00:00Z"
    }
  ]
};

const DEFAULT_CONFIG = {
  schema_version: 1,
  app: {
    maintenance_mode: false,
    maintenance_message: "PromptVault is undergoing scheduled maintenance. Please check back shortly!",
    min_version_code: 1,
    update_url: "https://play.google.com/store/apps/details?id=com.nilasharafi.promptvault",
    force_update: false
  },
  ads: {
    master_enabled: true,
    use_test_ads: true,
    banner: {
      enabled: true,
      unit_id: "ca-app-pub-3940256099942544/6300978111",
      screens: ["home", "detail"],
      position: "bottom"
    },
    interstitial: {
      enabled: true,
      unit_id: "ca-app-pub-3940256099942544/1033173712",
      show_every_n_prompt_views: 3,
      min_interval_seconds: 60,
      max_per_session: 8,
      skip_for_first_n_actions: 2
    },
    native: {
      enabled: true,
      unit_id: "ca-app-pub-3940256099942544/2247696110",
      start_after_items: 4,
      every_n_items: 7
    },
    rewarded: {
      enabled: true,
      unit_id: "ca-app-pub-3940256099942544/5224354917"
    }
  },
  premium: {
    unlock_duration_hours: 48,
    unlock_scope: "single",
    ads_required_per_unlock: 1
  }
};

// Global App State
let state = {
  isLocalServer: false,
  owner: localStorage.getItem(STORAGE_KEYS.OWNER) || 'alamin91221',
  repo: localStorage.getItem(STORAGE_KEYS.REPO) || 'PromptVault',
  branch: localStorage.getItem(STORAGE_KEYS.BRANCH) || 'main',
  token: localStorage.getItem(STORAGE_KEYS.TOKEN) || '',
  promptsData: loadInitialPrompts(),
  promptsSha: null,
  configData: loadInitialConfig(),
  configSha: null,
  pendingWebpBase64: null,
  pendingWebpDataUrl: null,
  pendingWebpSize: 0,
  pendingJsonImportType: null
};

function loadInitialPrompts() {
  const saved = localStorage.getItem(STORAGE_KEYS.LOCAL_PROMPTS);
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      if (parsed && Array.isArray(parsed.items)) return parsed;
    } catch (_) {}
  }
  return JSON.parse(JSON.stringify(DEFAULT_PROMPTS));
}

function loadInitialConfig() {
  const saved = localStorage.getItem(STORAGE_KEYS.LOCAL_CONFIG);
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      if (parsed && parsed.app && parsed.ads) return parsed;
    } catch (_) {}
  }
  return JSON.parse(JSON.stringify(DEFAULT_CONFIG));
}

function saveLocalState() {
  try {
    localStorage.setItem(STORAGE_KEYS.LOCAL_PROMPTS, JSON.stringify(state.promptsData));
    localStorage.setItem(STORAGE_KEYS.LOCAL_CONFIG, JSON.stringify(state.configData));
  } catch (e) {
    console.warn('LocalStorage save failed:', e);
  }
}

// UTF-8 safe Base64 encoder/decoder (supports Bengali text & emojis)
function utf8ToBase64(str) {
  return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (_, p1) => String.fromCharCode('0x' + p1)));
}

function base64ToUtf8(str) {
  const cleanStr = str.replace(/[\r\n\s]/g, '');
  return decodeURIComponent(Array.prototype.map.call(atob(cleanStr), c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
}

function showToast(message, type = 'success') {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  const icon = type === 'success' ? '✓' : (type === 'error' ? '⚠️' : 'ℹ️');
  toast.innerHTML = `<span style="font-size:1.1rem">${icon}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// GitHub REST API wrapper
async function ghRequest(path, options = {}) {
  if (!state.token || !state.owner || !state.repo) {
    throw new Error('GitHub credentials not configured.');
  }

  const url = `https://api.github.com/repos/${state.owner}/${state.repo}/contents/${path}?ref=${state.branch}&t=${Date.now()}`;
  const headers = {
    'Accept': 'application/vnd.github.v3+json',
    'Authorization': `Bearer ${state.token}`,
    ...options.headers
  };

  const response = await fetch(url, { ...options, headers });
  if (response.status === 404) return null;
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || `GitHub error: ${response.status} ${response.statusText}`);
  }
  return response.json();
}

async function ghPutFile(path, contentBase64, sha, commitMessage) {
  const url = `https://api.github.com/repos/${state.owner}/${state.repo}/contents/${path}`;
  
  // If sha wasn't provided, check if file exists on GitHub to obtain sha
  if (!sha) {
    try {
      const existing = await ghRequest(path);
      if (existing && existing.sha) {
        sha = existing.sha;
      }
    } catch (_) {}
  }

  const body = {
    message: commitMessage,
    content: contentBase64,
    branch: state.branch
  };
  if (sha) body.sha = sha;

  const response = await fetch(url, {
    method: 'PUT',
    headers: {
      'Accept': 'application/vnd.github.v3+json',
      'Authorization': `Bearer ${state.token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || `Failed to commit ${path}`);
  }
  return response.json();
}

// Canvas-based WebP conversion & compression
function convertImageToWebp(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        const maxWidth = 1600;
        let width = img.width;
        let height = img.height;

        if (width > maxWidth) {
          height = Math.round((height * maxWidth) / width);
          width = maxWidth;
        }

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);

        const dataUrl = canvas.toDataURL('image/webp', 0.85);
        const base64 = dataUrl.split(',')[1];
        resolve({ dataUrl, base64, sizeBytes: Math.round(base64.length * 0.75) });
      };
      img.onerror = reject;
      img.src = e.target.result;
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

// ---------------------------------------------------------------------
// Server Connection & Sync Logic
// ---------------------------------------------------------------------
async function checkServerConnection() {
  const statusDot = document.getElementById('statusDot');
  const label = document.getElementById('connectionLabel');
  const infoMode = document.getElementById('infoMode');
  const subTitleMode = document.getElementById('subTitleMode');
  const offlineBanner = document.getElementById('offlineBanner');

  try {
    const res = await fetch('/api/status', { cache: 'no-store' });
    if (res.ok) {
      const data = await res.json();
      if (data.status === 'ok') {
        state.isLocalServer = true;
        statusDot.className = 'status-dot connected';
        label.textContent = '🟢 Local Live Sync';
        if (infoMode) infoMode.textContent = '🟢 Local Disk Server (Live Sync)';
        if (subTitleMode) subTitleMode.textContent = 'Live Project Sync — Direct Disk Access';
        if (offlineBanner) offlineBanner.style.display = 'none';

        // Load fresh data directly from server
        await loadFromLocalServer();
        return;
      }
    }
  } catch (_) {
    // Not running on local server
  }

  state.isLocalServer = false;

  // Check if GitHub credentials configured
  if (state.token && state.owner && state.repo) {
    statusDot.className = 'status-dot cloud';
    label.textContent = `🟣 ${state.owner}/${state.repo}`;
    if (infoMode) infoMode.textContent = `🟣 GitHub Cloud Mode (${state.owner}/${state.repo})`;
    if (subTitleMode) subTitleMode.textContent = `GitHub Cloud Connected: ${state.branch}`;
    if (offlineBanner) offlineBanner.style.display = 'none';
    syncFromGitHub();
  } else {
    statusDot.className = 'status-dot disconnected';
    label.textContent = window.location.protocol === 'file:' ? '🟡 Offline (file://)' : 'Local Browser';
    if (infoMode) infoMode.textContent = window.location.protocol === 'file:' ? '🟡 Offline File Mode' : 'Local Storage Only';
    if (subTitleMode) subTitleMode.textContent = 'Prompt Gallery & Remote Control';
    if (offlineBanner && window.location.protocol === 'file:') {
      offlineBanner.style.display = 'flex';
    }
  }
}

async function loadFromLocalServer() {
  try {
    // 1. Prompts
    const pRes = await fetch('/api/prompts', { cache: 'no-store' });
    if (pRes.ok) {
      state.promptsData = await pRes.json();
      saveLocalState();
    }

    // 2. Config
    const cRes = await fetch('/api/config', { cache: 'no-store' });
    if (cRes.ok) {
      state.configData = await cRes.json();
      saveLocalState();
    }

    renderDashboard();
    renderPosts();
    renderConfigForm();
  } catch (err) {
    console.error('Failed to load from local server:', err);
  }
}

async function syncFromGitHub() {
  if (!state.token || !state.owner || !state.repo) {
    showToast('GitHub credentials not set. Open 🔑 Settings to configure.', 'error');
    return;
  }

  try {
    showToast('Fetching latest files from GitHub...', 'info');

    // 1. Fetch prompts.json
    const promptsFile = await ghRequest('prompts.json');
    if (promptsFile && promptsFile.content) {
      state.promptsSha = promptsFile.sha;
      const decoded = base64ToUtf8(promptsFile.content);
      state.promptsData = JSON.parse(decoded);
      saveLocalState();
    }

    // 2. Fetch app_config.json
    const configFile = await ghRequest('app_config.json');
    if (configFile && configFile.content) {
      state.configSha = configFile.sha;
      const decodedConfig = base64ToUtf8(configFile.content);
      state.configData = JSON.parse(decodedConfig);
      saveLocalState();
    }

    renderDashboard();
    renderPosts();
    renderConfigForm();
    showToast('Synced successfully with GitHub repository!');
  } catch (err) {
    console.error(err);
    showToast(err.message, 'error');
  }
}

// ---------------------------------------------------------------------
// UI Renderers
// ---------------------------------------------------------------------
function renderDashboard() {
  const items = state.promptsData.items || [];
  const total = items.length;
  const premium = items.filter(i => i.is_premium).length;
  const free = total - premium;
  const cats = state.promptsData.categories || [];

  document.getElementById('statTotalPosts').textContent = total;
  document.getElementById('statPremiumPosts').textContent = premium;
  document.getElementById('statFreePosts').textContent = free;
  document.getElementById('statCategoriesCount').textContent = cats.length;
  document.getElementById('tabPostsCount').textContent = total;

  const repoLabel = state.isLocalServer ? 'Local Project (Disk)' : (state.owner ? `${state.owner}/${state.repo}` : 'Local Browser');
  document.getElementById('infoRepoName').textContent = repoLabel;
  document.getElementById('infoBranch').textContent = state.branch || 'main';
  document.getElementById('infoLastUpdated').textContent = state.promptsData.updated_at ? new Date(state.promptsData.updated_at).toLocaleString() : 'Recently';

  if (state.configData && state.configData.ads) {
    document.getElementById('infoAdsStatus').textContent = state.configData.ads.master_enabled ? 'Active (Live)' : 'Disabled (Kill-switch)';
    document.getElementById('infoAdsStatus').style.color = state.configData.ads.master_enabled ? 'var(--brand-green)' : 'var(--brand-danger)';
  }

  // Category breakdown list
  const catList = document.getElementById('categoryBreakdownList');
  if (catList) {
    catList.innerHTML = '';
    const breakdown = {};
    cats.forEach(c => { breakdown[c] = 0; });
    items.forEach(i => {
      breakdown[i.category] = (breakdown[i.category] || 0) + 1;
    });

    const entries = Object.entries(breakdown);
    if (entries.length === 0) {
      catList.innerHTML = '<div style="color:var(--text-muted);font-size:0.85rem">No categories defined yet.</div>';
    } else {
      for (const [cat, count] of entries) {
        const row = document.createElement('div');
        row.className = 'cat-row';
        row.innerHTML = `<strong>${escapeHtml(cat)}</strong><span class="cat-chip">${count} prompt${count === 1 ? '' : 's'}</span>`;
        catList.appendChild(row);
      }
    }
  }
}

function renderPosts() {
  const container = document.getElementById('postsListContainer');
  const categorySelect = document.getElementById('filterCategorySelect');
  if (!container || !categorySelect) return;

  const searchInput = document.getElementById('postSearchInput');
  const search = searchInput ? searchInput.value.trim().toLowerCase() : '';
  const clearBtn = document.getElementById('btnClearSearch');
  if (clearBtn) clearBtn.style.display = search ? 'block' : 'none';

  const selectedCat = categorySelect.value || 'All';
  const premiumFilter = document.getElementById('filterPremiumSelect')?.value || 'All';
  const sortBy = document.getElementById('sortSelect')?.value || 'newest';

  // Repopulate category filter options cleanly
  const currentCatVal = categorySelect.value;
  categorySelect.innerHTML = '<option value="All">All Categories</option>';
  (state.promptsData.categories || []).forEach(cat => {
    const opt = document.createElement('option');
    opt.value = cat;
    opt.textContent = cat;
    if (cat === currentCatVal) opt.selected = true;
    categorySelect.appendChild(opt);
  });

  let items = [...(state.promptsData.items || [])];

  // Filter
  items = items.filter(item => {
    const matchCat = selectedCat === 'All' || item.category === selectedCat;
    const matchPrem = premiumFilter === 'All' || (premiumFilter === 'premium' ? item.is_premium : !item.is_premium);
    const matchSearch = !search ||
      (item.title && item.title.toLowerCase().includes(search)) ||
      (item.prompt && item.prompt.toLowerCase().includes(search)) ||
      (item.model && item.model.toLowerCase().includes(search)) ||
      (item.tags && item.tags.some(t => t.toLowerCase().includes(search)));
    return matchCat && matchPrem && matchSearch;
  });

  // Sort
  if (sortBy === 'oldest') {
    items.sort((a, b) => new Date(a.created_at || 0) - new Date(b.created_at || 0));
  } else if (sortBy === 'title') {
    items.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
  } else {
    items.sort((a, b) => new Date(b.created_at || 0) - new Date(a.created_at || 0));
  }

  container.innerHTML = '';
  if (items.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px; color: var(--text-secondary); background: var(--bg-secondary); border-radius: var(--radius-lg); border: 1px dashed var(--border-color);">
        <p style="font-size: 1.1rem; font-weight: 600; margin-bottom: 8px;">No prompts found</p>
        <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 16px;">Try adjusting your search query or filters, or add a new prompt.</p>
        <button class="btn btn-sm btn-primary" onclick="openAddPostModal()">+ Add New Prompt</button>
      </div>
    `;
    return;
  }

  items.forEach(item => {
    const card = document.createElement('div');
    card.className = 'post-card';
    
    // Resolve display image url
    let displayImgUrl = item.image_url;
    if (state.isLocalServer && displayImgUrl && !displayImgUrl.startsWith('http') && !displayImgUrl.startsWith('data:')) {
      displayImgUrl = '/' + displayImgUrl.replace(/^\//, '');
    }

    const tagsHtml = (item.tags || []).map(t => `<span class="post-tag">#${escapeHtml(t)}</span>`).join('');

    card.innerHTML = `
      <div class="post-card-thumb">
        <img src="${escapeHtml(displayImgUrl)}" alt="${escapeHtml(item.title)}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=400&q=80'">
        <span class="post-badge ${item.is_premium ? 'premium' : 'free'}">
          ${item.is_premium ? '👑 Premium' : 'Free'}
        </span>
      </div>
      <div class="post-card-body">
        <h4 class="post-title">${escapeHtml(item.title)}</h4>
        <div class="post-category">${escapeHtml(item.category || 'General')} • ${escapeHtml(item.model || 'AI')}</div>
        <p class="post-prompt-preview" title="Click to copy prompt" data-prompt="${escapeHtml(item.prompt)}">${escapeHtml(item.prompt)}</p>
        <div class="post-tags-list">${tagsHtml}</div>
        <div class="post-card-actions">
          <button class="btn btn-sm btn-secondary btn-edit-post" data-id="${item.id}" type="button">✏️ Edit</button>
          <button class="btn btn-sm btn-outline btn-copy-prompt" data-prompt="${escapeHtml(item.prompt)}" type="button" title="Copy Prompt Text">📋 Copy</button>
          <button class="btn btn-sm btn-outline btn-toggle-prem" data-id="${item.id}" type="button">${item.is_premium ? 'Make Free' : '👑 Lock'}</button>
          <button class="btn btn-sm btn-danger btn-delete-post" data-id="${item.id}" type="button" title="Delete Prompt">🗑️</button>
        </div>
      </div>
    `;
    container.appendChild(card);
  });

  // Attach card event listeners
  container.querySelectorAll('.btn-edit-post').forEach(b => {
    b.addEventListener('click', () => openEditPostModal(b.dataset.id));
  });
  container.querySelectorAll('.btn-toggle-prem').forEach(b => {
    b.addEventListener('click', () => togglePostPremium(b.dataset.id));
  });
  container.querySelectorAll('.btn-delete-post').forEach(b => {
    b.addEventListener('click', () => deletePost(b.dataset.id));
  });
  container.querySelectorAll('.btn-copy-prompt').forEach(b => {
    b.addEventListener('click', (e) => copyTextToClipboard(b.dataset.prompt, 'Prompt text copied to clipboard!'));
  });
  container.querySelectorAll('.post-prompt-preview').forEach(p => {
    p.addEventListener('click', () => copyTextToClipboard(p.dataset.prompt, 'Prompt text copied to clipboard!'));
  });
}

function copyTextToClipboard(text, successMsg) {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(() => {
      showToast(successMsg, 'success');
    }).catch(() => fallbackCopy(text, successMsg));
  } else {
    fallbackCopy(text, successMsg);
  }
}

function fallbackCopy(text, successMsg) {
  const ta = document.createElement('textarea');
  ta.value = text;
  document.body.appendChild(ta);
  ta.select();
  try {
    document.execCommand('copy');
    showToast(successMsg, 'success');
  } catch (_) {
    showToast('Failed to copy', 'error');
  }
  document.body.removeChild(ta);
}

// ---------------------------------------------------------------------
// Add / Edit Post Modal Logic
// ---------------------------------------------------------------------
function openAddPostModal() {
  document.getElementById('modalPostTitle').textContent = 'Add New Prompt';
  document.getElementById('editPostId').value = '';
  document.getElementById('postTitleInput').value = '';
  document.getElementById('postPromptInput').value = '';
  document.getElementById('promptCharCount').textContent = '0';
  document.getElementById('postNegativePromptInput').value = '';
  document.getElementById('postModelInput').value = '';
  document.getElementById('postTagsInput').value = '';
  document.getElementById('postPremiumToggle').checked = false;
  document.getElementById('postImageUrlInput').value = '';

  state.pendingWebpBase64 = null;
  state.pendingWebpDataUrl = null;
  state.pendingWebpSize = 0;

  // Reset image tabs to upload mode cleanly
  document.getElementById('tabUploadFile').classList.add('active');
  document.getElementById('tabImageUrl').classList.remove('active');
  document.getElementById('uploadFileArea').style.display = 'block';
  document.getElementById('imageUrlArea').style.display = 'none';
  document.getElementById('dropzoneContent').style.display = 'block';
  document.getElementById('imagePreviewContainer').style.display = 'none';
  document.getElementById('urlPreviewContainer').style.display = 'none';

  populateCategorySelect();
  document.getElementById('postModal').classList.add('active');
  document.getElementById('postTitleInput').focus();
}

function openEditPostModal(id) {
  const item = state.promptsData.items.find(i => i.id === id);
  if (!item) return;

  document.getElementById('modalPostTitle').textContent = 'Edit Prompt';
  document.getElementById('editPostId').value = item.id;
  document.getElementById('postTitleInput').value = item.title;
  document.getElementById('postPromptInput').value = item.prompt;
  document.getElementById('promptCharCount').textContent = item.prompt.length;
  document.getElementById('postNegativePromptInput').value = item.negative_prompt || '';
  document.getElementById('postModelInput').value = item.model || '';
  document.getElementById('postTagsInput').value = (item.tags || []).join(', ');
  document.getElementById('postPremiumToggle').checked = !!item.is_premium;

  state.pendingWebpBase64 = null;
  state.pendingWebpDataUrl = null;

  // Show URL tab by default for editing
  document.getElementById('tabImageUrl').classList.add('active');
  document.getElementById('tabUploadFile').classList.remove('active');
  document.getElementById('imageUrlArea').style.display = 'block';
  document.getElementById('uploadFileArea').style.display = 'none';
  document.getElementById('postImageUrlInput').value = item.image_url;

  // Show preview of current image
  let previewUrl = item.image_url;
  if (state.isLocalServer && previewUrl && !previewUrl.startsWith('http') && !previewUrl.startsWith('data:')) {
    previewUrl = '/' + previewUrl.replace(/^\//, '');
  }
  document.getElementById('urlPreviewImg').src = previewUrl;
  document.getElementById('urlPreviewContainer').style.display = 'flex';

  populateCategorySelect(item.category);
  document.getElementById('postModal').classList.add('active');
}

function populateCategorySelect(selected = '') {
  const select = document.getElementById('postCategorySelect');
  if (!select) return;
  select.innerHTML = '';
  const cats = state.promptsData.categories || ['Portrait', 'Anime', 'Nature', 'Cyberpunk'];
  
  if (selected && !cats.includes(selected)) {
    cats.push(selected);
  }

  cats.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c;
    opt.textContent = c;
    if (c === selected) opt.selected = true;
    select.appendChild(opt);
  });
}

async function handleSavePost(e) {
  if (e) e.preventDefault();
  const id = document.getElementById('editPostId').value || `pv-${Date.now()}`;
  const title = document.getElementById('postTitleInput').value.trim();
  const prompt = document.getElementById('postPromptInput').value.trim();
  const negativePrompt = document.getElementById('postNegativePromptInput').value.trim();
  const category = document.getElementById('postCategorySelect').value;
  const model = document.getElementById('postModelInput').value.trim();
  const tags = document.getElementById('postTagsInput').value.split(',').map(t => t.trim()).filter(Boolean);
  const isPremium = document.getElementById('postPremiumToggle').checked;

  let imageUrl = document.getElementById('postImageUrlInput').value.trim();

  // If a new image was uploaded via dropzone
  if (state.pendingWebpBase64) {
    if (state.isLocalServer) {
      try {
        showToast('Saving WebP image directly to images/ ...', 'info');
        const upRes = await fetch('/api/upload-image', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ id, base64: state.pendingWebpBase64 })
        });
        if (!upRes.ok) throw new Error('Image upload failed');
        const upData = await upRes.json();
        imageUrl = upData.relativePath || `images/${id}.webp`;
      } catch (err) {
        showToast(`Image upload failed: ${err.message}`, 'error');
        return;
      }
    } else if (state.token && state.owner && state.repo) {
      try {
        showToast('Uploading WebP image to GitHub repo...', 'info');
        const imgPath = `images/${id}.webp`;
        await ghPutFile(imgPath, state.pendingWebpBase64, null, `admin: upload image ${imgPath}`);
        imageUrl = `https://raw.githubusercontent.com/${state.owner}/${state.repo}/${state.branch}/${imgPath}`;
      } catch (err) {
        showToast(`Image upload failed: ${err.message}`, 'error');
        return;
      }
    } else {
      // In standalone fallback mode, use WebP data URL
      imageUrl = state.pendingWebpDataUrl;
    }
  }

  if (!imageUrl) {
    showToast('Please provide an image URL or upload an image file.', 'error');
    return;
  }

  const existingIdx = state.promptsData.items.findIndex(i => i.id === id);
  const postItem = {
    id,
    title,
    image_url: imageUrl,
    prompt,
    negative_prompt: negativePrompt,
    model: model || 'Midjourney v7',
    category: category || 'General',
    tags,
    is_premium: isPremium,
    created_at: existingIdx >= 0 ? state.promptsData.items[existingIdx].created_at : new Date().toISOString()
  };

  if (existingIdx >= 0) {
    state.promptsData.items[existingIdx] = postItem;
  } else {
    state.promptsData.items.unshift(postItem);
  }

  state.promptsData.updated_at = new Date().toISOString();
  saveLocalState();

  // Persist to Server or GitHub
  if (state.isLocalServer) {
    try {
      const res = await fetch('/api/prompts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(state.promptsData)
      });
      if (!res.ok) throw new Error('Failed to save prompts.json on disk');
      showToast(`Prompt "${title}" saved directly to project disk!`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  } else if (state.token && state.owner && state.repo) {
    try {
      showToast('Saving prompts.json to GitHub...', 'info');
      const jsonStr = JSON.stringify(state.promptsData, null, 2);
      const result = await ghPutFile(
        'prompts.json',
        utf8ToBase64(jsonStr),
        state.promptsSha,
        `admin: ${existingIdx >= 0 ? 'update' : 'add'} prompt "${title}"`
      );
      state.promptsSha = result.content.sha;
      showToast('Prompt saved and committed to GitHub!');
    } catch (err) {
      showToast(`GitHub save error: ${err.message}`, 'error');
    }
  } else {
    showToast(`Prompt "${title}" saved in local browser storage!`);
  }

  document.getElementById('postModal').classList.remove('active');
  renderDashboard();
  renderPosts();
}

async function togglePostPremium(id) {
  const item = state.promptsData.items.find(i => i.id === id);
  if (!item) return;
  item.is_premium = !item.is_premium;
  state.promptsData.updated_at = new Date().toISOString();
  saveLocalState();

  if (state.isLocalServer) {
    try {
      await fetch('/api/prompts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(state.promptsData)
      });
      showToast(`"${item.title}" is now ${item.is_premium ? '👑 Premium' : '✨ Free'} (Saved to disk)`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  } else if (state.token && state.owner && state.repo) {
    try {
      const jsonStr = JSON.stringify(state.promptsData, null, 2);
      const result = await ghPutFile(
        'prompts.json',
        utf8ToBase64(jsonStr),
        state.promptsSha,
        `admin: toggle premium for "${item.title}"`
      );
      state.promptsSha = result.content.sha;
      showToast(`Committed: "${item.title}" is now ${item.is_premium ? '👑 Premium' : '✨ Free'}`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  } else {
    showToast(`Updated "${item.title}" to ${item.is_premium ? '👑 Premium' : '✨ Free'}`);
  }

  renderDashboard();
  renderPosts();
}

async function deletePost(id) {
  const item = state.promptsData.items.find(i => i.id === id);
  if (!item) return;
  if (!confirm(`Are you sure you want to delete prompt "${item.title}"?`)) return;

  state.promptsData.items = state.promptsData.items.filter(i => i.id !== id);
  state.promptsData.updated_at = new Date().toISOString();
  saveLocalState();

  if (state.isLocalServer) {
    try {
      await fetch('/api/prompts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(state.promptsData)
      });
      showToast(`Deleted "${item.title}" from project disk.`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  } else if (state.token && state.owner && state.repo) {
    try {
      const jsonStr = JSON.stringify(state.promptsData, null, 2);
      const result = await ghPutFile(
        'prompts.json',
        utf8ToBase64(jsonStr),
        state.promptsSha,
        `admin: delete prompt "${item.title}"`
      );
      state.promptsSha = result.content.sha;
      showToast(`Deleted "${item.title}" from GitHub.`);
    } catch (err) {
      showToast(err.message, 'error');
    }
  } else {
    showToast(`Deleted "${item.title}".`);
  }

  renderDashboard();
  renderPosts();
}

// ---------------------------------------------------------------------
// Config Form & Raw JSON Logic
// ---------------------------------------------------------------------
function renderConfigForm() {
  if (!state.configData) {
    state.configData = JSON.parse(JSON.stringify(DEFAULT_CONFIG));
  }
  const c = state.configData;

  const mMode = document.getElementById('cfgMaintenanceMode');
  if (mMode) mMode.checked = !!c.app?.maintenance_mode;
  const fUpd = document.getElementById('cfgForceUpdate');
  if (fUpd) fUpd.checked = !!c.app?.force_update;
  const mMsg = document.getElementById('cfgMaintenanceMsg');
  if (mMsg) mMsg.value = c.app?.maintenance_message || '';
  const mVer = document.getElementById('cfgMinVersionCode');
  if (mVer) mVer.value = c.app?.min_version_code || 1;
  const uUrl = document.getElementById('cfgUpdateUrl');
  if (uUrl) uUrl.value = c.app?.update_url || '';

  const mAds = document.getElementById('cfgMasterAds');
  if (mAds) mAds.checked = !!c.ads?.master_enabled;
  const tAds = document.getElementById('cfgUseTestAds');
  if (tAds) tAds.checked = !!c.ads?.use_test_ads;
  const uDur = document.getElementById('cfgUnlockDuration');
  if (uDur) uDur.value = c.premium?.unlock_duration_hours || 48;
  const uScope = document.getElementById('cfgUnlockScope');
  if (uScope) uScope.value = c.premium?.unlock_scope || 'single';
  const aReq = document.getElementById('cfgAdsRequiredPerUnlock');
  if (aReq) aReq.value = c.premium?.ads_required_per_unlock || 1;

  const bEn = document.getElementById('cfgBannerEnabled');
  if (bEn) bEn.checked = !!c.ads?.banner?.enabled;
  const bPos = document.getElementById('cfgBannerPosition');
  if (bPos) bPos.value = c.ads?.banner?.position || 'bottom';
  const bId = document.getElementById('cfgBannerUnitId');
  if (bId) bId.value = c.ads?.banner?.unit_id || '';

  const iEn = document.getElementById('cfgInterstitialEnabled');
  if (iEn) iEn.checked = !!c.ads?.interstitial?.enabled;
  const iEvery = document.getElementById('cfgIntEveryN');
  if (iEvery) iEvery.value = c.ads?.interstitial?.show_every_n_prompt_views || 3;
  const iSec = document.getElementById('cfgIntMinSec');
  if (iSec) iSec.value = c.ads?.interstitial?.min_interval_seconds || 60;
  const iMax = document.getElementById('cfgIntMaxSession');
  if (iMax) iMax.value = c.ads?.interstitial?.max_per_session || 8;
  const iSkip = document.getElementById('cfgIntSkipFirst');
  if (iSkip) iSkip.value = c.ads?.interstitial?.skip_for_first_n_actions ?? 2;
  const iId = document.getElementById('cfgIntUnitId');
  if (iId) iId.value = c.ads?.interstitial?.unit_id || '';

  const rEn = document.getElementById('cfgRewardedEnabled');
  if (rEn) rEn.checked = !!c.ads?.rewarded?.enabled;
  const rId = document.getElementById('cfgRewardedUnitId');
  if (rId) rId.value = c.ads?.rewarded?.unit_id || '';

  const nEn = document.getElementById('cfgNativeEnabled');
  if (nEn) nEn.checked = !!c.ads?.native?.enabled;
  const nStart = document.getElementById('cfgNativeStartAfter');
  if (nStart) nStart.value = c.ads?.native?.start_after_items || 4;
  const nEvery = document.getElementById('cfgNativeEveryN');
  if (nEvery) nEvery.value = c.ads?.native?.every_n_items || 7;
  const nId = document.getElementById('cfgNativeUnitId');
  if (nId) nId.value = c.ads?.native?.unit_id || '';

  const rawEd = document.getElementById('rawConfigEditor');
  if (rawEd) rawEd.value = JSON.stringify(c, null, 2);
}

function syncFormToConfigObject() {
  const c = state.configData || DEFAULT_CONFIG;
  c.app = {
    ...(c.app || {}),
    maintenance_mode: document.getElementById('cfgMaintenanceMode').checked,
    force_update: document.getElementById('cfgForceUpdate').checked,
    maintenance_message: document.getElementById('cfgMaintenanceMsg').value.trim(),
    min_version_code: parseInt(document.getElementById('cfgMinVersionCode').value, 10) || 1,
    update_url: document.getElementById('cfgUpdateUrl').value.trim()
  };

  c.ads = {
    ...(c.ads || {}),
    master_enabled: document.getElementById('cfgMasterAds').checked,
    use_test_ads: document.getElementById('cfgUseTestAds').checked,
    banner: {
      ...(c.ads?.banner || {}),
      enabled: document.getElementById('cfgBannerEnabled').checked,
      position: document.getElementById('cfgBannerPosition').value || 'bottom',
      unit_id: document.getElementById('cfgBannerUnitId').value.trim(),
      screens: c.ads?.banner?.screens || ["home", "detail"]
    },
    interstitial: {
      ...(c.ads?.interstitial || {}),
      enabled: document.getElementById('cfgInterstitialEnabled').checked,
      unit_id: document.getElementById('cfgIntUnitId').value.trim(),
      show_every_n_prompt_views: parseInt(document.getElementById('cfgIntEveryN').value, 10) || 3,
      min_interval_seconds: parseInt(document.getElementById('cfgIntMinSec').value, 10) || 60,
      max_per_session: parseInt(document.getElementById('cfgIntMaxSession').value, 10) || 8,
      skip_for_first_n_actions: parseInt(document.getElementById('cfgIntSkipFirst').value, 10) ?? 2
    },
    native: {
      ...(c.ads?.native || {}),
      enabled: document.getElementById('cfgNativeEnabled').checked,
      unit_id: document.getElementById('cfgNativeUnitId').value.trim(),
      start_after_items: parseInt(document.getElementById('cfgNativeStartAfter').value, 10) || 4,
      every_n_items: parseInt(document.getElementById('cfgNativeEveryN').value, 10) || 7
    },
    rewarded: {
      ...(c.ads?.rewarded || {}),
      enabled: document.getElementById('cfgRewardedEnabled').checked,
      unit_id: document.getElementById('cfgRewardedUnitId').value.trim()
    }
  };

  c.premium = {
    ...(c.premium || {}),
    unlock_duration_hours: parseInt(document.getElementById('cfgUnlockDuration').value, 10) || 48,
    unlock_scope: document.getElementById('cfgUnlockScope').value,
    ads_required_per_unlock: parseInt(document.getElementById('cfgAdsRequiredPerUnlock').value, 10) || 1
  };

  return c;
}

async function handleSaveConfig() {
  let configToSave;
  const isRawActive = document.getElementById('rawConfigContainer')?.classList.contains('active');

  if (isRawActive) {
    try {
      configToSave = JSON.parse(document.getElementById('rawConfigEditor').value);
    } catch (e) {
      showToast('Invalid JSON in Raw Editor: ' + e.message, 'error');
      return;
    }
  } else {
    configToSave = syncFormToConfigObject();
  }

  state.configData = configToSave;
  saveLocalState();

  if (state.isLocalServer) {
    try {
      const res = await fetch('/api/config', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(configToSave)
      });
      if (!res.ok) throw new Error('Failed to save app_config.json on disk');
      renderConfigForm();
      renderDashboard();
      showToast('app_config.json saved directly to project disk!');
    } catch (err) {
      showToast(err.message, 'error');
    }
  } else if (state.token && state.owner && state.repo) {
    try {
      showToast('Saving app_config.json to GitHub...', 'info');
      const jsonStr = JSON.stringify(configToSave, null, 2);
      const result = await ghPutFile('app_config.json', utf8ToBase64(jsonStr), state.configSha, 'admin: update app_config.json');
      state.configSha = result.content.sha;
      renderConfigForm();
      renderDashboard();
      showToast('app_config.json committed to GitHub!');
    } catch (err) {
      showToast(`Failed to save config: ${err.message}`, 'error');
    }
  } else {
    renderConfigForm();
    renderDashboard();
    showToast('app_config.json saved in browser storage!');
  }
}

// ---------------------------------------------------------------------
// Category Management Modal
// ---------------------------------------------------------------------
function openCategoryModal() {
  renderCategoryManageList();
  document.getElementById('categoryModal').classList.add('active');
  document.getElementById('newCatInput').focus();
}

function renderCategoryManageList() {
  const container = document.getElementById('categoryListManage');
  if (!container) return;
  container.innerHTML = '';
  const cats = state.promptsData.categories || [];

  if (cats.length === 0) {
    container.innerHTML = '<div style="color:var(--text-muted);padding:10px;">No categories exist.</div>';
    return;
  }

  cats.forEach(c => {
    const count = (state.promptsData.items || []).filter(i => i.category === c).length;
    const item = document.createElement('div');
    item.className = 'cat-manage-item';
    item.innerHTML = `
      <span><strong>${escapeHtml(c)}</strong> <small style="color:var(--text-muted);">(${count} prompts)</small></span>
      <button class="btn btn-sm btn-danger btn-del-cat" data-cat="${escapeHtml(c)}" type="button">Remove</button>
    `;
    container.appendChild(item);
  });

  container.querySelectorAll('.btn-del-cat').forEach(b => {
    b.addEventListener('click', () => {
      const catToDelete = b.dataset.cat;
      const usage = (state.promptsData.items || []).filter(i => i.category === catToDelete).length;
      if (usage > 0) {
        if (!confirm(`Category "${catToDelete}" is used by ${usage} prompt(s). Delete anyway?`)) return;
      }
      state.promptsData.categories = state.promptsData.categories.filter(c => c !== catToDelete);
      saveLocalState();
      renderCategoryManageList();
      renderDashboard();
      renderPosts();
      showToast(`Category "${catToDelete}" removed`);
    });
  });
}

function handleAddNewCategory(catName) {
  if (!catName || !catName.trim()) return;
  const clean = catName.trim();
  if (!state.promptsData.categories) state.promptsData.categories = [];
  if (!state.promptsData.categories.includes(clean)) {
    state.promptsData.categories.push(clean);
    saveLocalState();
    if (state.isLocalServer) {
      fetch('/api/prompts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(state.promptsData)
      }).catch(() => {});
    }
    populateCategorySelect(clean);
    renderDashboard();
    renderPosts();
    showToast(`Category "${clean}" added!`);
  }
}

// ---------------------------------------------------------------------
// JSON Import / Export
// ---------------------------------------------------------------------
function downloadJson(filename, obj) {
  const blob = new Blob([JSON.stringify(obj, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
  showToast(`Downloaded ${filename}`);
}

function triggerJsonImport(type) {
  state.pendingJsonImportType = type;
  const input = document.getElementById('jsonFileInput');
  if (input) {
    input.value = '';
    input.click();
  }
}

function handleImportedJsonFile(file) {
  const reader = new FileReader();
  reader.onload = async (e) => {
    try {
      const parsed = JSON.parse(e.target.result);
      if (state.pendingJsonImportType === 'prompts') {
        if (!parsed.items || !Array.isArray(parsed.items)) {
          throw new Error('Invalid prompts.json format: "items" array required.');
        }
        state.promptsData = parsed;
        saveLocalState();
        if (state.isLocalServer) {
          await fetch('/api/prompts', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(state.promptsData)
          });
        }
        renderDashboard();
        renderPosts();
        showToast(`Imported ${parsed.items.length} prompts successfully!`);
      } else if (state.pendingJsonImportType === 'config') {
        if (!parsed.app || !parsed.ads) {
          throw new Error('Invalid app_config.json format: "app" and "ads" objects required.');
        }
        state.configData = parsed;
        saveLocalState();
        if (state.isLocalServer) {
          await fetch('/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(state.configData)
          });
        }
        renderConfigForm();
        renderDashboard();
        showToast('Imported app_config.json successfully!');
      }
    } catch (err) {
      showToast('Import error: ' + err.message, 'error');
    }
  };
  reader.readAsText(file);
}

// ---------------------------------------------------------------------
// Helper Functions
// ---------------------------------------------------------------------
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function switchView(viewId, tabId) {
  document.querySelectorAll('.view-section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  const targetView = document.getElementById(viewId);
  const targetTab = document.getElementById(tabId);
  if (targetView) targetView.classList.add('active');
  if (targetTab) targetTab.classList.add('active');
}

async function handleImageFile(file) {
  if (!file.type.startsWith('image/')) {
    showToast('Please select a valid image file', 'error');
    return;
  }
  try {
    showToast('Compressing to optimized WebP format...', 'info');
    const { dataUrl, base64, sizeBytes } = await convertImageToWebp(file);
    state.pendingWebpBase64 = base64;
    state.pendingWebpDataUrl = dataUrl;
    state.pendingWebpSize = sizeBytes;

    document.getElementById('imagePreviewImg').src = dataUrl;
    document.getElementById('imageSizeLabel').textContent = `${(sizeBytes / 1024).toFixed(1)} KB (WebP Optimized)`;
    document.getElementById('dropzoneContent').style.display = 'none';
    document.getElementById('imagePreviewContainer').style.display = 'flex';
  } catch (err) {
    showToast('Failed to process image: ' + err.message, 'error');
  }
}

// ---------------------------------------------------------------------
// Setup Event Listeners
// ---------------------------------------------------------------------
function initEvents() {
  // Navigation tabs
  document.getElementById('tabDashboard')?.addEventListener('click', () => switchView('viewDashboard', 'tabDashboard'));
  document.getElementById('tabPosts')?.addEventListener('click', () => switchView('viewPosts', 'tabPosts'));
  document.getElementById('tabConfig')?.addEventListener('click', () => {
    switchView('viewConfig', 'tabConfig');
    renderConfigForm();
  });

  // Settings Modal
  document.getElementById('settingsBtn')?.addEventListener('click', () => {
    document.getElementById('ghOwnerInput').value = state.owner;
    document.getElementById('ghRepoInput').value = state.repo;
    document.getElementById('ghBranchInput').value = state.branch;
    document.getElementById('ghTokenInput').value = state.token;
    document.getElementById('settingsModal').classList.add('active');
  });

  document.getElementById('btnCloseSettingsModal')?.addEventListener('click', () => {
    document.getElementById('settingsModal').classList.remove('active');
  });

  document.getElementById('settingsForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    state.owner = document.getElementById('ghOwnerInput').value.trim();
    state.repo = document.getElementById('ghRepoInput').value.trim();
    state.branch = document.getElementById('ghBranchInput').value.trim() || 'main';
    state.token = document.getElementById('ghTokenInput').value.trim();

    localStorage.setItem(STORAGE_KEYS.OWNER, state.owner);
    localStorage.setItem(STORAGE_KEYS.REPO, state.repo);
    localStorage.setItem(STORAGE_KEYS.BRANCH, state.branch);
    localStorage.setItem(STORAGE_KEYS.TOKEN, state.token);

    document.getElementById('settingsModal').classList.remove('active');
    checkServerConnection();
    showToast('GitHub credentials saved!');
  });

  document.getElementById('btnDisconnectGh')?.addEventListener('click', () => {
    localStorage.removeItem(STORAGE_KEYS.OWNER);
    localStorage.removeItem(STORAGE_KEYS.REPO);
    localStorage.removeItem(STORAGE_KEYS.BRANCH);
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    state.owner = '';
    state.repo = '';
    state.token = '';
    document.getElementById('settingsModal').classList.remove('active');
    checkServerConnection();
    showToast('GitHub token cleared.');
  });

  // Theme toggle
  document.getElementById('themeToggleBtn')?.addEventListener('click', () => {
    document.body.classList.toggle('light-mode');
    const isLight = document.body.classList.contains('light-mode');
    localStorage.setItem(STORAGE_KEYS.THEME, isLight ? 'light' : 'dark');
  });

  // Reload / Sync button
  document.getElementById('btnRefreshData')?.addEventListener('click', () => {
    if (state.isLocalServer) {
      loadFromLocalServer();
      showToast('Reloaded latest project files from disk!');
    } else if (state.token && state.owner && state.repo) {
      syncFromGitHub();
    } else {
      showToast('Reloaded browser data.');
      renderDashboard();
      renderPosts();
    }
  });

  // Add Post buttons
  document.getElementById('btnNewPostFromDash')?.addEventListener('click', openAddPostModal);
  document.getElementById('btnAddNewPost')?.addEventListener('click', openAddPostModal);
  document.getElementById('btnClosePostModal')?.addEventListener('click', () => {
    document.getElementById('postModal').classList.remove('active');
  });
  document.getElementById('btnCancelPost')?.addEventListener('click', () => {
    document.getElementById('postModal').classList.remove('active');
  });
  document.getElementById('postForm')?.addEventListener('submit', handleSavePost);

  // Character counter for prompt textarea
  document.getElementById('postPromptInput')?.addEventListener('input', (e) => {
    document.getElementById('promptCharCount').textContent = e.target.value.length;
  });

  // Live URL preview
  document.getElementById('postImageUrlInput')?.addEventListener('input', (e) => {
    const val = e.target.value.trim();
    const container = document.getElementById('urlPreviewContainer');
    const img = document.getElementById('urlPreviewImg');
    if (val && (val.startsWith('http://') || val.startsWith('https://') || val.startsWith('images/'))) {
      img.src = val;
      container.style.display = 'flex';
    } else {
      container.style.display = 'none';
    }
  });

  // Category buttons
  document.getElementById('btnAddNewCategory')?.addEventListener('click', () => {
    const newCat = prompt('Enter new category name:');
    if (newCat) handleAddNewCategory(newCat);
  });
  document.getElementById('btnManageCategories')?.addEventListener('click', openCategoryModal);
  document.getElementById('btnCloseCategoryModal')?.addEventListener('click', () => {
    document.getElementById('categoryModal').classList.remove('active');
  });
  document.getElementById('btnCreateCategory')?.addEventListener('click', () => {
    const input = document.getElementById('newCatInput');
    if (input && input.value) {
      handleAddNewCategory(input.value);
      input.value = '';
      renderCategoryManageList();
    }
  });

  // Image upload tab toggle
  document.getElementById('tabUploadFile')?.addEventListener('click', () => {
    document.getElementById('tabUploadFile').classList.add('active');
    document.getElementById('tabImageUrl').classList.remove('active');
    document.getElementById('uploadFileArea').style.display = 'block';
    document.getElementById('imageUrlArea').style.display = 'none';
  });

  document.getElementById('tabImageUrl')?.addEventListener('click', () => {
    document.getElementById('tabImageUrl').classList.add('active');
    document.getElementById('tabUploadFile').classList.remove('active');
    document.getElementById('imageUrlArea').style.display = 'block';
    document.getElementById('uploadFileArea').style.display = 'none';
  });

  // Dropzone file handling
  const dropzone = document.getElementById('uploadFileArea');
  const fileInput = document.getElementById('imageFileInput');

  dropzone?.addEventListener('click', () => fileInput?.click());
  dropzone?.addEventListener('dragover', (e) => { e.preventDefault(); dropzone.style.borderColor = 'var(--brand-purple)'; });
  dropzone?.addEventListener('dragleave', () => { dropzone.style.borderColor = 'var(--border-highlight)'; });
  dropzone?.addEventListener('drop', (e) => {
    e.preventDefault();
    dropzone.style.borderColor = 'var(--border-highlight)';
    if (e.dataTransfer.files.length) handleImageFile(e.dataTransfer.files[0]);
  });
  fileInput?.addEventListener('change', () => {
    if (fileInput.files.length) handleImageFile(fileInput.files[0]);
  });

  document.getElementById('btnRemoveImage')?.addEventListener('click', (e) => {
    e.stopPropagation();
    state.pendingWebpBase64 = null;
    state.pendingWebpDataUrl = null;
    document.getElementById('dropzoneContent').style.display = 'block';
    document.getElementById('imagePreviewContainer').style.display = 'none';
    if (fileInput) fileInput.value = '';
  });

  // Search & Filter
  document.getElementById('postSearchInput')?.addEventListener('input', renderPosts);
  document.getElementById('btnClearSearch')?.addEventListener('click', () => {
    const s = document.getElementById('postSearchInput');
    if (s) { s.value = ''; renderPosts(); }
  });
  document.getElementById('filterCategorySelect')?.addEventListener('change', renderPosts);
  document.getElementById('filterPremiumSelect')?.addEventListener('change', renderPosts);
  document.getElementById('sortSelect')?.addEventListener('change', renderPosts);

  // Config Subtabs
  document.getElementById('tabFriendlyConfig')?.addEventListener('click', () => {
    // When switching from Raw to Friendly, parse raw JSON first
    const rawEd = document.getElementById('rawConfigEditor');
    if (rawEd && rawEd.value.trim()) {
      try {
        state.configData = JSON.parse(rawEd.value);
        saveLocalState();
      } catch (err) {
        showToast('Fix JSON syntax error before switching: ' + err.message, 'error');
        return;
      }
    }
    document.getElementById('tabFriendlyConfig').classList.add('active');
    document.getElementById('tabRawConfig').classList.remove('active');
    document.getElementById('friendlyConfigContainer').classList.add('active');
    document.getElementById('rawConfigContainer').classList.remove('active');
    renderConfigForm();
  });

  document.getElementById('tabRawConfig')?.addEventListener('click', () => {
    // When switching from Friendly to Raw, sync form to state first
    state.configData = syncFormToConfigObject();
    saveLocalState();
    document.getElementById('tabRawConfig').classList.add('active');
    document.getElementById('tabFriendlyConfig').classList.remove('active');
    document.getElementById('rawConfigContainer').classList.add('active');
    document.getElementById('friendlyConfigContainer').classList.remove('active');
    const rawEd = document.getElementById('rawConfigEditor');
    if (rawEd) rawEd.value = JSON.stringify(state.configData, null, 2);
  });

  document.getElementById('btnFormatRawConfig')?.addEventListener('click', () => {
    const rawEd = document.getElementById('rawConfigEditor');
    if (rawEd) {
      try {
        const parsed = JSON.parse(rawEd.value);
        rawEd.value = JSON.stringify(parsed, null, 2);
        showToast('JSON formatted!');
      } catch (e) {
        showToast('JSON Syntax Error: ' + e.message, 'error');
      }
    }
  });

  document.getElementById('btnSaveConfig')?.addEventListener('click', handleSaveConfig);

  // Backups & Downloads
  document.getElementById('btnDownloadPromptsBackup')?.addEventListener('click', () => {
    downloadJson('prompts.json', state.promptsData);
  });
  document.getElementById('btnDownloadConfigBackup')?.addEventListener('click', () => {
    downloadJson('app_config.json', state.configData || DEFAULT_CONFIG);
  });

  // Direct JSON Import
  document.getElementById('btnUploadPromptsDirect')?.addEventListener('click', () => triggerJsonImport('prompts'));
  document.getElementById('btnUploadConfigDirect')?.addEventListener('click', () => triggerJsonImport('config'));
  document.getElementById('btnBannerImportPrompts')?.addEventListener('click', () => triggerJsonImport('prompts'));
  document.getElementById('btnBannerExportPrompts')?.addEventListener('click', () => downloadJson('prompts.json', state.promptsData));
  document.getElementById('btnDismissBanner')?.addEventListener('click', () => {
    document.getElementById('offlineBanner').style.display = 'none';
  });

  document.getElementById('jsonFileInput')?.addEventListener('change', (e) => {
    if (e.target.files.length) handleImportedJsonFile(e.target.files[0]);
  });

  // Modal Backdrop Click & Escape Key
  document.querySelectorAll('.modal-backdrop').forEach(modal => {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) modal.classList.remove('active');
    });
  });

  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-backdrop.active').forEach(m => m.classList.remove('active'));
    }
  });
}

function initApp() {
  if (localStorage.getItem(STORAGE_KEYS.THEME) === 'light') {
    document.body.classList.add('light-mode');
  }

  initEvents();
  renderDashboard();
  renderPosts();
  renderConfigForm();
  checkServerConnection();
}

// Execution
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp);
} else {
  initApp();
}
