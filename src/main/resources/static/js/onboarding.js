/**
 * Onboarding Tutorial for SkillBarter
 * 
 * Shows interactive walkthrough for new users
 * Uses localStorage to track completion
 */

(function() {
    'use strict';

    const ONBOARDING_KEY = 'skillbarter-onboarding-completed';
    const ONBOARDING_VERSION = '1.0';

    /**
     * Check if user has completed onboarding
     */
    function hasCompletedOnboarding() {
        return localStorage.getItem(ONBOARDING_KEY) === ONBOARDING_VERSION;
    }

    /**
     * Mark onboarding as completed
     */
    function completeOnboarding() {
        localStorage.setItem(ONBOARDING_KEY, ONBOARDING_VERSION);
    }

    /**
     * Create overlay for tutorial
     */
    function createOverlay() {
        const overlay = document.createElement('div');
        overlay.className = 'onboarding-overlay';
        overlay.id = 'onboarding-overlay';
        document.body.appendChild(overlay);
        return overlay;
    }

    /**
     * Create tooltip element
     */
    function createTooltip(step) {
        const tooltip = document.createElement('div');
        tooltip.className = 'onboarding-tooltip';
        tooltip.innerHTML = `
            <div class="onboarding-header">
                <h3>${step.title}</h3>
                <button class="onboarding-skip" onclick="skipOnboarding()">Skip</button>
            </div>
            <div class="onboarding-body">
                <p>${step.description}</p>
            </div>
            <div class="onboarding-footer">
                <span class="onboarding-progress">${step.current} of ${step.total}</span>
                <button class="btn btn-primary" onclick="nextOnboardingStep()">${step.isLast ? 'Finish' : 'Next'}</button>
            </div>
        `;
        return tooltip;
    }

    /**
     * Position tooltip near target element
     */
    function positionTooltip(tooltip, target) {
        const rect = target.getBoundingClientRect();
        const tooltipRect = tooltip.getBoundingClientRect();
        
        // Position below target by default
        let top = rect.bottom + 10;
        let left = rect.left;

        // Adjust if tooltip goes off screen
        if (top + tooltipRect.height > window.innerHeight) {
            top = rect.top - tooltipRect.height - 10;
        }
        if (left + tooltipRect.width > window.innerWidth) {
            left = window.innerWidth - tooltipRect.width - 20;
        }

        tooltip.style.top = top + 'px';
        tooltip.style.left = left + 'px';
    }

    /**
     * Highlight target element
     */
    function highlightElement(selector) {
        const element = document.querySelector(selector);
        if (element) {
            element.classList.add('onboarding-highlight');
            element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return element;
    }

    /**
     * Remove all highlights
     */
    function clearHighlights() {
        document.querySelectorAll('.onboarding-highlight').forEach(el => {
            el.classList.remove('onboarding-highlight');
        });
    }

    /**
     * Onboarding steps
     */
    const steps = [
        {
            title: 'Welcome to SkillBarter! 👋',
            description: 'Trade skills without money. Teach what you know, learn what you need. Let\'s take a quick tour!',
            target: '.dash-hero',
            current: 1,
            total: 5
        },
        {
            title: 'Your Credit Balance 💰',
            description: 'You start with 5 free credits! Use them to book sessions. Earn more by teaching others.',
            target: '.stat-card--credits',
            current: 2,
            total: 5
        },
        {
            title: 'Find a Teacher 🔍',
            description: 'Browse skills and find teachers. Filter by category, rating, and price.',
            target: 'a[href="/skills/search"]',
            current: 3,
            total: 5
        },
        {
            title: 'List Your Skills ✏️',
            description: 'Share what you can teach! Set your hourly rate and availability.',
            target: 'a[href="/skills/my"]',
            current: 4,
            total: 5
        },
        {
            title: 'Track Your Progress 📊',
            description: 'View your sessions, wallet, and analytics. Earn badges and climb the leaderboard!',
            target: 'a[href="/analytics"]',
            current: 5,
            total: 5,
            isLast: true
        }
    ];

    let currentStep = 0;
    let overlay, tooltip;

    /**
     * Show specific step
     */
    function showStep(index) {
        if (index >= steps.length) {
            finishOnboarding();
            return;
        }

        currentStep = index;
        const step = steps[index];

        // Clear previous highlights
        clearHighlights();

        // Remove previous tooltip
        if (tooltip) {
            tooltip.remove();
        }

        // Highlight target element
        const target = highlightElement(step.target);
        if (!target) {
            // Skip to next step if target not found
            showStep(index + 1);
            return;
        }

        // Create and position tooltip
        tooltip = createTooltip(step);
        document.body.appendChild(tooltip);
        positionTooltip(tooltip, target);
    }

    /**
     * Show next step
     */
    window.nextOnboardingStep = function() {
        showStep(currentStep + 1);
    };

    /**
     * Skip onboarding
     */
    window.skipOnboarding = function() {
        if (confirm('Are you sure you want to skip the tutorial?')) {
            finishOnboarding();
        }
    };

    /**
     * Finish onboarding
     */
    function finishOnboarding() {
        clearHighlights();
        if (overlay) overlay.remove();
        if (tooltip) tooltip.remove();
        completeOnboarding();
    }

    /**
     * Start onboarding
     */
    function startOnboarding() {
        overlay = createOverlay();
        showStep(0);
    }

    /**
     * Initialize onboarding on dashboard page
     */
    function init() {
        // Only run on dashboard page
        if (!window.location.pathname.includes('/dashboard')) {
            return;
        }

        // Check if user has completed onboarding
        if (hasCompletedOnboarding()) {
            return;
        }

        // Wait for page to fully load
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                setTimeout(startOnboarding, 500);
            });
        } else {
            setTimeout(startOnboarding, 500);
        }
    }

    // Initialize
    init();

    // Expose function to manually trigger onboarding
    window.startOnboarding = startOnboarding;
})();
