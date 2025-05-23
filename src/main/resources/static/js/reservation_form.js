import { getSelectedRegions, getSelectedFishTypes, closeModal } from "./modal_state.js";  // closeModal을 가져옵니다.
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
          closeModal(modal);  // modal_state.js에서 가져온 closeModal을 사용
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
  position: "auto left top", // 좌상단 정렬
  positionElement: document.getElementById("datePicker"), // 기준 요소 명시
  onChange: (selectedDates, dateStr, instance) => {
    const container = document.getElementById("dateContainer");
    container.innerHTML = ""; // 이전 내용을 지우고 새로 생성

    selectedDates.forEach((date, idx) => {
      const formatted = date.toLocaleDateString("sv-SE");
      const div = document.createElement("div");
      div.className = "date-entry";
      div.innerHTML = `
        <label>${formatted}</label>
        <input type="hidden" name="availableDates[${idx}].date" value="${formatted}">
        <input type="number" name="availableDates[${idx}].capacity" placeholder="정원" min="1" required>
        <button type="button" class="remove-date" data-date="${formatted}">❌</button>
      `;
      container.appendChild(div);
    });

    // 각 삭제 버튼에 이벤트 핸들러 부착
    container.querySelectorAll(".remove-date").forEach(btn => {
      btn.addEventListener("click", () => {
        const dateToRemove = btn.dataset.date;

        // 삭제 후 업데이트된 selectedDates 상태 반영
        const updatedDates = selectedDates.filter(date =>
          date.toLocaleDateString("sv-SE") !== dateToRemove
        );

        // 삭제된 날짜로 setDate를 갱신하고, 화면을 다시 그리도록 처리
        instance.setDate(updatedDates, true);  // true를 두 번째 인자로 전달하여, onChange를 트리거하도록 합니다.
      });
    });
  }
});
