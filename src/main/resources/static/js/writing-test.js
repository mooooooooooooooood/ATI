// Writing Test JavaScript
const API_BASE_URL = 'https://zoogleal-parsonish-almeda.ngrok-free.dev';

// Timer variables
let timeRemaining = 3600; // 60 minutes in seconds
let timerInterval = null;

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
    }
    
    if (task2Textarea && task2Counter) {
        task2Textarea.addEventListener('input', function() {
            updateWordCount(this, task2Counter, 250);
        });
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
        if (task1Textarea && task1Textarea.value) {
            localStorage.setItem(`draft_${testId}_task1`, task1Textarea.value);
        }
        if (task2Textarea && task2Textarea.value) {
            localStorage.setItem(`draft_${testId}_task2`, task2Textarea.value);
        }
        console.log('Draft auto-saved');
    }, 30000);
}

// Load drafts from localStorage
function loadDrafts() {
    const testId = document.getElementById('testId').value;
    const task1Textarea = document.getElementById('task1Answer');
    const task2Textarea = document.getElementById('task2Answer');
    
    const draft1 = localStorage.getItem(`draft_${testId}_task1`);
    const draft2 = localStorage.getItem(`draft_${testId}_task2`);
    
    if (draft1 && task1Textarea) {
        task1Textarea.value = draft1;
        updateWordCount(task1Textarea, document.getElementById('task1WordCount'), 150);
    }
    
    if (draft2 && task2Textarea) {
        task2Textarea.value = draft2;
        updateWordCount(task2Textarea, document.getElementById('task2WordCount'), 250);
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
       
        // Show loading
        showLoading(true);
       
        try {
            // Calculate time spent
            const timeSpent = 3600 - timeRemaining;
           
            // LẤY CSRF TOKEN TỪ META TAG
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            
            // Submit to backend
            const formData = new FormData();
            formData.append('task1Answer', task1Answer);
            formData.append('task2Answer', task2Answer);
            formData.append('timeSpent', timeSpent);
           
            // THÊM CSRF TOKEN VÀO HEADERS
            const headers = {};
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            
            const response = await fetch(`/writing/test/${testId}/submit`, {
                method: 'POST',
                headers: headers,  // THÊM HEADERS VÀO ĐÂY
                body: formData
            });
           
            const result = await response.json();
           
            if (result.status === 'success') {
                // Clear drafts
                localStorage.removeItem(`draft_${testId}_task1`);
                localStorage.removeItem(`draft_${testId}_task2`);
               
                // Stop timer
                clearInterval(timerInterval);
               
                // Show success message and redirect
                alert('✅ Test submitted successfully!\n\nYour test is being graded. You will be redirected to dashboard.');
               
                // Redirect to dashboard
                window.location.href = result.redirect || '/dashboard';
               
            } else if (result.status === 'error' && result.redirect) {
                // Need to login
                alert('Please login to submit your test.');
                window.location.href = result.redirect;
            } else {
                throw new Error(result.message || 'Submission failed');
            }
           
        } catch (error) {
            console.error('Submission error:', error);
            alert('❌ Failed to submit test: ' + error.message);
            showLoading(false);
        }
    });
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

// Submit with image link (Task 1)
async function submitWithImageLink(task1Question, task1Answer, imageUrl, task2Question, task2Answer) {
    console.log('Submitting with image link...');
    
    // Submit Task 1 with image
    const formData1 = new FormData();
    formData1.append('writing_task', task1Question);
    formData1.append('writing_input', task1Answer);
    formData1.append('link', imageUrl);
    
    const response1 = await fetch(`${API_BASE_URL}/writing/1/link`, {
        method: 'POST',
        headers: {
            'ngrok-skip-browser-warning': 'true'
        },
        body: formData1
    });
    
    if (!response1.ok) {
        throw new Error('Task 1 submission failed');
    }
    
    const result1 = await response1.json();
    console.log('Task 1 result:', result1);
    
    // Submit Task 2
    const formData2 = new FormData();
    formData2.append('writing_task', task2Question);
    formData2.append('writing_input', task2Answer);
    
    const response2 = await fetch(`${API_BASE_URL}/writing/2`, {
        method: 'POST',
        headers: {
            'ngrok-skip-browser-warning': 'true'
        },
        body: formData2
    });
    
    if (!response2.ok) {
        throw new Error('Task 2 submission failed');
    }
    
    const result2 = await response2.json();
    console.log('Task 2 result:', result2);
    
    return {
        task1: result1,
        task2: result2
    };
}

// Submit without image (Task 2 only)
async function submitWithoutImage(task1Question, task1Answer, task2Question, task2Answer) {
    console.log('Submitting without image...');
    
    // Submit Task 1 (without image - using Task 2 endpoint as fallback)
    const formData1 = new FormData();
    formData1.append('writing_task', task1Question);
    formData1.append('writing_input', task1Answer);
    
    const response1 = await fetch(`${API_BASE_URL}/writing/2`, {
        method: 'POST',
        headers: {
            'ngrok-skip-browser-warning': 'true'
        },
        body: formData1
    });
    
    if (!response1.ok) {
        throw new Error('Task 1 submission failed');
    }
    
    const result1 = await response1.json();
    
    // Submit Task 2
    const formData2 = new FormData();
    formData2.append('writing_task', task2Question);
    formData2.append('writing_input', task2Answer);
    
    const response2 = await fetch(`${API_BASE_URL}/writing/2`, {
        method: 'POST',
        headers: {
            'ngrok-skip-browser-warning': 'true'
        },
        body: formData2
    });
    
    if (!response2.ok) {
        throw new Error('Task 2 submission failed');
    }
    
    const result2 = await response2.json();
    
    return {
        task1: result1,
        task2: result2
    };
}

// Show loading overlay
function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    const submitBtn = document.getElementById('submitBtn');
    
    if (show) {
        overlay.classList.add('active');
        submitBtn.disabled = true;
    } else {
        overlay.classList.remove('active');
        submitBtn.disabled = false;
    }
}

// Show result
function showResult(result) {
    showLoading(false);
    
    // Extract scores from HTML response
    const task1Score = extractScore(result.task1.message);
    const task2Score = extractScore(result.task2.message);
    
    // Calculate overall
    const overall = ((task1Score + task2Score) / 2).toFixed(1);
    
    // Display result
    const resultHtml = `
        <div style="text-align: center; padding: 40px;">
            <h2 style="font-size: 36px; color: #4caf50; margin-bottom: 20px;">Test Completed!</h2>
            
            <div style="background: white; padding: 30px; border-radius: 15px; box-shadow: 0 2px 15px rgba(0,0,0,0.1); margin-bottom: 20px;">
                <h3 style="font-size: 24px; margin-bottom: 20px;">Your Scores</h3>
                
                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 20px;">
                    <div style="padding: 20px; background: #f5f5f5; border-radius: 10px;">
                        <div style="font-size: 14px; color: #666; margin-bottom: 5px;">Task 1</div>
                        <div style="font-size: 32px; font-weight: bold; color: #e53935;">Band ${task1Score}</div>
                    </div>
                    
                    <div style="padding: 20px; background: #f5f5f5; border-radius: 10px;">
                        <div style="font-size: 14px; color: #666; margin-bottom: 5px;">Task 2</div>
                        <div style="font-size: 32px; font-weight: bold; color: #e53935;">Band ${task2Score}</div>
                    </div>
                    
                    <div style="padding: 20px; background: #e53935; border-radius: 10px;">
                        <div style="font-size: 14px; color: rgba(255,255,255,0.8); margin-bottom: 5px;">Overall</div>
                        <div style="font-size: 32px; font-weight: bold; color: white;">Band ${overall}</div>
                    </div>
                </div>
                
                <div style="margin-top: 30px;">
                    <button onclick="window.location.href='/writing/tests'" 
                            style="padding: 15px 40px; background: #e53935; color: white; border: none; border-radius: 30px; font-size: 16px; cursor: pointer; margin-right: 10px;">
                        Back to Tests
                    </button>
                    <button onclick="window.location.reload()" 
                            style="padding: 15px 40px; background: white; color: #e53935; border: 2px solid #e53935; border-radius: 30px; font-size: 16px; cursor: pointer;">
                        Try Again
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Replace main content
    document.querySelector('.writing-test-container').innerHTML = resultHtml;
}

// Extract score from HTML message
function extractScore(htmlMessage) {
    // Extract number from "Band X.X" or "Predicted score: Band X.X"
    const match = htmlMessage.match(/Band\s+(\d+\.?\d*)/i);
    return match ? parseFloat(match[1]) : 0;
}

// Prevent accidental page close
window.addEventListener('beforeunload', function(e) {
    const task1 = document.getElementById('task1Answer')?.value;
    const task2 = document.getElementById('task2Answer')?.value;
    
    if ((task1 && task1.trim()) || (task2 && task2.trim())) {
        e.preventDefault();
        e.returnValue = '';
        return '';
    }
});