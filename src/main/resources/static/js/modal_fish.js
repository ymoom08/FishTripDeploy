// ✅ modal_fish.js - 어종 모달 리팩토링 버전
import { getSelectedFishTypes, setSelectedFishTypes } from "./reservation_list.js";
import { closeModal } from "./reservation_list.js";
import { fetchFilteredCards, updateSelectedFishText } from "./reservation_list.js";

/**
 * ✅ 어종 모달 초기화
 */
export function initFishModal() {
  const fishBtn = document.getElementById("fishBtn");
  const fishModal = document.getElementById("fishModal");
  const fishList = document.getElementById("fishList");
  const fishApply = document.getElementById("fishApply");
  const fishReset = document.getElementById("fishReset");

  // 🔘 모달 열기
  fishBtn?.addEventListener("click", () => {
    fishModal.classList.remove("hidden");
    fishModal.classList.add("show");

    if (fishList.children.length > 0) return; // 이미 렌더링 됐다면 재요청 X

    fetch("/api/fish-types")
      .then(res => res.json())
      .then(data => {
        data.sort((a, b) => a.localeCompare(b, 'ko'));
        const grouped = groupByInitial(data);
        fishList.innerHTML = renderGroupedFish(grouped);
        attachFishButtonEvents();
      });
  });

  // 🔘 적용 버튼
  fishApply?.addEventListener("click", () => {
    closeModal(fishModal);
    updateSelectedFishText();
    fetchFilteredCards();
  });

  // 🔘 초기화 버튼
  fishReset?.addEventListener("click", () => {
    setSelectedFishTypes([]);
    document.querySelectorAll(".fish-type-btn.selected").forEach(btn => btn.classList.remove("selected"));
    updateSelectedFishText();
  });
}

/**
 * ✅ 초성 기준으로 어종 그룹핑
 */
function groupByInitial(data) {
  const grouped = {};
  data.forEach(name => {
    const initial = getInitialConsonant(name);
    if (!grouped[initial]) grouped[initial] = [];
    grouped[initial].push(name);
  });
  return grouped;
}

/**
 * ✅ 그룹된 어종을 HTML로 렌더링
 */
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

/**
 * ✅ 어종 버튼 이벤트 등록
 */
function attachFishButtonEvents() {
  document.querySelectorAll(".fish-type-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const value = btn.dataset.fish;
      btn.classList.toggle("selected");

      const types = getSelectedFishTypes();
      const idx = types.indexOf(value);
      if (idx !== -1) {
        types.splice(idx, 1);
      } else {
        types.push(value);
      }
      setSelectedFishTypes(types);
      updateSelectedFishText();
    });
  });
}

/**
 * ✅ 한글 초성 추출 함수
 */
function getInitialConsonant(kor) {
  const initialTable = ["ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"];
  const uni = kor.charCodeAt(0) - 44032;
  if (uni < 0 || uni > 11171) return "#";
  const index = Math.floor(uni / 588);
  return initialTable[index];
}
