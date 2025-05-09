const map = new kakao.maps.Map(document.getElementById('map'), {
    center: new kakao.maps.LatLng(37.5665, 126.9780),
    level: 5
});

const geocoder = new kakao.maps.services.Geocoder();
let markers = [];
let polyline = null;
let waypointCoords = [];
let departureMarker = null;
let destinationMarker = null;

// 우클릭 메뉴
kakao.maps.event.addListener(map, 'rightclick', function(mouseEvent) {
    const latlng = mouseEvent.latLng;
    const menu = document.createElement('div');
    menu.style.position = 'absolute';
    menu.style.left = mouseEvent.point.x + 'px';
    menu.style.top = mouseEvent.point.y + 'px';
    menu.style.zIndex = '10';
    menu.style.background = 'white';
    menu.style.border = '1px solid black';
    menu.innerHTML = `
      <button onclick="selectPoint('departure', ${latlng.getLat()}, ${latlng.getLng()})">출발지</button>
      <button onclick="selectPoint('waypoint', ${latlng.getLat()}, ${latlng.getLng()})">경유지</button>
      <button onclick="selectPoint('destination', ${latlng.getLat()}, ${latlng.getLng()})">목적지</button>
    `;
    document.body.appendChild(menu);
    setTimeout(() => document.body.removeChild(menu), 3000);
});

window.selectPoint = function(type, lat, lng) {
    const position = new kakao.maps.LatLng(lat, lng);

    if (type === 'departure') {
        if (departureMarker) departureMarker.setMap(null);
        departureMarker = new kakao.maps.Marker({ position, map });
        document.getElementById('departureLat').value = lat;
        document.getElementById('departureLng').value = lng;
        geocoder.coord2Address(lng, lat, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
                document.getElementById('departurePoint').value = result[0].address.address_name;
            }
        });
    } else if (type === 'destination') {
        if (destinationMarker) destinationMarker.setMap(null);
        destinationMarker = new kakao.maps.Marker({ position, map });
        document.getElementById('destinationLat').value = lat;
        document.getElementById('destinationLng').value = lng;
        geocoder.coord2Address(lng, lat, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
                document.getElementById('destination').value = result[0].address.address_name;
            }
        });
    } else if (type === 'waypoint') {
        if (markers.length >= 5) {
            alert('경유지는 최대 5개까지 가능합니다.');
            return;
        }
        const marker = new kakao.maps.Marker({ position, map });
        markers.push(marker);
        waypointCoords.push({ name: `경유지${markers.length}`, lat, lng, stayTime: 0 });
        document.getElementById('waypointsJson').value = JSON.stringify(waypointCoords);
    }
};

document.getElementById('openRouteSelect').addEventListener('click', function() {
    if (!departureMarker || !destinationMarker) {
        alert('출발지와 목적지를 먼저 설정하세요.');
        return;
    }

    // 실제 경로 검색 요청 (서버 측에 REST API 연동 시 사용)
    const waypointsParam = waypointCoords.map(wp => `${wp.lng},${wp.lat}`).join('|');
    const origin = `${departureMarker.getPosition().getLng()},${departureMarker.getPosition().getLat()}`;
    const destination = `${destinationMarker.getPosition().getLng()},${destinationMarker.getPosition().getLat()}`;

    fetch(`/api/route?startX=${departureMarker.getPosition().getLng()}&startY=${departureMarker.getPosition().getLat()}&endX=${destinationMarker.getPosition().getLng()}&endY=${destinationMarker.getPosition().getLat()}&waypoints=${waypointsParam}`)
        .then(res => res.json())
        .then(data => {
            if (polyline) polyline.setMap(null);
            const path = data.routes[0].path;
            const latlngs = path.map(pair => new kakao.maps.LatLng(pair[1], pair[0]));
            polyline = new kakao.maps.Polyline({
                path: latlngs,
                strokeWeight: 5,
                strokeColor: '#007BFF',
                strokeOpacity: 0.8,
                strokeStyle: 'solid'
            });
            polyline.setMap(map);
        })
        .catch(err => {
            console.error("경로 검색 실패", err);
            alert('경로를 불러오는 데 실패했습니다.');
        });
});

// 폼 제출 전 참가자 JSON 예시 세팅 (개발용)
document.getElementById('partyForm').addEventListener('submit', function(e) {
    const sampleMembers = [
        { username: "member1", joinedAt: new Date().toISOString() },
        { username: "member2", joinedAt: new Date().toISOString() }
    ];
    document.getElementById('partyMembersJson').value = JSON.stringify(sampleMembers);
});
