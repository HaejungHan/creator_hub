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
    <h2>추천 동영상</h2>
    <div class="video-grid" id="allVideos"></div>
    <div id="loginMessage" style="display: none;">
      <p>로그인이 필요합니다.</p>
    </div>
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
  $(document).ready(function() {
    function getCookie(name) {
      const value = document.cookie;
      const parts = value.split('; ');

      for (let i = 0; i < parts.length; i++) {
        const part = parts[i];
        const cookieParts = part.split('=');

        if (cookieParts.length === 2) {
          const cookieName = cookieParts[0];
          const cookieValue = cookieParts[1];

          if (cookieName === name) {
            return cookieValue ? cookieValue.replace(/^Bearer\s*/, '') : '';
          }
        }
      }
      return '';
    }

    const token = getCookie('access_token');
    const isLoggedIn = token != null && token !== '';

    if (isLoggedIn) {
      $.ajax({
        url: '/recommendation',
        type: 'GET',
        headers: {
          'Authorization': 'Bearer ' + token
        },
        success: function(response) {
          const recommendations = JSON.parse(response.recommendations);

          if (recommendations && recommendations.length > 0) {
            recommendations.forEach(function(video) {
              const videoCard = generateVideoCard(video);
              $('#allVideos').append(videoCard);
            });
            $('#allVideos').show();  // 동영상 목록을 표시
          } else {
            $('#allVideos').append('<p>추천 동영상이 없습니다.</p>');
            $('#allVideos').show();
          }
        },
        error: function(xhr, status, error) {
          console.error('Error fetching recommendations:', error);
          $('#loginMessage').show();
        }
      });
    } else {
      $('#loginMessage').show();
    }
  });

  // 동영상 카드 생성 함수
  function generateVideoCard(video) {
    const formattedDuration = formatDuration(video.duration);
    const publishedAt = Date.parse(video.publishedAt) ? new Date(video.publishedAt) : new Date();
    const formattedDate = !isNaN(publishedAt) ? new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    }).format(publishedAt) : '알 수 없음';

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
