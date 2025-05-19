 let routeData = null;
  let selectedVehicle = null;
  window.onload = function () {
    kakao.maps.load(() => {
      const map = new kakao.maps.Map(document.getElementById('map'), {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 5
      });

      const geocoder = new kakao.maps.services.Geocoder();
      const places = new kakao.maps.services.Places();
      let waypoints = [];
      let departure = null;
      let destination = null;
      let polyline = null;
      let tempMarker = null;

      const searchBox = document.getElementById('addressSearch');
      const suggestions = document.getElementById('searchSuggestions');
      let debounceTimer;
      searchBox.addEventListener('input', () => {
        const keyword = searchBox.value.trim();
        clearTimeout(debounceTimer);
        if (keyword.length < 2) {
          suggestions.style.display = 'none';
          return;
        }
        debounceTimer = setTimeout(() => {
          places.keywordSearch(keyword, (data, status) => {
            if (status === kakao.maps.services.Status.OK) {
              suggestions.innerHTML = '';
              data.forEach(place => {
                const div = document.createElement('div');
                div.className = 'suggestion';
                div.textContent = place.place_name;
                div.onclick = () => {
                  map.setCenter(new kakao.maps.LatLng(place.y, place.x));
                  suggestions.style.display = 'none';
                };
                suggestions.appendChild(div);
              });
              suggestions.style.display = 'block';
            }
          });
        }, 300);
      });

      window.openSearchModal = () => {
        const keyword = searchBox.value.trim();
        if (keyword.length < 2) return;
        places.keywordSearch(keyword, (data, status) => {
          if (status === kakao.maps.services.Status.OK) {
            const modal = document.getElementById('searchModal');
            modal.innerHTML = '<h3>검색 결과</h3>';
            data.forEach(place => {
              const div = document.createElement('div');
              div.textContent = place.place_name;
              div.style.cursor = 'pointer';
              div.onclick = () => {
                map.setCenter(new kakao.maps.LatLng(place.y, place.x));
                modal.style.display = 'none';
              };
              modal.appendChild(div);
            });
            modal.style.display = 'block';
          }
        });
      };

      function tryAutoFetchRoute() {
        if (departure && destination) {
          fetch(`/api/route?startX=${departure.getPosition().getLng()}&startY=${departure.getPosition().getLat()}&endX=${destination.getPosition().getLng()}&endY=${destination.getPosition().getLat()}${waypoints.length ? '&waypoints=' + waypoints.map(m => m.getPosition().getLng() + ',' + m.getPosition().getLat()).join('_') : ''}`)
            .then(res => res.json())
            .then(data => {
             routeData = data;
              if (polyline) polyline.setMap(null);
              const route = data.routes[0];
              const path = route.path.map(p => new kakao.maps.LatLng(p[1], p[0]));
              polyline = new kakao.maps.Polyline({
                path,
                strokeWeight: 5,
                strokeColor: '#007BFF',
                strokeOpacity: 0.9,
                strokeStyle: 'solid'
              });
              polyline.setMap(map);


              ///////////////////////////////////////////////////////////////////////////
  // 예상 시간, 거리, 비용 표시
  const minutes = Math.round(data.duration / 60);
  const toll = data.toll || 0;

  document.getElementById('durationText').textContent = `예상 소요 시간: 약 ${minutes}분`;
  document.getElementById('tollText').textContent = `예상 톨게이트 비용: 약 ${toll.toLocaleString()}원`;

  document.getElementById('routeInfo').style.display = 'block';
   // ✅ 연료비 계산 추가
        if (selectedVehicle) {
          calculateFuelCost(routeData, selectedVehicle).then(result => {
            if (result) {
              document.getElementById("fuelText").innerHTML =
                `🚘 선택 차량: ${carInput.value}<br>` +
                `도심연비: ${selectedVehicle.cityEff} km/L, 고속도로연비: ${selectedVehicle.highwayEff} km/L<br>` +
                `연료 종류: ${selectedVehicle.fuelType}<br><br>` +
                `예상 연료 사용량: ${result.fuelUsed}L<br>` +
                `유류 단가 (${result.fuelType}): ${result.fuelPrice.toLocaleString()}원/L<br>` +
                `<strong>예상 연료비: ${result.fuelCost.toLocaleString()}원</strong>`;
            }
          });
        }
});

        }
      }

      kakao.maps.event.addListener(map, 'rightclick', function(mouseEvent) {
        const latlng = mouseEvent.latLng;
        const containerRect = document.getElementById('map').getBoundingClientRect();
        const menuX = containerRect.left + mouseEvent.point.x;
        const menuY = containerRect.top + mouseEvent.point.y;

        if (tempMarker) tempMarker.setMap(null);
        tempMarker = new kakao.maps.Marker({ position: latlng, map });

        const menu = document.getElementById('mapContextMenu');
        menu.innerHTML = `
          <button onclick="selectPoint('departure')">출발지</button>
          <button onclick="selectPoint('waypoint')">경유지</button>
          <button onclick="selectPoint('destination')">도착지</button>
        `;
        menu.style.left = `${menuX}px`;
        menu.style.top = `${menuY}px`;
        menu.style.display = 'block';

        window.selectPoint = function(type) {
  const position = tempMarker.getPosition();
  tempMarker.setMap(null);
  tempMarker = null;
  menu.style.display = 'none';

  if (type === 'departure') {
    if (departure) departure.setMap(null);
    departure = new kakao.maps.Marker({ position, map });
    geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
      if (status === kakao.maps.services.Status.OK) {
        document.getElementById('departurePoint').value = result[0].address.address_name;
        tryAutoFetchRoute();
      }
    });
  } else if (type === 'destination') {
    if (destination) destination.setMap(null);
    destination = new kakao.maps.Marker({ position, map });
    geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
      if (status === kakao.maps.services.Status.OK) {
        document.getElementById('destination').value = result[0].address.address_name;
        tryAutoFetchRoute();
      }
    });
  }
   else if (type === 'waypoint') {
    const marker = new kakao.maps.Marker({ position, map });
     geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
          if (status === kakao.maps.services.Status.OK) {
            document.getElementById('waypoint').value = result[0].address.address_name;}});
    waypoints.push(marker);

    kakao.maps.event.addListener(marker, 'click', () => {
      if (confirm('이 마커를 삭제할까요?')) {
        marker.setMap(null);
        waypoints = waypoints.filter(m => m !== marker);
        tryAutoFetchRoute();
      }
    });
    tryAutoFetchRoute();
  }
};
      });

      document.getElementById('partyForm').addEventListener('submit', () => {
        const container = document.getElementById('waypointInputs');
        container.innerHTML = '';
        waypoints.forEach((marker, idx) => {
          const lat = marker.getPosition().getLat();
          const lng = marker.getPosition().getLng();
          container.innerHTML += `
            <input type="hidden" name="waypoints[${idx}].lat" value="${lat}" />
            <input type="hidden" name="waypoints[${idx}].lng" value="${lng}" />
            <input type="hidden" name="waypoints[${idx}].name" value="경유지${idx + 1}" />
          `;
        });
      });
    });
  }



  ////////////////////////////////////////////////////////////////////////////////////////////////
  // 연료 단가 비동기로 받아서 유류비 계산에 반영
async function calculateFuelCost(routeData, vehicleData) {
  const distanceKm = routeData.distance / 1000;
  const cityEff = vehicleData.cityEff;
  const highwayEff = vehicleData.highwayEff;
  const fuelType = vehicleData.fuelType;

  // 최신 유가 정보 fetch
  const fuelPrices = await fetch("/api/fuel-price")
    .then(res => res.json())
    .catch(() => {
      alert("유가 정보를 불러올 수 없습니다.");
      return null;
    });

  if (!fuelPrices || !fuelPrices[fuelType]) {
    alert(`'${fuelType}'에 대한 유가 정보가 없습니다.`);
    return;
  }

  const fuelPrice = fuelPrices[fuelType]; // 최신 단가 적용

  // 가중 평균 연비
  const weightedEff = routeData.highwayRatio * highwayEff + routeData.generalRatio * cityEff;
  const usedFuel = distanceKm / weightedEff;
  const fuelCost = Math.round(usedFuel * fuelPrice);

  // 결과 객체 반환
  return {
    fuelUsed: usedFuel.toFixed(2),
    fuelCost: fuelCost,
    fuelType: fuelType,
    fuelPrice: fuelPrice
  };
}
  /////////////////////////////////////////////////////////////
  //차량 자동검색 및 연비 출력
 const carInput = document.getElementById("carSearchInput");
const suggestionBox = document.getElementById("carSuggestions");


let carDebounce;
carInput.addEventListener("input", () => {
  const keyword = carInput.value.trim();
  clearTimeout(carDebounce);
  if (keyword.length < 2) {
    suggestionBox.style.display = "none";
    return;
  }

  carDebounce = setTimeout(() => {
    fetch(`/api/car/search?keyword=${encodeURIComponent(keyword)}`)
      .then(res => res.json())
      .then(data => {
        suggestionBox.innerHTML = "";
        data.forEach(fullModelName => {
          const div = document.createElement("div");
          div.textContent = fullModelName; // 예: "볼보 XC60"
          div.classList.add("suggestion");
          div.style.padding = "8px";
          div.style.cursor = "pointer";
          div.onclick = () => selectCarModel(fullModelName);
          suggestionBox.appendChild(div);
        });
        suggestionBox.style.display = "block";
      });
  }, 300);
});

function selectCarModel(modelName) {
  console.log("선택된 모델명:", modelName);
  carInput.value = modelName;
  suggestionBox.style.display = "none";

  fetch(`/api/car/model?name=${encodeURIComponent(modelName)}`)
    .then(res => {
      if (!res.ok) throw new Error("차량 정보 없음");
      return res.json();
    })
    .then(data => {
      console.log("차량 정보 받아옴:", data);
      selectedVehicle = {
        cityEff: data.cityEff,
        highwayEff: data.highwayEff,
        fuelType: data.fuelType
      };

      document.getElementById("fuelText").innerHTML =
        `🚘 선택 차량: ${modelName}<br>` +
        `도심연비: ${data.cityEff} km/L, 고속도로연비: ${data.highwayEff} km/L<br>` +
        `연료 종류: ${data.fuelType}`;

      if (routeData) {
        console.log("routeData 있음. 연료비 계산 시작");
        calculateFuelCost(routeData, selectedVehicle).then(result => {
          if (result) {
            document.getElementById("fuelText").innerHTML +=
              `<br><br>예상 연료 사용량: ${result.fuelUsed}L<br>` +
              `유류 단가 (${result.fuelType}): ${result.fuelPrice.toLocaleString()}원/L<br>` +
              `<strong>예상 연료비: ${result.fuelCost.toLocaleString()}원</strong>`;
          }
        }).catch(err => {
          console.error("연료비 계산 에러:", err);
        });
      } else {
        console.warn("routeData가 아직 null임. 지도에서 출발지/도착지를 먼저 지정해야 함.");
      }
    })
    .catch((err) => {
      console.error("🚨 차량 정보 로드 실패", err);
      alert("차량 정보 로드 실패");
    });
}