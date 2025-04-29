const weatherCache = {};
const regionList = [
  "감천항", "경인항", "경포대해수욕장", "고래불해수욕장", "광양항", "군산항",
  "낙산해수욕장", "남해동부", "대천해수욕장", "대한해협", "마산항", "망상해수욕장",
  "부산항", "부산항신항", "상왕등도", "생일도", "속초해수욕장", "송정해수욕장",
  "여수항", "완도항", "우이도", "울릉도북동", "울릉도북서", "인천항", "임랑해수욕장",
  "제주남부", "제주해협", "중문해수욕장", "태안항", "통영항", "평택당진항",
  "한수원_고리", "한수원_기장", "한수원_나곡", "한수원_덕천", "한수원_온양", "한수원_진하",
  "해운대해수욕장"
];

window.addEventListener("DOMContentLoaded", () => {
  loadWeatherFromLocalStorage();
  loadCachedWeather();
  refreshAllWeather();
});

function loadWeatherFromLocalStorage() {
  const saved = localStorage.getItem("weatherCache");
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      for (const [region, data] of Object.entries(parsed)) {
        if (data && data["관측시간"] && !data.error) {
          weatherCache[region] = data;
        }
      }
    } catch (e) {
      console.error("❌ 캐시 파싱 실패", e);
    }
  }
}

function saveWeatherToLocalStorage() {
  localStorage.setItem("weatherCache", JSON.stringify(weatherCache));
}

function loadCachedWeather() {
  const container = document.getElementById("allRegionWeather");
  if (!container) return;
  container.innerHTML = "";

  regionList.forEach(region => {
    const data = weatherCache[region];
    const card = document.createElement("div");
    card.className = "weather-card";

    if (!data) {
      card.innerHTML = `<h3>🌊 ${region}</h3><p>⏳ 정보 없음</p>`;
    } else if (data.error) {
      card.innerHTML = `<h3>🌊 ${region}</h3><p>❌ ${data.error}</p>`;
    } else {
      card.innerHTML = formatWeatherHTML(region, data);
    }

    container.appendChild(card);
  });
}

function refreshAllWeather() {
  regionList.forEach(region => {
    fetch(`/api/weather?region=${encodeURIComponent(region)}`)
      .then(res => res.json())
      .then(data => {
        weatherCache[region] = data;
        saveWeatherToLocalStorage();
        loadCachedWeather();
      })
      .catch(err => {
        weatherCache[region] = { error: err.message };
        loadCachedWeather();
      });
  });
}

function formatWeatherHTML(region, data) {
  return `
    <h3>🌍 ${region}</h3>
    <p>🕒 관측시각: ${data["관측시간"] || "정보 없음"}</p>
    <p>🌡 기온: ${data["기온"] || "정보 없음"}</p>
    <p>💨 풍속: ${data["풍속"] || "정보 없음"}</p>
    <p>🌊 수온: ${data["수온"] || "정보 없음"}</p>
    <p>🧂 염분: ${data["염분"] || "정보 없음"}</p>
    <p>🌊 파고: ${data["파고"] || "정보 없음"}</p>
    <p>🌫 기압: ${data["기압"] || "정보 없음"}</p>
  `;
}
