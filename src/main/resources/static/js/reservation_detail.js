// reservation_detail.js (🔥 지역 + 날짜 선택 기능 통합 완성본)

// DOMContentLoaded 이벤트: 페이지가 완전히 로드되면 실행
document.addEventListener("DOMContentLoaded", function () {

  // 🔥 지역 관련 변수
  let cachedRegions = null;
  let selectedRegions = [];

  const regionBtn = document.getElementById("regionBtn");
  const regionModal = document.getElementById("regionModal");
  const regionList = document.getElementById("regionList");
  const regionApply = document.getElementById("regionApply");
  const regionReset = document.getElementById("regionReset");

  // 🔥 날짜 관련 변수
  let selectedDate = null;
  const dateBtn = document.getElementById("dateBtn");
  const dateModal = document.getElementById("dateModal");
  const dateApply = document.getElementById("dateApply");
  const dateReset = document.getElementById("dateReset");

  // 🔥 지역 버튼 클릭 시 모달 열기
  regionBtn.addEventListener("click", () => {
    if (!regionModal || !regionList) return;
    regionModal.classList.remove("hidden");
    regionModal.classList.add("show");

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

  // 🔥 날짜 버튼 클릭 시 모달 열기
  dateBtn.addEventListener("click", () => {
    if (!dateModal) return;
    dateModal.classList.remove("hidden");
    dateModal.classList.add("show");
  });

  // 🔥 지역 모달 적용 버튼
  regionApply.addEventListener("click", () => {
    regionModal.classList.remove("show");
    regionModal.classList.add("hidden");
    fetchFilteredCards();
  });

  // 🔥 지역 모달 초기화 버튼
  regionReset.addEventListener("click", () => {
    selectedRegions = [];
    updateSelectedRegionText();
    document.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
  });

  // 🔥 날짜 모달 적용 버튼
  dateApply.addEventListener("click", () => {
    const dateInput = document.getElementById("selectedDateInput").value;
    selectedDate = dateInput || null;
    dateModal.classList.remove("show");
    dateModal.classList.add("hidden");
    fetchFilteredCards();
  });

  // 🔥 날짜 모달 초기화 버튼
  dateReset.addEventListener("click", () => {
    selectedDate = null;
    document.getElementById("selectedDateInput").value = "";
    dateModal.classList.remove("show");
    dateModal.classList.add("hidden");
    fetchFilteredCards();
  });

  // 🔥 지역 리스트 렌더링 함수
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

      allBtn.addEventListener("click", () => {
        const childBtnList = Array.from(childWrapper.querySelectorAll('.region-child-btn:not(:first-child)'));
        const alreadySelected = childBtnList.every(btn => btn.classList.contains("selected"));

        if (alreadySelected) {
          selectedRegions = selectedRegions.filter(r => r.parent !== region.name);
          allBtn.classList.remove("selected");
          childBtnList.forEach(btn => btn.classList.remove("selected"));
        } else {
          selectedRegions = selectedRegions.filter(r => r.parent !== region.name);

          region.children.forEach(child => {
            selectedRegions.push({ id: child.id, name: child.name, parent: region.name });
          });

          childBtnList.forEach(btn => btn.classList.add("selected"));
          allBtn.classList.add("selected");
        }

        updateSelectedRegionText();
      });

      childWrapper.appendChild(allBtn);

      region.children.forEach(child => {
        const btn = document.createElement('button');
        btn.innerText = child.name;
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

          const totalSelected = region.children.every(c =>
            selectedRegions.some(r => r.id === c.id)
          );

          if (totalSelected) {
            allBtn.classList.add("selected");
            childWrapper.querySelectorAll('.region-child-btn:not(:first-child)').forEach(b => b.classList.add("selected"));
          } else {
            allBtn.classList.remove("selected");
          }

          updateSelectedRegionText();
        });

        childWrapper.appendChild(btn);
      });

      parentWrapper.appendChild(childWrapper);
      container.appendChild(parentWrapper);
    });
  }

  // 🔥 선택된 지역/날짜를 페이지에 표시
  function updateSelectedRegionText() {
    const modalSelectionDiv = document.querySelector("#regionModal .current-selection");
    const pageSelectionDiv = document.getElementById("selectedInfo");

    let text = "";

    if (selectedRegions.length > 0) {
      let grouped = {};
      selectedRegions.forEach(r => {
        if (!grouped[r.parent]) grouped[r.parent] = [];
        grouped[r.parent].push(r.name);
      });

      const regionTexts = Object.entries(grouped).map(([parent, names]) => {
        const totalChildCount = cachedRegions.find(r => r.name === parent)?.children.length ?? 0;
        if (names.length === totalChildCount) {
          return `(${parent}) 전체`;
        } else {
          return `(${parent}) ${names.join(', ')}`;
        }
      });

      text += `현재 선택 지역: ${regionTexts.join(', ')}`;
    }

    if (selectedDate) {
      text += `\n선택한 날짜: ${selectedDate}`;
    }

    modalSelectionDiv.innerText = text || "선택된 지역 없음";
    pageSelectionDiv.innerText = text;
  }

  // 🔥 서버에 필터 조건 넘기고 카드 다시 불러오기
  function fetchFilteredCards() {
    const type = window.location.pathname.split("/")[2];
    const query = new URLSearchParams();
    query.append("type", type);
    query.append("page", 0);

    selectedRegions.forEach(region => {
      if (region.id) query.append("regionId", region.id);
    });

    if (selectedDate) {
      query.append("date", selectedDate);
    }

    fetch(`/api/reservation?${query.toString()}`)
      .then(response => response.json())
      .then(cards => updateCards(cards))
      .catch(err => {
        console.error("필터링 데이터 요청 실패:", err);
      });
  }

  // 🔥 카드 목록을 페이지에 그리기
  function updateCards(cards) {
    const container = document.querySelector("#cardContainer");
    container.innerHTML = "";

    cards.forEach(card => {
      const div = document.createElement("div");
      div.className = "ad-card";

      let imgSrc = card.imageUrl;
      if (!imgSrc.startsWith("/images/")) {
        imgSrc = "/images/boat.jpg";
      }

      div.innerHTML = `
        <div class="ad-image">
          <img src="${imgSrc}" alt="예약 이미지" style="width:100%; height:180px; object-fit:cover;">
        </div>
        <div class="ad-desc">${card.title}</div>
        <div class="ad-detail">
          <p>지역: ${card.region ?? "없음"}</p>
          <p>회사명: ${card.companyName ?? "알 수 없음"}</p>
          <p>어종: ${card.fishTypes?.join(", ") ?? "정보 없음"}</p>
          <p>${card.content}</p>
        </div>
      `;
      container.appendChild(div);
    });
  }

});