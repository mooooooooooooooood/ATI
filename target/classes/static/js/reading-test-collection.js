// Reading Test Collection - Client-side filtering
document.addEventListener('DOMContentLoaded', function() {
    initializeFilters();
});

// Store all test cards for filtering
let allTestCards = [];

function initializeFilters() {
    // Get all test cards
    const testGrid = document.getElementById('readingTestGrid');
    if (!testGrid) return;
    
    allTestCards = Array.from(testGrid.querySelectorAll('.test-card'));
    
    // Initialize search
    initializeSearch();
    
    // Initialize level filters
    initializeLevelFilters();
    
    // Initialize clear filters button
    initializeClearFilters();
}

// Search functionality
function initializeSearch() {
    const searchInput = document.getElementById('sidebarSearch');
    const searchButton = document.getElementById('searchButton');
    
    if (!searchInput) return;
    
    // Search on Enter key
    searchInput.addEventListener('keyup', function(e) {
        if (e.key === 'Enter') {
            performFilter();
        }
    });
    
    // Search on button click
    if (searchButton) {
        searchButton.addEventListener('click', performFilter);
    }
    
    // Optional: Real-time search (uncomment if you want instant filtering)
    // searchInput.addEventListener('input', performFilter);
}

// Level filter checkboxes
function initializeLevelFilters() {
    const levelCheckboxes = document.querySelectorAll('input[name="level"]');
    
    levelCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', performFilter);
    });
}

// Clear filters button
function initializeClearFilters() {
    const clearBtn = document.getElementById('clearFiltersBtn');
    
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            // Clear search input
            const searchInput = document.getElementById('sidebarSearch');
            if (searchInput) {
                searchInput.value = '';
            }
            
            // Uncheck all level checkboxes
            const levelCheckboxes = document.querySelectorAll('input[name="level"]');
            levelCheckboxes.forEach(checkbox => {
                checkbox.checked = false;
            });
            
            // Show all tests
            performFilter();
        });
    }
}

// Main filter function
function performFilter() {
    const searchInput = document.getElementById('sidebarSearch');
    const searchQuery = searchInput ? searchInput.value.toLowerCase().trim() : '';
    
    // Get selected levels
    const selectedLevels = getSelectedLevels();
    
    let visibleCount = 0;
    
    // Filter each test card
    allTestCards.forEach(card => {
        const testName = (card.getAttribute('data-test-name') || '').toLowerCase();
        const testLevel = card.getAttribute('data-test-level') || '';
        
        // Check search match
        const matchesSearch = !searchQuery || testName.includes(searchQuery);
        
        // Check level match
        const matchesLevel = selectedLevels.length === 0 || selectedLevels.includes(testLevel);
        
        // Show or hide card
        if (matchesSearch && matchesLevel) {
            card.style.display = 'block';
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });
    
    // Update "no results" message
    updateNoResultsMessage(visibleCount);
}

// Get selected level filters
function getSelectedLevels() {
    const selectedLevels = [];
    const levelCheckboxes = document.querySelectorAll('input[name="level"]:checked');
    
    levelCheckboxes.forEach(checkbox => {
        selectedLevels.push(checkbox.value);
    });
    
    return selectedLevels;
}

// Show/hide "no results" message
function updateNoResultsMessage(visibleCount) {
    const testGrid = document.getElementById('readingTestGrid');
    if (!testGrid) return;
    
    // Remove existing "no results" message
    let noResultsMsg = testGrid.querySelector('.no-results-message');
    
    if (visibleCount === 0) {
        // Show "no results" message
        if (!noResultsMsg) {
            noResultsMsg = document.createElement('div');
            noResultsMsg.className = 'no-results-message';
            noResultsMsg.style.cssText = 'grid-column: 1 / -1; text-align: center; padding: 60px 20px;';
            noResultsMsg.innerHTML = `
                <h3 style="font-size: 24px; color: #666; margin-bottom: 10px;">
                    &#128269; No tests match your filters
                </h3>
                <p style="color: #999;">
                    Try adjusting your search or filters
                </p>
            `;
            testGrid.appendChild(noResultsMsg);
        }
    } else {
        // Remove "no results" message if it exists
        if (noResultsMsg) {
            noResultsMsg.remove();
        }
    }
    
    // Update test count
    updateTestCount(visibleCount);
}

// Update test count display
function updateTestCount(visibleCount) {
    const statsElement = document.querySelector('.test-stats p');
    if (statsElement) {
        const totalTests = allTestCards.length;
        if (visibleCount < totalTests) {
            statsElement.innerHTML = `Showing <span>${visibleCount}</span> of <span>${totalTests}</span> reading tests`;
        } else {
            statsElement.innerHTML = `Total: <span>${totalTests}</span> reading tests available`;
        }
    }
}

// Helper function to start a test (called from onclick)
function startReadingTest(testId) {
    window.location.href = `/reading/tests/${testId}`;
}

// Export for global use
window.startReadingTest = startReadingTest;