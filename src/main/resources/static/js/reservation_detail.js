// ✅ DOMContentLoaded: 모든 DOM 요소가 준비된 후 실행되도록 보장

document.addEventListener("DOMContentLoaded", () => {

  // ✅ 전역 변수 선언
  let cachedRegions = null;
  let selectedRegions = [];
  let selectedDate = null;
  let selectedFishTypes = [];

  // ✅ 주요 DOM 요소 캐싱
  const regionBtn = document.getElementById("regionBtn");
  const regionModal = document.getElementById("regionModal");
  const regionList = document.getElementById("regionList");
  const regionApply = document.getElementById("regionApply");
  const regionReset = document.getElementById("regionReset");

  const dateBtn = document.getElementById("dateBtn");
  const dateModal = document.getElementById("dateModal");
  const dateApply = document.getElementById("dateApply");
  const dateCancel = document.getElementById("dateCancel");
  const dateReset = document.getElementById("dateReset");

  const fishBtn = document.getElementById("fishBtn");
  const fishModal = document.getElementById("fishModal");
  const fishList = document.getElementById("fishList");
  const fishApply = document.getElementById("fishApply");
  const fishReset = document.getElementById("fishReset");

  // ✅ 달력 초기화
  flatpickr.localize(flatpickr.l10ns.ko);
  flatpickr("#datePickerContainer", {
    dateFormat: "Y-m-d",
    inline: true,
    locale: "ko",
    onChange: (selectedDates, dateStr) => {
      selectedDate = dateStr;
    },
    appendTo: document.getElementById("datePickerContainer")
  });

  // ✅ 한글 초성 추출 함수 (어종 그룹핑용)
  function getInitialConsonant(kor) {
    const initialTable = ["ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"];
    const uni = kor.charCodeAt(0) - 44032;
    const index = Math.floor(uni / 588);
    return initialTable[index];
  }

  // ✅ 지역 모달 열기
  regionBtn?.addEventListener("click", () => {
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
        console.error("지역 데이터 로딩 실패:", err);
        regionList.innerHTML = '<p style="color:red;">지역 데이터를 불러오는 데 실패했습니다.</p>';
      });
  });

  // ✅ 지역 리스트 렌더링
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

  function toggleRegionAll(region, childWrapper, allBtn) {
    const childBtns = Array.from(childWrapper.querySelectorAll('.region-child-btn:not(:first-child)'));
    const alreadySelected = childBtns.every(btn => btn.classList.contains("selected"));
    selectedRegions = selectedRegions.filter(r => r.parent !== region.name);

    if (alreadySelected) {
      allBtn.classList.remove("selected");
      childBtns.forEach(btn => btn.classList.remove("selected"));
    } else {
      region.children.forEach(child => {
        selectedRegions.push({ id: child.id, name: child.name, parent: region.name });
      });
      childBtns.forEach(btn => btn.classList.add("selected"));
      allBtn.classList.add("selected");
    }

    updateSelectedRegionTextOnly();
  }

  function toggleRegionChild(child, region, childWrapper, allBtn, btn) {
    const existingIndex = selectedRegions.findIndex(r => r.id === child.id);
    btn.classList.toggle("selected");

    if (existingIndex !== -1) {
      selectedRegions.splice(existingIndex, 1);
    } else {
      selectedRegions.push({ id: child.id, name: child.name, parent: region.name });
    }

    const totalSelected = region.children.every(c => selectedRegions.some(r => r.id === c.id));
    if (totalSelected) {
      allBtn.classList.add("selected");
      childWrapper.querySelectorAll('.region-child-btn:not(:first-child)').forEach(b => b.classList.add("selected"));
    } else {
      allBtn.classList.remove("selected");
    }

    updateSelectedRegionTextOnly();
  }

  regionApply?.addEventListener("click", () => {
    regionModal.classList.remove("show", "hidden");
    updateSelectedRegionTextOnly();
    fetchFilteredCards();
  });

  regionReset?.addEventListener("click", () => {
    selectedRegions = [];
    document.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
    updateSelectedRegionTextOnly();
  });

  function updateSelectedRegionTextOnly() {
    const modalDiv = document.querySelector("#regionModal .current-selection");
    const pageDiv = document.getElementById("selectedInfo");
    let text = "선택된 지역 없음";

    if (selectedRegions.length > 0) {
      const grouped = selectedRegions.reduce((acc, cur) => {
        (acc[cur.parent] = acc[cur.parent] || []).push(cur.name);
        return acc;
      }, {});
      const regionTexts = Object.entries(grouped).map(([parent, names]) => {
        const total = cachedRegions.find(r => r.name === parent)?.children.length || 0;
        return names.length === total ? `(${parent}) 전체` : `(${parent}) ${names.join(", ")}`;
      });
      text = `현재 선택 지역: ${regionTexts.join(", ")}`;
    }

    modalDiv.innerText = text;
    const dateText = selectedDate ? `선택한 날짜: ${selectedDate}` : "";
    pageDiv.innerText = [text, dateText].filter(Boolean).join("\n");
  }

  // ✅ 날짜 모달 열기 및 처리
  dateBtn?.addEventListener("click", () => {
    dateModal.classList.remove("hidden");
    dateModal.classList.add("show");
  });

  dateApply?.addEventListener("click", () => {
    dateModal.classList.remove("show", "hidden");
    updateSelectedDateTextOnly();
    fetchFilteredCards();
  });

  dateCancel?.addEventListener("click", () => {
    dateModal.classList.remove("show", "hidden");
  });

  dateReset?.addEventListener("click", () => {
    selectedDate = null;
    updateSelectedDateTextOnly();
  });

  function updateSelectedDateTextOnly() {
    const modalDiv = document.querySelector("#dateModal .current-selection");
    const pageDiv = document.getElementById("selectedInfo");
    const text = selectedDate ? `선택한 날짜: ${selectedDate}` : "선택된 날짜 없음";
    modalDiv.innerText = text;

    const regionText = selectedRegions.length > 0 ? `현재 선택 지역: ${selectedRegions.map(r => r.name).join(", ")}` : "";
    pageDiv.innerText = [regionText, text].filter(Boolean).join("\n");
  }

  // ✅ 어종 모달 열기 및 렌더링
  fishBtn?.addEventListener("click", () => {
    fishModal.classList.remove("hidden");
    fishModal.classList.add("show");
    if (fishList.children.length > 0) return;

    fetch("/api/fish-types")
      .then(res => res.json())
      .then(data => {
        data.sort((a, b) => a.localeCompare(b, 'ko'));
        const grouped = {};
        data.forEach(name => {
          const initial = getInitialConsonant(name);
          if (!grouped[initial]) grouped[initial] = [];
          grouped[initial].push(name);
        });
        fishList.innerHTML = Object.entries(grouped).map(([initial, names]) => {
          const groupHTML = names.map(name => `<button class="fish-type-btn" data-fish="${name}">${name}</button>`).join("");
          return `<div class="fish-group"><div class="fish-group-title">[ ${initial} ]</div><div class="fish-group-body">${groupHTML}</div></div>`;
        }).join("");

        document.querySelectorAll(".fish-type-btn").forEach(btn => {
          btn.addEventListener("click", () => {
            btn.classList.toggle("selected");
            const value = btn.dataset.fish;
            if (selectedFishTypes.includes(value)) {
              selectedFishTypes = selectedFishTypes.filter(v => v !== value);
            } else {
              selectedFishTypes.push(value);
            }
            updateSelectedFishText();
          });
        });
      });
  });

  fishApply?.addEventListener("click", () => {
    fishModal.classList.remove("show", "hidden");
    updateSelectedFishText();
    fetchFilteredCards();
  });

  fishReset?.addEventListener("click", () => {
    selectedFishTypes = [];
    document.querySelectorAll(".fish-type-btn.selected").forEach(btn => btn.classList.remove("selected"));
    updateSelectedFishText();
  });

  function updateSelectedFishText() {
    const modalDiv = document.querySelector("#fishModal .current-selection");
    const pageDiv = document.getElementById("selectedInfo");
    const fishText = selectedFishTypes.length > 0 ? `선택한 어종: ${selectedFishTypes.join(", ")}` : "선택된 어종 없음";
    modalDiv.innerText = fishText;

    const regionText = selectedRegions.length > 0 ? `현재 선택 지역: ${selectedRegions.map(r => r.name).join(", ")}` : "";
    const dateText = selectedDate ? `선택한 날짜: ${selectedDate}` : "";
    pageDiv.innerText = [regionText, dateText, fishText].filter(Boolean).join("\n");
  }

  // ✅ 모달 외부 클릭 시 닫기
  [regionModal, dateModal, fishModal].forEach(modal => {
    modal?.addEventListener("click", e => {
      if (e.target === modal) {
        modal.classList.remove("show", "hidden");
      }
    });
  });

  // ✅ 카드 필터링 및 렌더링
  function fetchFilteredCards() {
    const type = location.pathname.split("/").at(-1);
    const query = new URLSearchParams({ type, page: 0 });
    selectedRegions.forEach(r => query.append("regionId", r.id));
    if (selectedDate) query.append("date", selectedDate);
    selectedFishTypes.forEach(fish => query.append("fishType", fish));

    console.log("🔥 API 호출 URL:", `/api/reservation?${query.toString()}`);

    fetch(`/api/reservation?${query.toString()}`)
      .then(res => {
        if (!res.ok) throw new Error("서버 오류 발생");
        return res.json();
      })
      .then(data => {
        if (!Array.isArray(data)) throw new Error("받은 데이터가 배열이 아님");
        updateCards(data);
      })
      .catch(err => {
        console.error("카드 불러오기 실패:", err);
        const container = document.getElementById("cardContainer");
        container.innerHTML = '<p style="text-align:center; color:red;">카드 데이터를 불러오지 못했습니다.</p>';
      });
  }

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

});