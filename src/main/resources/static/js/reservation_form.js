import { getSelectedRegions, getSelectedFishTypes, closeModal } from "./modal_state.js";
import { initRegionModalIfExist } from "./modal_region.js";
import { initFishModalIfExist } from "./modal_fish.js";
import { initDateModalIfExist } from "./modal_date.js";
import { getCachedRegions } from "./modal_region.js"; // ✅ 추가: 지역 계층 정보 사용

// ✅ 보기 좋은 지역 텍스트 출력 함수
function getCompactRegionText(selectedRegions, regionHierarchy) {
  const grouped = {};

  selectedRegions.forEach(r => {
    const parent = r.parent || "기타";
    if (!grouped[parent]) grouped[parent] = [];
    grouped[parent].push(r.name);
  });

  return Object.entries(grouped).map(([parent, names]) => {
    const region = regionHierarchy.find(r => r.name === parent);
    const isAll = region && region.children.every(child =>
      selectedRegions.find(sel => sel.name === child.name)
    );
    return isAll ? `${parent}(전체)` : names.map(name => `(${parent}) ${name}`).join(", ");
  }).join(", ");
}

// ✅ DOM 요소
const regionApplyBtn = document.getElementById("regionApply");
const fishApplyBtn = document.getElementById("fishApply");
const regionIdInput = document.getElementById("regionIdInput");
const fishTypeInputGroup = document.getElementById("fishTypeInputGroup");
const selectedRegionOutput = document.getElementById("selectedRegionText");
const selectedFishOutput = document.getElementById("selectedFishText");

// ✅ 지역 적용 버튼 클릭 시
regionApplyBtn?.addEventListener("click", () => {
  const regions = getSelectedRegions();
  console.log("🟡 selectedRegions:", regions);

  const cached = getCachedRegions();
  console.log("🔵 getCachedRegions:", cached);

  const label = getCompactRegionText(regions, cached);
  selectedRegionOutput.textContent = label || "선택된 지역 없음";

  regionIdInput.value = regions.length > 0 ? regions[0].id : "";
});

// ✅ 어종 적용 버튼 클릭 시
fishApplyBtn?.addEventListener("click", () => {
  const fish = getSelectedFishTypes();
  selectedFishOutput.textContent = fish.length > 0 ? fish.join(", ") : "선택된 어종 없음";

  fishTypeInputGroup.innerHTML = '';
  fish.forEach(name => {
    const input = document.createElement("input");
    input.type = "hidden";
    input.name = "fishTypeNames";
    input.value = name;
    fishTypeInputGroup.appendChild(input);
  });
});

// ✅ 모달 외부 클릭 시 닫기
function initModalOutsideClose() {
  [document.getElementById("regionModal"), document.getElementById("dateModal"), document.getElementById("fishModal")]
    .forEach(modal => {
      modal?.addEventListener("click", (e) => {
        if (e.target.classList.contains("modal")) {
          closeModal(modal);
        }
      });
    });
}

// ✅ 최초 초기화
document.addEventListener("DOMContentLoaded", () => {
  initRegionModalIfExist();
  initFishModalIfExist();
  initDateModalIfExist();
  initModalOutsideClose();
});

flatpickr("#datePicker", {
  mode: "multiple",
  dateFormat: "Y-m-d",
  position: "auto left top", // ✅ 좌상단 정렬
  positionElement: document.getElementById("datePicker"), // ✅ 이 input 기준으로 위치 정렬
  onChange: (selectedDates) => {
    const container = document.getElementById("dateContainer");
    container.innerHTML = "";
    selectedDates.forEach((date, idx) => {
      const formatted = date.toLocaleDateString("sv-SE");
      const div = document.createElement("div");
      div.innerHTML = `
        <label>${formatted}</label>
        <input type="hidden" name="availableDates[${idx}].date" value="${formatted}">
        <input type="number" name="availableDates[${idx}].capacity" placeholder="정원" min="1" required>
      `;
      container.appendChild(div);
    });
  }
});
