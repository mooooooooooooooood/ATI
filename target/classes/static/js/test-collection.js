// Test Collection Page JavaScript

// Global variables
let currentPage = 1;
let totalPages = 20;
let currentSort = 'newest';
let searchQuery = '';
let allTests = [];

// Initialize test collection page
document.addEventListener('DOMContentLoaded', function() {
    console.log('Test Collection page initialized');
    
    // Load tests
    loadTests();
    
    // Setup event listeners
    setupSortFilters();
    setupSearch();
    setupPagination();
    
    // Animate test cards on load
    animateTestCards();
});

// Load tests from backend
async function loadTests() {
    try {
        // TODO: Replace with actual API call
        // const response = await fetch(`/api/tests?type=speaking&page=${currentPage}&sort=${currentSort}`);
        // const data = await response.json();
        
        // For now, generate sample data
        allTests = generateSampleTests();
        renderTests(allTests);
        
    } catch (error) {
        console.error('Error loading tests:',showError('Failed to load tests. Please try again.');
    }
}

// Generate sample tests data
function generateSampleTests() {
    const tests = [];
    const cams = [20, 19, 18, 17];
    const backgrounds = ['purple', 'beige', 'dark', 'green'];
    
    cams.forEach((cam, index) => {
        for (let i = 4; i >= 1; i--) {
            tests.push({
                id: `cam${cam}-test${i}`,
                title: `CAM ${cam} - Speaking Test ${i}`,
                views: '33K lượt làm',
                background: backgrounds[index],
                cam: cam,
                testNumber: i
            });
        }
    });
    
    return tests;
}

// Render tests to grid
function renderTests(tests) {
    const grid = document.querySelector('.test-grid');
    if (!grid) return;
    
    // Clear existing content
    grid.innerHTML = '';
    
    // Render each test card
    tests.forEach((test, index) => {
        const card = createTestCard(test);
        card.style.animationDelay = `${index * 0.05}s`;
        grid.appendChild(card);
    });
}

// Create test card element
function createTestCard(test) {
    const card = document.createElement('div');
    card.className = 'test-card';
    card.setAttribute('data-bg', test.background);
    card.setAttribute('data-test-id', test.id);
    
    card.innerHTML = `
        <div class="test-thumbnail">
            <img src="/images/cam${test.cam}-cover.png" alt="CAM ${test.cam}">
        </div>
        <h3>${test.title}</h3>
        <p class="test-views">${test.views}</p>
        <button class="btn-do-test" onclick="startTest('speaking', '${test.id}')">
            <span class="icon">▶</span> Do the test
            <span class="dropdown">▼</span>
        </button>
    `;
    
    return card;
}

// Start test function
function startTest(type, testId) {
    console.log(`Starting ${type} test:`, testId);
    
    // Track test start
    trackTestAction('start_test', { type, testId });
    
    // Show loading
    if (window.IELTSApp) {
        window.IELTSApp.showLoading();
    }
    
    // Navigate to test page
    setTimeout(() => {
        window.location.href = `/test/${type}/${testId}`;
    }, 500);
}

// Setup sort filters
function setupSortFilters() {
    const radioOptions = document.querySelectorAll('input[name="sort"]');
    
    radioOptions.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                currentSort = this.value;
                sortTests(currentSort);
                trackTestAction('sort_change', { sort: currentSort });
            }
        });
    });
}

// Sort tests based on selected filter
function sortTests(sortBy) {
    let sortedTests = [...allTests];
    
    switch(sortBy) {
        case 'newest':
            sortedTests.sort((a, b) => b.cam - a.cam || b.testNumber - a.testNumber);
            break;
        case 'oldest':
            sortedTests.sort((a, b) => a.cam - b.cam || a.testNumber - b.testNumber);
            break;
        case 'most-attempted':
            // Sort by views (in real app, this would be from backend)
            sortedTests.sort((a, b) => {
                const viewsA = parseInt(a.views.replace('K', '')) * 1000;
                const viewsB = parseInt(b.views.replace('K', '')) * 1000;
                return viewsB - viewsA;
            });
            break;
    }
    
    renderTests(sortedTests);
    animateTestCards();
}

// Setup search functionality
function setupSearch() {
    const searchInputs = [
        document.getElementById('searchInput'),
        document.getElementById('sidebarSearch')
    ];
    
    searchInputs.forEach(input => {
        if (!input) return;
        
        let searchTimeout;
        input.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            const query = this.value.trim().toLowerCase();
            
            searchTimeout = setTimeout(() => {
                searchQuery = query;
                filterTests(query);
            }, 300);
        });
    });
    
    // Search button in sidebar
    const searchBtn = document.querySelector('.search-box-sidebar button');
    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const input = document.getElementById('sidebarSearch');
            if (input) {
                filterTests(input.value.trim().toLowerCase());
            }
        });
    }
}

// Filter tests based on search query
function filterTests(query) {
    if (!query) {
        renderTests(allTests);
        animateTestCards();
        return;
    }
    
    const filteredTests = allTests.filter(test => {
        return test.title.toLowerCase().includes(query) ||
               test.id.toLowerCase().includes(query);
    });
    
    renderTests(filteredTests);
    animateTestCards();
    
    // Track search
    trackTestAction('search', { query: query, results: filteredTests.length });
    
    // Show message if no results
    if (filteredTests.length === 0) {
        showNoResults();
    }
}

// Show no results message
function showNoResults() {
    const grid = document.querySelector('.test-grid');
    if (!grid) return;
    
    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px;">
            <h3 style="font-size: 24px; color: #666; margin-bottom: 10px;">
                No tests found
            </h3>
            <p style="color: #999;">
                Try adjusting your search or filters
            </p>
        </div>
    `;
}

// Setup pagination
function setupPagination() {
    const pageButtons = document.querySelectorAll('.page-btn');
    
    pageButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const text = this.textContent.trim();
            
            if (text === '‹') {
                goToPage(Math.max(1, currentPage - 1));
            } else if (text === '›') {
                goToPage(Math.min(totalPages, currentPage + 1));
            } else if (!isNaN(text)) {
                goToPage(parseInt(text));
            }
        });
    });
}

// Go to specific page
function goToPage(pageNumber) {
    if (pageNumber === currentPage) return;
    
    currentPage = pageNumber;
    
    // Update active state
    document.querySelectorAll('.page-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.textContent.trim() === pageNumber.toString()) {
            btn.classList.add('active');
        }
    });
    
    // Scroll to top smoothly
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
    
    // Load new page data
    loadTests();
    
    // Track pagination
    trackTestAction('pagination', { page: pageNumber });
}

// Animate test cards on load/filter
function animateTestCards() {
    const cards = document.querySelectorAll('.test-card');
    
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.4s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 50);
    });
}

// Track test-related actions
function trackTestAction(action, data) {
    console.log('Test Action:', action, data);
    // TODO: Implement actual tracking
    // analytics.track(action, data);
}

// Show error message
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-toast';
    errorDiv.textContent = message;
    errorDiv.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        background: #f44336;
        color: white;
        padding: 15px 25px;
        border-radius: 8px;
        box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        z-index: 10000;
        animation: slideInRight 0.3s ease;
    `;
    
    document.body.appendChild(errorDiv);
    
    setTimeout(() => {
        errorDiv.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => errorDiv.remove(), 300);
    }, 3000);
}

// Handle test card hover effects
document.addEventListener('mouseover', function(e) {
    const card = e.target.closest('.test-card');
    if (card) {
        const thumbnail = card.querySelector('.test-thumbnail img');
        if (thumbnail) {
            thumbnail.style.transform = 'scale(1.1) rotate(2deg)';
        }
    }
});

document.addEventListener('mouseout', function(e) {
    const card = e.target.closest('.test-card');
    if (card) {
        const thumbnail = card.querySelector('.test-thumbnail img');
        if (thumbnail) {
            thumbnail.style.transform = 'scale(1) rotate(0deg)';
        }
    }
});

// Lazy load images
function lazyLoadImages() {
    const images = document.querySelectorAll('.test-thumbnail img');
    
    const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.getAttribute('src');
                img.classList.add('loaded');
                imageObserver.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
}

// Call lazy load on page load
setTimeout(lazyLoadImages, 500);

// Export functions for global use
window.startTest = startTest;// Test Collection Page JavaScript

// Global variables
let currentPage = 1;
let totalPages = 20;
let currentSort = 'newest';
let searchQuery = '';
let allTests = [];

// Initialize test collection page
document.addEventListener('DOMContentLoaded', function() {
    console.log('Test Collection page initialized');
    
    // Load tests
    loadTests();
    
    // Setup event listeners
    setupSortFilters();
    setupSearch();
    setupPagination();
    
    // Animate test cards on load
    animateTestCards();
});

// Load tests from backend
async function loadTests() {
    try {
        // TODO: Replace with actual API call
        // const response = await fetch(`/api/tests?type=speaking&page=${currentPage}&sort=${currentSort}`);
        // const data = await response.json();
        
        // For now, generate sample data
        allTests = generateSampleTests();
        renderTests(allTests);
        
    } catch (error) {
        console.error('Error loading tests:',