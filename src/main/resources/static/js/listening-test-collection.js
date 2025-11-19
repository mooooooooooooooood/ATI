document.addEventListener('DOMContentLoaded', function() {
    console.log("Listening Test Collection Script Loaded"); // Check your browser console for this

    // ------------------------------------------------------
    // 1. SELECT DOM ELEMENTS
    // ------------------------------------------------------
    const searchInput = document.getElementById('sidebarSearch');
    const levelCheckboxes = document.querySelectorAll('input[name="level"]');
    const clearBtn = document.getElementById('clearFiltersBtn');
    const testGrid = document.getElementById('listeningTestGrid');
    const testCards = document.querySelectorAll('.test-card');
    const countSpan = document.querySelector('.test-stats span');

    // ------------------------------------------------------
    // 2. SETUP "NO RESULTS" MESSAGE
    // ------------------------------------------------------
    let noTestsMsg = document.querySelector('.no-tests-message');

    // Create the message element dynamically if it doesn't exist
    if (!noTestsMsg && testGrid) {
        noTestsMsg = document.createElement('div');
        noTestsMsg.className = 'no-tests-message';
        noTestsMsg.style.gridColumn = '1 / -1'; // Span full width of grid
        noTestsMsg.style.textAlign = 'center';
        noTestsMsg.style.padding = '40px';
        noTestsMsg.style.display = 'none'; // Hidden by default
        noTestsMsg.innerHTML = `
            <h3 style="color: #666; margin-bottom: 10px;">No tests found</h3>
            <p style="color: #999;">Try adjusting your search or filters</p>
        `;
        testGrid.appendChild(noTestsMsg);
    }

    // ------------------------------------------------------
    // 3. FILTER LOGIC
    // ------------------------------------------------------
    function filterTests() {
        // Safety: If search input is missing, stop (prevents errors on other pages)
        if (!searchInput) return;

        const searchTerm = searchInput.value.toLowerCase().trim();

        // Get list of checked levels (e.g., ['Academic'])
        const selectedLevels = Array.from(levelCheckboxes)
            .filter(cb => cb.checked)
            .map(cb => cb.value);

        let visibleCount = 0;

        testCards.forEach(card => {
            // Safe access to data attributes (defaults to empty string if missing)
            const testName = (card.dataset.testName || '').toLowerCase();
            const testLevel = card.dataset.testLevel || '';

            // 1. Check Name Match
            const matchesSearch = testName.includes(searchTerm);

            // 2. Check Level Match (If no boxes checked, show ALL levels)
            const matchesLevel = selectedLevels.length === 0 || selectedLevels.includes(testLevel);

            // Apply Visibility
            if (matchesSearch && matchesLevel) {
                card.style.display = 'flex'; // Show card
                visibleCount++;
            } else {
                card.style.display = 'none'; // Hide card
            }
        });

        // Update the "Total" counter
        if (countSpan) {
            countSpan.textContent = visibleCount;
        }

        // Toggle "No Tests" message
        if (noTestsMsg) {
            noTestsMsg.style.display = (visibleCount === 0) ? 'block' : 'none';
        }
    }

    // ------------------------------------------------------
    // 4. EVENT LISTENERS
    // ------------------------------------------------------

    // Search Input (Runs on every keystroke)
    if (searchInput) {
        searchInput.addEventListener('input', filterTests);
    }

    // Checkboxes (Runs when checked/unchecked)
    levelCheckboxes.forEach(cb => {
        cb.addEventListener('change', filterTests);
    });

    // Clear Button
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            if (searchInput) searchInput.value = '';
            levelCheckboxes.forEach(cb => cb.checked = false);
            filterTests(); // Refresh view
        });
    }

    // ------------------------------------------------------
    // 5. INITIALIZATION (THE FIX)
    // ------------------------------------------------------
    // We manually trigger the filter once on page load.
    // Since search is empty and checkboxes are unchecked by default,
    // this will force all cards to be VISIBLE (display: flex).
    filterTests();
});