document.addEventListener('DOMContentLoaded', function () {
    // Dropdown "IELTS Online Test" toggle
    const dropdownToggle = document.querySelector('.dropdown-toggle');
    const dropdown = document.querySelector('.dropdown');

    if (dropdownToggle && dropdown) {
        dropdownToggle.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            dropdown.classList.toggle('active');
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', function (e) {
            if (!dropdown.contains(e.target)) {
                dropdown.classList.remove('active');
            }
        });

        // Prevent dropdown from closing when clicking inside menu
        const dropdownMenu = dropdown.querySelector('.dropdown-menu');
        if (dropdownMenu) {
            dropdownMenu.addEventListener('click', function (e) {
                // Allow navigation to links
                if (e.target.tagName === 'A') {
                    dropdown.classList.remove('active');
                }
            });
        }
    }

    // User menu toggle
    const userMenuToggle = document.querySelector('.user-menu-toggle');
    const userMenu = document.querySelector('.user-menu');

    if (userMenuToggle && userMenu) {
        userMenuToggle.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            userMenu.classList.toggle('active');
        });

        // Close user menu when clicking outside
        document.addEventListener('click', function (e) {
            if (!userMenu.contains(e.target)) {
                userMenu.classList.remove('active');
            }
        });

        // Close user menu when clicking a link
        const userDropdown = userMenu.querySelector('.user-dropdown');
        if (userDropdown) {
            userDropdown.addEventListener('click', function (e) {
                if (e.target.tagName === 'A') {
                    userMenu.classList.remove('active');
                }
            });
        }
    }
});