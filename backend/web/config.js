// VulnArk Frontend Configuration
// This file provides dynamic configuration for the frontend application

(function() {
    // Get current host and protocol
    const protocol = window.location.protocol;
    const host = window.location.host;
    
    // Create global configuration object
    window.VULNARK_CONFIG = {
        // API Configuration
        API_BASE_URL: `${protocol}//${host}/api/v1`,
        
        // Application Information
        APP_NAME: 'VulnArk',
        APP_VERSION: '1.0.0',
        
        // Feature Flags
        FEATURES: {
            AI_ASSISTANT: true,
            FILE_UPLOAD: true,
            NOTIFICATIONS: true,
            REPORTS: true,
            KNOWLEDGE_BASE: true
        },
        
        // UI Configuration
        UI: {
            THEME: 'light',
            LANGUAGE: 'zh-CN',
            PAGE_SIZE: 10,
            MAX_FILE_SIZE: 10 * 1024 * 1024, // 10MB
        },
        
        // Development/Debug flags
        DEBUG: false,
        
        // Timeout configurations
        TIMEOUTS: {
            API_REQUEST: 30000, // 30 seconds
            FILE_UPLOAD: 300000, // 5 minutes
        }
    };
    
    // Log configuration for debugging
    if (window.VULNARK_CONFIG.DEBUG) {
        console.log('VulnArk Configuration Loaded:', window.VULNARK_CONFIG);
    }
    
    // Dispatch configuration loaded event
    window.dispatchEvent(new CustomEvent('vulnark-config-loaded', {
        detail: window.VULNARK_CONFIG
    }));
})();
