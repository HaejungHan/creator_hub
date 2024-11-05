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
                <input type="text" id="searchInput" placeholder="검색어를 입력하세요" required>
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
<div class="video-modal" id="videoModal">
    <div class="video-modal-content">
        <span class="video-modal-close">&times;</span>

        <div class="video-player-container">
            <iframe id="videoPlayer" width="100%" height="100%" frameborder="0" allowfullscreen>
            </iframe>
        </div>

        <div class="video-info">
            <h2 id="modalVideoTitle">Video Title</h2>
            <div class="video-meta">
                <span id="modalVideoViews">0 views</span>
                <span id="modalVideoDate">Upload date</span>
            </div>
        </div>

        <div class="video-analytics">
            <div class="analytics-card">
                <div class="analytics-icon">
                    <i class="fas fa-eye"></i>
                </div>
                <div>
                    <h3>조회수</h3>
                    <div class="analytics-value">0</div>
                    <div class="analytics-trend trend-up">
                        <i class="fas fa-arrow-up"></i>
                        <span>12% from last week</span>
                    </div>
                </div>
            </div>

            <div class="analytics-card">
                <div class="analytics-icon">
                    <i class="fas fa-clock"></i>
                </div>
                <div>
                    <h3>시청 시간</h3>
                    <div class="analytics-value">0</div>
                    <div class="analytics-trend trend-up">
                        <i class="fas fa-arrow-up"></i>
                        <span>8% from last week</span>
                    </div>
                </div>
            </div>

            <div class="analytics-card">
                <div class="analytics-icon">
                    <i class="fas fa-thumbs-up"></i>
                </div>
                <div>
                    <h3>좋아요</h3>
                    <div class="analytics-value">0</div>
                    <div class="analytics-trend trend-down">
                        <i class="fas fa-arrow-down"></i>
                        <span>3% from last week</span>
                    </div>
                </div>
            </div>

            <div class="analytics-card">
                <div class="analytics-icon">
                    <i class="fas fa-users"></i>
                </div>
                <div>
                    <h3>구독자 증가</h3>
                    <div class="analytics-value">0</div>
                    <div class="analytics-trend trend-up">
                        <i class="fas fa-arrow-up"></i>
                        <span>15% from last week</span>
                    </div>
                </div>
            </div>

            <div class="analytics-card">
                <div class="analytics-icon">
                    <i class="fas fa-clock"></i>
                </div>
                <div>
                    <h3>평균 시청 시간</h3>
                    <div class="analytics-value">0</div>
                    <div class="analytics-trend trend-up">
                        <i class="fas fa-arrow-up"></i>
                        <span>5% from last week</span>
                    </div>
                </div>
            </div>

            <div class="analytics-card">
                <div class="analytics-icon">
                    <i class="fas fa-comments"></i>
                </div>
                <div>
                    <h3>댓글 수</h3>
                    <div class="analytics-value">0</div>
                    <div class="analytics-trend trend-down">
                        <i class="fas fa-arrow-down"></i>
                        <span>2% from last week</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="graph-section">
            <h2>상세 분석</h2>
            <div class="graph-container">
                <div class="graph-card">
                    <div class="graph-title">주간 조회수 트렌드</div>
                    <div class="bar-chart">
                        <div class="bar" style="height: 60%;" data-value="120K"></div>
                        <div class="bar" style="height: 75%;" data-value="150K"></div>
                        <div class="bar" style="height: 85%;" data-value="170K"></div>
                        <div class="bar" style="height: 95%;" data-value="190K"></div>
                        <div class="bar" style="height: 100%;" data-value="200K"></div>
                        <div class="bar" style="height: 90%;" data-value="180K"></div>
                        <div class="bar" style="height: 80%;" data-value="160K"></div>
                    </div>
                </div>

                <div class="graph-card">
                    <div class="graph-title">연령대별 시청자 분포</div>
                    <div class="demographic-chart">
                        <div class="demographic-bar" style="height: 40%;" data-age="13-17"></div>
                        <div class="demographic-bar" style="height: 80%;" data-age="18-24"></div>
                        <div class="demographic-bar" style="height: 95%;" data-age="25-34"></div>
                        <div class="demographic-bar" style="height: 60%;" data-age="35-44"></div>
                        <div class="demographic-bar" style="height: 30%;" data-age="45-54"></div>
                        <div class="demographic-bar" style="height: 20%;" data-age="55+"></div>
                    </div>
                </div>

                <div class="graph-card">
                    <div class="graph-title">시청 디바이스 분포</div>
                    <div class="device-chart">
                        <div class="device-pie"></div>
                    </div>
                    <div class="legend">
                        <div class="legend-item">
                            <div class="legend-color" style="background: #1a73e8;"></div>
                            <span>모바일 (45%)</span>
                        </div>
                        <div class="legend-item">
                            <div class="legend-color" style="background: #34a853;"></div>
                            <span>데스크톱 (30%)</span>
                        </div>
                        <div class="legend-item">
                            <div class="legend-color" style="background: #ea4335;"></div>
                            <span>태블릿 (25%)</span>
                        </div>
                    </div>
                </div>

                <div class="graph-card">
                    <div class="graph-title">평균 시청 시간 (분)</div>
                    <svg class="line-chart" viewBox="0 0 300 200">
                        <defs>
                            <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                                <stop offset="0%" style="stop-color:#1a73e8"/>
                                <stop offset="100%" style="stop-color:#4285f4"/>
                            </linearGradient>
                            <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                                <stop offset="0%" style="stop-color:rgba(26,115,232,0.2)"/>
                                <stop offset="100%" style="stop-color:rgba(26,115,232,0)"/>
                            </linearGradient>
                        </defs>
                        <path class="area" d="M0 200 L0 150 L50 140 L100 160 L150 120 L200 100 L250 80 L300 90 L300 200 Z"></path>
                        <path class="line" d="M0 150 L50 140 L100 160 L150 120 L200 100 L250 80 L300 90"></path>
                    </svg>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</body>
</html>
