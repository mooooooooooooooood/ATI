document.addEventListener('DOMContentLoaded', function() {
    const containers = document.querySelectorAll('.detail-row');

    function fetchExplanation(container) {
        const loadingDiv = container.querySelector('[id^="explanation-"]');
        if (!loadingDiv) return;

        // Note: The following lines are safe because Thymeleaf already pre-processed the hidden fields.
        const questionId = container.querySelector('.question-id').value;
        const userResponse = container.querySelector('.user-response').value;
        const correctAnswer = container.querySelector('.correct-answer').value;
        const targetId = loadingDiv.id;

        // Use Fetch API to call the new backend endpoint
        fetch('/reading/tests/get-explanation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                'questionId': questionId,
                'userResponse': userResponse,
                'correctAnswer': correctAnswer
            })
        })
        .then(response => response.json())
        .then(data => {
            const explanation = data.explanation || 'Failed to retrieve explanation.';
            const targetElement = document.getElementById(targetId);

            if (targetElement) {
                // Replace the loading indicator with the actual explanation
                targetElement.innerHTML = '<p>' + explanation.replace(/\n/g, '<br>') + '</p>';
            }
        })
        .catch(error => {
            console.error('Fetch error:', error);
            const targetElement = document.getElementById(targetId);
             if (targetElement) {
                 targetElement.innerHTML = '<p style="color:red;">Error loading insight. Try again later.</p>';
             }
        });
    }

    // Iterate over all detail rows and initiate the fetch for pending items
    containers.forEach(fetchExplanation);

    // Add CSS animation for the ziggling dots (can be moved to a CSS file too)
    const style = document.createElement('style');
    style.textContent = `
        @keyframes ziggle { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
        .loading-dots { animation: ziggle 1.5s infinite; }
    `;
    document.head.append(style);
});