import {
  ModalState,
  injectHiddenInputs,
  bindModalOutsideClick
} from "./modal_common.js";

import { getCachedRegions } from "./modal_region.js";
import { initRegionModalIfExist } from "./modal_region.js";
import { initFishModalIfExist } from "./modal_fish.js";
import { initDateModalIfExist } from "./modal_date.js";

/**
 * ✅ 문서 로딩 시 전체 초기화
 */
window.addEventListener("DOMContentLoaded", () => {
  initAllModals();
  setupFlatpickrEntry();
});

/**
 * ✅ 전체 모달 초기화
 */
function initAllModals() {
  initRegionModalIfExist(updateRegionLabel);
  initFishModalIfExist(updateFishLabel);
  initDateModalIfExist();

  ["regionModal", "dateModal", "fishModal"].forEach(id => {
    const modal = document.getElementById(id);
    bindModalOutsideClick(modal);
  });
}

/**
 * ✅ 지역 선택 결과 라벨 및 input 갱신
 */
function updateRegionLabel() {
  const selected = ModalState.getRegions();
  const regionHierarchy = getCachedRegions();
  const grouped = {};

  selected.forEach(r => {
    const parent = r.parent || "기타";
    if (!grouped[parent]) grouped[parent] = [];
    grouped[parent].push(r.name);
  });

  const text = Object.entries(grouped).map(([parent, names]) => {
    const region = regionHierarchy?.find(r => r.name === parent);
    const isAll = region?.children.every(child => names.includes(child.name));
    return isAll ? `${parent}(전체)` : names.map(name => `(${parent}) ${name}`).join(", ");
  }).join(", ");

  const label = document.getElementById("selectedRegionText");
  if (label) label.textContent = text || "선택된 지역 없음";

  const ids = selected.map(r => r.id);
  injectHiddenInputs("regionIdsInput", "regionIds", ids);
}

/**
 * ✅ 어종 선택 결과 라벨 및 input 갱신
 */
function updateFishLabel() {
  const selected = ModalState.getFishTypes();
  const label = document.getElementById("selectedFishText");
  if (label) label.textContent = selected.length > 0 ? selected.join(", ") : "선택된 어종 없음";
  injectHiddenInputs("fishTypeInputGroup", "fishTypeNames", selected);
}

/**
 * ✅ 날짜 선택 시 필드 생성
 */
function setupFlatpickrEntry() {
  const container = document.getElementById("dateContainer");
  const pickerEl = document.getElementById("datePicker");
  if (!pickerEl || !container) return;

  const fp = flatpickr(pickerEl, {
    locale: "ko",
    mode: "multiple",
    dateFormat: "Y-m-d",
    position: "auto left top",
    positionElement: pickerEl,

    onDayCreate(_, __, ___, dayElem) {
      const day = dayElem.dateObj.getDay();
      if (day === 0) dayElem.classList.add("sunday");
      else if (day === 6) dayElem.classList.add("saturday");
    },

    onChange(selectedDates, _, instance) {
      // ✅ 상태 저장
      const formattedDates = selectedDates.map(d => {
        const local = new Date(d.getTime() + 9 * 60 * 60000);
        return local.toISOString().split("T")[0];
      });
      ModalState.setDates(formattedDates);

      // ✅ DOM 갱신
      container.innerHTML = "";
      formattedDates.forEach((date, idx) => {
        const div = document.createElement("div");
        div.className = "date-entry";
        div.innerHTML = `
          <label>${date}</label>
          <input type="hidden" name="availableDates[${idx}].date" value="${date}">
          <input type="text" name="availableDates[${idx}].time" placeholder="예: 06:00~14:00" pattern="^\\d{2}:\\d{2}~\\d{2}:\\d{2}$" required>
          <input type="number" name="availableDates[${idx}].capacity" placeholder="정원" min="1" required>
          <button type="button" class="remove-date" data-date="${date}">❌</button>
        `;
        container.appendChild(div);
      });
    }
  });

  // ✅ 삭제 버튼 이벤트 위임
  container.addEventListener("click", (e) => {
    const btn = e.target.closest(".remove-date");
    if (!btn) return;

    const dateToRemove = btn.dataset.date;
    const updated = ModalState.getDates().filter(date => date !== dateToRemove);
    ModalState.setDates(updated);
    fp.setDate(updated, true); // UI 동기화
  });
}
