let player;
let videoStartTime = 0;
let videoTimer;
let videoId;

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

function onPlayerReady(event) {
    console.log("Player ready!");
    videoId = event.target.getVideoData().video_id;  // 현재 비디오 ID를 가져옵니다.
}

function onPlayerStateChange(event) {
    if (event.data === YT.PlayerState.PLAYING) {
        startVideoTimer(videoId);
    } else if (event.data === YT.PlayerState.PAUSED || event.data === YT.PlayerState.ENDED) {
        sendPlaybackTimeToServer(videoId, getCurrentPlaybackTime());
        stopVideoTimer();
    }
}

function startVideoTimer(videoId) {
    videoStartTime = Date.now();
    videoTimer = setInterval(function () {
        const currentTime = getCurrentPlaybackTime();
        sendPlaybackTimeToServer(videoId, currentTime);
    }, 1000);
}

function getCurrentPlaybackTime() {
    return Math.floor((Date.now() - videoStartTime) / 1000);
}

function stopVideoTimer() {
    if (videoTimer) {
        clearInterval(videoTimer);
        videoTimer = null;
    }
}

function sendPlaybackTimeToServer(videoId, currentTime) {
    const token = Cookies.get('Authorization');
    if (!token) {
        console.error('No JWT token found.');
        return;
    }

    const jwtToken = token.split(' ')[1];
    $.ajax({
        url: '/api/save-playback-time',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ videoId, playbackTime: currentTime }),
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        },
        success: function () {
            console.log('Playback time saved successfully');
        },
        error: function (err) {
            console.error('Error saving playback time:', err);
        }
    });
}

function loadVideoModal(id) {
    $.ajax({
        url: `/api/video/${id}`,
        method: 'GET',
        success: function (video) {
            $('#modalVideoTitle').text(video.title);
            $('#modalVideoViews').text(`${video.viewCount} views`);
            $('#modalVideoDate').text(formatPublishedDate(video.publishedAt));

            const youtubeEmbedUrl = `https://www.youtube.com/embed/${id}`;
            $('#videoPlayer').attr('src', youtubeEmbedUrl);

            showModal($('#videoModal'));

            $('#playButton').on('click', function () {
                if (!player) {
                    player = new YT.Player('videoPlayer', {
                        events: {
                            'onReady': function (event) {
                                player.playVideo();
                                startVideoTimer(id);
                            },
                            'onStateChange': function (event) {
                                onPlayerStateChange(event);
                            }
                        }
                    });
                } else {
                    player.playVideo();
                    startVideoTimer(id);
                }
                $(this).hide();
            });
        },
        error: function (err) {
            console.error('Error loading video modal:', err);
        }
    });
}
