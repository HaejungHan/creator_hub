<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="path" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <meta charset="UTF-8">
    <title>YouTube Creator Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="${path}/js/index.js"></script>
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
                <input type="text" placeholder="영감을 찾아보세요..." id="searchInput">
                <button id="searchButton">
                    <i class="fas fa-search"></i>
                    검색
                </button>
            </div>
        </div>

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

        <div class="content-section" id="searchResults" style="display: none;">
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

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</body>
</html>
