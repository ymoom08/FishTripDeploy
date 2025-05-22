import { getSelectedFishTypes, setSelectedFishTypes } from "./modal_state.js";

// ✅ 모달 닫기 함수
function closeModal(modal) {
  modal?.classList.remove("show");
  modal?.classList.add("hidden");
}

// ✅ 초기화 함수
export function initFishModal() {
  const fishBtn = document.getElementById("fishBtn");
  const fishModal = document.getElementById("fishModal");
  const fishList = document.getElementById("fishList");
  const fishApply = document.getElementById("fishApply");
  const fishReset = document.getElementById("fishReset");

  if (!fishBtn || !fishModal || !fishList || !fishApply || !fishReset) {
    console.warn("⚠️ [initFishModal] 필수 요소가 없음. HTML 확인 필요.");
    return;
  }

  fishBtn.addEventListener("click", () => {
    fishModal.classList.remove("hidden");
    fishModal.classList.add("show");

    // 매번 새로 렌더링
    fishList.innerHTML = '';

    fetch("/api/fish-types")
      .then(res => res.json())
      .then(data => {
        data.sort((a, b) => a.localeCompare(b, 'ko'));
        const grouped = groupByInitial(data);
        fishList.innerHTML = renderGroupedFish(grouped);
        attachFishButtonEvents(fishModal);
      });
  });

  fishApply.addEventListener("click", () => {
    closeModal(fishModal);
  });

  fishReset.addEventListener("click", () => {
    setSelectedFishTypes([]);
    document.querySelectorAll(".fish-type-btn.selected").forEach(btn => btn.classList.remove("selected"));
    updateSelectedFishTextOnly(fishModal);
  });
}

function groupByInitial(data) {
  const grouped = {};
  data.forEach(name => {
    const initial = getInitialConsonant(name);
    if (!grouped[initial]) grouped[initial] = [];
    grouped[initial].push(name);
  });
  return grouped;
}

function renderGroupedFish(grouped) {
  return Object.entries(grouped).map(([initial, names]) => {
    const groupHTML = names.map(name =>
      `<button class="fish-type-btn" data-fish="${name}">${name}</button>`
    ).join("");

    return `
      <div class="fish-group">
        <div class="fish-group-title">[ ${initial} ]</div>
        <div class="fish-group-body">${groupHTML}</div>
      </div>`;
  }).join("");
}

function attachFishButtonEvents(modalRoot) {
  document.querySelectorAll(".fish-type-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const value = btn.dataset.fish;
      btn.classList.toggle("selected");

      const types = getSelectedFishTypes();
      const idx = types.indexOf(value);
      if (idx !== -1) types.splice(idx, 1);
      else types.push(value);
      setSelectedFishTypes(types);

      updateSelectedFishTextOnly(modalRoot);
    });
  });
}

function updateSelectedFishTextOnly(modalRoot) {
  const types = getSelectedFishTypes();
  const text = types.length > 0 ? types.join(', ') : "선택된 어종 없음";
  const label = modalRoot.querySelector(".current-selection");
  if (label) label.textContent = text;
}

function getInitialConsonant(kor) {
  const initialTable = ["ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"];
  const uni = kor.charCodeAt(0) - 44032;
  if (uni < 0 || uni > 11171) return "#";
  const index = Math.floor(uni / 588);
  return initialTable[index];
}
