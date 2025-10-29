// Main JavaScript for IELTS Website

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize all components
    initSearchBox();
    initUserDropdown();
    initNavDropdown();
    initCategoryCards();
    initLoginButtons();
    initDashboardChart();
    
});

// Search Box Functionality
function initSearchBox() {
    const searchInput = document.getElementById('searchInput');
    const searchIcon = document.querySelector('.search-box span');
    
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch(this.value);
            }
        });
    }
    
    if (searchIcon) {
        searchIcon.addEventListener('click', function() {
            if (searchInput) {
                performSearch(searchInput.value);
            }
        });
    }
}

function performSearch(query) {
    if (query.trim()) {
        console.log('Searching for:', query);
        // Implement search functionality here
        // window.location.href = '/search?q=' + encodeURIComponent(query);
    }
}

// User Profile Dropdown
function initUserDropdown() {
    const userProfile = document.querySelector('.user-profile');
    
    if (userProfile) {
        userProfile.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleUserMenu();
        });
        
        // Close dropdown when clicking outside
        document.addEventListener('click', function() {
            closeUserMenu();
        });
    }
}

function toggleUserMenu() {
    // Create dropdown menu if it doesn't exist
    let dropdown = document.querySelector('.user-dropdown-menu');
    
    if (!dropdown) {
        dropdown = createUserDropdown();
        document.querySelector('.user-profile').appendChild(dropdown);
    }
    
    dropdown.classList.toggle('active');
}

function createUserDropdown() {
    const dropdown = document.createElement('div');
    dropdown.className = 'user-dropdown-menu';
    dropdown.innerHTML = `
        <a href="/profile">Profile</a>
        <a href="/settings">Settings</a>
        <a href="/logout">Logout</a>
    `;
    return dropdown;
}

function closeUserMenu() {
    const dropdown = document.querySelector('.user-dropdown-menu');
    if (dropdown) {
        dropdown.classList.remove('active');
    }
}

// Navigation Dropdown
function initNavDropdown() {
    const navDropdown = document.querySelector('.nav-dropdown');
    
    if (navDropdown) {
        navDropdown.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleNavMenu();
        });
    }
}

function toggleNavMenu() {
    let navMenu = document.querySelector('.nav-menu');
    
    if (!navMenu) {
        navMenu = createNavMenu();
        document.querySelector('.nav-dropdown').appendChild(navMenu);
    }
    
    navMenu.classList.toggle('active');
}

function createNavMenu() {
    const menu = document.createElement('div');
    menu.className = 'nav-menu';
    menu.innerHTML = `
        <a href="/test/writing">Writing Tests</a>
        <a href="/test/listening">Listening Tests</a>
        <a href="/test/speaking">Speaking Tests</a>
        <a href="/test/reading">Reading Tests</a>
    `;
    return menu;
}

// Category Cards Animation
function initCategoryCards() {
    const cards = document.querySelectorAll('.category-card, .category-card-dashboard');
    
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

// Login Buttons for Require-Login Page
function initLoginButtons() {
    const loginBtn = document.getElementById('open-login');
    const signupBtn = document.getElementById('open-signup');
    
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            window.location.href = '/user/login';
        });
    }
    
    if (signupBtn) {
        signupBtn.addEventListener('click', function() {
            window.location.href = '/user/signup';
        });
    }
}

// Dashboard Radar Chart
function initDashboardChart() {
    const chartCanvas = document.getElementById('radarChart');
    
    if (chartCanvas) {
        // Check if Chart.js is loaded
        if (typeof Chart !== 'undefined') {
            createRadarChart();
        } else {
            console.warn('Chart.js library not loaded');
        }
    }
}

function createRadarChart() {
    const ctx = document.getElementById('radarChart').getContext('2d');
    
    const data = {
        labels: ['Writing', 'Reading', 'Listening', 'Speaking'],
        datasets: [{
            label: 'Your Scores',
            data: [7.0, 6.5, 7.5, 6.0],
            backgroundColor: 'rgba(201, 48, 44, 0.2)',
            borderColor: 'rgba(201, 48, 44, 1)',
            borderWidth: 2,
            pointBackgroundColor: 'rgba(201, 48, 44, 1)',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: 'rgba(201, 48, 44, 1)',
            pointRadius: 5,
            pointHoverRadius: 7
        }]
    };
    
    const config = {
        type: 'radar',
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: true,
            scales: {
                r: {
                    beginAtZero: true,
                    max: 9,
                    min: 0,
                    ticks: {
                        stepSize: 1,
                        font: {
                            size: 12
                        }
                    },
                    pointLabels: {
                        font: {
                            size: 14,
                            weight: 'bold'
                        }
                    },
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom',
                    labels: {
                        font: {
                            size: 14
                        },
                        padding: 20
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleFont: {
                        size: 14
                    },
                    bodyFont: {
                        size: 13
                    },
                    padding: 12,
                    displayColors: false
                }
            }
        }
    };
    
    new Chart(ctx, config);
}

// Smooth Scroll for Navigation Links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Add loading animation for page transitions
window.addEventListener('beforeunload', function() {
    document.body.style.opacity = '0.5';
});

// Helper function to show notifications
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 25px;
        background-color: ${type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : '#2196F3'};
        color: white;
        border-radius: 5px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.2);
        z-index: 9999;
        animation: slideIn 0.3s ease-out;
    `;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Add CSS animations for notifications
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
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
            transform: translateX(400px);
            opacity: 0;
        }
    }
    
    .user-dropdown-menu,
    .nav-menu {
        position: absolute;
        top: 100%;
        right: 0;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        padding: 10px 0;
        margin-top: 10px;
        min-width: 150px;
        display: none;
        z-index: 1000;
    }
    
    .user-dropdown-menu.active,
    .nav-menu.active {
        display: block;
    }
    
    .user-dropdown-menu a,
    .nav-menu a {
        display: block;
        padding: 10px 20px;
        color: #333;
        text-decoration: none;
        transition: background-color 0.2s;
    }
    
    .user-dropdown-menu a:hover,
    .nav-menu a:hover {
        background-color: #f5f5f5;
    }
`;
document.head.appendChild(style);