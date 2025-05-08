import {
  getCachedRegions,
  setCachedRegions,
  getSelectedRegions,
  setSelectedRegions,
  closeModal,
  fetchFilteredCards,
  updateSelectedRegionTextOnly
} from "./reservation_list.js";

/**
 * ✅ 지역 모달 초기화 (이벤트 등록 및 첫 렌더링)
 */
export function initRegionModal() {
  const regionBtn = document.getElementById("regionBtn");
  const regionModal = document.getElementById("regionModal");
  const regionList = document.getElementById("regionList");
  const regionApply = document.getElementById("regionApply");
  const regionReset = document.getElementById("regionReset");

  regionBtn?.addEventListener("click", () => {
    regionModal.classList.remove("hidden");
    regionModal.classList.add("show");

    const cached = getCachedRegions();
    if (cached) {
      renderFilteredRegions(cached, regionList);
      return;
    }

    fetch("/api/regions/hierarchy")
      .then(res => res.ok ? res.json() : Promise.reject(res))
      .then(data => {
        setCachedRegions(data);
        renderFilteredRegions(data, regionList);
      })
      .catch(err => {
        console.error("지역 데이터 로딩 실패:", err);
        regionList.innerHTML = '<p style="color:red;">지역 데이터를 불러오는 데 실패했습니다.</p>';
      });
  });

  regionApply?.addEventListener("click", () => {
    closeModal(regionModal);
    updateSelectedRegionTextOnly();
    fetchFilteredCards();
  });

  regionReset?.addEventListener("click", () => {
    setSelectedRegions([]); // 전체 초기화
    document.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
    updateSelectedRegionTextOnly();
  });
}

/**
 * ✅ 지역 리스트 렌더링
 */
function renderFilteredRegions(data, container) {
  container.innerHTML = '';
  data.forEach(region => {
    const parentWrapper = document.createElement('div');
    parentWrapper.classList.add('region-group');

    const title = document.createElement('div');
    title.innerText = region.name;
    title.classList.add('region-title');
    parentWrapper.appendChild(title);

    const childWrapper = document.createElement('div');
    childWrapper.classList.add('region-children');

    const allBtn = document.createElement('button');
    allBtn.innerText = '전체';
    allBtn.classList.add('region-child-btn');
    allBtn.addEventListener("click", () => toggleRegionAll(region, childWrapper, allBtn));
    childWrapper.appendChild(allBtn);

    region.children.forEach(child => {
      const btn = document.createElement('button');
      btn.innerText = child.name;
      btn.classList.add('region-child-btn');
      btn.dataset.regionId = child.id;
      btn.addEventListener("click", () => toggleRegionChild(child, region, childWrapper, allBtn, btn));
      childWrapper.appendChild(btn);
    });

    parentWrapper.appendChild(childWrapper);
    container.appendChild(parentWrapper);
  });
}

/**
 * ✅ 전체 버튼 클릭 시 하위 지역 모두 선택/해제
 */
function toggleRegionAll(region, childWrapper, allBtn) {
  const selected = getSelectedRegions();
  const childBtns = Array.from(childWrapper.querySelectorAll('.region-child-btn:not(:first-child)'));
  const allSelected = childBtns.every(btn => btn.classList.contains("selected"));

  // 현재 지역에 해당하는 선택 목록 제거
  const filtered = selected.filter(r => r.parent !== region.name);

  if (allSelected) {
    // 전체 해제
    childBtns.forEach(btn => btn.classList.remove("selected"));
    allBtn.classList.remove("selected");
    setSelectedRegions(filtered);
  } else {
    // 전체 선택
    childBtns.forEach(btn => btn.classList.add("selected"));
    allBtn.classList.add("selected");

    const newItems = region.children.map(child => ({
      id: child.id,
      name: child.name,
      parent: region.name
    }));

    setSelectedRegions([...filtered, ...newItems]);
  }

  updateSelectedRegionTextOnly();
}

/**
 * ✅ 개별 지역 버튼 토글 처리
 */
function toggleRegionChild(child, region, childWrapper, allBtn, btn) {
  const selected = getSelectedRegions();
  const exists = selected.findIndex(r => r.id === child.id);

  btn.classList.toggle("selected");

  if (exists !== -1) {
    selected.splice(exists, 1);
  } else {
    selected.push({ id: child.id, name: child.name, parent: region.name });
  }

  setSelectedRegions(selected);

  // 모든 자식이 선택됐는지 확인해 전체 버튼에 반영
  const isAllSelected = region.children.every(c =>
    selected.some(r => r.id === c.id)
  );

  if (isAllSelected) {
    allBtn.classList.add("selected");
    childWrapper.querySelectorAll('.region-child-btn:not(:first-child)').forEach(btn => btn.classList.add("selected"));
  } else {
    allBtn.classList.remove("selected");
  }

  updateSelectedRegionTextOnly();
}
