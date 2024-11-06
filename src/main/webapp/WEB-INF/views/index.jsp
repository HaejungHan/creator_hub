<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="path" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <meta charset="UTF-8">
    <title>YouTube Creator Dashboard</title>

    <!-- Google Fonts (Roboto) -->
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">

    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">

    <!-- JS Cookie -->
    <script src="https://cdn.jsdelivr.net/npm/js-cookie@3.0.5/dist/js.cookie.min.js"></script>

    <!-- jQuery -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

    <!-- YouTube Iframe API -->
    <script src="https://www.youtube.com/iframe_api"></script>

    <!-- JavaScript 파일들 (순서 중요) -->
    <script src="${path}/js/utils.js"></script>    <!-- 유틸리티 함수들이 포함된 파일 -->
    <script src="${path}/js/modal.js"></script>    <!-- 모달 관련 코드 -->
    <script src="${path}/js/player.js"></script>   <!-- YouTube 플레이어 코드 -->
    <script src="${path}/js/main.js"></script>     <!-- 메인 로직 (이게 마지막에 로드되어야 함) -->
    <script src="${path}/js/login.js"></script>     <!-- 로그인 관련 기능 처리 -->

    <!-- CSS 스타일 시트 -->
    <link rel="icon" href="${path}/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="${path}/css/index.css"/>
</head>
<body>

<div class="dashboard">
    <div class="sidebar">
        <div class="logo">
            <i class="fab fa-youtube"></i>
            <span>Creator_hub</span>
        </div>

        <div class="menu-item active">
            <i class="fas fa-home"></i>
            <span>홈</span>
        </div>

        <div class="menu-item">
            <i class="fas fa-compass"></i>
            <span>탐색</span>
        </div>

        <div class="menu-item">
            <i class="fas fa-video"></i>
            <span>내 콘텐츠</span>
        </div>

        <div class="menu-item">
            <i class="fas fa-chart-line"></i>
            <span>분석</span>
        </div>

        <div class="menu-item">
            <i class="fas fa-comments"></i>
            <span>커뮤니티</span>
        </div>

        <div class="menu-item">
            <i class="fas fa-cog"></i>
            <span>설정</span>
        </div>

        <button class="auth-button-side login-btn" id="loginBtn">
            <i class="fas fa-sign-in-alt"></i>
            <span>로그인</span>
        </button>
    </div>

    <div class="main-content">
        <div class="search-container">
            <div class="search-bar">
                <input type="text" id="searchInput" placeholder="검색어를 입력하세요" required>
                <button id="searchButton">
                    <i class="fas fa-search"></i>
                    검색
                </button>
            </div>
        </div>

        <!-- 로그인 모달 -->
        <div class="modal" id="loginModal">
            <div class="modal-content">
                <span class="close-modal" data-modal="loginModal">&times;</span>
                <div class="modal-header">
                    <h2>로그인</h2>
                    <p>Creator_hub에 오신 것을 환영합니다</p>
                </div>
                <form class="modal-form" id="loginForm">
                    <div class="form-group">
                        <label for="loginEmail">이메일</label>
                        <input type="email" id="loginEmail" required>
                    </div>
                    <div class="form-group">
                        <label for="loginPassword">비밀번호</label>
                        <input type="password" id="loginPassword" required>
                    </div>
                    <button type="submit" class="modal-submit">로그인</button>
                </form>
            </div>
        </div>

        <!-- 검색 결과 및 비디오 관련 콘텐츠 -->
        <div class="content-section" id="searchResults" style="display: block;">
            <div class="section-header">
                <h2 class="section-title">검색 결과</h2>
            </div>
            <div class="video-grid" id="searchResultsGrid"></div>
        </div>

        <div class="content-section">
            <div class="section-header">
                <h2 class="section-title">인기 동영상</h2>
            </div>
            <div class="video-grid" id="popularVideos"></div>
        </div>

        <div class="content-section">
            <div class="section-header">
                <h2 class="section-title">추천 동영상</h2>
            </div>
            <div class="video-grid" id="recommendedVideos"></div>
        </div>

    </div>
</div>

<!-- Video Modal -->
<div class="video-modal" id="videoModal" style="display: none;">
    <div class="video-modal-content">
        <!-- 모달 닫기 버튼 -->
        <span class="video-modal-close">&times;</span>

        <!-- 비디오 플레이어 컨테이너 (iframe) -->
        <div class="video-player-container">
            <iframe id="videoPlayer" width="100%" height="100%" frameborder="0" allowfullscreen></iframe>
        </div>

        <!-- 비디오 정보 영역 -->
        <div class="video-info">
            <h2 id="modalVideoTitle">Video Title</h2>
            <div class="video-meta">
                <span id="modalVideoViews">0 views</span>
                <span id="modalVideoDate">Upload date</span>
            </div>
        </div>
    </div>
</div>
</body>
</html>
