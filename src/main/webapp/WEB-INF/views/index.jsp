<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <base href="https://yourwebsite.com/">
    <meta charset="UTF-8">
    <title>YouTube Creator Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        :root {
            --primary: #ff0000;
            --dark: #282828;
            --light: #f8f8f8;
            --shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Roboto', sans-serif;
            background: #f9f9f9;
        }

        .dashboard {
            display: flex;
        }

        /* Sidebar Styles */
        .sidebar {
            width: 240px;
            height: 100vh;
            background: white;
            position: fixed;
            left: 0;
            top: 0;
            padding: 20px 0;
            border-right: 1px solid #e5e5e5;
        }

        .logo {
            display: flex;
            align-items: center;
            padding: 0 24px;
            margin-bottom: 24px;
        }

        .logo img {
            height: 30px;
        }

        .menu-item {
            display: flex;
            align-items: center;
            padding: 10px 24px;
            color: #606060;
            cursor: pointer;
            transition: all 0.2s;
        }

        .menu-item i {
            margin-right: 24px;
            font-size: 20px;
            width: 24px;
            text-align: center;
        }

        .menu-item:hover {
            background: #f2f2f2;
        }

        .menu-item.active {
            background: #e5e5e5;
            color: #030303;
        }

        /* Main Content Styles */
        .main-content {
            margin-left: 240px;
            width: calc(100% - 240px);
            padding: 24px;
        }

        /* Search Bar Styles */
        .search-container {
            background: white;
            padding: 16px 24px;
            border-radius: 8px;
            box-shadow: var(--shadow);
            margin-bottom: 24px;
            position: sticky;
            top: 24px;
            z-index: 100;
        }

        .search-bar {
            width: 100%;
            display: flex;
            align-items: center;
        }

        .search-bar input {
            flex: 1;
            padding: 12px 16px;
            border: 1px solid #e5e5e5;
            border-radius: 24px;
            font-size: 16px;
            transition: all 0.3s;
        }

        .search-bar input:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 1px rgba(255, 0, 0, 0.1);
        }

        .search-bar button {
            background: #f8f8f8;
            border: none;
            padding: 12px 24px;
            margin-left: 12px;
            border-radius: 24px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .search-bar button:hover {
            background: #e5e5e5;
        }

        /* Content Section Styles */
        .content-section {
            margin-bottom: 40px;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .section-title {
            font-size: 20px;
            font-weight: 500;
            color: #030303;
        }

        .video-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
        }

        .video-card {
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: var(--shadow);
            transition: transform 0.2s;
        }

        .video-card:hover {
            transform: translateY(-4px);
        }

        .thumbnail {
            position: relative;
            padding-top: 56.25%; /* 16:9 Aspect Ratio */
        }

        .thumbnail img {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .duration {
            position: absolute;
            bottom: 8px;
            right: 8px;
            background: rgba(0, 0, 0, 0.8);
            color: white;
            padding: 3px 6px;
            border-radius: 4px;
            font-size: 12px;
        }

        .video-info {
            padding: 16px;
        }

        .video-title {
            font-weight: 500;
            margin-bottom: 8px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }

        .video-stats {
            color: #606060;
            font-size: 14px;
            display: flex;
            align-items: center;
        }

        .views {
            margin-right: 8px;
        }

        .upload-date::before {
            content: "•";
            margin: 0 4px;
        }

        /* Animation */
        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .content-section {
            animation: fadeIn 0.3s ease-out;
        }
    </style>
</head>
<body>

<div class="dashboard">
    <div class="sidebar">
        <div class="logo">
            <i class="fab fa-youtube" style="color: red; font-size: 30px; margin-right: 10px;"></i>
            <span style="font-size: 20px; font-weight: 500;">Creator_hub</span>
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
    </div>

    <div class="main-content">
        <div class="search-container">
            <div class="search-bar">
                <input type="text" placeholder="검색어를 입력하세요..." id="searchInput">
                <button id="searchButton">
                    <i class="fas fa-search"></i>
                    검색
                </button>
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
            <div class="video-grid" id="popularVideosGrid"></div>
        </div>

        <div class="content-section">
            <div class="section-header">
                <h2 class="section-title">추천 동영상</h2>
            </div>
            <div class="video-grid" id="recommendedVideosGrid"></div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function () {
        const $searchInput = $('#searchInput');
        const $searchButton = $('#searchButton');
        const $searchResults = $('#searchResults');
        const $searchResultsGrid = $('#searchResultsGrid');

        function performSearch() {
            const searchTerm = $searchInput.val().trim();
            if (searchTerm) {
                $searchResults.show();
                $searchResultsGrid.empty();

                const dummyResults = [
                    {title: `"${searchTerm}" 관련 영상 1`, views: '5.2만회', date: '2일 전', duration: '12:30'},
                    {title: `"${searchTerm}" 관련 영상 2`, views: '3.1만회', date: '1주일 전', duration: '08:45'},
                    {title: `"${searchTerm}" 관련 영상 3`, views: '8.9만회', date: '3일 전', duration: '15:20'}
                ];

                $.each(dummyResults, function (index, result) {
                    const videoCard = `
                    <div class="video-card">
                        <div class="thumbnail">
                            <img src="https://picsum.photos/300/169?random=${Math.random()}" alt="${result.title}">
                            <span class="duration">${result.duration}</span>
                        </div>
                        <div class="video-info">
                            <h3 class="video-title">${result.title}</h3>
                            <div class="video-stats">
                                <span class="views">조회수 ${result.views}</span>
                                <span class="upload-date">${result.date}</span>
                            </div>
                        </div>
                    </div>`;
                    $searchResultsGrid.append(videoCard);
                });
            } else {
                $searchResults.hide();
            }
        }

        $searchButton.on('click', performSearch);
        $searchInput.on('keypress', function (e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });

        // Menu item activation
        $('.menu-item').on('click', function () {
            $('.menu-item').removeClass('active');
            $(this).addClass('active');
        });

    });
</script>


</body>
</html>
