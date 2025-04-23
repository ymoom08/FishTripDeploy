function selectRegion(regionName) {
    const status = document.getElementById("weatherStatus");

    status.innerText = `[${regionName}] 해역 정보를 불러오는 중...`;
    status.style.display = "block";

    fetch(`/api/weather?region=${regionName}`)
        .then(response => {
            if (!response.ok) throw new Error("서버 응답 오류");
            return response.json();
        })
        .then(data => {
            status.style.display = "none";

            if (data.error) throw new Error(data.error);

            const info = [
                `🌡 수온: ${data.waterTemp}°C`,
                `🧊 기온: ${data.temperature}°C`,
                `💨 풍속: ${data.windSpeed} m/s`,
                `❌ 돌풍: ${data.windGust} m/s`,
                `🌊 파고: ${data.waveHeight} m`
            ].join('\n');

            alert(`[${data.region}] 해역 정보\n${info}`);
        })
        .catch(error => {
            status.style.display = "none";
            alert(`[${regionName}] 날씨 정보를 가져오지 못했습니다.\n❗ ${error.message}`);
        });
}
