// Dashboard Chart Configuration

// Sample data - replace with real user data from backend
const userData = {
    writing: [6, 5, 7, 6, 5, 6, 7],
    listening: [7, 6, 8, 7, 6, 7, 8],
    speaking: [5, 4, 6, 5, 4, 5, 6],
    reading: [8, 7, 6, 8, 7, 8, 7]
};

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
    window.location.href = `/${category}-tests`;
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
    console.log('Dashboard initialized');
    
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
            const category = this.querySelector('h3').textContent.toLowerCase();
            handleCategoryClick(category);
        });
    });
    
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

// Export functions for external use
window.DashboardApp = {
    updateChartData,
    calculateAverage,
    fetchUserStats,
    trackUserAction
};