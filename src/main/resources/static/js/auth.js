// Authentication JavaScript for Login/Register pages

// API Base URL
const API_BASE_URL = '/api';

// Store token in localStorage
function setAuthToken(token) {
    if (token) {
        localStorage.setItem('accessToken', token);
    } else {
        localStorage.removeItem('accessToken');
    }
}

// Get stored token
function getAuthToken() {
    return localStorage.getItem('accessToken');
}

// Check if user is authenticated
function isAuthenticated() {
    return !!getAuthToken();
}

// Logout function
async function logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('rememberEmail');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userName');
    sessionStorage.clear();
    window.location.href = '/';
}

function showForm(formId) {
    const forms = document.querySelectorAll('.auth-box');
    forms.forEach(form => {
        form.classList.remove('active');
    });
    const targetForm = document.getElementById(formId);
    if (targetForm) {
        targetForm.classList.add('active');
    }
}

async function handleLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const rememberMe = document.getElementById('rememberMe')?.checked || false;
    
    if (!email || !password) {
        showNotification('Please enter both email and password', 'error');
        return;
    }
    
    // Show loading state
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Signing in...';
    submitBtn.disabled = true;
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password, remember_me: rememberMe })
        });
        
        const data = await response.json();
        
        if (data.success) {
            // Store JWT token
            if (data.token) {
                setAuthToken(data.token);
                localStorage.setItem('userRole', data.role);
                localStorage.setItem('userName', data.fullName);
            }
            
            // Store remember me preference
            if (rememberMe) {
                localStorage.setItem('rememberEmail', email);
            } else {
                localStorage.removeItem('rememberEmail');
            }
            
            showNotification('Login successful! Redirecting...', 'success');
            
            setTimeout(() => {
                window.location.href = data.redirect;
            }, 1000);
        } else {
            showNotification('Login failed: ' + (data.error || 'Invalid credentials'), 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification('Login failed. Please check your connection and try again.', 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const fullName = document.getElementById('regFullName').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    
    if (!fullName || !email || !password) {
        showNotification('Please fill in all fields', 'error');
        return;
    }
    
    if (password.length < 6) {
        showNotification('Password must be at least 6 characters long', 'error');
        return;
    }
    
    // Show loading state
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Creating account...';
    submitBtn.disabled = true;
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ fullName, email, password })
        });
        
        const data = await response.json();
        
        if (data.success) {
            // Store JWT token
            if (data.token) {
                setAuthToken(data.token);
                localStorage.setItem('userRole', 'student');
                localStorage.setItem('userName', fullName);
            }
            
            showNotification('Account created successfully! Redirecting to dashboard...', 'success');
            
            setTimeout(() => {
                window.location.href = data.redirect;
            }, 1000);
        } else {
            showNotification('Registration failed: ' + (data.error || 'Please try again'), 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    } catch (error) {
        console.error('Registration error:', error);
        showNotification('Registration failed. Please check your connection and try again.', 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// Notification system
function showNotification(message, type = 'info') {
    let container = document.getElementById('notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
        `;
        document.body.appendChild(container);
    }
    
    const notification = document.createElement('div');
    const colors = {
        success: '#22c55e',
        error: '#ef4444',
        info: '#3b82f6',
        warning: '#f59e0b'
    };
    
    notification.style.cssText = `
        background: ${colors[type] || colors.info};
        color: white;
        padding: 12px 20px;
        border-radius: 12px;
        margin-bottom: 10px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideIn 0.3s ease;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
    `;
    notification.textContent = message;
    
    container.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
    
    notification.onclick = () => notification.remove();
}

// Add animation styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Auto-fill remembered email on page load
document.addEventListener('DOMContentLoaded', () => {
    const rememberedEmail = localStorage.getItem('rememberEmail');
    if (rememberedEmail) {
        const emailInput = document.getElementById('loginEmail');
        if (emailInput) {
            emailInput.value = rememberedEmail;
            const rememberCheckbox = document.getElementById('rememberMe');
            if (rememberCheckbox) {
                rememberCheckbox.checked = true;
            }
        }
    }
});

// Fetch current user info
async function fetchCurrentUser() {
    try {
        const token = getAuthToken();
        if (!token) return null;
        
        const response = await fetch(`${API_BASE_URL}/auth/me`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (response.ok) {
            return await response.json();
        }
    } catch (error) {
        console.error('Error fetching user:', error);
    }
    return null;
}

// Attach event listeners when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const forgotForm = document.getElementById('forgotForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
    
    if (forgotForm) {
        forgotForm.addEventListener('submit', handleForgot);
    }
});

async function handleForgot(event) {
    event.preventDefault();
    
    const email = document.getElementById('resetEmail').value;
    
    if (!email) {
        showNotification('Please enter your email address', 'error');
        return;
    }
    
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Sending...';
    submitBtn.disabled = true;
    
    try {
        await new Promise(resolve => setTimeout(resolve, 1000));
        showNotification('If an account exists with this email, you will receive a password reset link.', 'success');
        showForm('login-form');
    } catch (error) {
        showNotification('Error sending reset link. Please try again.', 'error');
    } finally {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

// Google Login function (if OAuth2 is configured)
function googleLoginWithRemember(rememberMe = false) {
    const url = `/oauth2/authorization/google?remember_me=${rememberMe}`;
    window.location.href = url;
}

// Export functions for global use
window.showForm = showForm;
window.handleLogin = handleLogin;
window.handleRegister = handleRegister;
window.handleForgot = handleForgot;
window.logout = logout;
window.googleLoginWithRemember = googleLoginWithRemember;
window.showNotification = showNotification;
window.getAuthToken = getAuthToken;
window.isAuthenticated = isAuthenticated;
window.setAuthToken = setAuthToken;
window.fetchCurrentUser = fetchCurrentUser;