import {
  ModalState,
  openModal,
  closeModal,
  bindModalOutsideClick,
  getRequiredElements
} from "./modal_common.js";

/**
 * ✅ 날짜 모달 초기화
 */
export function initDateModal({ onApply } = {}) {
  const ids = {
    btn: "dateBtn",
    modal: "dateModal",
    apply: "dateApply",
    cancel: "dateCancel",
    reset: "dateReset",
    hiddenInput: "dateContainer",
    container: "datePickerContainer"
  };

  const el = getRequiredElements(ids);
  if (!el) return;

  const container = document.getElementById(ids.hiddenInput);
  const pickerContainer = document.getElementById(ids.container);

  // 🔘 flatpickr용 임시 input 생성
  const tempInput = document.createElement("input");
  tempInput.type = "text";
  tempInput.style.display = "none";
  pickerContainer.appendChild(tempInput);

  // 🔘 flatpickr 인스턴스 생성
  flatpickr.localize(flatpickr.l10ns.ko);
  const fp = flatpickr(tempInput, {
    dateFormat: "Y-m-d",
    locale: "ko",
    mode: "multiple",
    clickOpens: false,
    inline: true,
    appendTo: pickerContainer,

    onDayCreate(_, __, ___, dayElem) {
      const day = dayElem.dateObj.getDay();
      if (day === 0) dayElem.classList.add("sunday");
      else if (day === 6) dayElem.classList.add("saturday");
    },

    onChange(selectedDates) {
      const formatted = selectedDates.map(d => {
        const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
        return local.toISOString().split("T")[0];
      });

      ModalState.setDates(formatted);
      renderDateEntries(formatted, container);
    }
  });

  // 🔘 모달 열기
  el.btn.addEventListener("click", () => {
    openModal(el.modal);
    fp.open();
  });

  // 🔘 적용
  el.apply.addEventListener("click", () => {
    closeModal(el.modal);
    onApply?.();
  });

  // 🔘 취소
  el.cancel.addEventListener("click", () => {
    closeModal(el.modal);
  });

  // 🔘 초기화
  el.reset.addEventListener("click", () => {
    ModalState.setDates([]);
    fp.clear();
    container.innerHTML = "";
    onApply?.();
  });

  // 🔘 날짜 개별 삭제
  container.addEventListener("click", e => {
    const btn = e.target.closest(".remove-date");
    if (!btn) return;

    const dateToRemove = btn.dataset.date;
    const updated = ModalState.getDates().filter(d => d !== dateToRemove);
    ModalState.setDates(updated);
    fp.setDate(updated, true);
    renderDateEntries(updated, container);
  });

  bindModalOutsideClick(el.modal);
}

/**
 * ✅ 날짜 입력 필드 렌더링
 * - form.html → 날짜 입력 필드들 렌더링
 * - list.html → 출력 생략 (외부에서 렌더링)
 */
function renderDateEntries(dates, container) {
  if (!container) return;

  const isFormMode = container.dataset.formMode?.toLowerCase() === "true";
  container.innerHTML = "";

  if (isFormMode) {
    dates.forEach((date, idx) => {
      const wrapper = document.createElement("div");
      wrapper.className = "date-entry";

      wrapper.innerHTML = `
        <div class="date-label">${date}</div>
        <input type="hidden" name="availableDates[${idx}].date" value="${date}">
        <input type="text" name="availableDates[${idx}].time" placeholder="예: 06:00~14:00"
               pattern="^\\d{2}:\\d{2}~\\d{2}:\\d{2}$" required>
        <input type="number" name="availableDates[${idx}].capacity" placeholder="정원" min="1" required>
        <button type="button" class="remove-date" data-date="${date}">❌</button>
      `;

      container.appendChild(wrapper);
    });
  }

  // ❌ list 모드는 외부 템플릿에서 처리하므로 아무 출력도 하지 않음
}

/**
 * ✅ 조건부 초기화 (요소가 존재하는 경우에만)
 */
export function initDateModalIfExist({ onApply } = {}) {
  const requiredIds = [
    "dateBtn",
    "dateModal",
    "dateApply",
    "dateCancel",
    "dateReset",
    "dateContainer",
    "datePickerContainer"
  ];

  const allExist = requiredIds.every(id => document.getElementById(id));
  if (allExist) {
    initDateModal({ onApply });
  }
}
