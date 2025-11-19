// Writing Test Collection Page JavaScript

// Global variables
let currentPage = 1;
let totalPages = 1;
let currentSort = 'newest';
let searchQuery = '';
let allTests = [];

// Initialize test collection page
document.addEventListener('DOMContentLoaded', function() {
    console.log('Writing Test Collection page initialized');
    
    // Load tests from backend (Thymeleaf will inject data)
    loadTestsFromBackend();
    
    // Setup event listeners
    setupSortFilters();
    setupSearch();
    setupPagination();
    
    // Animate test cards on load
    animateTestCards();
});

// Load tests from backend data injected by Thymeleaf
function loadTestsFromBackend() {
    try {
        // Check if tests data exists from Thymeleaf
        const testGrid = document.getElementById('writingTestGrid');
        
        // If grid already has tests from server-side rendering, extract them
        const existingCards = testGrid.querySelectorAll('.test-card');
        if (existingCards.length > 0) {
            console.log('Tests already rendered by server:', existingCards.length);
            allTests = Array.from(existingCards).map(card => ({
                id: card.getAttribute('data-test-id'),
                title: card.querySelector('h3')?.textContent || '',
                views: card.querySelector('.test-views')?.textContent || '0 lượt làm',
                background: card.getAttribute('data-bg'),
                cam: parseInt(card.getAttribute('data-cam')) || 0,
                testNumber: parseInt(card.getAttribute('data-test-number')) || 0
            }));
            return;
        }
        
        // Fallback: Generate sample data if no server data
        console.log('No server data found, generating samples...');
        allTests = generateWritingTests();
        renderTests(allTests);
        
    } catch (error) {
        console.error('Error loading tests:', error);
        showError('Failed to load tests. Please refresh the page.');
    }
}

// Generate writing tests data
function generateWritingTests() {
    const tests = [];
    const cams = [20, 19, 18, 17, 16, 15];
    const backgrounds = ['purple', 'beige', 'dark', 'green', 'blue', 'orange'];
    
    cams.forEach((cam, index) => {
        for (let i = 4; i >= 1; i--) {
            tests.push({
                id: `cam${cam}-test${i}`,
                title: `CAM ${cam} - Writing Test ${i}`,
                views: `${Math.floor(Math.random() * 50 + 10)}K lượt làm`,
                background: backgrounds[index % backgrounds.length],
                cam: cam,
                testNumber: i
            });
        }
    });
    
    return tests;
}

// Render tests to grid
function renderTests(tests) {
    const grid = document.getElementById('writingTestGrid');
    if (!grid) {
        console.error('Test grid not found!');
        return;
    }
    
    // Clear existing content
    grid.innerHTML = '';
    
    // Show message if no tests
    if (tests.length === 0) {
        showNoResults();
        return;
    }
    
    // Render each test card
    tests.forEach((test, index) => {
        const card = createTestCard(test);
        card.style.animationDelay = `${index * 0.05}s`;
        grid.appendChild(card);
    });
    
    console.log(`Rendered ${tests.length} writing tests`);
}

// Create test card element
function createTestCard(test) {
    const card = document.createElement('div');
    card.className = 'test-card';
    card.setAttribute('data-bg', test.background);
    card.setAttribute('data-test-id', test.id);
    card.setAttribute('data-cam', test.cam);
    card.setAttribute('data-test-number', test.testNumber);
    
    card.innerHTML = `
        <div class="test-thumbnail">
            <img src="/images/writing.png" alt="${test.title}" 
                 onerror="this.src='/images/Logo.png'">
        </div>
        <h3>${test.title}</h3>
        <p class="test-views">${test.views}</p>
        <button class="btn-do-test" onclick="startWritingTest('${test.id}')">
            <span class="icon">✍️</span> Do the test
            <span class="dropdown">▼</span>
        </button>
    `;
    
    return card;
}

// Start writing test
function startWritingTest(testId) {
    console.log('Starting writing test:', testId);
    
    // Show loading
    const btn = event.target.closest('button');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="icon">⏳</span> Loading...';
    }
    
    // Navigate to writing test page
    setTimeout(() => {
        window.location.href = `/writing/test/${testId}`;
    }, 300);
}

// Setup sort filters
function setupSortFilters() {
    const radioOptions = document.querySelectorAll('input[name="sort"]');
    
    radioOptions.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                currentSort = this.value;
                sortTests(currentSort);
                console.log('Sorting by:', currentSort);
            }
        });
    });
}

// Sort tests based on selected filter
function sortTests(sortBy) {
    let sortedTests = [...allTests];
    
    switch(sortBy) {
        case 'newest':
            sortedTests.sort((a, b) => {
                if (b.cam !== a.cam) return b.cam - a.cam;
                return b.testNumber - a.testNumber;
            });
            break;
        case 'oldest':
            sortedTests.sort((a, b) => {
                if (a.cam !== b.cam) return a.cam - b.cam;
                return a.testNumber - b.testNumber;
            });
            break;
        case 'most-attempted':
            sortedTests.sort((a, b) => {
                const viewsA = parseInt(a.views.replace(/[^\d]/g, '')) || 0;
                const viewsB = parseInt(b.views.replace(/[^\d]/g, '')) || 0;
                return viewsB - viewsA;
            });
            break;
    }
    
    renderTests(sortedTests);
    animateTestCards();
}

// Setup search functionality
function setupSearch() {
    const searchInput = document.getElementById('sidebarSearch');
    
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            const query = this.value.trim().toLowerCase();
            
            searchTimeout = setTimeout(() => {
                searchQuery = query;
                filterTests(query);
            }, 300);
        });
    }
    
    // Search button
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
               test.id.toLowerCase().includes(query) ||
               `cam${test.cam}`.includes(query) ||
               `test${test.testNumber}`.includes(query);
    });
    
    renderTests(filteredTests);
    animateTestCards();
    
    console.log(`Search "${query}": found ${filteredTests.length} results`);
}

// Show no results message
function showNoResults() {
    const grid = document.getElementById('writingTestGrid');
    if (!grid) return;
    
    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px;">
            <h3 style="font-size: 24px; color: #666; margin-bottom: 10px;">
                📝 No writing tests found
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
    if (pageNumber === currentPage || pageNumber < 1 || pageNumber > totalPages) {
        return;
    }
    
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
    
    console.log('Navigated to page:', pageNumber);
}

// Animate test cards
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
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(errorDiv);
    
    setTimeout(() => {
        errorDiv.style.opacity = '0';
        setTimeout(() => errorDiv.remove(), 300);
    }, 3000);
}

// Handle test card hover effects
document.addEventListener('mouseover', function(e) {
    const card = e.target.closest('.test-card');
    if (card) {
        const thumbnail = card.querySelector('.test-thumbnail img');
        if (thumbnail) {
            thumbnail.style.transform = 'scale(1.1)';
        }
    }
});

document.addEventListener('mouseout', function(e) {
    const card = e.target.closest('.test-card');
    if (card) {
        const thumbnail = card.querySelector('.test-thumbnail img');
        if (thumbnail) {
            thumbnail.style.transform = 'scale(1)';
        }
    }
});

// Export for global use
window.startWritingTest = startWritingTest;
window.renderTests = renderTests;
window.generateWritingTests = generateWritingTests;

console.log('✅ Writing test collection script loaded successfully');