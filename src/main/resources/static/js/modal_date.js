import {
  ModalState,
  injectHiddenInputs,
  openModal,
  closeModal,
  bindModalOutsideClick,
  getRequiredElements
} from "./modal_common.js";

/**
 * ✅ 날짜 모달 초기화
 * @param {Object} options - 설정 객체
 * @param {Function} options.onApply - 날짜 적용 시 실행할 외부 콜백 함수
 */
export function initDateModal({ onApply } = {}) {
  const ids = {
    btn: "dateBtn",
    modal: "dateModal",
    apply: "dateApply",
    cancel: "dateCancel",
    reset: "dateReset",
    picker: "datePickerContainer",
    hiddenInput: "dateContainer"
  };

  const el = getRequiredElements(ids);
  if (!el) return;

  // 🔘 모달 열기
  el.btn.addEventListener("click", () => {
    openModal(el.modal);
  });

  // 🔘 적용
  el.apply.addEventListener("click", () => {
    injectHiddenInputs(ids.hiddenInput, "availableDates", ModalState.getDates());
    closeModal(el.modal);
    onApply?.();
  });

  // 🔘 닫기
  el.cancel.addEventListener("click", () => {
    closeModal(el.modal);
  });

  // 🔘 초기화
  el.reset.addEventListener("click", () => {
    ModalState.setDates([]);
    onApply?.();
  });

  // 🔘 외부 클릭 시 닫기
  bindModalOutsideClick(el.modal);

  // 🔘 달력 초기화
  flatpickr.localize(flatpickr.l10ns.ko);
  flatpickr(`#${ids.picker}`, {
    dateFormat: "Y-m-d",
    inline: true,
    locale: "ko",
    mode: "multiple",
    onDayCreate: (_, __, ___, dayElem) => {
      const day = dayElem.dateObj.getDay();
      if (day === 0) dayElem.classList.add("sunday");
      else if (day === 6) dayElem.classList.add("saturday");
    },
    onChange: (selectedDates) => {
      const formatted = selectedDates.map(d => {
        const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
        return local.toISOString().split("T")[0];
      });
      ModalState.setDates(formatted);
    }
  });
}

/**
 * ✅ 조건부 초기화
 */
export function initDateModalIfExist({ onApply } = {}) {
  const requiredIds = ["dateBtn", "dateModal", "dateApply", "dateCancel", "dateReset", "datePickerContainer"];
  const allExist = requiredIds.every(id => document.getElementById(id));
  if (allExist) {
    initDateModal({ onApply });
  } else {
    console.warn("⚠️ [initDateModalIfExist] 일부 요소가 없어서 초기화 생략됨");
  }
}
