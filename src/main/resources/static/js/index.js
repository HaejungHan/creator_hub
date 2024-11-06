// $(document).ready(function () {
//     const searchInput = $('#searchInput');
//     const searchButton = $('#searchButton');
//     const searchResultsGrid = $('#searchResultsGrid');
//     const loginBtn = $('#loginBtn');
//     const loginModal = $('#loginModal');
//     const closeButtons = $('.close-modal');
//     const loginForm = $('#loginForm');
//     const popularVideosGrid = $('#popularVideos');
//     const videoModal = $('#videoModal'); // 모달
//     const videoPlayer = $('#videoPlayer'); // 비디오 플레이어 iframe
//     let player;  // 유튜브 플레이어 객체
//     let videoId; // 전역 videoId 변수 (onReady에서 가져온 videoId 저장)
//     let videoStartTime = 0;  // 비디오 시작 시간을 저장할 변수
//     let videoTimer;
//
//     function onYouTubeIframeAPIReady() {
//         const playerElement = document.getElementById('videoPlayer');
//         if (playerElement) {
//             player = new YT.Player('videoPlayer', {
//                 events: {
//                     'onReady': onPlayerReady,
//                     'onStateChange': onPlayerStateChange
//                 }
//             });
//         } else {
//             console.error('Player element not found!');
//         }
//     }
//
//     // 플레이어 준비 완료 후 실행
//     function onPlayerReady(event) {
//         console.log("Player ready!");
//         videoId = event.target.getVideoData().video_id;  // 현재 비디오 ID를 가져옵니다.
//         // 이곳에서 타이머를 바로 시작하는 대신, 'onStateChange'에서 타이머 시작
//     }
//
//     // 플레이어 상태 변화 시
//     function onPlayerStateChange(event) {
//         // 비디오 재생 시작
//         if (event.data === YT.PlayerState.PLAYING) {
//             console.log("Video is playing");
//             startVideoTimer(videoId);  // 타이머 시작
//         }
//
//         // 비디오가 멈췄을 때 (일시 정지)
//         else if (event.data === YT.PlayerState.PAUSED) {
//             console.log("Video is paused");
//             sendPlaybackTimeToServer(videoId, getCurrentPlaybackTime());  // 서버로 시간 전송
//             stopVideoTimer();  // 타이머 멈추기
//         }
//
//         // 비디오가 끝났을 때
//         else if (event.data === YT.PlayerState.ENDED) {
//             console.log("Video has ended");
//             sendPlaybackTimeToServer(videoId, getCurrentPlaybackTime());  // 서버로 시간 전송
//             stopVideoTimer();  // 타이머 멈추기
//         }
//     }
//
//
//     function startVideoTimer(videoId) {
//         videoStartTime = Date.now();  // 비디오 시작 시간 기록
//
//         videoTimer = setInterval(function () {
//             const currentTime = getCurrentPlaybackTime();
//             console.log(`Video playback time: ${currentTime} seconds`);
//
//             // 서버에 재생 시간 전송
//             sendPlaybackTimeToServer(videoId, currentTime);
//         }, 1000); // 1초마다 실행
//     }
//
// // 현재까지의 재생 시간 계산
//     function getCurrentPlaybackTime() {
//         return Math.floor((Date.now() - videoStartTime) / 1000);  // 초 단위로 계산
//     }
//
// // 타이머 멈추기
//     function stopVideoTimer() {
//         if (videoTimer) {
//             clearInterval(videoTimer);  // 타이머 멈추기
//             videoTimer = null;  // 타이머 리셋
//         }
//     }
//
// // 서버에 재생 시간 전송
//     function sendPlaybackTimeToServer(videoId, currentTime) {
//         const token = Cookies.get('Authorization');  // 'Bearer <jwt_token>' 형태
//
//         if (!token) {
//             console.error('No JWT token found.');
//             return;
//         }
//
//         // 'Bearer ' 부분을 제외한 실제 JWT 토큰만 추출
//         const jwtToken = token.split(' ')[1];
//
//         $.ajax({
//             url: '/api/save-playback-time',
//             method: 'POST',
//             contentType: 'application/json',
//             data: JSON.stringify({
//                 videoId: videoId,
//                 playbackTime: currentTime
//             }),
//             headers: {
//                 'Authorization': `Bearer ${jwtToken}`  // Authorization 헤더에 JWT 토큰 포함
//             },
//             success: function(response) {
//                 console.log('Playback time saved successfully');
//             },
//             error: function(err) {
//                 console.error('Error saving playback time:', err);
//             }
//         });
//     }
//
//     // 동영상 모달 로드
//     function loadVideoModal(id) {
//         $.ajax({
//             url: `/api/video/${id}`, // 백엔드에서 비디오 데이터 가져오기
//             method: 'GET',
//             success: function (video) {
//                 // 비디오 정보 업데이트
//                 $('#modalVideoTitle').text(video.title);
//                 $('#modalVideoViews').text(`${video.viewCount} views`);
//                 $('#modalVideoDate').text(formatPublishedDate(video.publishedAt));
//
//                 // iframe src에 유튜브 Embed URL 설정
//                 const youtubeEmbedUrl = `https://www.youtube.com/embed/${id}`;
//                 $('#videoPlayer').attr('src', youtubeEmbedUrl);
//
//                 // 모달 열기
//                 showModal($('#videoModal'));
//
//                 $('#playButton').on('click', function () {
//                     if (!player) {
//                         // 플레이어 객체가 없으면 새로 생성
//                         player = new YT.Player('videoPlayer', {
//                             events: {
//                                 'onReady': function (event) {
//                                     player.playVideo();
//                                     startVideoTimer(id); // 타이머 시작
//                                 },
//                                 'onStateChange': function (event) {
//                                     onPlayerStateChange(event); // 플레이어 상태 변경 처리
//                                 }
//                             }
//                         });
//                     } else {
//                         // 기존 플레이어가 있으면 바로 재생
//                         player.playVideo();
//                         startVideoTimer(id); // 타이머 시작
//                     }
//
//                     $(this).hide(); // 플레이 버튼 숨기기
//                 });
//             },
//             error: function (err) {
//                 console.error('Error loading video modal:', err);
//             }
//         });
//     }
//
//     // 모달 닫기 시 타이머와 비디오 종료
//     $(document).on('click', '.video-modal-close', function () {
//         stopVideo();
//         $('#videoPlayer').attr('src', '');  // iframe src 비워서 비디오 종료
//         closeModal($('#videoModal'));
//     });
//
//     // 비디오 중지 함수
//     function stopVideo() {
//         if (player) {
//             player.stopVideo();
//             console.log("Video stopped");
//         }
//     }
//
//     loginBtn.on('click', () => {
//         if (loginBtn.text().trim() === "로그인") {
//             showModal(loginModal);  // 로그인 모달 열기
//         } else if (loginBtn.text().trim() === "로그아웃") {
//             handleLogout(); // 로그아웃 처리
//         }
//     });
//
//     // 모달 열기/닫기
//     function showModal(modal) {
//         modal.show();
//         $('body').css('overflow', 'hidden');
//     }
//
//     function closeModal(modal) {
//         modal.hide();
//         $('body').css('overflow', 'auto');
//     }
//
//     // 로그인 버튼 클릭 시 모달 열기
//     loginBtn.on('click', () => showModal(loginModal));
//
//     // 모달 닫기 버튼 클릭 시
//     closeButtons.on('click', function () {
//         const modalId = $(this).data('modal');
//         closeModal($('#' + modalId));
//     });
//
//     // 화면 클릭 시 모달 닫기 (모달 외 영역 클릭 시)
//     $(window).on('click', function (e) {
//         if ($(e.target).hasClass('modal')) {
//             closeModal($(e.target));
//         }
//     });
//
// // 로그인 폼 제출
//     $('#loginForm').submit(function (e) {
//         e.preventDefault();  // 기본 폼 제출을 막고, AJAX 요청을 처리하도록 합니다.
//
//         const email = $('#loginEmail').val().trim();
//         const password = $('#loginPassword').val().trim();
//
//         if (!email || !password) {
//             $('#error-message').text('이메일과 비밀번호를 입력해주세요.');
//             $('#error-message').show();
//             return;
//         }
//
//         const data = {
//             username: email,  // 이메일을 'username'으로 사용
//             password: password
//         };
//
//
//         $.ajax({
//             type: 'POST',
//             url: '/login',  // 로그인 API URL
//             contentType: 'application/json',
//             data: JSON.stringify(data),
//             success: function (response, textStatus, xhr) {
//
//                 // Authorization 헤더에서 토큰을 추출
//                 var accessToken = xhr.getResponseHeader("Authorization");
//                 var refreshToken = xhr.getResponseHeader("RefreshToken");
//
//                 // Authorization 토큰을 쿠키에 저장
//                 if (accessToken) {
//                     accessToken = accessToken.split(' ')[1]; // Bearer 접두어 제거
//                     Cookies.set('Authorization', accessToken, {
//                         path: '/',
//                         sameSite: 'Lax',  // Lax로 설정 (로컬 환경에서 동작)
//                         secure: false     // HTTP 환경에서는 secure를 false로 설정
//                     });
//                 }
//
//                 // RefreshToken을 쿠키에 저장
//                 if (refreshToken) {
//                     refreshToken = refreshToken.split(' ')[1]; // Bearer 접두어 제거
//                     Cookies.set('RefreshToken', refreshToken, {
//                         path: '/',
//                         sameSite: 'Lax',  // Lax로 설정
//                         secure: false     // HTTP 환경에서는 secure를 false로 설정
//                     });
//                 }
//                 alert('로그인이 완료되었습니다.');
//                 window.location.href = '/main';
//                 setTimeout(function () {
//                     $('#loginBtn').html('<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>');  // 버튼 텍스트를 "로그아웃"으로 변경
//                     $('#loginBtn').attr('id', 'logoutBtn');  // 버튼 ID를 "logoutBtn"으로 변경
//                 }, 100); // DOM이 렌더링된 후에 버튼 텍스트 및 ID를 변경
//
//             },
//             error: function (jqXHR, textStatus, errorThrown) {
//                 console.error('Login failed', errorThrown);
//                 $('#error-message').text('Invalid email or password. Please try again.');
//                 $('#error-message').show();
//                 alert('로그인 실패');
//             }
//         });
//     });
//
//     // 로그아웃 처리
//     function handleLogout() {
//         // 쿠키에서 토큰 삭제
//         Cookies.remove('Authorization');
//         Cookies.remove('RefreshToken');
//
//         // 로그아웃 후 버튼 텍스트를 "로그인"으로 변경
//         $('#logoutBtn').html('<i class="fas fa-sign-in-alt"></i><span>로그인</span>'); // 버튼 텍스트 변경
//         $('#logoutBtn').attr('id', 'loginBtn'); // 버튼 ID 변경
//
//         // 로그아웃 후 페이지 리디렉션 (메인 페이지로 리디렉션)
//         window.location.href = '/main'; // 홈으로 리디렉션
//     }
//
//     // 로그인 상태 체크 및 버튼 초기화
//     function checkLoginStatus() {
//         const token = Cookies.get('Authorization');
//         if (token) {
//             $('#loginBtn').html('<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>');
//             $('#loginBtn').attr('id', 'logoutBtn');
//         }
//     }
//
//     // 페이지 로드 시 로그인 상태 확인
//     checkLoginStatus();
//
//     // 동영상 지속 시간 포맷 함수
//     function parseDuration(duration) {
//         const regex = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/;
//         const matches = duration.match(regex);
//
//         const hours = matches[1] ? parseInt(matches[1]) : 0;
//         const minutes = matches[2] ? parseInt(matches[2]) : 0;
//         const seconds = matches[3] ? parseInt(matches[3]) : 0;
//
//         const parts = [];
//         if (hours) parts.push(`${hours}시간`);
//         if (minutes) parts.push(`${minutes}분`);
//         if (seconds) parts.push(`${seconds}초`);
//
//         return parts.join(' ') || '0초';
//     }
//
//     // 게시 날짜 포맷 함수
//     function formatPublishedDate(publishedAt) {
//         const publishDate = new Date(publishedAt);
//         const now = new Date();
//         const timeDiff = now - publishDate; // 밀리초 차이
//         const daysDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24)); // 일 수로 변환
//
//         if (daysDiff < 1) {
//             return "오늘";
//         } else if (daysDiff === 1) {
//             return "어제";
//         } else {
//             return `${daysDiff}일 전`;
//         }
//     }
//
//     // 인기 동영상 불러오기
//     function loadPopularVideos() {
//         $.ajax({
//             url: '/api/popular', // API endpoint
//             method: 'GET',
//             success: function (videos) {
//                 popularVideosGrid.empty(); // 기존 내용 비우기
//
//                 videos.forEach(video => {
//                     const readableDuration = parseDuration(video.duration); // 변환된 지속 시간
//                     const readablePublishedDate = formatPublishedDate(video.publishedAt); // 변환된 게시 날짜
//                     const videoCard = `
//                     <div class="video-card" data-id="${video.videoId}">
//                         <div class="thumbnail">
//                             <img src="${video.thumbnailUrl.replace('default.jpg', 'hqdefault.jpg')}" alt="${video.title}">
//                             <span class="duration">${readableDuration}</span>
//                         </div>
//                         <div class="video-info">
//                             <h3 class="video-title">${video.title}</h3>
//                             <div class="video-stats">
//                                 <span class="views">
//                                     <i class="fas fa-eye"></i>
//                                     ${video.viewCount} views
//                                 </span>
//                                 <span class="upload-date">
//                                     <i class="far fa-clock"></i>
//                                     ${readablePublishedDate}
//                                 </span>
//                             </div>
//                         </div>
//                     </div>`;
//                     popularVideosGrid.append(videoCard);
//                 });
//             },
//             error: function (err) {
//                 console.error('Error loading popular videos:', err);
//             }
//         });
//     }
//
//     // 검색 기능
//     function performSearch() {
//         const query = searchInput.val().trim();
//         if (query) {
//             $.ajax({
//                 url: '/api/search', // API endpoint
//                 method: 'GET',
//                 data: { query: query },
//                 success: function (videos) {
//                     searchResultsGrid.empty(); // 기존 내용 비우기
//
//                     videos.forEach(video => {
//                         const readableDuration = parseDuration(video.duration); // 변환된 지속 시간
//                         const readablePublishedDate = formatPublishedDate(video.publishedAt); // 변환된 게시 날짜
//                         const videoCard = `
//                         <div class="video-card" data-id="${video.videoId}">
//                             <div class="thumbnail">
//                                 <img src="${video.thumbnailUrl.replace('default.jpg', 'hqdefault.jpg')}" alt="${video.title}">
//                                 <span class="duration">${readableDuration}</span>
//                             </div>
//                             <div class="video-info">
//                                 <h3 class="video-title">${video.title}</h3>
//                                 <div class="video-stats">
//                                     <span class="views">
//                                         <i class="fas fa-eye"></i>
//                                         ${video.viewCount} views
//                                     </span>
//                                     <span class="upload-date">
//                                         <i class="far fa-clock"></i>
//                                         ${readablePublishedDate}
//                                     </span>
//                                 </div>
//                             </div>
//                         </div>`;
//                         searchResultsGrid.append(videoCard);
//                     });
//                 },
//                 error: function (err) {
//                     console.error('Error searching videos:', err);
//                 }
//             });
//         }
//     }
//
//     // 검색 버튼 클릭 시
//     searchButton.on('click', function (e) {
//         e.preventDefault(); // 기본 폼 제출 방지
//         performSearch();
//     });
//
//     // Enter 키로 검색
//     searchInput.on('keypress', function (e) {
//         if (e.key === 'Enter') performSearch();
//     });
//
//     // 메뉴 항목 클릭 시 활성화 효과
//     $('.menu-item').on('click', function () {
//         $('.menu-item').removeClass('active');
//         $(this).addClass('active');
//     });
//
//     // 동영상 카드 클릭 시 모달 열기
//     $(document).on('click', '.video-card', function () {
//         const videoId = $(this).data('id'); // 클릭된 카드의 video id
//         console.log("Clicked video ID:", videoId);
//         loadVideoModal(videoId); // 해당 videoId로 모달을 채워넣음
//     });
//
//     loadPopularVideos();
// });
