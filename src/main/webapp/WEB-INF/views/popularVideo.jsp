<%--
  Created by IntelliJ IDEA.
  User: guro13
  Date: 24. 11. 23.
  Time: 오후 7:09
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Creator Hub</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sidebar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css"/>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://www.youtube.com/iframe_api"></script>
</head>
<body>
<div class="container">
    <div class="sidebar">
        <%@ include file="sidebar.jsp" %>
    </div>
    <%@ include file="login.jsp" %>

    <div class="main-content">
        <h2>인기 동영상 TOP10</h2>
        <div class="video-grid" id="allVideos"></div>
    </div>
</div>

<!-- 비디오 모달 (모달 HTML 추가) -->
<div class="video-modal" style="display: none;">
    <div class="video-modal-content">
        <button class="close-modal">&times;</button>
        <div class="video-player-container">
            <!-- iframe으로 유튜브 비디오를 삽입 -->
            <iframe class="video-player" width="560" height="315" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
        </div>
        <div class="video-details">
            <div class="video-details-header">
                <h2 class="video-details-title"></h2>
                <div class="video-details-stats">
                    <span class="video-views"></span>
                    <span class="video-comments"></span> <!-- 댓글 수 -->
                    <span class="video-likes"></span> <!-- 좋아요 수 -->
                </div>
            </div>
            <div class="video-creator">
                <div class="creator-avatar"></div>
                <div class="creator-info">
                    <div class="creator-name"></div>
                    <div class="creator-subscribers"></div>
                </div>
            </div>
            <div class="video-description"></div>
        </div>
    </div>
</div>
<script>
    const videosData = ${videoDto}; // 비디오 데이터

    // 비디오 그리드 렌더링
    function renderVideos(videosData) {
        const videoGrid = $('#allVideos');
        videoGrid.empty();
        if (videosData && videosData.length > 0) {
            videosData.forEach(video => {
                const videoCard = generateVideoCard(video);
                videoGrid.append(videoCard);
            });
        } else {
            videoGrid.append('<p>결과가 없습니다.</p>');
        }
    }

    // 비디오 카드 생성
    function generateVideoCard(video) {
        const formattedDuration = formatDuration(video.duration);

        // publishedAt 값을 명시적으로 파싱
        const publishedAt = Date.parse(video.publishedAt) ? new Date(video.publishedAt) : new Date();

        // 잘못된 날짜일 경우 기본값 처리
        const formattedDate = !isNaN(publishedAt) ? new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        }).format(publishedAt) : '알 수 없음';  // 날짜가 잘못된 경우 기본값으로 '알 수 없음' 사용

        const videoCard = $('<div>').addClass('video-card').attr('data-videoId', video.videoId);
        const videoThumbnail = $('<div>').addClass('thumbnail');
        const thumbnailImg = $('<img>').attr('src', video.thumbnailUrl).attr('alt', 'Thumbnail');
        const videoDuration = $('<span>').addClass('duration').text(formattedDuration);

        videoThumbnail.append(thumbnailImg, videoDuration);

        const videoInfo = $('<div>').addClass('video-info');
        const videoTitle = $('<h3>').addClass('video-title').text(video.title);

        const videoStats = $('<div>').addClass('video-stats');
        const views = $('<span>').addClass('views').html('<i class="fas fa-eye"></i> ' + video.viewCount);
        const uploadDate = $('<span>').addClass('upload-date').html('<i class="far fa-clock"></i> ' + formattedDate);

        videoStats.append(views, uploadDate);
        videoInfo.append(videoTitle, videoStats);
        videoCard.append(videoThumbnail, videoInfo);

        videoCard.on('click', function() {
            console.log('클릭된 비디오 ID:', video.videoId);
            openVideoModal(video.videoId);
        });

        return videoCard;
    }

    function formatDuration(duration) {
        const regex = /^PT(\d+H)?(\d+M)?(\d+S)?$/;
        const matches = duration.match(regex);
        if (!matches) return duration;

        let hours = 0, minutes = 0, seconds = 0, formattedDuration = "";
        if (matches[1]) hours = parseInt(matches[1].replace('H', ''), 10);
        if (matches[2]) minutes = parseInt(matches[2].replace('M', ''), 10);
        if (matches[3]) seconds = parseInt(matches[3].replace('S', ''), 10);

        if (hours > 0) formattedDuration += hours + "시간 ";
        if (minutes > 0) formattedDuration += minutes + "분 ";
        if (seconds > 0 || (hours === 0 && minutes === 0)) formattedDuration += seconds + "초";

        return formattedDuration.trim();
    }

    // 페이지 로드 시 상위 10개 비디오만 렌더링
    $(document).ready(function() {
        // 상위 10개 비디오만 추출
        const topVideos = videosData.slice(0, 10);
        renderVideos(topVideos);
    });

    let player;

    function onYouTubeIframeAPIReady() {
        const iframe = document.querySelector('.video-modal .video-player');

        if (iframe) {
            player = new YT.Player(iframe, {
                events: {
                    'onReady': onPlayerReady,
                }
            });
        }
    }

    function openVideoModal(videoId) {
        console.log("modal videoId : " + videoId);
        fetch('/video/' + videoId)
            .then(response => response.json())
            .then(videoData => {
                const videoModal = document.querySelector('.video-modal');
                videoModal.querySelector('.video-details-title').textContent = videoData.snippet.localized.title;
                videoModal.querySelector('.video-views').textContent = '조회수: ' + videoData.statistics.viewCount;
                videoModal.querySelector('.video-comments').textContent = '댓글 수: ' + videoData.statistics.commentCount;
                videoModal.querySelector('.video-likes').textContent = '좋아요 수: ' + videoData.statistics.likeCount;
                videoModal.querySelector('.video-player').src = "https://www.youtube.com/embed/" + videoId;
                videoModal.querySelector('.creator-name').textContent = videoData.snippet.channelTitle;
                videoModal.querySelector('.video-description').textContent = videoData.snippet.description;
                videoModal.style.display = 'flex';
            })
            .catch(error => console.error('비디오 데이터를 가져오는 데 오류 발생:', error));
    }

    function closeModal() {
        const videoModal = document.querySelector('.video-modal');

        videoModal.style.display = 'none';

        // YouTube Player가 존재하면 동영상 멈추기
        if (player) {
            player.stopVideo();  // 동영상 멈추고 소리도 끄기
        }
        const videoIframe = document.querySelector('.video-modal .video-player');
        if (videoIframe) {
            videoIframe.src = '';  // iframe src 초기화로 동영상 멈추기
        }
    }

    $('.video-modal').on('click', function (e) {
        if ($(e.target).is('.video-modal')) {
            closeModal();
        }
    });

    $('.video-modal .close-modal').on('click', closeModal);
</script>
</body>
</html>
