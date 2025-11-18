// Writing Test JavaScript
const API_BASE_URL = 'https://zoogleal-parsonish-almeda.ngrok-free.dev';

// Timer variables
let timeRemaining = 3600; // 60 minutes in seconds
let timerInterval = null;

// Submission tracking
let isSubmitting = false;
let hasSubmitted = false;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    console.log('Writing Test page initialized');
    
    // Setup word counters
    setupWordCounters();
    
    // Setup auto-save
    setupAutoSave();
    
    // Setup form submission
    setupFormSubmission();
    
    // Start timer
    startTimer();
    
    // Load drafts if exists
    loadDrafts();
    
    // Setup beforeunload warning
    setupBeforeUnloadWarning();
});

// Setup word counters
function setupWordCounters() {
    const task1Textarea = document.getElementById('task1Answer');
    const task2Textarea = document.getElementById('task2Answer');
    const task1Counter = document.getElementById('task1WordCount');
    const task2Counter = document.getElementById('task2WordCount');
    
    if (task1Textarea && task1Counter) {
        task1Textarea.addEventListener('input', function() {
            updateWordCount(this, task1Counter, 150);
        });
        // Update initial count if there's draft content
        updateWordCount(task1Textarea, task1Counter, 150);
    }
    
    if (task2Textarea && task2Counter) {
        task2Textarea.addEventListener('input', function() {
            updateWordCount(this, task2Counter, 250);
        });
        // Update initial count if there's draft content
        updateWordCount(task2Textarea, task2Counter, 250);
    }
}

// Update word count
function updateWordCount(textarea, counterElement, minWords) {
    const text = textarea.value.trim();
    const wordCount = text ? text.split(/\s+/).length : 0;
    
    counterElement.textContent = `Words: ${wordCount} / ${minWords}`;
    
    if (wordCount >= minWords) {
        counterElement.className = 'word-count success';
    } else if (wordCount > minWords * 0.8) {
        counterElement.className = 'word-count warning';
    } else {
        counterElement.className = 'word-count';
    }
}

// Setup auto-save
function setupAutoSave() {
    const task1Textarea = document.getElementById('task1Answer');
    const task2Textarea = document.getElementById('task2Answer');
    const testId = document.getElementById('testId').value;
    
    // Auto-save every 30 seconds
    setInterval(() => {
        // Only save if not submitted yet
        if (!hasSubmitted) {
            if (task1Textarea && task1Textarea.value) {
                localStorage.setItem(`draft_${testId}_task1`, task1Textarea.value);
            }
            if (task2Textarea && task2Textarea.value) {
                localStorage.setItem(`draft_${testId}_task2`, task2Textarea.value);
            }
            console.log('Draft auto-saved');
        }
    }, 30000);
}

// Load drafts from localStorage
function loadDrafts() {
    const testId = document.getElementById('testId').value;
    const task1Textarea = document.getElementById('task1Answer');
    const task2Textarea = document.getElementById('task2Answer');
    
    const draft1 = localStorage.getItem(`draft_${testId}_task1`);
    const draft2 = localStorage.getItem(`draft_${testId}_task2`);
    
    if (draft1 && task1Textarea && !task1Textarea.value) {
        task1Textarea.value = draft1;
        updateWordCount(task1Textarea, document.getElementById('task1WordCount'), 150);
        console.log('Task 1 draft loaded');
    }
    
    if (draft2 && task2Textarea && !task2Textarea.value) {
        task2Textarea.value = draft2;
        updateWordCount(task2Textarea, document.getElementById('task2WordCount'), 250);
        console.log('Task 2 draft loaded');
    }
}

// Start timer
function startTimer() {
    const timerElement = document.getElementById('timer');
    
    timerInterval = setInterval(() => {
        timeRemaining--;
        
        const minutes = Math.floor(timeRemaining / 60);
        const seconds = timeRemaining % 60;
        
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        // Change color when time is running out
        if (timeRemaining <= 300) { // 5 minutes
            timerElement.style.color = '#ff9800';
        }
        if (timeRemaining <= 60) { // 1 minute
            timerElement.style.color = '#f44336';
        }
        
        // Auto-submit when time is up
        if (timeRemaining <= 0) {
            clearInterval(timerInterval);
            alert('Time is up! Submitting your test...');
            document.getElementById('writingTestForm').dispatchEvent(new Event('submit'));
        }
    }, 1000);
}

// Setup form submission
function setupFormSubmission() {
    const form = document.getElementById('writingTestForm');
   
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
       
        const task1Answer = document.getElementById('task1Answer').value.trim();
        const task2Answer = document.getElementById('task2Answer').value.trim();
        const testId = document.getElementById('testId').value;
       
        // Validate
        const errors = [];
        const task1Words = task1Answer ? task1Answer.split(/\s+/).length : 0;
        const task2Words = task2Answer ? task2Answer.split(/\s+/).length : 0;
       
        if (task1Words < 150) {
            errors.push(`Task 1 requires at least 150 words (you have ${task1Words})`);
        }
       
        if (task2Words < 250) {
            errors.push(`Task 2 requires at least 250 words (you have ${task2Words})`);
        }
       
        if (errors.length > 0) {
            alert('Please fix the following errors:\n\n' + errors.join('\n'));
            return;
        }
       
        // Confirm submission
        if (!confirm('Are you sure you want to submit? You cannot change your answers after submission.')) {
            return;
        }
       
        // Mark as submitting - QUAN TRỌNG: Tắt beforeunload warning
        isSubmitting = true;
        hasSubmitted = true;
        window.onbeforeunload = null; // ✅ TẮT CẢNH BÁO "Leave site?"
       
        // Show loading
        showLoading(true);
       
        try {
            // Calculate time spent (in seconds)
            const timeSpent = Math.floor((3600 - timeRemaining) / 60); // Convert to minutes
           
            // Lấy CSRF TOKEN từ meta tag
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            
            // Submit to backend
            const formData = new FormData();
            formData.append('task1Answer', task1Answer);
            formData.append('task2Answer', task2Answer);
            formData.append('timeSpent', timeSpent);
           
            // Thêm CSRF token vào headers
            const headers = {};
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            
            console.log('Submitting to:', `/writing/test/${testId}/submit`);
            console.log('Time spent:', timeSpent, 'minutes');
            
            const response = await fetch(`/writing/test/${testId}/submit`, {
                method: 'POST',
                headers: headers,
                body: formData
            });
           
            const result = await response.json();
            
            console.log('Submit response:', result);
           
            if (result.status === 'success') {
                // Clear drafts
                localStorage.removeItem(`draft_${testId}_task1`);
                localStorage.removeItem(`draft_${testId}_task2`);
               
                // Stop timer
                if (timerInterval) {
                    clearInterval(timerInterval);
                }
               
                // Show success message
                console.log('✅ Test submitted successfully!');
                console.log('Submission ID:', result.submissionId);
                
                // Optional: Show a brief success message
                const loadingContent = document.querySelector('.loading-content');
                if (loadingContent) {
                    loadingContent.innerHTML = `
                        <div style="text-align: center;">
                            <div style="font-size: 48px; color: #4caf50; margin-bottom: 15px;">✅</div>
                            <h3 style="color: #4caf50; margin-bottom: 10px;">Test Submitted Successfully!</h3>
                            <p style="color: #666;">Your test is being graded in the background.</p>
                            <p style="color: #666;">Redirecting to dashboard...</p>
                        </div>
                    `;
                }
                
                // Redirect to dashboard after a short delay
                setTimeout(() => {
                    window.location.href = result.redirect || '/dashboard';
                }, 1500);
               
            } else if (result.status === 'error' && result.redirect) {
                // Need to login
                alert('Please login to submit your test.');
                window.location.href = result.redirect;
            } else {
                throw new Error(result.message || 'Submission failed');
            }
           
        } catch (error) {
            console.error('❌ Submission error:', error);
            alert('❌ Failed to submit test: ' + error.message);
            
            // Re-enable form on error
            isSubmitting = false;
            hasSubmitted = false;
            showLoading(false);
            
            // Re-enable beforeunload warning on error
            setupBeforeUnloadWarning();
        }
    });
}

// Setup beforeunload warning (chỉ khi chưa submit)
function setupBeforeUnloadWarning() {
    window.onbeforeunload = function(e) {
        // Chỉ cảnh báo nếu:
        // 1. Chưa submit
        // 2. Có nội dung trong textarea
        if (!hasSubmitted && !isSubmitting) {
            const task1 = document.getElementById('task1Answer')?.value?.trim();
            const task2 = document.getElementById('task2Answer')?.value?.trim();
            
            if (task1 || task2) {
                e.preventDefault();
                e.returnValue = 'Changes you made may not be saved.';
                return e.returnValue;
            }
        }
        
        // Không cảnh báo nếu đã submit hoặc đang submit
        return undefined;
    };
}

// Check submission status (optional - for real-time updates)
async function checkSubmissionStatus(submissionId) {
    try {
        const response = await fetch(`/api/submission/${submissionId}/status`);
        const data = await response.json();
        return data.status;
    } catch (error) {
        console.error('Error checking status:', error);
        return 'unknown';
    }
}

// Show loading overlay
function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    const submitBtn = document.getElementById('submitBtn');
    
    if (show) {
        overlay.classList.add('active');
        if (submitBtn) {
            submitBtn.disabled = true;
        }
    } else {
        overlay.classList.remove('active');
        if (submitBtn) {
            submitBtn.disabled = false;
        }
    }
}

// Extract score from HTML message
function extractScore(htmlMessage) {
    // Extract number from "Band X.X" or "Predicted score: Band X.X"
    const match = htmlMessage.match(/Band\s+(\d+\.?\d*)/i);
    return match ? parseFloat(match[1]) : 0;
}

// Clear drafts when leaving page normally (not during submit)
window.addEventListener('unload', function() {
    // Only clear if actually submitted
    if (hasSubmitted) {
        const testId = document.getElementById('testId')?.value;
        if (testId) {
            localStorage.removeItem(`draft_${testId}_task1`);
            localStorage.removeItem(`draft_${testId}_task2`);
        }
    }
});