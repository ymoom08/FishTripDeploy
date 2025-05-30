import {
  ModalState,
  openModal,
  closeModal,
  bindModalOutsideClick,
  getRequiredElements
} from "./modal_common.js";

/**
 * ✅ 날짜 모달 초기화 (flatpickr + 시간 + 정원 - form 모드만 적용)
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
  const isFormMode = container.dataset.formMode === "true";

  const tempInput = document.createElement("input");
  tempInput.type = "text";
  tempInput.style.display = "none";
  pickerContainer.appendChild(tempInput);

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
      const prevDates = ModalState.getDates();
      const updated = selectedDates.map(d => {
        const dateStr = new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split("T")[0];
        const existing = prevDates.find(p => p.date === dateStr);
        return existing || { date: dateStr, start: "", end: "", capacity: 1 };
      });
      ModalState.setDates(updated);
      renderDateEntries(updated, container, isFormMode);
    }
  });

  el.btn.addEventListener("click", () => {
    openModal(el.modal);
    fp.open();
  });

  el.apply.addEventListener("click", () => {
    if (isFormMode) {
      updateModalStateFromInputs(container);
    }
    closeModal(el.modal);
    onApply?.();
  });

  el.cancel.addEventListener("click", () => {
    closeModal(el.modal);
  });

  el.reset.addEventListener("click", () => {
    ModalState.setDates([]);
    fp.clear();
    container.innerHTML = "";
    onApply?.();
  });

  if (isFormMode) {
    container.addEventListener("click", e => {
      const btn = e.target.closest(".remove-date");
      if (!btn) return;

      const dateToRemove = btn.dataset.date;
      const updated = ModalState.getDates().filter(d => d.date !== dateToRemove);
      ModalState.setDates(updated);
      fp.setDate(updated.map(d => d.date), true);
      renderDateEntries(updated, container, isFormMode);
    });
  }

  bindModalOutsideClick(el.modal);
}


/**
 * ✅ 날짜 항목 렌더링 (모드에 따라 다르게 구성)
 */
function renderDateEntries(dateEntries, container, isFormMode) {
  if (!container) return;
  container.innerHTML = "";

  if (!isFormMode) return; // 필터 모드에서는 렌더링하지 않음

  dateEntries.forEach((entry, idx) => {
    const wrapper = document.createElement("div");
    wrapper.className = "date-entry";

    wrapper.innerHTML = `
      <div class="date-label">${entry.date}</div>
      <input type="text" class="timepicker start" data-index="${idx}" placeholder="시작 시간" value="${entry.start}" required>
      <input type="text" class="timepicker end" data-index="${idx}" placeholder="종료 시간" value="${entry.end}" required>
      <input type="number" class="capacity" data-index="${idx}" placeholder="정원" value="${entry.capacity}" min="1" required>
      <button type="button" class="remove-date" data-date="${entry.date}">&times;</button>
    `;

    container.appendChild(wrapper);
  });

  container.querySelectorAll(".timepicker").forEach(el => {
    flatpickr(el, {
      enableTime: true,
      noCalendar: true,
      dateFormat: "H:i",
      time_24hr: true,
      locale: 'ko'
    });
  });
}


/**
 * ✅ form 모드일 때 입력값 -> 상태로 반영
 */
function updateModalStateFromInputs(container) {
  const entries = Array.from(container.querySelectorAll(".date-entry"));
  const updated = entries.map(entry => {
    const date = entry.querySelector(".date-label").textContent;
    const start = entry.querySelector(".timepicker.start")?.value || "";
    const end = entry.querySelector(".timepicker.end")?.value || "";
    const capacity = Number(entry.querySelector(".capacity")?.value || 1);
    return { date, start, end, capacity };
  });
  ModalState.setDates(updated);
}


/**
 * ✅ 존재하는 경우에만 초기화
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
