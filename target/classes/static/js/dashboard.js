// Dashboard Chart Configuration

// Sample data - replace with real user data from backend
const userData = {
    writing: [6, 5, 7, 6, 5, 6, 7],
    listening: [7, 6, 8, 7, 6, 7, 8],
    speaking: [5, 4, 6, 5, 4, 5, 6],
    reading: [8, 7, 6, 8, 7, 8, 7]
};

// Tracking variables for submissions
let pendingSubmissions = [];
let completedSubmissions = [];
let pollingInterval = null;

// Initialize Radar Chart
function initRadarChart() {
    const ctx = document.getElementById('radarChart');
    
    if (!ctx) {
        console.error('Canvas element not found');
        return;
    }
    
    const radarChart = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            datasets: [
                {
                    label: 'Writing',
                    data: userData.writing,
                    backgroundColor: 'rgba(63, 81, 181, 0.2)',
                    borderColor: 'rgb(63, 81, 181)',
                    borderWidth: 2,
                    pointBackgroundColor: 'rgb(63, 81, 181)',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: 'rgb(63, 81, 181)',
                    pointRadius: 4,
                    pointHoverRadius: 6
                },
                {
                    label: 'Listening',
                    data: userData.listening,
                    backgroundColor: 'rgba(255, 152, 0, 0.2)',
                    borderColor: 'rgb(255, 152, 0)',
                    borderWidth: 2,
                    pointBackgroundColor: 'rgb(255, 152, 0)',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: 'rgb(255, 152, 0)',
                    pointRadius: 4,
                    pointHoverRadius: 6
                },
                {
                    label: 'Speaking',
                    data: userData.speaking,
                    backgroundColor: 'rgba(158, 158, 158, 0.2)',
                    borderColor: 'rgb(158, 158, 158)',
                    borderWidth: 2,
                    pointBackgroundColor: 'rgb(158, 158, 158)',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: 'rgb(158, 158, 158)',
                    pointRadius: 4,
                    pointHoverRadius: 6
                },
                {
                    label: 'Reading',
                    data: userData.reading,
                    backgroundColor: 'rgba(255, 193, 7, 0.2)',
                    borderColor: 'rgb(255, 193, 7)',
                    borderWidth: 2,
                    pointBackgroundColor: 'rgb(255, 193, 7)',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: 'rgb(255, 193, 7)',
                    pointRadius: 4,
                    pointHoverRadius: 6
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        font: {
                            size: 13,
                            family: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif"
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    titleFont: {
                        size: 14
                    },
                    bodyFont: {
                        size: 13
                    },
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + context.parsed.r + ' band';
                        }
                    }
                }
            },
            scales: {
                r: {
                    min: 0,
                    max: 9,
                    ticks: {
                        stepSize: 2,
                        font: {
                            size: 12
                        },
                        backdropColor: 'transparent'
                    },
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    },
                    angleLines: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    },
                    pointLabels: {
                        font: {
                            size: 13,
                            weight: '600'
                        },
                        color: '#333'
                    }
                }
            },
            animation: {
                duration: 1500,
                easing: 'easeInOutQuart'
            }
        }
    });
    
    return radarChart;
}

// ========== SUBMISSION TRACKING FUNCTIONS ==========

/**
 * Check for pending submissions from user
 */
async function checkPendingSubmissions() {
    try {
        const response = await fetch('/api/submissions/pending');
        
        if (!response.ok) {
            throw new Error('Failed to fetch pending submissions');
        }
        
        const data = await response.json();
        
        if (data.submissions && data.submissions.length > 0) {
            pendingSubmissions = data.submissions;
            showProcessingBanner(data.submissions);
            
            console.log('📋 Found pending submissions:', data.submissions.length);
        } else {
            hideProcessingBanner();
        }
        
        return data.submissions || [];
        
    } catch (error) {
        console.error('Error checking pending submissions:', error);
        return [];
    }
}

/**
 * Check status of specific submission
 */
async function checkSubmissionStatus(submissionUuid) {
    try {
        const response = await fetch(`/api/submission/${submissionUuid}/status`);
        
        if (!response.ok) {
            throw new Error('Failed to fetch submission status');
        }
        
        const data = await response.json();
        console.log('📊 Submission status:', submissionUuid, '→', data.status);
        
        return data;
        
    } catch (error) {
        console.error('Error checking submission status:', error);
        return null;
    }
}

/**
 * Show processing banner for pending submissions
 */
function showProcessingBanner(submissions) {
    // Remove existing banner if any
    hideProcessingBanner();
    
    const banner = document.createElement('div');
    banner.id = 'processing-banner';
    banner.className = 'processing-banner';
    
    const submissionCount = submissions.length;
    const submissionText = submissionCount === 1 ? 'submission' : 'submissions';
    
    banner.innerHTML = `
        <div class="banner-content">
            <div class="banner-icon">
                <div class="spinner-small"></div>
            </div>
            <div class="banner-text">
                <strong>⏳ Grading in Progress</strong>
                <p>You have ${submissionCount} ${submissionText} being graded. Results will appear shortly.</p>
            </div>
            <button class="banner-close" onclick="hideBannerTemporarily()">✕</button>
        </div>
    `;
    
    // Add to page (at the top of main content)
    const mainContent = document.querySelector('main') || document.body;
    mainContent.insertBefore(banner, mainContent.firstChild);
    
    // Add styles if not already added
    addProcessingBannerStyles();
}

/**
 * Hide processing banner
 */
function hideProcessingBanner() {
    const banner = document.getElementById('processing-banner');
    if (banner) {
        banner.remove();
    }
}

/**
 * Hide banner temporarily (user clicked close)
 */
function hideBannerTemporarily() {
    hideProcessingBanner();
    sessionStorage.setItem('banner_hidden_until', Date.now() + 60000); // Hide for 1 minute
}

/**
 * Add CSS styles for processing banner
 */
function addProcessingBannerStyles() {
    if (document.getElementById('processing-banner-styles')) {
        return; // Already added
    }
    
    const style = document.createElement('style');
    style.id = 'processing-banner-styles';
    style.textContent = `
        .processing-banner {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 15px;
            margin: 20px 0;
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.3);
            animation: slideDown 0.5s ease;
        }
        
        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .banner-content {
            display: flex;
            align-items: center;
            gap: 20px;
            position: relative;
        }
        
        .banner-icon {
            flex-shrink: 0;
        }
        
        .spinner-small {
            width: 30px;
            height: 30px;
            border: 3px solid rgba(255, 255, 255, 0.3);
            border-top-color: white;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
        
        .banner-text {
            flex: 1;
        }
        
        .banner-text strong {
            display: block;
            font-size: 18px;
            margin-bottom: 5px;
        }
        
        .banner-text p {
            margin: 0;
            opacity: 0.9;
            font-size: 14px;
        }
        
        .banner-close {
            position: absolute;
            top: -10px;
            right: -10px;
            background: rgba(255, 255, 255, 0.2);
            border: none;
            color: white;
            width: 30px;
            height: 30px;
            border-radius: 50%;
            cursor: pointer;
            font-size: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s;
        }
        
        .banner-close:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: scale(1.1);
        }
        
        /* Notification for completed submissions */
        .completion-notification {
            position: fixed;
            top: 20px;
            right: 20px;
            background: white;
            padding: 20px;
            border-radius: 15px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.2);
            z-index: 10000;
            min-width: 300px;
            animation: slideInRight 0.5s ease;
        }
        
        @keyframes slideInRight {
            from {
                opacity: 0;
                transform: translateX(100%);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
        
        .notification-header {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 10px;
        }
        
        .notification-icon {
            font-size: 32px;
        }
        
        .notification-title {
            font-size: 18px;
            font-weight: 600;
            color: #4caf50;
        }
        
        .notification-body {
            color: #666;
            margin-bottom: 15px;
            font-size: 14px;
        }
        
        .notification-actions {
            display: flex;
            gap: 10px;
        }
        
        .notification-btn {
            flex: 1;
            padding: 10px;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            font-weight: 600;
            transition: all 0.3s;
        }
        
        .notification-btn-primary {
            background: #4caf50;
            color: white;
        }
        
        .notification-btn-primary:hover {
            background: #45a049;
            transform: translateY(-2px);
        }
        
        .notification-btn-secondary {
            background: #f5f5f5;
            color: #666;
        }
        
        .notification-btn-secondary:hover {
            background: #e0e0e0;
        }
    `;
    
    document.head.appendChild(style);
}

/**
 * Show notification when submission is completed
 */
function showCompletedNotification(submission) {
    const notification = document.createElement('div');
    notification.className = 'completion-notification';
    notification.id = `notification-${submission.submissionUuid}`;
    
    notification.innerHTML = `
        <div class="notification-header">
            <div class="notification-icon">✅</div>
            <div class="notification-title">Grading Complete!</div>
        </div>
        <div class="notification-body">
            Your <strong>${submission.testType || 'writing'}</strong> test has been graded.
            <br>Overall Score: <strong>Band ${submission.overallScore || 'N/A'}</strong>
        </div>
        <div class="notification-actions">
            <button class="notification-btn notification-btn-primary" 
                    onclick="viewResult('${submission.submissionUuid}')">
                View Result
            </button>
            <button class="notification-btn notification-btn-secondary" 
                    onclick="dismissNotification('${submission.submissionUuid}')">
                Dismiss
            </button>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Auto-dismiss after 10 seconds
    setTimeout(() => {
        dismissNotification(submission.submissionUuid);
    }, 10000);
    
    // Play sound notification (optional)
    playNotificationSound();
}

/**
 * View submission result
 */
function viewResult(submissionUuid) {
    window.location.href = `/writing/result/${submissionUuid}`;
}

/**
 * Dismiss notification
 */
function dismissNotification(submissionUuid) {
    const notification = document.getElementById(`notification-${submissionUuid}`);
    if (notification) {
        notification.style.animation = 'slideOutRight 0.5s ease';
        setTimeout(() => notification.remove(), 500);
    }
}

/**
 * Play notification sound
 */
function playNotificationSound() {
    try {
        // Create a simple beep sound using Web Audio API
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.value = 800;
        oscillator.type = 'sine';
        
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.5);
    } catch (error) {
        console.log('Could not play notification sound:', error);
    }
}

/**
 * Poll for submission status updates
 */
function startSubmissionPolling() {
    // Stop existing polling if any
    stopSubmissionPolling();
    
    // Check immediately
    checkAndUpdateSubmissions();
    
    // Then check every 5 seconds
    pollingInterval = setInterval(() => {
        checkAndUpdateSubmissions();
    }, 5000);
    
    console.log('🔄 Started submission polling');
}

/**
 * Stop polling
 */
function stopSubmissionPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
        pollingInterval = null;
        console.log('⏸️ Stopped submission polling');
    }
}

/**
 * Check and update all pending submissions
 */
async function checkAndUpdateSubmissions() {
    // Check if banner should be shown
    const hiddenUntil = sessionStorage.getItem('banner_hidden_until');
    const shouldShowBanner = !hiddenUntil || Date.now() > parseInt(hiddenUntil);
    
    // Fetch pending submissions
    const pending = await checkPendingSubmissions();
    
    if (pending.length === 0) {
        stopSubmissionPolling();
        return;
    }
    
    // Check each submission status
    for (const submission of pending) {
        const status = await checkSubmissionStatus(submission.submissionUuid);
        
        if (status && status.status === 'completed') {
            // Show notification
            showCompletedNotification(status);
            
            // Remove from pending list
            pendingSubmissions = pendingSubmissions.filter(
                s => s.submissionUuid !== submission.submissionUuid
            );
            
            // Add to completed list
            if (!completedSubmissions.find(s => s.submissionUuid === submission.submissionUuid)) {
                completedSubmissions.push(status);
            }
            
            // Refresh page data
            location.reload(); // Or update specific sections
        } else if (status && status.status === 'failed') {
            console.error('❌ Submission failed:', submission.submissionUuid);
            
            // Remove from pending
            pendingSubmissions = pendingSubmissions.filter(
                s => s.submissionUuid !== submission.submissionUuid
            );
        }
    }
    
    // Update banner
    if (pendingSubmissions.length > 0 && shouldShowBanner) {
        showProcessingBanner(pendingSubmissions);
    } else if (pendingSubmissions.length === 0) {
        hideProcessingBanner();
        stopSubmissionPolling();
    }
}

// ========== ORIGINAL FUNCTIONS ==========

// Fetch user statistics from backend
async function fetchUserStats() {
    try {
        // TODO: Replace with actual API call
        // const response = await fetch('/api/user/statistics');
        // const data = await response.json();
        
        // For now, using sample data
        console.log('User statistics loaded');
        return userData;
    } catch (error) {
        console.error('Error fetching user statistics:', error);
        return null;
    }
}

// Update chart with new data
function updateChartData(chart, newData) {
    chart.data.datasets[0].data = newData.writing;
    chart.data.datasets[1].data = newData.listening;
    chart.data.datasets[2].data = newData.speaking;
    chart.data.datasets[3].data = newData.reading;
    chart.update();
}

// Calculate average score
function calculateAverage(scores) {
    const sum = scores.reduce((a, b) => a + b, 0);
    return (sum / scores.length).toFixed(1);
}

// Display user statistics
function displayUserStats() {
    const avgWriting = calculateAverage(userData.writing);
    const avgListening = calculateAverage(userData.listening);
    const avgSpeaking = calculateAverage(userData.speaking);
    const avgReading = calculateAverage(userData.reading);
    
    console.log('Average Scores:');
    console.log('Writing:', avgWriting);
    console.log('Listening:', avgListening);
    console.log('Speaking:', avgSpeaking);
    console.log('Reading:', avgReading);
    
    // Overall average
    const overallAvg = ((parseFloat(avgWriting) + parseFloat(avgListening) + 
                        parseFloat(avgSpeaking) + parseFloat(avgReading)) / 4).toFixed(1);
    console.log('Overall Average:', overallAvg);
}

// Animate category cards on scroll
function animateCategoryCards() {
    const cards = document.querySelectorAll('.category-card-dashboard');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }, index * 100);
            }
        });
    }, {
        threshold: 0.1
    });
    
    cards.forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        card.style.transition = 'all 0.6s ease';
        observer.observe(card);
    });
}

// Handle category card clicks with tracking
function handleCategoryClick(category) {
    console.log(`Navigating to ${category} tests`);
    
    // Track user interaction
    trackUserAction('category_click', { category: category });
    
    // Navigate to test page
    window.location.href = `/${category}/tests`;
}

// Track user actions (for analytics)
function trackUserAction(action, data) {
    // TODO: Implement analytics tracking
    console.log('User Action:', action, data);
    
    // Example: Send to analytics service
    // analytics.track(action, data);
}

// Initialize dashboard on page load
document.addEventListener('DOMContentLoaded', async function() {
    console.log('📊 Dashboard initialized');
    
    // Fetch user statistics
    const stats = await fetchUserStats();
    
    // Initialize radar chart
    const chart = initRadarChart();
    
    // Display statistics
    displayUserStats();
    
    // Animate category cards
    animateCategoryCards();
    
    // Add click listeners to category cards
    const categoryCards = document.querySelectorAll('.category-card-dashboard');
    categoryCards.forEach(card => {
        card.addEventListener('click', function() {
            const category = this.querySelector('h3').textContent.toLowerCase().trim();
            handleCategoryClick(category);
        });
    });
    
    // ✅ NEW: Check for pending submissions and start polling
    const pending = await checkPendingSubmissions();
    if (pending.length > 0) {
        startSubmissionPolling();
    }
    
    // Refresh data every 5 minutes
    setInterval(async () => {
        const newStats = await fetchUserStats();
        if (newStats && chart) {
            updateChartData(chart, newStats);
        }
    }, 300000); // 5 minutes
});

// Handle window resize
window.addEventListener('resize', function() {
    // Chart.js handles responsive resizing automatically
    console.log('Window resized');
});

// Handle page visibility change (stop polling when page is hidden)
document.addEventListener('visibilitychange', function() {
    if (document.hidden) {
        stopSubmissionPolling();
    } else {
        // Resume polling if there are pending submissions
        if (pendingSubmissions.length > 0) {
            startSubmissionPolling();
        }
    }
});

// Export functions for external use
window.DashboardApp = {
    updateChartData,
    calculateAverage,
    fetchUserStats,
    trackUserAction,
    checkPendingSubmissions,
    checkSubmissionStatus,
    viewResult,
    dismissNotification,
    hideBannerTemporarily
};