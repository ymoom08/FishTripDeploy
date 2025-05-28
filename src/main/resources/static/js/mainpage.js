// 페이지 이동 버튼 처리
function navigatePage(url) {
  window.location.href = url;
}

document.addEventListener("DOMContentLoaded", function () {
  lottie.loadAnimation({
    container: document.getElementById('lottiePopularPosts'),
    renderer: 'svg',
    loop: true,
    autoplay: true,
    path: '/json/mascot.json'  // ← 반드시 이 경로로 수정
  });
});