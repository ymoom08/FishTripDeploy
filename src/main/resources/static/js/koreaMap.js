// weather.js

let refreshIntervalId = null;
const weatherCache = {};
const regionList = [
  "서해북부", "서해중부", "서해남부", "남해서부",
  "제주도", "남해동부", "동해남부", "동해중부"
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
        if (data && !data.error && data.observedAt) {
          weatherCache[region] = data;
        }
      }
    } catch (e) {
      console.error("캐시 파싱 실패", e);
    }
  }
}

function saveWeatherToLocalStorage() {
  localStorage.setItem("weatherCache", JSON.stringify(weatherCache));
}

function loadCachedWeather() {
  const container = document.getElementById("allRegionWeather");
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
      card.innerHTML = formatWeatherHTML(data);
    }

    container.appendChild(card);
  });
}

function refreshAllWeather() {
  regionList.forEach(region => {
    fetch(`/api/weather?region=${region}`)
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

function selectRegion(regionName) {
  const status = document.getElementById("weatherStatus");
  status.innerText = `[${regionName}] 해역 정보를 불러오는 중...`;
  status.style.display = "block";

  fetch(`/api/weather?region=${regionName}`)
    .then(res => res.json())
    .then(data => {
      weatherCache[regionName] = data;
      saveWeatherToLocalStorage();
      loadCachedWeather();
      status.style.display = "none";
    })
    .catch(error => {
      weatherCache[regionName] = { error: error.message };
      loadCachedWeather();
      status.style.display = "none";
      alert(`[${regionName}] 정보 요청 실패: ${error.message}`);
    });

  if (refreshIntervalId) clearInterval(refreshIntervalId);
  refreshIntervalId = setInterval(() => {
    selectRegion(regionName);
  }, 1200000);
}

function formatWeatherHTML(data) {
  return `
    <h3>🌍 지역: ${data.region}</h3>
    <p>🌡 수온: ${data.waterTemp}°C</p>
    <p>🧊 기온: ${data.temperature}°C</p>
    <p>💨 풍속: ${data.windSpeed} m/s</p>
    <p>❌ 돌풍: ${data.windGust} m/s</p>
    <p>🌊 파고: ${data.waveHeight} m</p>
    <p>🕒 관측시각: ${data.observedAt}</p>
  `;
}
