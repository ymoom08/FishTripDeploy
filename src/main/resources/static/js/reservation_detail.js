document.addEventListener("DOMContentLoaded", function () {
  let cachedRegions = null;
  let selectedRegions = []; // [{ id: 1, name: "속초", parent: "강원" }, ...]

  document.getElementById("regionBtn").addEventListener("click", () => {
    const regionModal = document.getElementById("regionModal");
    const regionList = document.getElementById("regionList");

    if (!regionModal || !regionList) return;

    regionModal.classList.remove("hidden");

    if (cachedRegions) {
      renderFilteredRegions(cachedRegions, regionList);
      return;
    }

    fetch("/api/regions/hierarchy")
      .then(res => res.ok ? res.json() : Promise.reject(res))
      .then(data => {
        cachedRegions = data;
        renderFilteredRegions(data, regionList);
      })
      .catch(err => {
        console.error("데이터 로딩 실패:", err);
        regionList.innerHTML = '<p style="color:red;">지역 데이터를 불러오는 데 실패했습니다.</p>';
      });
  });

  document.getElementById("regionApply").addEventListener("click", () => {
    document.getElementById("regionModal").classList.add("hidden");
    fetchFilteredCards();
  });

  document.getElementById("regionReset").addEventListener("click", () => {
    selectedRegions = [];
    updateSelectedRegionText();
    document.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
  });

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

      // 전체 버튼
      const allBtn = document.createElement('button');
      allBtn.innerText = '전체';
      allBtn.classList.add('region-child-btn');

      allBtn.addEventListener("click", () => {
        const childBtns = childWrapper.querySelectorAll('.region-child-btn:not(:first-child)');
        const allSelected = Array.from(childBtns).every(btn => btn.classList.contains('selected'));

        region.children.forEach(child => {
          const name = child.name;
          const isSelected = selectedRegions.some(r => r.name === name);

          if (allSelected && isSelected) {
            selectedRegions = selectedRegions.filter(r => r.name !== name);
          } else if (!allSelected && !isSelected) {
            selectedRegions.push({ id: child.id, name, parent: region.name });
          }
        });

        childBtns.forEach(btn => {
          btn.classList.toggle("selected", !allSelected);
        });

        updateSelectedRegionText();
      });

      childWrapper.appendChild(allBtn);

      // 자식 지역 버튼
      region.children.forEach(child => {
        const btn = document.createElement('button');
        btn.innerText = child.fullName ?? child.name;
        btn.classList.add('region-child-btn');
        btn.dataset.regionId = child.id;

        btn.addEventListener("click", () => {
          const id = child.id;
          const existingIndex = selectedRegions.findIndex(r => r.id === id);
          btn.classList.toggle("selected");

          if (existingIndex !== -1) {
            selectedRegions.splice(existingIndex, 1);
          } else {
            selectedRegions.push({ id, name: child.name, parent: region.name });
          }

          updateSelectedRegionText();
        });

        childWrapper.appendChild(btn);
      });

      parentWrapper.appendChild(childWrapper);
      container.appendChild(parentWrapper);
    });
  }

  function updateSelectedRegionText() {
    const text = selectedRegions.length === 0
      ? "선택된 지역 없음"
      : selectedRegions.map(r => `(${r.parent}) ${r.name}`).join(", ");
    document.querySelector(".current-selection").innerText = text;
  }

  function fetchFilteredCards() {
    const type = window.location.pathname.split("/")[2];
    const query = new URLSearchParams();
    query.append("type", type);
    selectedRegions.forEach(region => query.append("regionId", region.id));

    fetch(`/api/reservation?${query.toString()}`)
      .then(response => response.json())
      .then(cards => updateCards(cards));
  }

  function updateCards(cards) {
    const container = document.querySelector("#cardContainer");
    container.innerHTML = "";

    cards.forEach(card => {
      const div = document.createElement("div");
      div.className = "ad-card";
      div.innerHTML = `
        <img src="${card.imageUrl}" alt="예약 이미지">
        <h3>${card.title}</h3>
        <p>지역: ${card.region ?? "없음"}</p>
        <p>회사명: ${card.companyName ?? "알 수 없음"}</p>
        <p>어종: ${card.fishTypes?.join(", ") ?? "정보 없음"}</p>
        <p>${card.content}</p>
      `;
      container.appendChild(div);
    });
  }
});
