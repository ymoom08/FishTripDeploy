// modal_state.js

// ✅ 상태 객체 (어종, 지역, 날짜 선택)
const state = {
  selectedFishTypes: [],
  selectedRegions: [],
  selectedDate: null,
};

// ✅ 어종 getter/setter
export function getSelectedFishTypes() {
  return state.selectedFishTypes;
}
export function setSelectedFishTypes(types) {
  state.selectedFishTypes = types;
}

// ✅ 지역 getter/setter
export function getSelectedRegions() {
  return state.selectedRegions;
}
export function setSelectedRegions(regions) {
  state.selectedRegions = regions;
}

// ✅ 날짜는 객체 형태로 외부와 값 공유
export const selectedDate = {
  get value() {
    return state.selectedDate;
  },
  set value(val) {
    state.selectedDate = val;
  }
};
