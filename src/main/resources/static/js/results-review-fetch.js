document.addEventListener('DOMContentLoaded', function() {

    // 1. Safely retrieve the testId
    const testIdElement = document.getElementById('testId');
    const testId = testIdElement ? parseInt(testIdElement.value, 10) : 0;

    const reviewContainer = document.getElementById('comprehensive-review-container');
    const loadingMessage = document.getElementById('loading-review-message');

    // CRITICAL CHECK
    if (isNaN(testId) || testId <= 0) {
        console.error("Setup error: Test ID is invalid.");
        if (loadingMessage) {
            loadingMessage.innerHTML = '<span style="color:red;">Review setup failed. Error: Missing Test ID.</span>';
            loadingMessage.style.display = 'block';
        }
        if (reviewContainer) reviewContainer.style.display = 'none';
        return;
    }

    if (loadingMessage) loadingMessage.style.display = 'block';
    if (reviewContainer) reviewContainer.style.display = 'none';

    // --- FIX STARTS HERE ---

    // Detect if we are on a "listening" page or a "reading" page based on the browser URL
    // e.g., if URL is "localhost:8080/listening/tests/result", this becomes "listening"
    const currentPath = window.location.pathname;
    let endpointType = "reading"; // Default fallback

    if (currentPath.includes("listening")) {
        endpointType = "listening";
    } else if (currentPath.includes("reading")) {
        endpointType = "reading";
    }

    // Construct the dynamic URL
    const fetchUrl = `/${endpointType}/tests/get-test-review/${testId}`;

    console.log(`Fetching AI review from: ${fetchUrl}`); // Debug log to see it working

    // --- FIX ENDS HERE ---

    // 2. Call the dynamic endpoint
    fetch(fetchUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 404) {
                 throw new Error(`Endpoint not found: ${fetchUrl}`);
            }
            return response.text().then(text => { throw new Error(text) });
        }
        return response.json();
    })
    .then(data => {
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (reviewContainer) reviewContainer.style.display = 'grid';

        document.getElementById('review-summary').innerHTML = data.overviewSummary || 'No summary provided.';
        document.getElementById('review-vocabulary').innerHTML = data.vocabularyWeaknesses || 'No specific vocabulary weaknesses detected.';
        document.getElementById('review-question-type').innerHTML = data.questionTypeInsights || 'No specific question type issues detected.';
        document.getElementById('review-strategy').innerHTML = data.strategyRecommendations || 'No specific strategy recommendations provided.';
    })
    .catch(error => {
        console.error('Fatal review fetch error:', error);
        if (loadingMessage) {
             loadingMessage.innerHTML = `<span style="color:red;">Error: Failed to load review. (${error.message})</span>`;
             loadingMessage.style.display = 'block';
        }
    });
});