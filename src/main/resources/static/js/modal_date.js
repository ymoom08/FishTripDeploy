// ✅ modal_date.js
import { selectedDate } from "./reservation_list.js";
import { closeModal } from "./reservation_list.js";
import { fetchFilteredCards, updateSelectedDateTextOnly } from "./reservation_list.js";

/**
 * ✅ 날짜 모달 초기화
 */
export function initDateModal() {
  const dateBtn = document.getElementById("dateBtn");
  const dateModal = document.getElementById("dateModal");
  const dateApply = document.getElementById("dateApply");
  const dateCancel = document.getElementById("dateCancel");
  const dateReset = document.getElementById("dateReset");

  // 🔘 버튼 클릭 시 모달 열기
  dateBtn?.addEventListener("click", () => {
    dateModal.classList.remove("hidden");
    dateModal.classList.add("show");
  });

  // 🔘 날짜 적용 버튼
  dateApply?.addEventListener("click", () => {
    closeModal(dateModal);
    updateSelectedDateTextOnly();
    fetchFilteredCards();
  });

  // 🔘 닫기 버튼
  dateCancel?.addEventListener("click", () => {
    closeModal(dateModal);
  });

  // 🔘 초기화 버튼
  dateReset?.addEventListener("click", () => {
    selectedDate.value = null;
    updateSelectedDateTextOnly();
  });

  // ✅ 달력 위젯 초기화
  flatpickr.localize(flatpickr.l10ns.ko); // 한국어 설정
  flatpickr("#datePickerContainer", {
    dateFormat: "Y-m-d",
    inline: true,
    locale: "ko",
    onDayCreate: function(dObj, dStr, fp, dayElem) {
      const date = dayElem.dateObj;
      const day = date.getDay(); // 0: 일요일, 6: 토요일

      if (day === 0) {
        dayElem.classList.add("sunday");
      } else if (day === 6) {
        dayElem.classList.add("saturday");
      }
    },
    onChange: (selectedDates, dateStr) => {
      selectedDate.value = dateStr;
    },
    appendTo: document.getElementById("datePickerContainer")
  });
}


