// modal_state.js

const state = {
  selectedFishTypes: [],
  selectedRegions: [],
};

export function getSelectedFishTypes() {
  return state.selectedFishTypes;
}
export function setSelectedFishTypes(types) {
  state.selectedFishTypes = types;
}

export function getSelectedRegions() {
  return state.selectedRegions;
}
export function setSelectedRegions(regions) {
  state.selectedRegions = regions;
}
