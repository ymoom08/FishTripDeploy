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

  // 🔘 임시 input 요소 생성 (flatpickr가 직접 타겟으로 사용)
  const tempInput = document.createElement("input");
  tempInput.type = "text";
  tempInput.style.display = "none";
  pickerContainer.appendChild(tempInput);

  // 🔘 달력 인스턴스 생성
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
 * - form.html → 입력 필드 모드
 * - list.html → 단순 라벨 모드
 */
function renderDateEntries(dates, container) {
  if (!container) return;

  const isFormMode = container.dataset.formMode?.toLowerCase() === "true";
  container.innerHTML = "";

  dates.forEach((date, idx) => {
    const wrapper = document.createElement("div");
    wrapper.className = "date-entry";

    if (isFormMode) {
      wrapper.innerHTML = `
        <div class="date-label">${date}</div>
        <input type="hidden" name="availableDates[${idx}].date" value="${date}">
        <input type="text" name="availableDates[${idx}].time" placeholder="예: 06:00~14:00"
               pattern="^\\d{2}:\\d{2}~\\d{2}:\\d{2}$" required>
        <input type="number" name="availableDates[${idx}].capacity" placeholder="정원" min="1" required>
        <button type="button" class="remove-date" data-date="${date}">❌</button>
      `;
    } else {
      wrapper.innerHTML = `
        <div class="date-label">
          ${date}
          <button type="button" class="remove-date" data-date="${date}">❌</button>
        </div>
      `;
    }

    container.appendChild(wrapper);
  });
}

/**
 * ✅ 조건부 초기화 (존재하는 경우만 적용)
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
  } else {
    console.warn("⚠️ [initDateModalIfExist] 일부 요소가 없어 초기화 생략됨");
  }
}
