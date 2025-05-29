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

// ✅ [2] 페이지 로드 시 모달 초기화만 진행
window.addEventListener("DOMContentLoaded", () => {
  initAllModals();
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
