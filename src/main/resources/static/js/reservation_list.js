// ✅ 공통 모듈 import
import {
  ModalState,
  closeModal,
  bindModalOutsideClick,
  injectHiddenInputs
} from "./modal_common.js";

import { initRegionModal } from "./modal_region.js";
import { initFishModal } from "./modal_fish.js";
import { initDateModalIfExist } from "./modal_date.js"; // ✅ 통일된 방식 사용

// ✅ 지역 캐시 로컬 저장
let cachedRegions = null;

export function getCachedRegions() {
  return cachedRegions;
}

export function setCachedRegions(data) {
  cachedRegions = data;
  if (ModalState.setRegionCache) {
    ModalState.setRegionCache(data);
  }
}

// ✅ 카드 목록 fetch 및 렌더링
export function applyFilters({ sortKey = "latest" } = {}) {
  const type = location.pathname.split("/").at(-1);
  const query = new URLSearchParams({ type, page: 0, sort: sortKey });

  ModalState.getRegions().forEach(r => query.append("regionId", r.id));
  ModalState.getDates().forEach(d => query.append("date", d));
  ModalState.getFishTypes().forEach(f => query.append("fishType", f));

  const keywordEl = document.querySelector(".search-input");
  const keyword = keywordEl ? keywordEl.value.trim() : "";
  if (keyword) query.append("keyword", keyword);

  fetch(`/api/reservation?${query.toString()}`)
    .then(res => res.ok ? res.json() : Promise.reject("서버 오류"))
    .then(data => Array.isArray(data) ? updateCards(data) : Promise.reject("데이터 오류"))
    .catch(err => {
      console.error("카드 불러오기 실패:", err);
      const container = document.getElementById("cardContainer");
      if (container) {
        container.innerHTML = '<p style="text-align:center; color:red;">카드 데이터를 불러오지 못했습니다.</p>';
      }
    });
}

// ✅ 카드 DOM 구성
function updateCards(cards) {
  const container = document.getElementById("cardContainer");
  if (!container) return;

  const typeToExtension = {
    boat: "jpg",
    float: "png",
    island: "jpg",
    rock: "jpg",
    stay: "png"
  };

  container.innerHTML = cards.length === 0
    ? '<p style="text-align:center;">조건에 맞는 예약이 없습니다.</p>'
    : cards.map(card => {
        const ext = typeToExtension[card.typeLower] || "jpg";
        const imageSrc = card.imageUrl || `/images/${card.typeLower || 'boat'}.${ext}`;

        return `
          <div class="ad-card">
            <a href="/reservation/detail/${card.id}" class="ad-card-link">
              <div class="ad-image">
                <img src="${imageSrc}" alt="예약 이미지" class="card-image">
              </div>
              <div class="ad-desc">${card.title}</div>
              <div class="ad-detail">
                <p>지역: ${card.region || "없음"}</p>
                <p>회사명: ${card.companyName || "알 수 없음"}</p>
                <p>어종: ${(card.fishTypes && card.fishTypes.join(", ")) || "정보 없음"}</p>
                <p>${card.content}</p>
              </div>
            </a>
          </div>
        `;
      }).join('');
}

// ✅ 선택된 지역 텍스트 포맷 생성
function getCompactRegionText() {
  const regions = ModalState.getRegions();
  const cached = getCachedRegions();
  if (!cached) return "선택된 지역 없음";

  const grouped = regions.reduce((acc, cur) => {
    (acc[cur.parent] = acc[cur.parent] || []).push(cur);
    return acc;
  }, {});

  return Object.entries(grouped)
    .flatMap(([parent, selected]) => {
      const total = (cached.find(r => r.name === parent) || {}).children?.length || 0;
      return selected.length === total
        ? [`(${parent}) 전체`]
        : selected.map(c => `(${parent}) ${c.name}`);
    })
    .join(", ");
}

// ✅ 전체 선택 정보 UI에 표시
function updateSelectedInfo() {
  const region = ModalState.getRegions();
  const fish = ModalState.getFishTypes();
  const date = ModalState.getDates();

  const label = document.getElementById("selectedInfo");
  if (!label) return;

  const parts = [];

  const regionText = region.length > 0
    ? `현재 선택 지역: ${getCompactRegionText()}`
    : "선택된 지역 없음";
  const regionLabel = document.querySelector("#regionModal .current-selection");
  if (regionLabel) regionLabel.innerText = regionText;
  parts.push(regionText);

  const dateText = date.length > 0
    ? `선택한 날짜: ${date.join(", ")}`
    : "선택된 날짜 없음";
  const dateLabel = document.querySelector("#dateModal .current-selection");
  if (dateLabel) dateLabel.innerText = dateText;
  parts.push(dateText);

  const fishText = fish.length > 0
    ? `선택한 어종: ${fish.join(", ")}`
    : "선택된 어종 없음";
  const fishLabel = document.querySelector("#fishModal .current-selection");
  if (fishLabel) fishLabel.innerText = fishText;
  parts.push(fishText);

  label.innerText = parts.filter(Boolean).join("\n");
}

// ✅ 날짜 텍스트 라벨 표시
function updateDateLabel() {
  const selected = ModalState.getDates();
  const container = document.getElementById("dateContainer");
  if (!container) return;
  container.innerHTML = "";
  selected.forEach(date => {
    const span = document.createElement("span");
    span.className = "date-label";
    span.textContent = date;
    container.appendChild(span);
  });
}

function initSortControl() {
  const sortBtn = document.getElementById("sortBtn");
  const sortOptions = document.getElementById("sortOptions");
  if (!sortBtn || !sortOptions) return;

  sortBtn.addEventListener("click", () => sortOptions.classList.toggle("hidden"));

  document.querySelectorAll("#sortOptions button").forEach(btn => {
    btn.addEventListener("click", () => {
      const selected = btn.getAttribute("data-sort");
      applyFilters({ sortKey: selected });
      sortOptions.classList.add("hidden");
    });
  });

  document.addEventListener("click", (e) => {
    if (!sortBtn.contains(e.target) && !sortOptions.contains(e.target)) {
      sortOptions.classList.add("hidden");
    }
  });
}

function initSearchControl() {
  const input = document.querySelector(".search-input");
  const btn = document.querySelector(".search-button");
  if (btn) btn.addEventListener("click", () => applyFilters({}));
  if (input) input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") applyFilters({});
  });
}

function initModalOutsideClose() {
  ["regionModal", "dateModal", "fishModal"].forEach(id => {
    const modal = document.getElementById(id);
    if (modal) bindModalOutsideClick(modal);
  });
}

function initAllModals() {
  initRegionModal({ onApply: handleRegionApply });
  initFishModal({ onApply: handleFishApply });
  initDateModalIfExist({ onApply: handleDateApply });
  initModalOutsideClose();
}

function handleRegionApply() {
  updateSelectedInfo();
  if (isListPage) applyFilters();
}

function handleFishApply() {
  updateSelectedInfo();
  if (isListPage) applyFilters();
}

function handleDateApply() {
  updateSelectedInfo();
  updateDateLabel();
  if (isListPage) applyFilters();
}

// ✅ 초기화 실행
const isListPage = location.pathname.includes("/reservation/list");

fetch("/api/regions/hierarchy")
  .then(res => res.json())
  .then(setCachedRegions)
  .catch(err => console.error("지역 데이터 초기화 실패:", err));

document.addEventListener("DOMContentLoaded", () => {
  initSortControl();
  initSearchControl();
  initAllModals();
  if (isListPage) applyFilters();
});
