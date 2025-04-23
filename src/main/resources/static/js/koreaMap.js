function selectRegion(regionName) {
    fetch(`/api/weather?region=${regionName}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("날씨 정보를 가져오는 데 실패했습니다.");
            }
            return response.json();
        })
        .then(data => {
            alert(
                `[${data.region}] 해역 정보\n` +
                `🌡 수온: ${data.waterTemp}°C\n` +
                `🧊 기온: ${data.temperature}°C\n` +
                `💨 풍속: ${data.windSpeed} m/s\n` +
                `❌ 돌풍: ${data.windGust} m/s\n` +
                `🌊 파고: ${data.waveHeight} m`
            );
        })
        .catch(error => {
            console.error("에러 발생:", error);
            alert("날씨 정보를 불러오는 데 실패했습니다.");
        });
}
