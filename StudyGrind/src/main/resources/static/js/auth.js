// Authentication JavaScript for Login/Register pages

// API Base URL
const API_BASE_URL = '/api';

// Store tokens in localStorage
function setAuthTokens(accessToken, refreshToken) {
    if (accessToken) {
        localStorage.setItem('accessToken', accessToken);
    }
    if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
    }
}

// Get stored access token
function getAuthToken() {
    return localStorage.getItem('accessToken');
}

// Get stored refresh token
function getRefreshToken() {
    return localStorage.getItem('refreshToken');
}

// Clear all tokens
function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userName');
    localStorage.removeItem('rememberEmail');
    sessionStorage.clear();
}

// Check if user is authenticated
function isAuthenticated() {
    return !!getAuthToken();
}

// Refresh access token using refresh token
async function refreshAccessToken() {
    const refreshToken = getRefreshToken();
    if (!refreshToken) return false;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        const data = await response.json();

        if (data.success && data.accessToken) {
            localStorage.setItem('accessToken', data.accessToken);
            return true;
        }
    } catch (error) {
        console.error('Token refresh failed:', error);
    }

    return false;
}

// Fetch with automatic token refresh
async function fetchWithAuth(url, options = {}) {
    let token = getAuthToken();

    const makeRequest = async (requestToken) => {
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
            'Authorization': `Bearer ${requestToken}`
        };

        const response = await fetch(url, { ...options, headers });

        if (response.status === 401) {
            // Token expired, try to refresh
            const refreshed = await refreshAccessToken();
            if (refreshed) {
                // Retry with new token
                const newToken = getAuthToken();
                headers['Authorization'] = `Bearer ${newToken}`;
                return fetch(url, { ...options, headers });
            } else {
                // Refresh failed, redirect to login
                clearTokens();
                window.location.href = '/';
                throw new Error('Session expired. Please login again.');
            }
        }

        return response;
    };

    return makeRequest(token);
}

// Logout function
async function logout() {
    const refreshToken = getRefreshToken();

    if (refreshToken) {
        try {
            await fetch(`${API_BASE_URL}/auth/logout`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });
        } catch (error) {
            console.error('Logout error:', error);
        }
    }

    clearTokens();
    window.location.href = '/';
}

// Show form function
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

// Handle Login
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
            // Store both access and refresh tokens
            if (data.accessToken) {
                setAuthTokens(data.accessToken, data.refreshToken);
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

// Handle Register
async function handleRegister(event) {
    event.preventDefault();

    const fullName = document.getElementById('regFullName').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const studentNumber = document.getElementById('regStudentNumber')?.value;
    const phoneNumber = document.getElementById('regPhoneNumber')?.value;
    const address = document.getElementById('regAddress')?.value;
    const dateOfBirth = document.getElementById('regDateOfBirth')?.value;

    if (!fullName || !email || !password) {
        showNotification('Please fill in all fields', 'error');
        return;
    }

    if (password.length < 6) {
        showNotification('Password must be at least 6 characters long', 'error');
        return;
    }

    // Get selected course IDs
    const courseIds = [];
    document.querySelectorAll('input[name="courseIds"]:checked').forEach(cb => {
        courseIds.push(parseInt(cb.value));
    });

    // Convert date to LocalDateTime format if present
    const dateOfBirthWithTime = dateOfBirth ? dateOfBirth + "T00:00:00" : null;

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
            body: JSON.stringify({
                fullName,
                email,
                password,
                courseIds,
                studentNumber,
                phoneNumber,
                address,
                dateOfBirth: dateOfBirthWithTime
            })
        });

        const data = await response.json();

        if (data.success) {
            // Store both access and refresh tokens
            if (data.accessToken) {
                setAuthTokens(data.accessToken, data.refreshToken);
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

// Handle Forgot Password
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
        const response = await fetch(`${API_BASE_URL}/auth/forgot-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        const data = await response.json();

        if (data.success) {
            showNotification('Verification code sent to your email!', 'success');
            // Switch to verification form or show code input
            document.getElementById('forgotForm').style.display = 'none';
            document.getElementById('verifyResetForm').style.display = 'block';
        } else {
            showNotification(data.error || 'Failed to send reset code', 'error');
        }
    } catch (error) {
        showNotification('Error sending reset link. Please try again.', 'error');
    } finally {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
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
window.getRefreshToken = getRefreshToken;
window.isAuthenticated = isAuthenticated;
window.setAuthTokens = setAuthTokens;
window.clearTokens = clearTokens;
window.fetchCurrentUser = fetchCurrentUser;
window.fetchWithAuth = fetchWithAuth;
window.refreshAccessToken = refreshAccessToken;