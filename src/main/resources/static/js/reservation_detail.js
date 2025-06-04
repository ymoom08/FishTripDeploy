document.addEventListener("DOMContentLoaded", () => {
  const container = document.body;

  // ✅ 요일 글자에만 flatpickr 스타일 클래스 적용
  document.querySelectorAll(".date-text").forEach(el => {
    const original = el.textContent; // 예: "2025-06-28(토)"
    const match = original.match(/(.+)\((.)\)/); // 괄호 안 요일 문자 추출

    if (!match) return;

    const [_, datePart, dayChar] = match;

    let className = "";
    if (dayChar === "토") className = "flatpickr-day saturday";
    else if (dayChar === "일") className = "flatpickr-day sunday";

    // 날짜(2025-06-28) + ( + <span class="...">토</span> + )
    if (className) {
      el.innerHTML = `${datePart}(<span class="${className}">${dayChar}</span>)`;
    } else {
      el.innerHTML = original; // 평일이면 그대로 출력
    }
  });

  // ✅ 예약 버튼 처리 (기존 유지)
  container.addEventListener("click", async (e) => {
    const button = e.target.closest(".reserve-button");
    if (!button) return;

    if (button.disabled) return;
    button.disabled = true;

    try {
      const postId = parseInt(button.dataset.postId);
      const availableDate = button.dataset.date;
      const userId = parseInt(window.currentUserId);
      const count = 1;
      const paid = false;

      if (!postId || !availableDate || !userId || isNaN(userId)) {
        alert("유효하지 않은 예약 정보입니다.");
        return;
      }

      const body = { reservationPostId: postId, userId, availableDate, count, paid };

      const res = await fetch("/api/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        const result = await res.json();
        alert("예약 성공! 예약 ID: " + result);
        // window.location.href = `/payment/${result}`;
      } else {
        const error = await res.text();
        alert("예약 실패\n" + (error || "서버 오류"));
      }
    } catch (err) {
      console.error("예약 요청 오류:", err);
      alert("요청 중 오류 발생");
    } finally {
      button.disabled = false;
    }
  });
});
