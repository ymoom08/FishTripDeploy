// ✅ MainPage 전용 스크립트

function selectRegion(regionName) {
    alert(`선택된 해역: ${regionName}`);
}

// ✅ 비디오 슬라이더
document.addEventListener('DOMContentLoaded', () => {
    const videos = document.querySelectorAll('.videoSlider .video');
    let current = 0;

    function showVideo(index) {
        videos.forEach((video, i) => {
            video.classList.remove('active');
            if (i === index) {
                video.classList.add('active');
            }
        });
    }

    if (videos.length > 0) {
        showVideo(current);
        setInterval(() => {
            current = (current + 1) % videos.length;
            showVideo(current);
        }, 4000);
    }
});
