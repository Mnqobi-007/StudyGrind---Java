// ==================== AUTHENTICATION CHECK ====================
(function() {
    const token = localStorage.getItem('accessToken');
    const currentPath = window.location.pathname;
    
    console.log('Dashboard loading - Token exists:', !!token);
    console.log('Current path:', currentPath);
    
    if (!token) {
        console.log('No token found, redirecting to login');
        window.location.href = '/';
        return;
    }

    if (token) {
        fetch('/api/auth/me', {
            headers: { 'Authorization': 'Bearer ' + token }
        }).then(function(response) {
            if (!response.ok) {
                console.log('Token invalid, clearing and redirecting');
                localStorage.removeItem('accessToken');
                localStorage.removeItem('userRole');
                localStorage.removeItem('userName');
                window.location.href = '/';
            } else {
                console.log('Token valid, loading dashboard');
                return response.json();
            }
        }).then(function(user) {
            if (user) {
                const expectedRole = currentPath.includes('/admin') ? 'admin' : (currentPath.includes('/teacher') ? 'teacher' : 'student');
                if (user.role !== expectedRole) {
                    console.log('Wrong role, redirecting to correct dashboard');
                    if (user.role === 'admin') window.location.href = '/admin/dashboard';
                    else if (user.role === 'teacher') window.location.href = '/teacher/dashboard';
                    else window.location.href = '/student/dashboard';
                }
            }
        }).catch(function() {
            console.log('Network error checking token');
            localStorage.removeItem('accessToken');
            window.location.href = '/';
        });
    }
})();

// ==================== GLOBAL VARIABLES ====================
const API_BASE_URL = '/api';
let currentTimetableEntries = [];
let currentFilterDay = 'all';
let currentNote = null;
let currentQuiz = null;
let currentQuestionIndex = 0;
let answers = {};
let hasActiveSubscription = false;
let hasActiveTrial = false;

function getAuthHeaders() {
    const token = localStorage.getItem('accessToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

// ==================== UTILITY FUNCTIONS ====================

function showNotification(message, type) {
    let container = document.getElementById('notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 10000;';
        document.body.appendChild(container);
    }
    
    const colors = {
        success: '#22c55e',
        error: '#ef4444',
        info: '#3b82f6',
        warning: '#f59e0b'
    };
    
    const notification = document.createElement('div');
    notification.style.cssText = `background: ${colors[type] || colors.info}; color: white; padding: 12px 20px; border-radius: 12px; margin-bottom: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); cursor: pointer;`;
    notification.textContent = message;
    container.appendChild(notification);
    
    setTimeout(() => {
        if (notification && notification.remove) {
            notification.remove();
        }
    }, 3000);
    
    notification.onclick = () => notification.remove();
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    } catch (e) {
        return dateString;
    }
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return dateString;
    }
}

function showSection(sectionId) {
    const sections = document.querySelectorAll('.content-section');
    if (sections) {
        for (let i = 0; i < sections.length; i++) {
            if (sections[i]) sections[i].classList.remove('active');
        }
    }
    
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    }
    
    const navItems = document.querySelectorAll('.sidebar-nav li');
    if (navItems) {
        for (let i = 0; i < navItems.length; i++) {
            if (navItems[i]) navItems[i].classList.remove('active');
        }
    }
    
    const activeNav = document.querySelector(`.sidebar-nav li[data-section="${sectionId}"]`);
    if (activeNav) activeNav.classList.add('active');
    
    localStorage.setItem('lastSection', sectionId);
}

// ==================== LOGOUT FUNCTION ====================
window.logout = function() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userRole');
        localStorage.removeItem('userName');
        localStorage.removeItem('rememberEmail');
        sessionStorage.clear();
        window.location.href = '/';
    }
};

// ==================== SUBSCRIPTION STATUS ====================
async function loadSubscriptionStatus() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/subscription/status', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const status = await response.json();
        
        hasActiveSubscription = status.hasActiveSubscription;
        hasActiveTrial = status.hasActiveTrial;
        
        const subInfo = document.getElementById('subscriptionInfo');
        
        if (hasActiveTrial) {
            subInfo.innerHTML = `<div style="background:#fef3c7; padding:15px; border-radius:12px;">
                <i class="fa-solid fa-gift"></i> You're on a free trial! ${status.trialRemainingDays} days remaining.
                <div style="margin-top:10px;"><button class="btn-yellow-sm" onclick="showSection('billing')">View Billing</button></div>
            </div>`;
        } else if (hasActiveSubscription) {
            subInfo.innerHTML = `<div style="background:#d1fae5; padding:15px; border-radius:12px; color:#059669;">
                <i class="fa-solid fa-crown"></i> Premium Active! ${status.subscriptionRemainingDays} days remaining.
            </div>`;
        } else {
            subInfo.innerHTML = `<div style="background:#fee2e2; padding:15px; border-radius:12px; color:#dc2626;">
                <i class="fa-solid fa-exclamation-triangle"></i> Your subscription has expired. Please pay to continue accessing courses.
                <div style="margin-top:10px;"><button class="btn-yellow-sm" onclick="showSection('billing')">Pay Now</button></div>
            </div>`;
        }
    } catch(e) {
        console.error('Error loading subscription:', e);
    }
}

// ==================== COURSE FUNCTIONS ====================

async function loadMyCourses() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/courses/my-courses', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!response.ok) throw new Error('Failed to load courses');
        const courses = await response.json();
        const container = document.getElementById('myCoursesList');
        
        if (!courses || courses.length === 0) {
            container.innerHTML = '<div class="content-card"><p>You are not enrolled in any courses yet. <a href="#" onclick="showSection(\'available-courses\')">Browse available courses</a> to get started!</p></div>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < courses.length; i++) {
            const course = courses[i];
            html += `
                <div class="course-card">
                    <h4>${escapeHtml(course.name)}</h4>
                    <p>${escapeHtml(course.description || 'No description')}</p>
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 10px;">
                        <span style="font-size: 12px; color: var(--text-gray);">
                            <i class="fa-solid fa-user"></i> Teacher: ${escapeHtml(course.teacherName)}
                        </span>
                        <span style="font-size: 12px; color: #22c55e;">
                            <i class="fa-solid fa-check-circle"></i> Enrolled
                        </span>
                    </div>
                    <div style="margin-top: 15px; display: flex; gap: 10px;">
                        <button class="btn-blue-sm" onclick="viewCourseContent(${course.id})">View Course Content</button>
                        <button class="btn-red-sm" onclick="unenrollFromCourse(${course.id})">Unenroll</button>
                    </div>
                </div>
            `;
        }
        container.innerHTML = html;
    } catch(e) {
        console.error('Error loading enrolled courses:', e);
    }
}

async function unenrollFromCourse(courseId) {
    const reason = prompt('Please provide a reason for unenrolling (optional):', '');
    
    const token = localStorage.getItem('accessToken');
    try {
        const response = await fetch(API_BASE_URL + `/courses/${courseId}/unenroll`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason: reason || 'User requested' })
        });
        const data = await response.json();
        
        if (data.success) {
            showNotification('Successfully unenrolled from course', 'success');
            loadMyCourses();
            loadAvailableCourses();
            loadBillingSummary();
        } else {
            showNotification(data.error || 'Failed to unenroll', 'error');
        }
    } catch(e) {
        showNotification('Error unenrolling from course', 'error');
    }
}

async function loadAvailableCourses() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/courses/available', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!response.ok) throw new Error('Failed to load available courses');
        const courses = await response.json();
        const container = document.getElementById('availableCoursesList');
        
        if (!courses || courses.length === 0) {
            container.innerHTML = '<div class="content-card"><p>No courses available at this time. Check back later!</p></div>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < courses.length; i++) {
            const course = courses[i];
            html += `
                <div class="course-card">
                    <h4>${escapeHtml(course.name)}</h4>
                    <p>${escapeHtml(course.description || 'No description')}</p>
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 10px;">
                        <span style="font-size: 12px; color: var(--text-gray);">
                            <i class="fa-solid fa-user"></i> Teacher: ${escapeHtml(course.teacherName)}
                        </span>
                        <span class="course-price">$${course.price || 29.99}</span>
                    </div>
                    <div style="margin-top: 15px;">
                        <button class="btn-green-sm" onclick="enrollInCourse(${course.id})">
                            <i class="fa-solid fa-plus"></i> Enroll (14-day free trial)
                        </button>
                    </div>
                </div>
            `;
        }
        container.innerHTML = html;
    } catch(e) {
        console.error('Error loading available courses:', e);
    }
}

async function enrollInCourse(courseId) {
    if (!confirm('Enrolling will start your 14-day free trial. After the trial, payment will be required to continue access. Do you want to proceed?')) return;
    
    const token = localStorage.getItem('accessToken');
    try {
        const response = await fetch(API_BASE_URL + `/courses/${courseId}/enroll`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' }
        });
        const data = await response.json();
        
        if (data.success) {
            showNotification('✓ Successfully enrolled in course! Your 14-day trial has started.', 'success');
            loadMyCourses();
            loadAvailableCourses();
            loadBillingSummary();
        } else {
            showNotification(data.error || 'Enrollment failed', 'error');
        }
    } catch(e) {
        showNotification('Error enrolling in course', 'error');
    }
}

// ==================== VIEW COURSE CONTENT ====================
function viewCourseContent(courseId) {
    localStorage.setItem('selectedCourseId', courseId);
    showSection('course-content');
    loadCourseContent(courseId);
}

function ensureCourseContentSection() {
    let section = document.getElementById('course-content');
    if (!section) {
        section = document.createElement('section');
        section.id = 'course-content';
        section.className = 'content-section';
        section.innerHTML = `
            <div class="content-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h3><i class="fa-solid fa-book"></i> Course Materials</h3>
                    <button class="btn-yellow-sm" onclick="showSection('my-courses')">← Back to Courses</button>
                </div>
                <div id="courseContentDisplay"></div>
            </div>
        `;
        document.querySelector('.main-content').appendChild(section);
    }
}

async function loadCourseContent(courseId) {
    ensureCourseContentSection();
    const container = document.getElementById('courseContentDisplay');
    if (!container) return;
    
    container.innerHTML = '<div class="loading-spinner">Loading course materials...</div>';
    
    const token = localStorage.getItem('accessToken');
    
    try {
        const courseRes = await fetch(API_BASE_URL + `/courses/${courseId}/details`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const course = await courseRes.json();
        
        const assignmentsRes = await fetch(API_BASE_URL + `/assignments?courseId=${courseId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const assignments = await assignmentsRes.json();
        
        const notesRes = await fetch(API_BASE_URL + `/notes?courseId=${courseId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const notes = await notesRes.json();
        
        let html = `
            <div style="margin-bottom: 30px;">
                <h2>${escapeHtml(course.name)}</h2>
                <p>${escapeHtml(course.description || 'No description')}</p>
                <p><strong>Teacher:</strong> ${escapeHtml(course.teacherName)}</p>
            </div>
        `;
        
        html += `<h3 style="margin-top: 20px;"><i class="fa-solid fa-file-arrow-up"></i> Assignments</h3>`;
        if (assignments && assignments.length > 0) {
            for (let i = 0; i < assignments.length; i++) {
                const a = assignments[i];
                html += `
                    <div style="background:var(--bg); padding:15px; border-radius:12px; margin-bottom:10px;">
                        <h4>${escapeHtml(a.title)}</h4>
                        <p>${escapeHtml(a.description)}</p>
                        <p>Due: ${formatDate(a.dueDate)} | Max Score: ${a.maxScore}</p>
                        ${!a.submitted ? 
                            `<textarea id="submission-${a.id}" style="width:100%; margin:10px 0; padding:10px; border-radius:8px;"></textarea>
                             <button class="btn-yellow-sm" onclick="submitAssignment(${a.id})">Submit Assignment</button>` : 
                            '<p style="color:#22c55e;">✓ Submitted</p>'}
                    </div>
                `;
            }
        } else {
            html += '<p>No assignments yet.</p>';
        }
        
        html += `<h3 style="margin-top: 20px;"><i class="fa-solid fa-pen-nib"></i> Study Notes</h3>`;
        if (notes && notes.length > 0) {
            html += `<div class="notes-grid" style="display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:15px;">`;
            for (let i = 0; i < notes.length; i++) {
                const n = notes[i];
                html += `
                    <div class="note-card" style="background:white; border-radius:16px; padding:15px; cursor:pointer; border:1px solid var(--border);" onclick="showNotification('Note details coming soon', 'info')">
                        <h4>${escapeHtml(n.title)}</h4>
                        <p>${escapeHtml(n.subject || 'General')}</p>
                        <small>${formatDate(n.createdAt)}</small>
                    </div>
                `;
            }
            html += `</div>`;
        } else {
            html += '<p>No notes available.</p>';
        }
        
        container.innerHTML = html;
        
    } catch(e) {
        console.error('Error loading course content:', e);
        container.innerHTML = '<p>Error loading course content. Please try again.</p>';
    }
}

// ==================== BILLING FUNCTIONS ====================
async function loadBillingSummary() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/billing/summary', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const summary = await response.json();
        
        const container = document.getElementById('billingSummary');
        const balanceEl = document.getElementById('outstandingBalance');
        
        if (balanceEl) {
            balanceEl.textContent = `$${summary.totalOutstandingBalance.toFixed(2)}`;
            if (summary.totalOutstandingBalance > 0) {
                balanceEl.style.color = '#ef4444';
            } else {
                balanceEl.style.color = '#22c55e';
            }
        }
        
        if (!container) return;
        
        let html = `
            <div class="stats-grid" style="margin-bottom: 20px;">
                <div class="stat-card" style="background: #fee2e2;">
                    <h3>$${summary.totalOutstandingBalance.toFixed(2)}</h3>
                    <p>Total Outstanding</p>
                </div>
                <div class="stat-card" style="background: #d1fae5;">
                    <h3>${summary.activeCourses}</h3>
                    <p>Active Courses</p>
                </div>
                <div class="stat-card" style="background: #fef3c7;">
                    <h3>${summary.expiredTrials}</h3>
                    <p>Expired Trials</p>
                </div>
            </div>
        `;
        
        if (summary.totalOutstandingBalance > 0) {
            html += `
                <div style="text-align: center; margin-top: 20px;">
                    <button class="btn-yellow-sm" onclick="payAllOutstanding()">
                        Pay All Outstanding ($${summary.totalOutstandingBalance.toFixed(2)})
                    </button>
                </div>
            `;
        }
        
        container.innerHTML = html;
        
        loadCoursePaymentsList(summary.enrollments);
        
    } catch(e) {
        console.error('Error loading billing summary:', e);
    }
}

function loadCoursePaymentsList(enrollments) {
    const container = document.getElementById('coursePaymentsList');
    if (!container) return;
    
    if (!enrollments || enrollments.length === 0) {
        container.innerHTML = '<p>No course enrollments yet.</p>';
        return;
    }
    
    let html = '';
    for (let i = 0; i < enrollments.length; i++) {
        const e = enrollments[i];
        let statusHtml = '';
        let actionHtml = '';
        
        if (e.canAccessContent) {
            statusHtml = '<span style="color: #22c55e;"><i class="fa-solid fa-check-circle"></i> Access Active</span>';
        } else if (e.trialActive && !e.canAccessContent) {
            statusHtml = '<span style="color: #f59e0b;"><i class="fa-solid fa-hourglass-half"></i> Trial Expired</span>';
            if (e.outstandingBalance > 0) {
                actionHtml = `<button class="btn-yellow-sm" onclick="payForCourse(${e.enrollmentId})">Pay $${e.outstandingBalance.toFixed(2)}</button>`;
            }
        } else {
            statusHtml = '<span style="color: #ef4444;"><i class="fa-solid fa-lock"></i> Access Blocked</span>';
            if (e.outstandingBalance > 0) {
                actionHtml = `<button class="btn-yellow-sm" onclick="payForCourse(${e.enrollmentId})">Pay $${e.outstandingBalance.toFixed(2)}</button>`;
            }
        }
        
        html += `
            <div class="course-card">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <h4>${escapeHtml(e.courseName)}</h4>
                    ${statusHtml}
                </div>
                <p><strong>Price:</strong> $${e.coursePrice}</p>
                <p><strong>Outstanding:</strong> $${e.outstandingBalance.toFixed(2)}</p>
                ${e.trialActive && e.trialDaysRemaining > 0 ? `<p><strong>Trial:</strong> ${e.trialDaysRemaining} days remaining</p>` : ''}
                ${e.subscriptionActive && e.subscriptionDaysRemaining > 0 ? `<p><strong>Subscription:</strong> ${e.subscriptionDaysRemaining} days remaining</p>` : ''}
                <div style="margin-top: 15px; display: flex; gap: 10px;">
                    ${actionHtml}
                    <button class="btn-blue-sm" onclick="viewCourseContent(${e.courseId})">View Course</button>
                </div>
            </div>
        `;
    }
    
    container.innerHTML = html;
}

async function payForCourse(enrollmentId) {
    const token = localStorage.getItem('accessToken');
    
    try {
        const response = await fetch(API_BASE_URL + `/billing/pay/${enrollmentId}`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' }
        });
        const data = await response.json();
        
        if (data.success && data.paymentRequest) {
            if (data.paymentRequest.mode === 'mock') {
                showNotification('Demo mode: Payment processed successfully!', 'success');
                loadBillingSummary();
                loadMyCourses();
            } else if (data.paymentRequest.payfast) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = 'https://sandbox.payfast.co.za/eng/process';
                
                for (const [key, value] of Object.entries(data.paymentRequest.payfast)) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = key;
                    input.value = value;
                    form.appendChild(input);
                }
                
                document.body.appendChild(form);
                form.submit();
            }
        } else {
            showNotification(data.error || 'Payment failed', 'error');
        }
    } catch(e) {
        showNotification('Error processing payment', 'error');
    }
}

async function payAllOutstanding() {
    const token = localStorage.getItem('accessToken');
    
    try {
        const response = await fetch(API_BASE_URL + '/billing/pay-all', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' }
        });
        const data = await response.json();
        
        if (data.success && data.paymentRequest) {
            if (data.paymentRequest.mode === 'mock') {
                showNotification(`Demo mode: Payment of $${data.totalAmount} processed successfully!`, 'success');
                loadBillingSummary();
                loadMyCourses();
            } else if (data.paymentRequest.payfast) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = 'https://sandbox.payfast.co.za/eng/process';
                
                for (const [key, value] of Object.entries(data.paymentRequest.payfast)) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = key;
                    input.value = value;
                    form.appendChild(input);
                }
                
                document.body.appendChild(form);
                form.submit();
            }
        } else {
            showNotification(data.error || 'Payment failed', 'error');
        }
    } catch(e) {
        showNotification('Error processing payment', 'error');
    }
}

// ==================== ASSIGNMENT FUNCTIONS ====================

async function loadAssignments() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/assignments', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const assignments = await response.json();
        const container = document.getElementById('assignmentsList');
        
        if (!assignments || assignments.length === 0) {
            container.innerHTML = '<p>No assignments yet.</p>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < assignments.length; i++) {
            const a = assignments[i];
            html += `
                <div style="background:white; padding:20px; border-radius:16px; margin-bottom:15px; border:1px solid var(--border);">
                    <h4>${escapeHtml(a.title)}</h4>
                    <p>${escapeHtml(a.description)}</p>
                    <p>Due: ${formatDate(a.dueDate)}</p>
                    ${!a.submitted ? 
                        `<textarea id="submission-${a.id}" style="width:100%; margin:10px 0; padding:10px; border-radius:8px; border:2px solid var(--border);"></textarea>
                         <button class="btn-yellow-sm" onclick="submitAssignment(${a.id})">Submit Assignment</button>` : 
                        '<p style="color:#22c55e;">✓ Submitted</p>'}
                </div>
            `;
        }
        container.innerHTML = html;
    } catch(e) {
        console.error('Error loading assignments:', e);
    }
}

async function submitAssignment(assignmentId) {
    const content = document.getElementById(`submission-${assignmentId}`)?.value;
    if (!content) {
        showNotification('Please write something', 'error');
        return;
    }
    
    const token = localStorage.getItem('accessToken');
    const response = await fetch(API_BASE_URL + `/assignments/${assignmentId}/submit`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ content })
    });
    
    if (response.ok) {
        showNotification('✓ Assignment submitted!', 'success');
        loadAssignments();
    } else {
        showNotification('Submission failed', 'error');
    }
}

// ==================== NOTES FUNCTIONS ====================

async function loadNotes() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/notes', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const notes = await response.json();
        const grid = document.getElementById('notesGrid');
        
        if (!notes || notes.length === 0) {
            grid.innerHTML = '<p>No notes available.</p>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < notes.length; i++) {
            const n = notes[i];
            html += `
                <div class="note-card" style="background:white; border-radius:16px; padding:15px; cursor:pointer; border:1px solid var(--border);" onclick="showNotification('Note details coming soon', 'info')">
                    <h4>${escapeHtml(n.title)}</h4>
                    <p>${escapeHtml(n.subject || 'General')} • by ${escapeHtml(n.teacher)}</p>
                    <small>${formatDate(n.createdAt)}</small>
                </div>
            `;
        }
        grid.innerHTML = html;
    } catch(e) {
        console.error('Error loading notes:', e);
    }
}

// ==================== STUDENT STATS ====================
async function loadStudentStats() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/stats', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const stats = await response.json();
        
        const pendingEl = document.getElementById('pendingCount');
        const avgEl = document.getElementById('avgScore');
        const quizEl = document.getElementById('quizCount');
        
        if (pendingEl) pendingEl.textContent = stats.pendingAssignments || 0;
        if (avgEl) avgEl.textContent = Math.round(stats.avgScore || 0) + '%';
        if (quizEl) quizEl.textContent = stats.completedQuizzes || 0;
    } catch(e) {
        console.error('Error loading student stats:', e);
    }
}

// ==================== USER INFO ====================
async function loadUserInfo() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/auth/me', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const user = await response.json();
        if (user) {
            const userNameEl = document.getElementById('userName');
            const welcomeNameEl = document.getElementById('welcomeName');
            const userAvatarEl = document.getElementById('userAvatar');
            
            if (userNameEl) userNameEl.textContent = user.fullName;
            if (welcomeNameEl) welcomeNameEl.textContent = user.fullName.split(' ')[0];
            if (userAvatarEl) userAvatarEl.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=facc15&color=000`;
        }
    } catch(e) {
        console.error('Error loading user info:', e);
    }
}

// ==================== TIMETABLE FUNCTIONS ====================

async function loadTimetable() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/timetable', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const entries = await response.json();
        currentTimetableEntries = entries;
        renderTimetable();
    } catch(e) {
        console.error('Error loading timetable:', e);
        const container = document.getElementById('timetableContainer');
        if (container) {
            container.innerHTML = '<div class="empty-timetable">⚠️ Error loading timetable. Please refresh the page.</div>';
        }
    }
}

function renderTimetable() {
    const container = document.getElementById('timetableContainer');
    if (!container) return;
    
    const filteredEntries = currentFilterDay === 'all'
        ? currentTimetableEntries
        : currentTimetableEntries.filter(e => e.dayOfWeek === currentFilterDay);
    
    if (filteredEntries.length === 0) {
        container.innerHTML = '<div class="empty-timetable">📅 No timetable entries. Click "Add Entry" to create your schedule!</div>';
        return;
    }
    
    const grouped = {};
    filteredEntries.forEach(entry => {
        if (!grouped[entry.dayOfWeek]) grouped[entry.dayOfWeek] = [];
        grouped[entry.dayOfWeek].push(entry);
    });
    
    const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    const sortedDays = Object.keys(grouped).sort((a, b) => dayOrder.indexOf(a) - dayOrder.indexOf(b));
    
    let html = '<table class="timetable-table"><thead><tr><th>Day</th><th>Schedule</th></tr></thead><tbody>';
    
    sortedDays.forEach(day => {
        const entries = grouped[day].sort((a, b) => a.startTime.localeCompare(b.startTime));
        html += `<tr><td style="width: 120px; font-weight: 700; vertical-align: top;">${escapeHtml(day)}</td><td>`;
        
        entries.forEach(entry => {
            html += `
                <div class="timetable-entry">
                    <strong>${escapeHtml(entry.subject)}</strong>
                    <small>${entry.startTime} - ${entry.endTime}</small>
                    ${entry.location ? `<div><i class="fa-solid fa-location-dot"></i> ${escapeHtml(entry.location)}</div>` : ''}
                    ${entry.notes ? `<div><small><i class="fa-solid fa-note-sticky"></i> ${escapeHtml(entry.notes)}</small></div>` : ''}
                    <div class="entry-actions">
                        <button class="edit-entry" onclick="editTimetableEntry(${entry.id})">
                            <i class="fa-solid fa-edit"></i> Edit
                        </button>
                        <button class="delete-entry" onclick="deleteTimetableEntry(${entry.id})">
                            <i class="fa-solid fa-trash"></i> Delete
                        </button>
                    </div>
                </div>
            `;
        });
        
        html += `</td></tr>`;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

function filterTimetableByDay(day) {
    currentFilterDay = day;
    const filters = document.querySelectorAll('.day-filter');
    filters.forEach(btn => btn.classList.remove('active'));
    if (window.event && window.event.currentTarget) {
        window.event.currentTarget.classList.add('active');
    }
    renderTimetable();
}

function openTimetableModal(entry) {
    entry = entry || null;
    const modal = document.getElementById('timetableModal');
    if (!modal) return;
    
    const form = document.getElementById('timetableForm');
    if (form) form.reset();
    
    if (entry) {
        document.getElementById('modalTitle').textContent = 'Edit Timetable Entry';
        document.getElementById('entryId').value = entry.id;
        document.getElementById('dayOfWeek').value = entry.dayOfWeek;
        document.getElementById('startTime').value = entry.startTime;
        document.getElementById('endTime').value = entry.endTime;
        document.getElementById('subject').value = entry.subject;
        document.getElementById('location').value = entry.location || '';
        document.getElementById('notes').value = entry.notes || '';
    } else {
        document.getElementById('modalTitle').textContent = 'Create Timetable Entry';
        document.getElementById('entryId').value = '';
    }
    
    modal.classList.add('active');
}

function closeTimetableModal() {
    const modal = document.getElementById('timetableModal');
    if (modal) modal.classList.remove('active');
}

async function editTimetableEntry(entryId) {
    const entry = currentTimetableEntries.find(e => e.id === entryId);
    if (entry) openTimetableModal(entry);
}

async function deleteTimetableEntry(entryId) {
    if (!confirm('Are you sure you want to delete this timetable entry?')) return;
    
    const token = localStorage.getItem('accessToken');
    try {
        const response = await fetch(API_BASE_URL + `/timetable/${entryId}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const data = await response.json();
        
        if (data.success) {
            showNotification('✓ Entry deleted successfully', 'success');
            loadTimetable();
        } else {
            showNotification('Error deleting entry: ' + (data.error || 'Unknown error'), 'error');
        }
    } catch(e) {
        showNotification('Error: ' + e.message, 'error');
    }
}

const timetableForm = document.getElementById('timetableForm');
if (timetableForm) {
    timetableForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const entryId = document.getElementById('entryId').value;
        const data = {
            dayOfWeek: document.getElementById('dayOfWeek').value,
            startTime: document.getElementById('startTime').value,
            endTime: document.getElementById('endTime').value,
            subject: document.getElementById('subject').value,
            location: document.getElementById('location').value,
            notes: document.getElementById('notes').value
        };
        
        if (!data.dayOfWeek || !data.startTime || !data.endTime || !data.subject) {
            showNotification('Please fill in all required fields', 'error');
            return;
        }
        
        const url = entryId ? API_BASE_URL + `/timetable/${entryId}` : API_BASE_URL + '/timetable';
        const method = entryId ? 'PUT' : 'POST';
        const token = localStorage.getItem('accessToken');
        
        try {
            const response = await fetch(url, {
                method: method,
                headers: getAuthHeaders(),
                body: JSON.stringify(data)
            });
            const result = await response.json();
            
            if (result.success) {
                showNotification(entryId ? '✓ Entry updated successfully' : '✓ Entry created successfully', 'success');
                closeTimetableModal();
                loadTimetable();
            } else {
                showNotification('Error: ' + (result.error || 'Unknown error'), 'error');
            }
        } catch(e) {
            showNotification('Error: ' + e.message, 'error');
        }
    });
}

// ==================== QUIZ FUNCTIONS ====================

async function loadQuizzes() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/quizzes', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const quizzes = await response.json();
        const selector = document.getElementById('quizSelector');
        
        if (!selector) return;
        
        if (quizzes.length === 0) {
            selector.innerHTML = '<p>No quizzes available.</p>';
            return;
        }
        
        let options = '<option value="">-- Select a quiz to take --</option>';
        for (let i = 0; i < quizzes.length; i++) {
            const q = quizzes[i];
            options += `<option value="${q.id}" ${q.attempted ? 'disabled' : ''}>${escapeHtml(q.title)} (${q.subject}) - ${q.questionCount} questions${q.attempted ? ' ✓ Completed' : ''}</option>`;
        }
        selector.innerHTML = `<select id="quizSelect" onchange="selectQuiz(this.value)" style="width:100%; padding:15px; margin-bottom:20px; border-radius:12px; border:2px solid var(--border);">${options}</select><div id="quizQuestions"></div>`;
    } catch(e) {
        console.error('Error loading quizzes:', e);
    }
}

async function selectQuiz(quizId) {
    if (!quizId) return;
    
    const token = localStorage.getItem('accessToken');
    try {
        const response = await fetch(API_BASE_URL + `/quizzes/${quizId}/take`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        currentQuiz = await response.json();
        currentQuestionIndex = 0;
        answers = {};
        displayQuestion();
    } catch(e) {
        showNotification('Error loading quiz: ' + e.message, 'error');
    }
}

function displayQuestion() {
    if (!currentQuiz || currentQuestionIndex >= currentQuiz.questions.length) {
        if (currentQuiz && currentQuiz.questions.length > 0) submitQuiz();
        return;
    }
    
    const q = currentQuiz.questions[currentQuestionIndex];
    const countDisplay = document.getElementById('quizCountDisplay');
    const progressBar = document.getElementById('quizProgress');
    
    if (countDisplay) countDisplay.innerHTML = `Question ${currentQuestionIndex + 1} of ${currentQuiz.questions.length}`;
    if (progressBar) progressBar.style.width = `${((currentQuestionIndex + 1) / currentQuiz.questions.length) * 100}%`;
    
    const container = document.getElementById('quizQuestions');
    if (!container) return;
    
    let optionsHtml = '';
    const letters = ['A', 'B', 'C', 'D'];
    for (let i = 0; i < letters.length; i++) {
        const letter = letters[i];
        const selectedClass = (answers[q.id] === letter) ? 'selected' : '';
        optionsHtml += `<div class="opt ${selectedClass}" onclick="selectAnswer(${q.id}, '${letter}')">${letter}. ${escapeHtml(q.options[i])}</div>`;
    }
    
    container.innerHTML = `<h2 class="q-text">${escapeHtml(q.text)}</h2><div class="options">${optionsHtml}</div><button class="btn-yellow-full" onclick="nextQuestion()">${currentQuestionIndex === currentQuiz.questions.length - 1 ? 'Submit Quiz' : 'Next Question'}</button>`;
}

function selectAnswer(questionId, answer) {
    answers[questionId] = answer;
    const opts = document.querySelectorAll('.opt');
    opts.forEach(opt => opt.classList.remove('selected'));
    if (window.event && window.event.currentTarget) window.event.currentTarget.classList.add('selected');
}

function nextQuestion() {
    currentQuestionIndex++;
    displayQuestion();
}

async function submitQuiz() {
    const token = localStorage.getItem('accessToken');
    try {
        const response = await fetch(API_BASE_URL + `/quizzes/${currentQuiz.id}/submit`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ answers })
        });
        const result = await response.json();
        showNotification(`🎉 Quiz completed! Your score: ${result.score.toFixed(1)}%`, 'success');
        loadQuizzes();
        loadStudentStats();
        
        const questionsDiv = document.getElementById('quizQuestions');
        if (questionsDiv) questionsDiv.innerHTML = '<p style="text-align:center;">✓ Quiz submitted! Select another quiz to continue.</p>';
        
        const quizSelect = document.getElementById('quizSelect');
        if (quizSelect) quizSelect.value = '';
    } catch(e) {
        showNotification('Error submitting quiz: ' + e.message, 'error');
    }
}

// ==================== TEACHER FUNCTIONS ====================

async function loadTeacherStats() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/stats', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const stats = await response.json();
        
        const assignmentsEl = document.getElementById('assignmentsCount');
        const quizzesEl = document.getElementById('quizzesCount');
        const submissionsEl = document.getElementById('submissionsCount');
        const studentsEl = document.getElementById('studentsCount');
        const statsEl = document.getElementById('teacherStats');
        
        if (assignmentsEl) assignmentsEl.textContent = stats.assignments || 0;
        if (quizzesEl) quizzesEl.textContent = stats.quizzes || 0;
        if (submissionsEl) submissionsEl.textContent = stats.submissions || 0;
        if (studentsEl) studentsEl.textContent = stats.students || 0;
        if (statsEl) statsEl.innerHTML = `You have ${stats.submissions || 0} pending submissions to grade`;
    } catch(e) {
        console.error('Error loading teacher stats:', e);
    }
}

async function loadSubmissions() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/submissions', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const submissions = await response.json();
        const tbody = document.getElementById('submissionsList');
        
        if (!tbody) return;
        
        if (submissions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5">No submissions yet</td></tr>';
            return;
        }
        
        const pendingSubmissions = submissions.filter(s => !s.graded);
        let html = '';
        for (let i = 0; i < pendingSubmissions.length; i++) {
            const sub = pendingSubmissions[i];
            html += `<tr>
                <td>${escapeHtml(sub.student)}</td>
                <td>${escapeHtml(sub.assignmentTitle)}</td>
                <td>${formatDateTime(sub.submittedAt)}</td>
                <td><input type="number" class="score-input" id="score-${sub.id}" placeholder="Score"></td>
                <td><button class="btn-save" onclick="gradeSubmission(${sub.id})">Save Grade</button></td>
            </tr>`;
        }
        tbody.innerHTML = html;
    } catch(e) {
        console.error('Error loading submissions:', e);
    }
}

async function gradeSubmission(submissionId) {
    const scoreInput = document.getElementById(`score-${submissionId}`);
    const score = scoreInput.value;
    
    if (score === "" || score < 0 || score > 100) {
        showNotification("Please enter a valid score between 0 and 100", 'error');
        return;
    }
    
    const btn = window.event.currentTarget;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';
    btn.disabled = true;
    const token = localStorage.getItem('accessToken');
    
    try {
        const response = await fetch(API_BASE_URL + `/submissions/${submissionId}/grade`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ score: parseFloat(score) })
        });
        
        if (response.ok) {
            showNotification('✓ Grade saved successfully!', 'success');
            loadSubmissions();
            loadTeacherStats();
        } else {
            showNotification('Error saving grade', 'error');
        }
    } catch(e) {
        showNotification('Error: ' + e.message, 'error');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

async function loadPendingGrading() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/submissions', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const submissions = await response.json();
        const pending = submissions.filter(s => !s.graded);
        const container = document.getElementById('pendingGrading');
        
        if (!container) return;
        
        if (pending.length === 0) {
            container.innerHTML = '<li>No pending submissions</li>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < Math.min(pending.length, 5); i++) {
            const s = pending[i];
            html += `<li>
                <div>
                    <span class="item-title">${escapeHtml(s.student)} - ${escapeHtml(s.assignmentTitle)}</span>
                    <div class="item-date">Submitted: ${formatDate(s.submittedAt)}</div>
                </div>
                <span class="item-status status-pending">Pending</span>
            </li>`;
        }
        container.innerHTML = html;
    } catch(e) {
        console.error('Error loading pending grading:', e);
    }
}

async function loadTeacherCourses() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/courses/teacher', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const courses = await response.json();
        const container = document.getElementById('teacherCoursesList');
        const courseSelect = document.getElementById('textbookCourseId');
        
        if (!container) return;
        
        if (!courses || courses.length === 0) {
            container.innerHTML = '<p>No courses created yet.</p>';
            if (courseSelect) courseSelect.innerHTML = '<option value="">-- No courses available --</option>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < courses.length; i++) {
            const course = courses[i];
            html += `<div class="course-card">
                <h4>${escapeHtml(course.name)}</h4>
                <p>${escapeHtml(course.description || 'No description')}</p>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 10px;">
                    <span style="font-size: 12px; color: var(--text-gray);"><i class="fa-solid fa-users"></i> Enrolled: ${course.enrolledCount || 0} students</span>
                    <span style="font-size: 12px; color: var(--text-gray);"><i class="fa-regular fa-calendar"></i> Created: ${formatDate(course.createdAt)}</span>
                </div>
            </div>`;
        }
        container.innerHTML = html;
        
        if (courseSelect) {
            let options = '<option value="">-- Select Course (Optional) --</option>';
            for (let i = 0; i < courses.length; i++) {
                options += `<option value="${courses[i].id}">${escapeHtml(courses[i].name)}</option>`;
            }
            courseSelect.innerHTML = options;
        }
    } catch(e) {
        console.error('Error loading teacher courses:', e);
    }
}

async function createCourse() {
    const name = document.getElementById('courseName')?.value;
    const description = document.getElementById('courseDescription')?.value;
    const price = document.getElementById('coursePrice')?.value;
    
    if (!name) {
        showNotification('Please enter a course name', 'error');
        return;
    }
    
    const btn = window.event.currentTarget;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Creating...';
    btn.disabled = true;
    const token = localStorage.getItem('accessToken');
    
    try {
        const response = await fetch(API_BASE_URL + '/courses', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ name, description, price: parseFloat(price) || 29.99 })
        });
        const data = await response.json();
        
        if (data.success) {
            showNotification('✓ Course created successfully!', 'success');
            document.getElementById('courseName').value = '';
            document.getElementById('courseDescription').value = '';
            loadTeacherCourses();
        } else {
            showNotification(data.error || 'Error creating course', 'error');
        }
    } catch(e) {
        showNotification('Error: ' + e.message, 'error');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

// ==================== TEXTBOOK FUNCTIONS ====================

async function uploadTextbook() {
    const title = document.getElementById('textbookTitle')?.value;
    const description = document.getElementById('textbookDescription')?.value;
    const courseId = document.getElementById('textbookCourseId')?.value;
    const fileInput = document.getElementById('textbookFile');
    const file = fileInput?.files[0];
    
    if (!title) {
        showNotification('Please enter a title', 'error');
        return;
    }
    
    if (!file) {
        showNotification('Please select a file to upload', 'error');
        return;
    }
    
    if (!courseId) {
        showNotification('Please select a course', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    formData.append('description', description || '');
    formData.append('courseId', courseId);
    
    const btn = window.event.currentTarget;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Uploading...';
    btn.disabled = true;
    const token = localStorage.getItem('accessToken');
    
    try {
        const response = await fetch(API_BASE_URL + '/textbooks/upload', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token },
            body: formData
        });
        const data = await response.json();
        
        if (data.success) {
            showNotification('✓ Textbook uploaded successfully!', 'success');
            document.getElementById('textbookTitle').value = '';
            document.getElementById('textbookDescription').value = '';
            if (fileInput) fileInput.value = '';
            loadTeacherTextbooks();
        } else {
            showNotification('Error uploading textbook: ' + (data.error || 'Unknown error'), 'error');
        }
    } catch(e) {
        showNotification('Error: ' + e.message, 'error');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

async function loadTeacherTextbooks() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/textbooks/teacher', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const textbooks = await response.json();
        const container = document.getElementById('teacherTextbooksList');
        
        if (!container) return;
        
        if (!textbooks || textbooks.length === 0) {
            container.innerHTML = '<p>No textbooks uploaded yet.</p>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < textbooks.length; i++) {
            const tb = textbooks[i];
            html += `<div class="textbook-item">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <i class="fa-solid fa-book"></i> <strong>${escapeHtml(tb.title)}</strong>
                        <div style="font-size: 12px; color: var(--text-gray);">${escapeHtml(tb.description || '')}</div>
                        <div style="font-size: 11px; color: var(--text-gray);"><i class="fa-regular fa-clock"></i> Uploaded: ${formatDate(tb.uploadDate)}</div>
                    </div>
                    <div><span style="font-size: 12px; background: var(--bg); padding: 4px 8px; border-radius: 8px;">${tb.fileName ? tb.fileName.substring(tb.fileName.lastIndexOf('.') + 1).toUpperCase() : 'FILE'}</span></div>
                </div>
            </div>`;
        }
        container.innerHTML = html;
    } catch(e) {
        console.error('Error loading teacher textbooks:', e);
    }
}

// ==================== ADMIN FUNCTIONS ====================

async function loadAdminStats() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const [teachersRes, studentsRes, assignmentsRes, quizzesRes] = await Promise.all([
            fetch(API_BASE_URL + '/teachers', { headers: { 'Authorization': 'Bearer ' + token } }),
            fetch(API_BASE_URL + '/students', { headers: { 'Authorization': 'Bearer ' + token } }),
            fetch(API_BASE_URL + '/assignments', { headers: { 'Authorization': 'Bearer ' + token } }),
            fetch(API_BASE_URL + '/quizzes', { headers: { 'Authorization': 'Bearer ' + token } })
        ]);
        
        const teachers = await teachersRes.json();
        const students = await studentsRes.json();
        const assignments = await assignmentsRes.json();
        const quizzes = await quizzesRes.json();
        
        const teacherEl = document.getElementById('teacherCount');
        const studentEl = document.getElementById('studentCount');
        const assignmentEl = document.getElementById('assignmentCount');
        const quizEl = document.getElementById('quizCount');
        const statsEl = document.getElementById('adminStats');
        
        if (teacherEl) teacherEl.textContent = teachers.length;
        if (studentEl) studentEl.textContent = students.length;
        if (assignmentEl) assignmentEl.textContent = assignments.length;
        if (quizEl) quizEl.textContent = quizzes.length;
        if (statsEl) statsEl.innerHTML = `Managing ${teachers.length} teachers and ${students.length} students`;
    } catch(e) {
        console.error('Error loading admin stats:', e);
    }
}

async function loadTeachers() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/teachers', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const teachers = await response.json();
        const tbody = document.getElementById('teachersList');
        
        if (!tbody) return;
        
        if (teachers.length === 0) {
            tbody.innerHTML = '<td><td colspan="3">No teachers yet</td></tr>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < teachers.length; i++) {
            const t = teachers[i];
            html += `<tr><td>${escapeHtml(t.fullName)}</td><td>${escapeHtml(t.email)}</td><td>${formatDate(t.createdAt)}</td></tr>`;
        }
        tbody.innerHTML = html;
    } catch(e) {
        console.error('Error loading teachers:', e);
    }
}

async function loadStudents() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/students', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const students = await response.json();
        const tbody = document.getElementById('studentsList');
        
        if (!tbody) return;
        
        if (students.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3">No students yet</td></tr>';
            return;
        }
        
        let html = '';
        for (let i = 0; i < students.length; i++) {
            const s = students[i];
            html += `<tr><td>${escapeHtml(s.fullName)}</td><td>${escapeHtml(s.email)}</td><td><span style="color: #22c55e;">● Active</span></td></tr>`;
        }
        tbody.innerHTML = html;
    } catch(e) {
        console.error('Error loading students:', e);
    }
}

async function addTeacher() {
    const fullName = document.getElementById('teacherName')?.value;
    const email = document.getElementById('teacherEmail')?.value;
    const username = document.getElementById('teacherUsername')?.value;
    
    if (!fullName || !email || !username) {
        showNotification('Please fill all fields', 'error');
        return;
    }
    
    const btn = window.event.currentTarget;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Adding...';
    btn.disabled = true;
    const token = localStorage.getItem('accessToken');
    
    try {
        const response = await fetch(API_BASE_URL + '/teachers', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ full_name: fullName, email, username, password: 'password123' })
        });
        
        if (response.ok) {
            showNotification('✓ Teacher added successfully! Default password: password123', 'success');
            document.getElementById('teacherName').value = '';
            document.getElementById('teacherEmail').value = '';
            document.getElementById('teacherUsername').value = '';
            loadTeachers();
            loadAdminStats();
        } else {
            showNotification('Error adding teacher', 'error');
        }
    } catch(e) {
        showNotification('Error: ' + e.message, 'error');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

function saveSettings() {
    showNotification('Settings saved successfully!', 'success');
}

async function loadAllCourses() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/courses', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const courses = await response.json();
        const container = document.getElementById('allCoursesList');
        
        if (!container) return;
        
        if (!courses || courses.length === 0) {
            container.innerHTML = '<p>No courses available.</p>';
            loadTeachersForSelect();
            return;
        }
        
        let html = '';
        for (let i = 0; i < courses.length; i++) {
            const course = courses[i];
            html += `<div class="course-card">
                <h4>${escapeHtml(course.name)}</h4>
                <p>${escapeHtml(course.description || 'No description')}</p>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 10px;">
                    <span style="font-size: 12px; color: var(--text-gray);"><i class="fa-solid fa-chalkboard-user"></i> Teacher: ${escapeHtml(course.teacherName)}</span>
                    <span style="font-size: 12px; color: var(--text-gray);"><i class="fa-solid fa-users"></i> Enrolled: ${course.enrolledCount || 0}</span>
                </div>
            </div>`;
        }
        container.innerHTML = html;
        loadTeachersForSelect();
    } catch(e) {
        console.error('Error loading all courses:', e);
    }
}

async function loadTeachersForSelect() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;
    
    try {
        const response = await fetch(API_BASE_URL + '/teachers', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const teachers = await response.json();
        const select = document.getElementById('adminCourseTeacherId');
        
        if (select) {
            let options = '<option value="">-- Select Teacher --</option>';
            for (let i = 0; i < teachers.length; i++) {
                options += `<option value="${teachers[i].id}">${escapeHtml(teachers[i].fullName)} (${escapeHtml(teachers[i].email)})</option>`;
            }
            select.innerHTML = options;
        }
    } catch(e) {
        console.error('Error loading teachers for select:', e);
    }
}

async function adminCreateCourse() {
    const name = document.getElementById('adminCourseName')?.value;
    const description = document.getElementById('adminCourseDescription')?.value;
    const teacherId = document.getElementById('adminCourseTeacherId')?.value;
    
    if (!name) {
        showNotification('Please enter a course name', 'error');
        return;
    }
    
    if (!teacherId) {
        showNotification('Please select a teacher', 'error');
        return;
    }
    
    const btn = window.event.currentTarget;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Creating...';
    btn.disabled = true;
    const token = localStorage.getItem('accessToken');
    
    try {
        const response = await fetch(API_BASE_URL + '/courses', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ name, description, teacherId })
        });
        const data = await response.json();
        
        if (data.success) {
            showNotification('✓ Course created successfully!', 'success');
            document.getElementById('adminCourseName').value = '';
            document.getElementById('adminCourseDescription').value = '';
            loadAllCourses();
        } else {
            showNotification('Error creating course', 'error');
        }
    } catch(e) {
        showNotification('Error: ' + e.message, 'error');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

// ==================== INITIALIZATION ====================

document.addEventListener('DOMContentLoaded', () => {
    const path = window.location.pathname;
    console.log('Initializing dashboard for path:', path);
    
    const lastSection = localStorage.getItem('lastSection');
    if (lastSection && document.getElementById(lastSection)) {
        showSection(lastSection);
    }
    
    if (path.includes('/student')) {
        console.log('Loading student dashboard');
        loadUserInfo();
        loadSubscriptionStatus();
        loadStudentStats();
        loadMyCourses();
        loadAvailableCourses();
        loadBillingSummary();
        loadAssignments();
        loadNotes();
        loadTimetable();
        loadQuizzes();
    } else if (path.includes('/teacher')) {
        console.log('Loading teacher dashboard');
        loadTeacherStats();
        loadTimetable();
        loadSubmissions();
        loadPendingGrading();
        loadTeacherCourses();
        loadTeacherTextbooks();
        loadNotes();
        loadAssignments();
    } else if (path.includes('/admin')) {
        console.log('Loading admin dashboard');
        loadAdminStats();
        loadTeachers();
        loadStudents();
        loadAllCourses();
        
        if (typeof Chart !== 'undefined') {
            const ctx = document.getElementById('activityChart')?.getContext('2d');
            if (ctx) {
                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
                        datasets: [{
                            label: 'Active Users',
                            data: [45, 62, 78, 94],
                            borderColor: '#3b82f6',
                            backgroundColor: 'rgba(59, 130, 246, 0.1)',
                            tension: 0.4,
                            fill: true
                        }]
                    },
                    options: { responsive: true, maintainAspectRatio: true, plugins: { legend: { position: 'bottom' } } }
                });
            }
            
            const pieCtx = document.getElementById('activityDistChart')?.getContext('2d');
            if (pieCtx) {
                new Chart(pieCtx, {
                    type: 'pie',
                    data: {
                        labels: ['Students', 'Teachers', 'Admins'],
                        datasets: [{ data: [75, 20, 5], backgroundColor: ['#3b82f6', '#facc15', '#ef4444'] }]
                    },
                    options: { responsive: true }
                });
            }
            
            const growthCtx = document.getElementById('userGrowthChart')?.getContext('2d');
            if (growthCtx) {
                new Chart(growthCtx, {
                    type: 'bar',
                    data: {
                        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                        datasets: [{ label: 'New Users', data: [12, 19, 15, 27, 34, 42], backgroundColor: '#3b82f6', borderRadius: 8 }]
                    },
                    options: { responsive: true }
                });
            }
        }
    }
});

// Export functions for global use
window.showSection = showSection;
window.filterTimetableByDay = filterTimetableByDay;
window.openTimetableModal = openTimetableModal;
window.closeTimetableModal = closeTimetableModal;
window.editTimetableEntry = editTimetableEntry;
window.deleteTimetableEntry = deleteTimetableEntry;
window.loadTimetable = loadTimetable;
window.loadNotes = loadNotes;
window.loadAssignments = loadAssignments;
window.loadQuizzes = loadQuizzes;
window.submitAssignment = submitAssignment;
window.publishAssignment = publishAssignment;
window.selectQuiz = selectQuiz;
window.selectAnswer = selectAnswer;
window.nextQuestion = nextQuestion;
window.setCorrect = setCorrect;
window.publishQuestion = publishQuestion;
window.gradeSubmission = gradeSubmission;
window.addTeacher = addTeacher;
window.saveSettings = saveSettings;
window.loadSubscriptionStatus = loadSubscriptionStatus;
window.loadMyCourses = loadMyCourses;
window.loadAvailableCourses = loadAvailableCourses;
window.enrollInCourse = enrollInCourse;
window.unenrollFromCourse = unenrollFromCourse;
window.viewCourseContent = viewCourseContent;
window.loadBillingSummary = loadBillingSummary;
window.payForCourse = payForCourse;
window.payAllOutstanding = payAllOutstanding;
window.loadTeacherCourses = loadTeacherCourses;
window.createCourse = createCourse;
window.uploadTextbook = uploadTextbook;
window.loadTeacherTextbooks = loadTeacherTextbooks;
window.loadAllCourses = loadAllCourses;
window.adminCreateCourse = adminCreateCourse;
window.logout = logout;