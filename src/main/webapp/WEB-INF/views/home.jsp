<%--
  Created by IntelliJ IDEA.
  User: guro13
  Date: 24. 11. 20.
  Time: 오후 8:43
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
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <script src="https://www.youtube.com/iframe_api"></script>
</head>

<body>
<div class="container">
  <div class="sidebar">
    <%@ include file="sidebar.jsp" %>
  </div>
  <%@ include file="login.jsp" %>

  <div class="main-content">
    <h2>검색 결과</h2>

    <div class="home-search-container">
        <div class="search-bar">
          <label for="searchInput"></label>
          <input type="text" placeholder="검색어를 입력해주세요...." id="searchInput" name="query" value="${param.query}">
          <button id="searchButton" type="button" class="search-button">
            <i class="fas fa-search"></i> 검색
          </button>
        </div>

      <!-- 인기 키워드 섹션 -->
      <div class="trending-tags">
        <span class="tag-label">인기 키워드:</span>
        <div class="tags" id="keywordTags">
          <!-- 인기 키워드는 자바스크립트로 동적으로 처리 -->
        </div>
      </div>
      <div class="chart-container">
        <canvas id="trendingKeywordsChart"></canvas> <!-- 원형 차트가 그려질 캔버스 -->
      </div>
    </div>

      <!-- 비디오 그리드 -->
      <div class="video-grid" id="allVideos">
        <!-- 비디오 카드 데이터는 자바스크립트로 동적으로 생성 -->
      </div>

    <div id="pagination"></div>
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
  $(document).ready(function () {
    const searchInput = $('#searchInput');
    const searchButton = $('#searchButton');
    const keywordTagsContainer = $('#keywordTags');
    const videoGrid = $('#allVideos');
    const noResultsMessage = $('#noResultsMessage');

    let videos = [];

    const trendKeywordsJson = '${trendKeywordsJson}';
    let trendKeywords = [];

    try {
      trendKeywords = JSON.parse(trendKeywordsJson);
    } catch (error) {
      console.error('Error parsing trendKeywordsJson:', error);
    }

    function renderKeywordTags() {
      if (trendKeywords.length > 0) {
        trendKeywords.forEach(tag => {
          const tagButton = $('<button>').addClass('tag-item').text(tag.id);
          tagButton.click(() => {
            searchInput.val(tag.id);
            performSearch();
          });
          keywordTagsContainer.append(tagButton);
        });
      }
    }

    let chartInitialized = false;

    function initializeTrendingKeywordsChart() {

      if (chartInitialized) {
        return;
      }

      const ctx = document.getElementById('trendingKeywordsChart').getContext('2d');

      const labels = $.map(trendKeywords, function(tag) {
        return '#' + tag.id;
      });

      const chartData = {
        labels: labels,
        datasets: [{
          data: $.map(trendKeywords, function(tag) {
            return tag.count;
          }),
          backgroundColor: [
            '#1a73e8', '#6c5ce7', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#14B8A6', '#F97316', '#6366F1'
          ],
          borderWidth: 0
        }]
      };

      const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              padding: 20,
              font: {
                size: 12,
                family: "'Poppins', sans-serif"
              }
            }
          },
          title: {
            display: true,
            text: '인기 키워드 분석',
            font: {
              size: 16,
              family: "'Poppins', sans-serif"
            },
            padding: 20,
            align: 'start'
          }
        }
      };

      new Chart(ctx, {
        type: 'doughnut',
        data: chartData,
        options: chartOptions
      });
      chartInitialized = true;
    }


    function renderVideos(videosData) {
      videoGrid.empty();
      if (videosData && videosData.length > 0) {
        videosData.forEach(video => {
          const videoCard = generateVideoCard(video);
          videoGrid.append(videoCard);
        });
        noResultsMessage.hide();
      } else {
        noResultsMessage.show();
      }
    }

    function generateVideoCard(video) {
      const formattedDuration = formatDuration(video.duration);
      const publishedAt = new Date(video.publishedAt);
      const formattedDate = new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      }).format(publishedAt);

      console.log('video.videoId:', video.videoId);

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

      return videoCard;
    }

    function formatDuration(duration) {
      const regex = /^PT(\d+H)?(\d+M)?(\d+S)?$/;
      const matches = duration.match(regex);

      if (!matches) {
        return duration;
      }

      let hours = 0;
      let minutes = 0;
      let seconds = 0;
      let formattedDuration = "";

      if (matches[1]) {
        hours = parseInt(matches[1].replace('H', ''), 10);
      }
      if (matches[2]) {
        minutes = parseInt(matches[2].replace('M', ''), 10);
      }
      if (matches[3]) {
        seconds = parseInt(matches[3].replace('S', ''), 10);
      }

      if (hours > 0) {
        formattedDuration += hours + "시간 ";
      }
      if (minutes > 0) {
        formattedDuration += minutes + "분 ";
      }
      if (seconds > 0 || (hours === 0 && minutes === 0)) {
        formattedDuration += seconds + "초";
      }

      return formattedDuration.trim();
    }

    let currentPage = 1;  // 현재 페이지
    const pageSize = 10;  // 한 페이지당 비디오 수 (기본값 10)


    function renderPagination(totalPages) {
      const paginationContainer = $('#pagination');
      paginationContainer.empty();


      if (currentPage > 1) {
        paginationContainer.append(
                $('<button>').addClass('page-btn').text('이전').click(function() {
                  currentPage--;
                  performSearch();
                })
        );
      }

      for (let i = 1; i <= totalPages; i++) {
        paginationContainer.append(
                $('<button>')
                        .addClass('page-btn')
                        .text(i)
                        .toggleClass('active', i === currentPage)
                        .click(function() {
                          currentPage = i;
                          performSearch();
                        })
        );
      }

      if (currentPage < totalPages) {
        paginationContainer.append(
                $('<button>').addClass('page-btn').text('다음').click(function() {
                  currentPage++;
                  performSearch();
                })
        );
      }
    }

    function performSearch() {
      const searchTerm = searchInput.val().trim();

      if (searchTerm) {
        $.ajax({
          url: '/searchVideos',
          type: 'GET',
          data: { query: searchTerm,
                  page: currentPage,
                  pageSize: pageSize
          },
          success: function (data) {
            console.log(data);
            renderVideos(data.videos);
            renderPagination(data.totalPages);
          },
          error: function (error) {
            console.error('Error fetching search results:', error);
            noResultsMessage.show();
          }
        });
      } else {
        renderVideos(videos);
        noResultsMessage.hide();
      }
    }

    renderKeywordTags();
    initializeTrendingKeywordsChart();
    renderVideos(videos);

    searchButton.on('click', function (event) {
      event.preventDefault();
      performSearch();
    });


    searchInput.on('keypress', function (e) {
      if (e.key === 'Enter') {
        e.preventDefault();
        performSearch();
      }
    });

    $(document).on('click', '.video-card', function () {
      const videoId = $(this).data('videoid');
      console.log('Opening modal for videoId:', videoId);
      if (videoId) {
        openModal(videoId);
      } else {
        console.error('Error: videoId is undefined');
      }
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

    function openModal(videoId) {
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

// 모달 창을 닫는 함수
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
  });

</script>
</body>
</html>

