// ✅ 전역 import
import { getSelectedRegions, getSelectedFishTypes } from "./modal_state.js";

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
  const names = regions.map(r => r.name);
  selectedRegionOutput.textContent = names.length > 0 ? names.join(", ") : "선택된 지역 없음";
  regionIdInput.value = regions.length > 0 ? regions[0].id : "";
});

// ✅ 어종 적용 버튼 클릭 시
fishApplyBtn?.addEventListener("click", () => {
  const fish = getSelectedFishTypes();
  selectedFishOutput.textContent = fish.length > 0 ? fish.join(", ") : "선택된 어종 없음";

  // 숨겨진 input 정리 후 새로 생성
  fishTypeInputGroup.innerHTML = '';
  fish.forEach(name => {
    const input = document.createElement("input");
    input.type = "hidden";
    input.name = "fishTypeNames";
    input.value = name;
    fishTypeInputGroup.appendChild(input);
  });
});
