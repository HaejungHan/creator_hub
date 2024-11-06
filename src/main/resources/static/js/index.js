$(document).ready(function () {
    const searchInput = $('#searchInput');
    const searchButton = $('#searchButton');
    const searchResultsGrid = $('#searchResultsGrid');
    const loginBtn = $('#loginBtn');
    const loginModal = $('#loginModal');
    const closeButtons = $('.close-modal');
    const loginForm = $('#loginForm');
    const popularVideosGrid = $('#popularVideos');
    const videoModal = $('#videoModal'); // 모달
    const videoPlayer = $('#videoPlayer'); // 비디오 플레이어 iframe

    // 모달 열기/닫기
    function showModal(modal) {
        modal.show();
        $('body').css('overflow', 'hidden');
    }

    function closeModal(modal) {
        modal.hide();
        $('body').css('overflow', 'auto');
    }

    // 로그인 버튼 클릭 시 모달 열기
    loginBtn.on('click', () => showModal(loginModal));

    // 모달 닫기 버튼 클릭 시
    closeButtons.on('click', function () {
        const modalId = $(this).data('modal');
        closeModal($('#' + modalId));
    });

    // 화면 클릭 시 모달 닫기 (모달 외 영역 클릭 시)
    $(window).on('click', function (e) {
        if ($(e.target).hasClass('modal')) {
            closeModal($(e.target));
        }
    });

    // 비밀번호 유효성 체크 (8자 이상)
    function validateForm(formId) {
        const password = $('#' + formId).find('input[type="password"]');
        if (password.val().length < 8) {
            alert('비밀번호는 8자 이상이어야 합니다.');
            return false;
        }
        return true;
    }

    // 로그인 폼 제출
    loginForm.on('submit', function (e) {
        e.preventDefault();
        if (validateForm('loginForm')) {
            // 로그인 로직
        }
    });

    // 동영상 지속 시간 포맷 함수
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

    // 게시 날짜 포맷 함수
    function formatPublishedDate(publishedAt) {
        const publishDate = new Date(publishedAt);
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

    // 인기 동영상 불러오기
    function loadPopularVideos() {
        $.ajax({
            url: '/api/popular', // API endpoint
            method: 'GET',
            success: function (videos) {
                popularVideosGrid.empty(); // 기존 내용 비우기

                videos.forEach(video => {
                    const readableDuration = parseDuration(video.duration); // 변환된 지속 시간
                    const readablePublishedDate = formatPublishedDate(video.publishedAt); // 변환된 게시 날짜
                    const videoCard = `
                    <div class="video-card" data-id="${video.videoId}">
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
            error: function (err) {
                console.error('Error loading popular videos:', err);
            }
        });
    }

    // 검색 기능
    function performSearch() {
        const query = searchInput.val().trim();
        if (query) {
            $.ajax({
                url: '/api/search', // API endpoint
                method: 'GET',
                data: { query: query },
                success: function (videos) {
                    searchResultsGrid.empty(); // 기존 내용 비우기

                    videos.forEach(video => {
                        const readableDuration = parseDuration(video.duration); // 변환된 지속 시간
                        const readablePublishedDate = formatPublishedDate(video.publishedAt); // 변환된 게시 날짜
                        const videoCard = `
                        <div class="video-card" data-id="${video.videoId}">
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
                error: function (err) {
                    console.error('Error searching videos:', err);
                }
            });
        }
    }

    // 검색 버튼 클릭 시
    searchButton.on('click', function (e) {
        e.preventDefault(); // 기본 폼 제출 방지
        performSearch();
    });

    // Enter 키로 검색
    searchInput.on('keypress', function (e) {
        if (e.key === 'Enter') performSearch();
    });

    // 메뉴 항목 클릭 시 활성화 효과
    $('.menu-item').on('click', function () {
        $('.menu-item').removeClass('active');
        $(this).addClass('active');
    });

    // 동영상 카드 클릭 시 모달 열기
    $(document).on('click', '.video-card', function () {
        const videoId = $(this).data('id'); // 클릭된 카드의 video id
        console.log("Clicked video ID:", videoId);
        loadVideoModal(videoId); // 해당 videoId로 모달을 채워넣음
    });

    function loadVideoModal(id) {
        // 비디오 정보를 백엔드에서 가져오기
        $.ajax({
            url: `/api/video/${id}`, // 백엔드에서 비디오 데이터 가져오기
            method: 'GET',
            success: function (video) {
                // 1. 비디오 정보를 업데이트 (제목, 조회수, 날짜 등)
                $('#modalVideoTitle').text(video.title); // 비디오 제목
                $('#modalVideoViews').text(`${video.viewCount} views`); // 조회수
                $('#modalVideoDate').text(formatPublishedDate(video.publishedAt)); // 게시 날짜 포맷

                // 2. iframe에 유튜브 동영상 링크 삽입
                const youtubeEmbedUrl = `https://www.youtube.com/embed/${id}`;  // 임베드 URL 대신 일반 유튜브 링크
                $('#videoPlayer').attr('src', youtubeEmbedUrl); // iframe src에 유튜브 Embed URL 설정

                // 3. 모달 열기
                showModal($('#videoModal')); // 모달 열기
            },
            error: function (err) {
                console.error('Error loading video modal:', err);
            }
        });
    }

    // 모달 닫기
    $('.video-modal-close').click(function () {
        closeModal($('#videoModal')); // 모달 닫기
    });

    // 페이지 로드 시 인기 비디오 불러오기
    loadPopularVideos();
});
