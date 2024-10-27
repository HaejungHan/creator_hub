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

    function performSearch() {
        const searchTerm = searchInput.val().trim();
        if (searchTerm) {
            searchResults.show();
            searchResultsGrid.empty();

            const searchQueries = [
                { title: `"${searchTerm}" 완벽 가이드`, views: '12.5만', date: '3일 전', duration: '18:42' },
                { title: `${searchTerm} 성공 비결 대공개`, views: '8.2만', date: '1주일 전', duration: '12:15' },
                { title: `${searchTerm} 트렌드 분석`, views: '15.7만', date: '5일 전', duration: '21:30' }
            ];

            searchQueries.forEach(result => {
                const videoCard = generateVideoCard(result);
                searchResultsGrid.append(videoCard);
            });
        } else {
            searchResults.hide();
        }
    }

    function generateVideoCard(data) {
        return `
            <div class="video-card">
                <div class="thumbnail">
                    <img src="https://picsum.photos/400/225?random=${Math.random()}" alt="${data.title}">
                    <span class="duration">${data.duration}</span>
                </div>
                <div class="video-info">
                    <h3 class="video-title">${data.title}</h3>
                    <div class="video-stats">
                        <span class="views">
                            <i class="fas fa-eye"></i>
                            ${data.views}
                        </span>
                        <span class="upload-date">
                            <i class="far fa-clock"></i>
                            ${data.date}
                        </span>
                    </div>
                </div>
            </div>
        `;
    }

    searchButton.on('click', performSearch);
    searchInput.on('keypress', function (e) {
        if (e.key === 'Enter') performSearch();
    });

    $('.menu-item').on('click', function () {
        $('.menu-item').removeClass('active');
        $(this).addClass('active');
    });

    const popularTitles = [
        "2024 유튜브 알고리즘 완벽 분석",
        "초보 크리에이터를 위한 장비 세팅",
        "매출 1억 유튜버의 비밀",
        "영상편집 마스터 클래스",
        "조회수 폭발하는 썸네일 제작법",
        "유튜브 수익화 가이드"
    ];

    const recommendedTitles = [
        "브이로그 촬영 꿀팁 모음",
        "유튜브 채널 성장 전략",
        "크리에이터 번아웃 극복하기",
        "1인 미디어 성공 사례",
        "유튜브 쇼츠 활용법",
        "구독자 10만 달성 노하우"
    ];

    function populateVideoGrid(gridId, titles) {
        const $grid = $('#' + gridId);
        titles.forEach(title => {
            const views = Math.floor(Math.random() * 50) + 5;
            const days = Math.floor(Math.random() * 7) + 1;
            const minutes = Math.floor(Math.random() * 20) + 5;
            const seconds = Math.floor(Math.random() * 59);

            const videoData = {
                title: title,
                views: views + '만',
                date: days + '일 전',
                duration: `${minutes}:${seconds.toString().padStart(2, '0')}`
            };

            const videoCard = generateVideoCard(videoData);
            $grid.append(videoCard);
        });
    }

    populateVideoGrid('popularVideos', popularTitles);
    populateVideoGrid('recommendedVideos', recommendedTitles);
});
