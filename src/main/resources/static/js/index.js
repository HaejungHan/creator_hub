$(document).ready(function () {
    const searchInput = $('#searchInput');
    const searchButton = $('#searchButton');
    const searchResultsGrid = $('#searchResultsGrid');
    const loginBtn = $('#loginBtn');
    const loginModal = $('#loginModal');
    const closeButtons = $('.close-modal');
    const loginForm = $('#loginForm');
    const popularVideosGrid = $('#popularVideos');
    const videoModal = $('#videoModal');
    const videoPlayer = $('#videoPlayer');
    let player;
    let videoId;
    let videoStartTime = 0;
    let videoTimer;

    function onYouTubeIframeAPIReady() {
        const playerElement = document.getElementById('videoPlayer');
        if (playerElement) {
            player = new YT.Player('videoPlayer', {
                events: {
                    'onReady': onPlayerReady,
                    'onStateChange': onPlayerStateChange
                }
            });
        } else {
            console.error('Player element not found!');
        }
    }

    // 동영상 모달 로드
    async function loadVideoModal(id) {
        try {
            const response = await fetch(`/api/video/${id}`); // fetch를 사용하여 백엔드에서 비디오 데이터 가져오기
            const video = await response.json(); // 응답을 JSON으로 파싱

            // 비디오 정보 업데이트
            $('#modalVideoTitle').text(video.title);
            $('#modalVideoViews').text(`${video.viewCount} views`);
            $('#modalVideoDate').text(formatPublishedDate(video.publishedAt));

            // iframe src에 유튜브 Embed URL 설정
            const youtubeEmbedUrl = `https://www.youtube.com/embed/${id}`;
            $('#videoPlayer').attr('src', youtubeEmbedUrl);

            // 모달 열기
            showModal($('#videoModal'));
        } catch (err) {
            console.error('Error loading video modal:', err);
        }
    }

    // 모달 닫기 시 타이머와 비디오 종료
    $(document).on('click', '.video-modal-close', function () {
        stopVideo();
        $('#videoPlayer').attr('src', '');  // iframe src 비워서 비디오 종료
        closeModal($('#videoModal'));
    });

    // 비디오 중지 함수
    function stopVideo() {
        if (player) {
            player.stopVideo();
            console.log("Video stopped");
        }
    }

    loginBtn.on('click', () => {
        if (loginBtn.text().trim() === "로그인") {
            showModal(loginModal);  // 로그인 모달 열기
        } else if (loginBtn.text().trim() === "로그아웃") {
            handleLogout(); // 로그아웃 처리
        }
    });

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

    // 로그인 폼 제출
    $('#loginForm').submit(async function (e) {
        e.preventDefault();  // 기본 폼 제출을 막고, AJAX 요청을 처리하도록 합니다.

        const email = $('#loginEmail').val().trim();
        const password = $('#loginPassword').val().trim();

        if (!email || !password) {
            $('#error-message').text('이메일과 비밀번호를 입력해주세요.');
            $('#error-message').show();
            return;
        }

        const data = {
            username: email,  // 이메일을 'username'으로 사용
            password: password
        };

        try {
            const response = await fetch('/login', {  // fetch로 로그인 요청
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                const accessToken = response.headers.get('Authorization');
                const refreshToken = response.headers.get('RefreshToken');

                // Authorization 토큰을 쿠키에 저장
                if (accessToken) {
                    Cookies.set('Authorization', accessToken.split(' ')[1], {
                        path: '/',
                        sameSite: 'Lax',
                        secure: false
                    });
                }

                // RefreshToken을 쿠키에 저장
                if (refreshToken) {
                    Cookies.set('RefreshToken', refreshToken.split(' ')[1], {
                        path: '/',
                        sameSite: 'Lax',
                        secure: false
                    });
                }

                alert('로그인이 완료되었습니다.');
                window.location.href = '/main';
                setTimeout(function () {
                    $('#loginBtn').html('<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>');
                    $('#loginBtn').attr('id', 'logoutBtn');
                }, 100);
            } else {
                const error = await response.json();
                $('#error-message').text('Invalid email or password. Please try again.');
                $('#error-message').show();
                alert('로그인 실패');
            }
        } catch (err) {
            console.error('Login failed', err);
            $('#error-message').text('로그인 중 오류가 발생했습니다.');
            $('#error-message').show();
            alert('로그인 실패');
        }
    });

    // 로그아웃 처리
    function handleLogout() {
        // 쿠키에서 토큰 삭제
        Cookies.remove('Authorization');
        Cookies.remove('RefreshToken');

        // 로그아웃 후 버튼 텍스트를 "로그인"으로 변경
        $('#logoutBtn').html('<i class="fas fa-sign-in-alt"></i><span>로그인</span>'); // 버튼 텍스트 변경
        $('#logoutBtn').attr('id', 'loginBtn'); // 버튼 ID 변경

        // 로그아웃 후 페이지 리디렉션 (메인 페이지로 리디렉션)
        window.location.href = '/main'; // 홈으로 리디렉션
    }

    // 로그인 상태 체크 및 버튼 초기화
    function checkLoginStatus() {
        const token = Cookies.get('Authorization');
        if (token) {
            $('#loginBtn').html('<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>');
            $('#loginBtn').attr('id', 'logoutBtn');
        }
    }

    // 페이지 로드 시 로그인 상태 확인
    checkLoginStatus();

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
    async function loadPopularVideos() {
        try {
            const response = await fetch('/api/popular');  // fetch로 인기 동영상 데이터를 가져오기
            const videos = await response.json(); // 응답을 JSON으로 파싱
            popularVideosGrid.empty(); // 기존 내용 비우기

            videos.forEach(video => {
                const readableDuration = parseDuration(video.duration);
                const readablePublishedDate = formatPublishedDate(video.publishedAt);
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
        } catch (err) {
            console.error('Error loading popular videos:', err);
        }
    }

    // 검색 기능
    async function performSearch() {
        const query = searchInput.val().trim();
        if (query) {
            try {
                const response = await fetch(`/api/search?query=${query}`);  // fetch로 검색 요청
                const videos = await response.json(); // 응답을 JSON으로 파싱
                searchResultsGrid.empty(); // 기존 내용 비우기

                videos.forEach(video => {
                    const readableDuration = parseDuration(video.duration);
                    const readablePublishedDate = formatPublishedDate(video.publishedAt);
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
            } catch (err) {
                console.error('Error searching videos:', err);
            }
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

    // 동영상 카드 클릭 시 모달 열기
    $(document).on('click', '.video-card', function () {
        const videoId = $(this).data('id'); // 클릭된 카드의 video id
        console.log("Clicked video ID:", videoId);
        loadVideoModal(videoId); // 해당 videoId로 모달을 채워넣음
    });

    // 인기 동영상 로드
    loadPopularVideos();
});
