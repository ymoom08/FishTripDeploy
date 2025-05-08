// 🔁 모듈 import
import { initRegionModal } from "./modal_region.js";
import { initDateModal } from "./modal_date.js";
import { initFishModal } from "./modal_fish.js";

// ✅ 지역 캐시 + getter/setter
let cachedRegions = null;
export function getCachedRegions() {
  return cachedRegions;
}
export function setCachedRegions(data) {
  cachedRegions = data;
}

// ✅ 선택된 지역 배열 + getter/setter
let selectedRegions = [];
export function getSelectedRegions() {
  return selectedRegions;
}
export function setSelectedRegions(data) {
  selectedRegions = data;
}

// ✅ 날짜는 객체로 감싸서 참조 유지
export const selectedDate = { value: null };

// ✅ 어종 리스트 배열 + getter/setter
let selectedFishTypes = [];
export function getSelectedFishTypes() {
  return selectedFishTypes;
}
export function setSelectedFishTypes(data) {
  selectedFishTypes = data;
}

// ✅ 공통 모달 닫기 함수
export function closeModal(modal) {
  modal.classList.remove("show");
  modal.classList.add("hidden");
}

// ✅ DOMContentLoaded 시 초기화 실행
document.addEventListener("DOMContentLoaded", () => {
  initRegionModal();
  initDateModal();
  initFishModal();
  initSortControl();
  initSearchControl();
  initModalOutsideClose();
});

// ✅ 정렬 컨트롤 초기화
function initSortControl() {
  const sortBtn = document.getElementById("sortBtn");
  const sortOptions = document.getElementById("sortOptions");

  sortBtn?.addEventListener("click", () => {
    sortOptions.classList.toggle("hidden");
  });

  document.querySelectorAll("#sortOptions button").forEach(btn => {
    btn.addEventListener("click", () => {
      const selectedSort = btn.getAttribute("data-sort");
      applyFilters({ sortKey: selectedSort });
      sortOptions.classList.add("hidden");
    });
  });

  document.addEventListener("click", (e) => {
    if (!sortBtn.contains(e.target) && !sortOptions.contains(e.target)) {
      sortOptions.classList.add("hidden");
    }
  });
}

// ✅ 검색 컨트롤 초기화
function initSearchControl() {
  const searchInput = document.querySelector(".search-input");
  const searchButton = document.querySelector(".search-button");

  searchButton?.addEventListener("click", () => {
    applyFilters({});
  });

  searchInput?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      applyFilters({});
    }
  });
}

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

// ✅ 서버로 필터링된 카드 요청
export function fetchFilteredCards(sortKey = "latest") {
  const type = location.pathname.split("/").at(-1);
  const query = new URLSearchParams({ type, page: 0, sort: sortKey });

  getSelectedRegions().forEach(r => query.append("regionId", r.id));
  if (selectedDate.value) query.append("date", selectedDate.value);
  getSelectedFishTypes().forEach(fish => query.append("fishType", fish));

  const keyword = document.querySelector(".search-input")?.value.trim();
  if (keyword) query.append("keyword", keyword);

  fetch(`/api/reservation?${query.toString()}`)
    .then(res => res.ok ? res.json() : Promise.reject("서버 오류"))
    .then(data => Array.isArray(data) ? updateCards(data) : Promise.reject("데이터 오류"))
    .catch(err => {
      console.error("카드 불러오기 실패:", err);
      const container = document.getElementById("cardContainer");
      container.innerHTML = '<p style="text-align:center; color:red;">카드 데이터를 불러오지 못했습니다.</p>';
    });
}

// ✅ 카드 DOM 업데이트
function updateCards(cards) {
  const container = document.getElementById("cardContainer");
  container.innerHTML = cards.length === 0
    ? '<p style="text-align:center;">조건에 맞는 예약이 없습니다.</p>'
    : cards.map(card => `
      <div class="ad-card">
        <div class="ad-image">
          <img src="${card.imageUrl || "/images/boat.jpg"}" alt="예약 이미지" class="card-image">
        </div>
        <div class="ad-desc">${card.title}</div>
        <div class="ad-detail">
          <p>지역: ${card.region ?? "없음"}</p>
          <p>회사명: ${card.companyName ?? "알 수 없음"}</p>
          <p>어종: ${card.fishTypes?.join(", ") ?? "정보 없음"}</p>
          <p>${card.content}</p>
        </div>
      </div>
    `).join('');
}

// ✅ 필터 실행 함수
export function applyFilters({ sortKey = "latest" }) {
  fetchFilteredCards(sortKey);
}

// ✅ 선택된 지역 텍스트 갱신
export function updateSelectedRegionTextOnly() {
  const modalDiv = document.querySelector("#regionModal .current-selection");
  const pageDiv = document.getElementById("selectedInfo");
  const regions = getSelectedRegions();
  let text = "선택된 지역 없음";

  if (regions.length > 0) {
    const grouped = regions.reduce((acc, cur) => {
      (acc[cur.parent] = acc[cur.parent] || []).push(cur.name);
      return acc;
    }, {});
    const regionTexts = Object.entries(grouped).map(([parent, names]) => {
      const total = getCachedRegions().find(r => r.name === parent)?.children.length || 0;
      return names.length === total ? `(${parent}) 전체` : `(${parent}) ${names.join(", ")}`;
    });
    text = `현재 선택 지역: ${regionTexts.join(", ")}`;
  }

  modalDiv.innerText = text;
  const dateText = selectedDate.value ? `선택한 날짜: ${selectedDate.value}` : "";
  pageDiv.innerText = [text, dateText].filter(Boolean).join("\n");
}

// ✅ 선택된 날짜 텍스트 갱신
export function updateSelectedDateTextOnly() {
  const modalDiv = document.querySelector("#dateModal .current-selection");
  const pageDiv = document.getElementById("selectedInfo");
  const dateText = selectedDate.value ? `선택한 날짜: ${selectedDate.value}` : "선택된 날짜 없음";
  modalDiv.innerText = dateText;

  const regionText = getSelectedRegions().length > 0
    ? `현재 선택 지역: ${getSelectedRegions().map(r => r.name).join(", ")}`
    : "";
  pageDiv.innerText = [regionText, dateText].filter(Boolean).join("\n");
}

// ✅ 선택된 어종 텍스트 갱신
export function updateSelectedFishText() {
  const modalDiv = document.querySelector("#fishModal .current-selection");
  const pageDiv = document.getElementById("selectedInfo");
  const fish = getSelectedFishTypes();
  const fishText = fish.length > 0 ? `선택한 어종: ${fish.join(", ")}` : "선택된 어종 없음";
  modalDiv.innerText = fishText;

  const regionText = getSelectedRegions().length > 0
    ? `현재 선택 지역: ${getSelectedRegions().map(r => r.name).join(", ")}`
    : "";
  const dateText = selectedDate.value ? `선택한 날짜: ${selectedDate.value}` : "";
  pageDiv.innerText = [regionText, dateText, fishText].filter(Boolean).join("\n");
}
