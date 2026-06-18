import { ROUTE_DEFAULTS } from '@/constants/travelConfig.js'

const MUTATIONS = {
  ADD_LANDMARK: 'ADD_LANDMARK',
  REMOVE_LANDMARK: 'REMOVE_LANDMARK',
  SET_LANDMARKS: 'SET_LANDMARKS',
  SET_FREE_TIME: 'SET_FREE_TIME',
  SET_HUMAN_SPEED: 'SET_HUMAN_SPEED',
  SET_ROUTE_INFO: 'SET_ROUTE_INFO',
  SET_ALTERNATIVE_DISTANCE: 'SET_ALTERNATIVE_DISTANCE',
  ADD_ALTERNATIVE_ROUTE: 'ADD_ALTERNATIVE_ROUTE',
  SET_SELECTED_ROUTE_INDEX: 'SET_SELECTED_ROUTE_INDEX',
  RESET_TRAVEL: 'RESET_TRAVEL'
}

const createLandmark = (coordinates, index) => ({
  id: Date.now() + Math.random(),
  coordinates,
  priority: index + 1
})

const normalizeLandmarks = (landmarks) => landmarks.map((landmark, index) => ({
  ...landmark,
  priority: index + 1
}))

const createInitialRouteInfo = () => ({
  canVisit: null,
  distanceKm: 0,
  totalSeconds: 0,
  message: 'Поставьте минимум две точки на карте и рассчитайте маршрут.'
})

export default {
  namespaced: true,
  state() {
    return {
      landmarks: [],
      freeTime: ROUTE_DEFAULTS.freeTime,
      humanSpeed: ROUTE_DEFAULTS.humanSpeed,
      routeInfo: createInitialRouteInfo(),
      alternativeDistance: '',
      alternativeRoutes: [],
      selectedRouteIndex: null
    }
  },
  getters: {
    getLandmarks: (state) => state.landmarks,
    getRoutePoints: (state) => state.landmarks.map((landmark) => landmark.coordinates),
    getFreeTime: (state) => state.freeTime,
    getHumanSpeed: (state) => state.humanSpeed,
    getRouteInfo: (state) => state.routeInfo,
    getAlternativeDistance: (state) => state.alternativeDistance,
    getAlternativeRoutes: (state) => state.alternativeRoutes,
    getSelectedRouteIndex: (state) => state.selectedRouteIndex
  },
  mutations: {
    [MUTATIONS.ADD_LANDMARK]: (state, coordinates) => {
      state.landmarks = normalizeLandmarks([...state.landmarks, createLandmark(coordinates, state.landmarks.length)])
    },
    [MUTATIONS.REMOVE_LANDMARK]: (state, index) => {
      state.landmarks = normalizeLandmarks(state.landmarks.filter((landmark, landmarkIndex) => landmarkIndex !== index))
    },
    [MUTATIONS.SET_LANDMARKS]: (state, coordinatesList) => {
      state.landmarks = coordinatesList.map((coordinates, index) => createLandmark(coordinates, index))
    },
    [MUTATIONS.SET_FREE_TIME]: (state, value) => {
      state.freeTime = value >= 0 ? value : 0
    },
    [MUTATIONS.SET_HUMAN_SPEED]: (state, value) => {
      state.humanSpeed = value > 0 ? value : ROUTE_DEFAULTS.humanSpeed
    },
    [MUTATIONS.SET_ROUTE_INFO]: (state, routeInfo) => {
      state.routeInfo = routeInfo
    },
    [MUTATIONS.SET_ALTERNATIVE_DISTANCE]: (state, value) => {
      state.alternativeDistance = value
    },
    [MUTATIONS.ADD_ALTERNATIVE_ROUTE]: (state, route) => {
      state.alternativeRoutes = [...state.alternativeRoutes, route]
    },
    [MUTATIONS.SET_SELECTED_ROUTE_INDEX]: (state, index) => {
      state.selectedRouteIndex = index
    },
    [MUTATIONS.RESET_TRAVEL]: (state) => {
      state.landmarks = []
      state.routeInfo = createInitialRouteInfo()
      state.alternativeDistance = ''
      state.alternativeRoutes = []
      state.selectedRouteIndex = null
    }
  },
  actions: {
    addLandmark: (store, coordinates) => {
      store.commit(MUTATIONS.ADD_LANDMARK, coordinates)
    },
    removeLandmark: (store, index) => {
      store.commit(MUTATIONS.REMOVE_LANDMARK, index)
    },
    setLandmarks: (store, coordinatesList) => {
      store.commit(MUTATIONS.SET_LANDMARKS, coordinatesList)
    },
    setFreeTime: (store, value) => {
      store.commit(MUTATIONS.SET_FREE_TIME, Number(value))
    },
    setHumanSpeed: (store, value) => {
      store.commit(MUTATIONS.SET_HUMAN_SPEED, Number(value))
    },
    setRouteInfo: (store, routeInfo) => {
      store.commit(MUTATIONS.SET_ROUTE_INFO, routeInfo)
    },
    setAlternativeDistance: (store, value) => {
      store.commit(MUTATIONS.SET_ALTERNATIVE_DISTANCE, value)
    },
    addAlternativeRoute: (store, route) => {
      store.commit(MUTATIONS.ADD_ALTERNATIVE_ROUTE, route)
    },
    setSelectedRouteIndex: (store, index) => {
      store.commit(MUTATIONS.SET_SELECTED_ROUTE_INDEX, index)
    },
    resetTravel: (store) => {
      store.commit(MUTATIONS.RESET_TRAVEL)
    }
  }
}
