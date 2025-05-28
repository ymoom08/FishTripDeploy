// reservation_form.js

import {
  ModalState,
  injectHiddenInputs,
  bindModalOutsideClick
} from "./modal_common.js";

import { getCachedRegions, setCachedRegions } from "./modal_region.js";
import { initRegionModalIfExist } from "./modal_region.js";
import { initFishModalIfExist } from "./modal_fish.js";
import { initDateModalIfExist } from "./modal_date.js";

// ✅ [1] 지역 데이터 초기화
fetch("/api/regions/hierarchy")
  .then(res => res.json())
  .then(setCachedRegions)
  .catch(err => console.error("지역 데이터 초기화 실패:", err));

// ✅ [2] 페이지 로드 시 모달 및 달력 초기화
window.addEventListener("DOMContentLoaded", () => {
  initAllModals();

  const isFormPage = location.pathname.includes("/reservation/form");
  if (isFormPage) {
    setupFlatpickrEntry();
  }
});

// ✅ [3] 모든 모달 초기화
function initAllModals() {
  initRegionModalIfExist({ onApply: updateRegionLabel });
  initFishModalIfExist({ onApply: updateFishLabel });
  initDateModalIfExist({ onApply: updateDateLabel });

  ["regionModal", "dateModal", "fishModal"].forEach(id => {
    const modal = document.getElementById(id);
    if (modal) bindModalOutsideClick(modal);
  });
}

// ✅ [4] 지역 UI 갱신
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

  const regionTextEl = document.getElementById("selectedRegionText");
  if (regionTextEl) regionTextEl.textContent = text || "선택된 지역 없음";

  const modalLabel = document.querySelector("#regionModal .current-selection");
  if (modalLabel) modalLabel.textContent = text || "선택된 지역 없음";

  const ids = selected.map(r => r.id);
  injectHiddenInputs("regionIdsInput", "regionIds", ids);
}

// ✅ [5] 어종 UI 갱신
function updateFishLabel() {
  const selected = ModalState.getFishTypes();
  const text = selected.length > 0 ? selected.join(", ") : "선택된 어종 없음";

  const fishTextEl = document.getElementById("selectedFishText");
  if (fishTextEl) fishTextEl.textContent = text;

  const modalLabel = document.querySelector("#fishModal .current-selection");
  if (modalLabel) modalLabel.textContent = text;

  injectHiddenInputs("fishTypeInputGroup", "fishTypeNames", selected);
}

// ✅ [6] 날짜 UI 갱신
function updateDateLabel() {
  const selected = ModalState.getDates();
  console.log("선택된 날짜:", selected);
}

// ✅ [7] Flatpickr 날짜 선택 처리
function setupFlatpickrEntry() {
  const container = document.getElementById("dateContainer");
  const pickerEl = document.getElementById("datePicker");

  if (!pickerEl || !container) return;

  const isFormMode = container.dataset.formMode === "true";

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

    onChange(selectedDates) {
      const formattedDates = selectedDates.map(d => {
        const local = new Date(d.getTime() + 9 * 60 * 60000);
        return local.toISOString().split("T")[0];
      });

      ModalState.setDates(formattedDates);
      container.innerHTML = "";

      formattedDates.forEach((date, idx) => {
        const div = document.createElement("div");
        div.className = "date-entry";

        if (isFormMode) {
          div.innerHTML = `
            <label>${date}</label>
            <input type="hidden" name="availableDates[${idx}].date" value="${date}">
            <input type="text" name="availableDates[${idx}].time" placeholder="예: 06:00~14:00" pattern="^\\d{2}:\\d{2}~\\d{2}:\\d{2}$" required>
            <input type="number" name="availableDates[${idx}].capacity" placeholder="정원" min="1" required>
            <button type="button" class="remove-date" data-date="${date}">❌</button>
          `;
        } else {
          div.innerHTML = `<span class="date-label">${date}</span>`;
        }

        container.appendChild(div);
      });
    }
  });

  container.addEventListener("click", (e) => {
    const btn = e.target.closest(".remove-date");
    if (!btn) return;

    const dateToRemove = btn.dataset.date;
    const updated = ModalState.getDates().filter(date => date !== dateToRemove);
    ModalState.setDates(updated);
    fp.setDate(updated, true);
  });
}
