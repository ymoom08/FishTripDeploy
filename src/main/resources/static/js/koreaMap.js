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
        if (data && data.observedAt && !data.error) {
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

function formatWeatherHTML(data) {
  const waterTempHTML =
    data.waterTemp !== undefined &&
    data.waterTemp !== "" &&
    data.waterTemp !== "-" &&
    data.waterTemp !== "null"
      ? `<p>🌊 수온: ${data.waterTemp}°C</p>`
      : "<p>🌊 수온 정보 없음</p>";

  return `
    <h3>🌍 ${data.region}</h3>
    <p>🌡 기온: ${data.temperature}°C</p>
    <p>💨 풍속: ${data.windSpeed} m/s</p>
    <p>💧 습도: ${data.humidity}%</p>
    ${waterTempHTML}
    <p>☁️ 하늘: ${data.sky}</p>
    <p>🌧 형태: ${data.precipType}</p>
    <p>🌧 강수량: ${data.precipitation}</p>
    <p>🕒 관측시각: ${data.observedAt}</p>
  `;
}
