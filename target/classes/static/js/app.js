/**
 * SkillBarter — Frontend JS
 * Handles: notification badge refresh, alert auto-dismiss,
 *          form enhancements, star picker, CSRF helper.
 */

// ── CSRF helper (used by AI chat and other AJAX calls) ─────────────
function getCsrfToken() {
    const meta = document.querySelector('meta[name="_csrf"]');
    return meta ? meta.getAttribute('content') : '';
}

function getCsrfHeader() {
    const meta = document.querySelector('meta[name="_csrf_header"]');
    return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
}

// Attach CSRF to all fetch calls automatically
const _origFetch = window.fetch;
window.fetch = function(url, opts = {}) {
    if (opts.method && opts.method.toUpperCase() !== 'GET') {
        opts.headers = opts.headers || {};
        const token = getCsrfToken();
        if (token) opts.headers[getCsrfHeader()] = token;
    }
    return _origFetch(url, opts);
};

// ── Alert auto-dismiss ─────────────────────────────────────────────
document.querySelectorAll('.alert').forEach(el => {
    setTimeout(() => {
        el.style.transition = 'opacity 0.4s';
        el.style.opacity = '0';
        setTimeout(() => el.remove(), 400);
    }, 5000);
});

// ── Active nav link ────────────────────────────────────────────────
const path = window.location.pathname;
document.querySelectorAll('.nav-link').forEach(link => {
    const href = link.getAttribute('href');
    if (href && href !== '/' && path.startsWith(href)) {
        link.style.background = 'var(--surface2)';
        link.style.color = 'var(--text)';
    }
});

// ── Session request: scheduled datetime min ────────────────────────
const schedInput = document.querySelector('input[name="scheduledAt"]');
if (schedInput) {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 60);
    schedInput.min = now.toISOString().slice(0, 16);
    schedInput.type = 'datetime-local';
}

// ── Confirm dangerous actions ──────────────────────────────────────
document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', e => {
        if (!confirm(el.dataset.confirm)) e.preventDefault();
    });
});

// ── Notification badge polling (every 30s) ─────────────────────────
async function refreshNotifCount() {
    try {
        const badge = document.getElementById('notifCount');
        if (!badge) return;
        const res = await fetch('/api/notifications/count');
        if (res.ok) {
            const data = await res.json();
            badge.textContent = data.count;
            badge.style.display = data.count > 0 ? 'flex' : 'none';
        }
    } catch (e) { /* silent */ }
}
setInterval(refreshNotifCount, 30_000);

// ── Timezone-aware session time display ────────────────────────────
// Converts UTC session times to user's local timezone using data-utc attribute
document.querySelectorAll('[data-utc]').forEach(el => {
    try {
        const utc = el.getAttribute('data-utc');
        const tz  = el.getAttribute('data-tz') || Intl.DateTimeFormat().resolvedOptions().timeZone;
        const d   = new Date(utc);
        el.textContent = d.toLocaleString('en-GB', {
            timeZone: tz,
            day: '2-digit', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch (e) { /* keep server-rendered text */ }
});

// ── Copy to clipboard helper ───────────────────────────────────────
document.querySelectorAll('[data-copy]').forEach(btn => {
    btn.addEventListener('click', () => {
        navigator.clipboard.writeText(btn.getAttribute('data-copy'))
            .then(() => {
                const orig = btn.textContent;
                btn.textContent = 'Copied!';
                setTimeout(() => btn.textContent = orig, 1500);
            });
    });
});
