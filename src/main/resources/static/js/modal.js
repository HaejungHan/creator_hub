function showModal(modal) {
    modal.show();
    $('body').css('overflow', 'hidden');
}

function closeModal(modal) {
    modal.hide();
    $('body').css('overflow', 'auto');
}

$(document).on('click', '.video-modal-close', function () {
    stopVideo();
    $('#videoPlayer').attr('src', '');  // iframe src 비워서 비디오 종료
    closeModal($('#videoModal'));
});
