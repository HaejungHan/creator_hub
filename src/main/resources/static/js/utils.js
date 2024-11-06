function parseDuration(duration) {
    const regex = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/;
    const matches = duration.match(regex);

    const hours = matches[1] ? parseInt(matches[1]) : 0;
    const minutes = matches[2] ? parseInt(matches[2]) : 0;
    const seconds = matches[3] ? parseInt(matches[3]) : 0;

    const parts = [];
    if (hours) parts.push(`${hours}시간`);
    if (minutes) parts.push(`${minutes}분`);
    if (seconds) parts.push(`${seconds}초`);

    return parts.join(' ') || '0초';
}

function formatPublishedDate(publishedAt) {
    const publishDate = new Date(publishedAt);
    const now = new Date();
    const timeDiff = now - publishDate;
    const daysDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24));

    if (daysDiff < 1) {
        return "오늘";
    } else if (daysDiff === 1) {
        return "어제";
    } else {
        return `${daysDiff}일 전`;
    }
}
