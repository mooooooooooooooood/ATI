// File: results-review-fetch.js

document.addEventListener('DOMContentLoaded', function() {

    // 1. Safely retrieve the testId from the hidden input
    const testIdElement = document.getElementById('testId');

    // Convert the retrieved value to an integer. If the element is missing, or value is empty, it becomes 0.
    const testId = testIdElement ? parseInt(testIdElement.value, 10) : 0;

    const reviewContainer = document.getElementById('comprehensive-review-container');
    const loadingMessage = document.getElementById('loading-review-message');

    // CRITICAL CHECK: Ensure testId is a valid positive number
    if (isNaN(testId) || testId <= 0) {
        console.error("Setup error: Test ID is invalid or missing from the DOM. Aborting fetch.");
        if (loadingMessage) {
            loadingMessage.innerHTML = '<span style="color:red;">Review setup failed. Error: Missing Test ID.</span>';
            loadingMessage.style.display = 'block'; // Ensure message is visible
        }
        if (reviewContainer) reviewContainer.style.display = 'none';
        return; // STOP execution if ID is invalid
    }

    // Hide individual question reviews and show only the loading message initially
    if (loadingMessage) loadingMessage.style.display = 'block';
    if (reviewContainer) reviewContainer.style.display = 'none';

    // 2. Single call to the new service endpoint (URL is now guaranteed to have a number)
    fetch(`/reading/tests/get-test-review/${testId}`, { // Correct Template Literal Usage
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            console.error(`Bulk review failed with status: ${response.status}`);
            if (response.status === 404) {
                 throw new Error("Endpoint not found. Check server mapping.");
            }
            return response.text().then(text => { throw new Error(text) });
        }
        return response.json();
    })
    .then(data => {
        // Hide loading message
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (reviewContainer) reviewContainer.style.display = 'grid';

        // 3. Populate the four review cards
        document.getElementById('review-summary').innerHTML = data.overviewSummary || 'No summary provided.';
        document.getElementById('review-vocabulary').innerHTML = data.vocabularyWeaknesses || 'No specific vocabulary weaknesses detected.';
        document.getElementById('review-question-type').innerHTML = data.questionTypeInsights || 'No specific question type issues detected.';
        document.getElementById('review-strategy').innerHTML = data.strategyRecommendations || 'No specific strategy recommendations provided.';

    })
    .catch(error => {
        console.error('Fatal review fetch error:', error);
        if (loadingMessage) {
             loadingMessage.innerHTML = '<span style="color:red;">Error: Failed to generate comprehensive review. Please check server logs.</span>';
             loadingMessage.style.display = 'block';
        }
        if (reviewContainer) reviewContainer.style.display = 'none';
    });
});