$(document).ready(function () {
    const searchInput = $('#searchInput');
    const searchButton = $('#searchButton');
    const searchResults = $('#searchResults');
    const searchResultsGrid = $('#searchResultsGrid');

    const loginBtn = $('#loginBtn');
    const loginModal = $('#loginModal');
    const closeButtons = $('.close-modal');
    const loginForm = $('#loginForm');

    function showModal(modal) {
        modal.show();
        $('body').css('overflow', 'hidden');
    }

    function closeModal(modal) {
        modal.hide();
        $('body').css('overflow', 'auto');
    }

    loginBtn.on('click', () => showModal(loginModal));

    closeButtons.on('click', function () {
        const modalId = $(this).data('modal');
        closeModal($('#' + modalId));
    });

    $(window).on('click', function (e) {
        if ($(e.target).hasClass('modal')) {
            closeModal($(e.target));
        }
    });

    function validateForm(formId) {
        const password = $('#' + formId).find('input[type="password"]');
        if (password.val().length < 8) {
            alert('비밀번호는 8자 이상이어야 합니다.');
            return false;
        }
        return true;
    }

    loginForm.on('submit', function (e) {
        e.preventDefault();
        if (validateForm('loginForm')) {
            // Login logic
        }
    });

    $(document).ready(function() {
        function parseDuration(duration) {
            const regex = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/;
            const matches = duration.match(regex);

            const hours = matches[1] ? parseInt(matches[1]) : 0;
            const minutes = matches[2] ? parseInt(matches[2]) : 0;
            const seconds = matches[3] ? parseInt(matches[3]) : 0;

            const parts = [];
            if (hours) parts.push(`${hours}시간`);
            if (minutes) parts.push(`${minutes}분`);
            if (seconds) parts.push(`${seconds}초`);

            return parts.join(' ') || '0초';
        }

        function formatPublishedDate(publishedAt) {
            const publishDate = new Date(publishedAt.value);
            const now = new Date();
            const timeDiff = now - publishDate; // 밀리초 차이
            const daysDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24)); // 일 수로 변환

            if (daysDiff < 1) {
                return "오늘";
            } else if (daysDiff === 1) {
                return "어제";
            } else {
                return `${daysDiff}일 전`;
            }
        }

        function loadPopularVideos() {
            $.ajax({
                url: '/api/popular', // API endpoint
                method: 'GET',
                success: function(videos) {
                    const popularVideosGrid = $('#popularVideos');
                    popularVideosGrid.empty(); // 기존 내용 비우기

                    videos.forEach(video => {
                        const readableDuration = parseDuration(video.duration); // 변환된 지속 시간
                        const readablePublishedDate = formatPublishedDate(video.publishedAt); // 변환된 게시 날짜
                        const videoCard = `
                    <div class="video-card">
    <div class="thumbnail">
        <img src="${video.thumbnailUrl.replace('default.jpg', 'hqdefault.jpg')}" alt="${video.title}">
        <span class="duration">${readableDuration}</span>
    </div>
    <div class="video-info">
        <h3 class="video-title">${video.title}</h3>
        <div class="video-stats">
            <span class="views">
                <i class="fas fa-eye"></i>
                ${video.viewCount} views
            </span>
            <span class="upload-date">
                <i class="far fa-clock"></i>
                ${readablePublishedDate}
            </span>
        </div>
    </div>
</div>`;
                        popularVideosGrid.append(videoCard);
                    });
                },
                error: function(err) {
                    console.error('Error loading popular videos:', err);
                }
            });
        }

        function performSearch() {
            const query = $('#searchInput').val().trim();
            if (query) {
                $.ajax({
                    url: '/api/search', // API endpoint
                    method: 'GET',
                    data: { query: query },
                    success: function(videos) {
                        const searchResultsGrid = $('#searchResultsGrid');
                        searchResultsGrid.empty(); // 기존 내용 비우기

                        videos.forEach(video => {
                            const readableDuration = parseDuration(video.duration); // 변환된 지속 시간
                            const readablePublishedDate = formatPublishedDate(video.publishedAt); // 변환된 게시 날짜
                            const videoCard = `
                        <div class="video-card">
    <div class="thumbnail">
        <img src="${video.thumbnailUrl.replace('default.jpg', 'hqdefault.jpg')}" alt="${video.title}">
        <span class="duration">${readableDuration}</span>
    </div>
    <div class="video-info">
        <h3 class="video-title">${video.title}</h3>
        <div class="video-stats">
            <span class="views">
                <i class="fas fa-eye"></i>
                ${video.viewCount} views
            </span>
            <span class="upload-date">
                <i class="far fa-clock"></i>
                ${readablePublishedDate}
            </span>
        </div>
    </div>
</div>`;
                            searchResultsGrid.append(videoCard);
                        });
                    },
                    error: function(err) {
                        console.error('Error searching videos:', err);
                    }
                });
            }
        }

        $('#searchButton').on('click', function(e) {
            e.preventDefault(); // 기본 폼 제출 방지
            performSearch();
        });

        // 페이지 로드 시 인기 동영상 로드
        loadPopularVideos();

        // Enter 키로 검색
        $('#searchInput').on('keypress', function(e) {
            if (e.key === 'Enter') performSearch();
        });

        // 메뉴 항목 클릭 시 활성화 효과
        $('.menu-item').on('click', function() {
            $('.menu-item').removeClass('active');
            $(this).addClass('active');
        });
    });

    document.addEventListener('DOMContentLoaded', function() {
        const videoData = {
            id: 'dQw4w9WgXcQ',
            title: 'Sample Video 1',
            views: '1M',
            date: '2023-06-01',
            analytics: {
                views: '1,000,000',
                watchTime: '50,000',
                likes: '50,000',
                subscribers: '1,000',
                avgWatchTime: '4:30',
                comments: '2,500',
                weeklyViews: [60, 75, 85, 95, 100, 90, 80], // Percentage values for bar chart
                demographics: [40, 80, 95, 60, 30, 20], // Percentage values for demographic bars
                devices: {
                    mobile: 45,
                    desktop: 30,
                    tablet: 25
                }
            }
        };

        setTimeout(() => {
            openVideoModal(videoData);
        }, 500);

        document.querySelectorAll('.video-card').forEach(card => {
            card.addEventListener('click', function() {
                openVideoModal(videoData);
            });
        });

        document.querySelector('.video-modal-close').addEventListener('click', function() {
            document.getElementById('videoModal').style.display = 'none';
            document.getElementById('videoPlayer').src = '';
        });

        window.addEventListener('click', function(event) {
            const modal = document.getElementById('videoModal');
            if (event.target === modal) {
                modal.style.display = 'none';
                document.getElementById('videoPlayer').src = '';
            }
        });

        function openVideoModal(videoData) {
            document.getElementById('modalVideoTitle').textContent = videoData.title;
            document.getElementById('modalVideoViews').textContent = `${videoData.views} views`;
            document.getElementById('modalVideoDate').textContent = videoData.date;
            document.getElementById('videoPlayer').src = `https://www.youtube.com/embed/${videoData.id}`;

            updateAnalytics(videoData.analytics);

            document.getElementById('videoModal').style.display = 'block';
        }

        function updateAnalytics(data) {
            // Update analytics cards
            const cards = document.querySelectorAll('.analytics-value');
            cards[0].textContent = data.views;
            cards[1].textContent = data.watchTime;
            cards[2].textContent = data.likes;
            cards[3].textContent = data.subscribers;
            cards[4].textContent = data.avgWatchTime;
            cards[5].textContent = data.comments;

            // Update bar chart
            document.querySelectorAll('.bar').forEach((bar, index) => {
                bar.style.height = `${data.weeklyViews[index]}%`;
                bar.setAttribute('data-value', `${data.weeklyViews[index]}K`);
            });

            // Update demographic bars
            document.querySelectorAll('.demographic-bar').forEach((bar, index) => {
                bar.style.height = `${data.demographics[index]}%`;
                bar.style.setProperty('--delay', `${index * 0.1}s`);
            });

            // Update device distribution
            const devicePie = document.querySelector('.device-pie');
            devicePie.style.background = `conic-gradient(
            #1a73e8 0% ${data.devices.mobile}%,
            #34a853 ${data.devices.mobile}% ${data.devices.mobile + data.devices.desktop}%,
            #ea4335 ${data.devices.mobile + data.devices.desktop}% 100%
        )`;
        }
    });

});
