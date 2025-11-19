// Speaking Test Collection Page JavaScript

// Global variables
let currentPage = 1;
let totalPages = 1;
let currentSort = 'newest';
let searchQuery = '';
let allTests = [];

// Initialize test collection page
document.addEventListener('DOMContentLoaded', function () {
    console.log('Speaking Test Collection page initialized');

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
        const testGrid = document.getElementById('speakingTestGrid');
        
        // Nếu có tests từ server
        const existingCards = testGrid.querySelectorAll('.test-card');
        if (existingCards.length > 0) {
            console.log('Tests already rendered by server:', existingCards.length);
            allTests = Array.from(existingCards).map(card => {
                const testId = card.getAttribute('data-test-id');
                return {
                    id: testId,  // ✅ Giữ nguyên ID từ backend (đã là số)
                    numericId: parseInt(testId) || testId,
                    title: card.querySelector('h3')?.textContent || '',
                    topics: card.querySelector('.test-topics')?.textContent || '',
                    element: card.cloneNode(true)
                };
            });
            
            // ✅ CẬP NHẬT LẠI EVENT LISTENER CHO CÁC BUTTON
            allTests.forEach(test => {
                const btn = test.element.querySelector('.btn-do-test');
                if (btn) {
                    // Xóa onclick cũ và thêm mới
                    btn.onclick = function() {
                        window.location.href = `/speaking/start/${test.id}`;
                    };
                }
            });
            
            return;
        }
        
        // Fallback: Generate sample data
        console.log('No server data found, generating samples...');
        allTests = generateSpeakingTests();
        renderTests(allTests);
        
    } catch (error) {
        console.error('Error loading tests:', error);
        showError('Failed to load tests. Please refresh the page.');
    }
}

// Generate speaking tests data (fallback)
function generateSpeakingTests() {
    const tests = [];
    const sampleTopics = [
        'Work/Parties',
        'Study/Music',
        'Home/Friends',
        'Transport/Food',
        'Weather/Hobbies',
        'Sports/Books'
    ];
    
    for (let i = 1; i <= 40; i++) {
        const date = new Date(2025, 0, i);
        const dateStr = `Ngày ${String(date.getDate()).padStart(2, '0')}.${String(date.getMonth() + 1).padStart(2, '0')}`;
        
        tests.push({
            id: i,  // ✅ CHỈ LÀ SỐ, KHÔNG PHẢI "test-1"
            title: `Test ${i} (${dateStr})`,
            topics: sampleTopics[i % sampleTopics.length]
        });
    }
    
    return tests;
}


// Render tests to grid
function renderTests(tests) {
    const grid = document.getElementById('speakingTestGrid');
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
        const card = test.element || createTestCard(test);
        card.style.animationDelay = `${index * 0.05}s`;
        grid.appendChild(card);
    });

    console.log(`Rendered ${tests.length} speaking tests`);
}

// Create test card element 
function createTestCard(test) {
    const card = document.createElement('div');
    card.className = 'test-card';
    card.setAttribute('data-test-id', test.id);
    
    card.innerHTML = `
        <div class="test-thumbnail">
            <img src="/images/speaking.png" alt="${test.title}" 
                 onerror="this.src='/images/Logo.png'">
        </div>

        <h3>${test.title}</h3>

        <p class="test-topics">${test.topics || ''}</p>

        <button class="btn-do-test" 
                onclick="window.location.href='/speaking/start/${test.id}'"
                type="button">
            <span class="icon">🎤</span> Do the test
        </button>
    `;

    return card;
}

function startSpeakingTest(testId) {
    console.log('Starting test:', testId);
    window.location.href = `/speaking/start/${testId}`;
}

// Setup sort filters
function setupSortFilters() {
    const radioOptions = document.querySelectorAll('input[name="sort"]');

    radioOptions.forEach(radio => {
        radio.addEventListener('change', function () {
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

    switch (sortBy) {
        case 'newest':
            sortedTests.reverse();
            break;
        case 'oldest':
            // Keep original order
            break;
        case 'most-attempted':
            // Random shuffle for demo (in production, sort by actual attempts)
            sortedTests.sort(() => Math.random() - 0.5);
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
        searchInput.addEventListener('input', function () {
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
        searchBtn.addEventListener('click', function () {
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
            test.topics.toLowerCase().includes(query) ||
            test.id.toLowerCase().includes(query);
    });

    renderTests(filteredTests);
    animateTestCards();

    console.log(`Search "${query}": found ${filteredTests.length} results`);
}

// Show no results message
function showNoResults() {
    const grid = document.getElementById('speakingTestGrid');
    if (!grid) return;

    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px;">
            <h3 style="font-size: 24px; color: #666; margin-bottom: 10px;">
                🎤 No speaking tests found
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
        btn.addEventListener('click', function () {
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
document.addEventListener('mouseover', function (e) {
    const card = e.target.closest('.test-card');
    if (card) {
        const thumbnail = card.querySelector('.test-thumbnail img');
        if (thumbnail) {
            thumbnail.style.transform = 'scale(1.1)';
        }
    }
});

document.addEventListener('mouseout', function (e) {
    const card = e.target.closest('.test-card');
    if (card) {
        const thumbnail = card.querySelector('.test-thumbnail img');
        if (thumbnail) {
            thumbnail.style.transform = 'scale(1)';
        }
    }
});

// Export for global use
window.startSpeakingTest = startSpeakingTest;
window.renderTests = renderTests;
window.generateSpeakingTests = generateSpeakingTests;

console.log('✅ Speaking test collection script loaded successfully');