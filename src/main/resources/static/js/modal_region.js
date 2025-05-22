import { getSelectedRegions, setSelectedRegions } from "./modal_state.js";

let cachedRegions = null;
function getCachedRegions() {
  return cachedRegions;
}
function setCachedRegions(data) {
  cachedRegions = data;
}

// ✅ 모달 닫기 함수
function closeModal(modal) {
  modal?.classList.remove("show");
  modal?.classList.add("hidden");
}

// ✅ 초기화 함수
export function initRegionModal() {
  const regionBtn = document.getElementById("regionBtn");
  const regionModal = document.getElementById("regionModal");
  const regionList = document.getElementById("regionList");
  const regionApply = document.getElementById("regionApply");
  const regionReset = document.getElementById("regionReset");

  if (!regionBtn || !regionModal || !regionList || !regionApply || !regionReset) {
    console.warn("⚠️ [initRegionModal] 필수 요소가 없음. HTML 확인 필요.");
    return;
  }

  regionBtn.addEventListener("click", () => {
    regionModal.classList.remove("hidden");
    regionModal.classList.add("show");

    const cached = getCachedRegions();
    if (cached) {
      renderFilteredRegions(cached, regionList, regionModal);
    } else {
      fetch("/api/regions/hierarchy")
        .then(res => res.json())
        .then(data => {
          setCachedRegions(data);
          renderFilteredRegions(data, regionList, regionModal);
        })
        .catch(err => {
          console.error("지역 데이터 로딩 실패:", err);
          regionList.innerHTML = '<p style="color:red;">지역 데이터를 불러오는 데 실패했습니다.</p>';
        });
    }
  });

  regionApply.addEventListener("click", () => {
    closeModal(regionModal);
  });

  regionReset.addEventListener("click", () => {
    setSelectedRegions([]);
    document.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
    const label = regionModal.querySelector(".current-selection");
    if (label) label.textContent = "선택된 지역 없음";
  });
}

function renderFilteredRegions(data, container, modalRoot) {
  container.innerHTML = '';
  data.forEach(region => {
    const parentWrapper = document.createElement('div');
    parentWrapper.className = 'region-group';

    const title = document.createElement('div');
    title.className = 'region-title';
    title.innerText = region.name;
    parentWrapper.appendChild(title);

    const childWrapper = document.createElement('div');
    childWrapper.className = 'region-children';

    region.children.forEach(child => {
      const btn = document.createElement('button');
      btn.className = 'region-child-btn';
      btn.innerText = child.name;
      btn.dataset.regionId = child.id;

      btn.addEventListener("click", () => {
        btn.classList.toggle("selected");

        const selected = getSelectedRegions();
        const idx = selected.findIndex(r => r.id == child.id);
        if (idx !== -1) selected.splice(idx, 1);
        else selected.push({ id: child.id, name: child.name });
        setSelectedRegions(selected);

        const selectedText = selected.map(r => r.name).join(', ') || "선택된 지역 없음";
        const label = modalRoot.querySelector(".current-selection");
        if (label) label.textContent = selectedText;
      });

      childWrapper.appendChild(btn);
    });

    parentWrapper.appendChild(childWrapper);
    container.appendChild(parentWrapper);
  });
}
