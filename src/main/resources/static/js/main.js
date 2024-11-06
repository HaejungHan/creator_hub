$(document).ready(function () {
    const searchInput = $('#searchInput');
    const searchButton = $('#searchButton');
    const searchResultsGrid = $('#searchResultsGrid');
    const loginBtn = $('#loginBtn');
    const popularVideosGrid = $('#popularVideos');

    // 로그인 상태 체크
    checkLoginStatus();

    // 인기 동영상 로드
    loadPopularVideos();

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
        loadVideoModal(videoId); // 해당 videoId로 모달을 채워넣음
    });
});
