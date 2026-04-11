/**
 * Dark Mode Toggle for SkillBarter
 * 
 * Persists user preference in localStorage
 * Respects system preference on first visit
 */

(function() {
    'use strict';

    const THEME_KEY = 'skillbarter-theme';
    const DARK_THEME = 'dark';
    const LIGHT_THEME = 'light';

    /**
     * Get saved theme or detect system preference
     */
    function getPreferredTheme() {
        const savedTheme = localStorage.getItem(THEME_KEY);
        if (savedTheme) {
            return savedTheme;
        }

        // Check system preference
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            return DARK_THEME;
        }

        return LIGHT_THEME;
    }

    /**
     * Apply theme to document
     */
    function applyTheme(theme) {
        document.body.setAttribute('data-theme', theme);
        localStorage.setItem(THEME_KEY, theme);
        
        // Update meta theme-color for mobile browsers
        const metaThemeColor = document.querySelector('meta[name="theme-color"]');
        if (metaThemeColor) {
            metaThemeColor.setAttribute('content', theme === DARK_THEME ? '#1a1a1a' : '#ffffff');
        }
    }

    /**
     * Toggle between light and dark theme
     */
    function toggleTheme() {
        const currentTheme = document.body.getAttribute('data-theme') || LIGHT_THEME;
        const newTheme = currentTheme === DARK_THEME ? LIGHT_THEME : DARK_THEME;
        applyTheme(newTheme);
    }

    /**
     * Create theme toggle button
     */
    function createToggleButton() {
        const button = document.createElement('button');
        button.className = 'theme-toggle';
        button.setAttribute('aria-label', 'Toggle dark mode');
        button.setAttribute('title', 'Toggle dark mode');
        button.onclick = toggleTheme;
        document.body.appendChild(button);
    }

    /**
     * Initialize theme on page load
     */
    function init() {
        // Apply saved/preferred theme immediately to prevent flash
        const theme = getPreferredTheme();
        applyTheme(theme);

        // Create toggle button when DOM is ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', createToggleButton);
        } else {
            createToggleButton();
        }

        // Listen for system theme changes
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
                // Only auto-switch if user hasn't manually set a preference
                if (!localStorage.getItem(THEME_KEY)) {
                    applyTheme(e.matches ? DARK_THEME : LIGHT_THEME);
                }
            });
        }
    }

    // Initialize immediately
    init();

    // Expose toggle function globally for manual triggers
    window.toggleDarkMode = toggleTheme;
})();
