<template>
  <main class="c-map-page" :style="themeStyle">
    <section class="c-map-page__panel">
      <header class="c-map-page__header">
        <div class="c-map-page__brandBlock">
          <p class="c-map-page__eyebrow">Moscow Sightseeing Guide</p>
          <h1 class="c-map-page__title">Достопримечательности Москвы</h1>
        </div>

        <RouterLink class="c-map-page__back" :to="{ name: $routes.INDEX }">
          <span class="c-map-page__backIcon">←</span>
          <span class="c-map-page__backText">Старт</span>
        </RouterLink>
      </header>

      <div class="c-map-page__summary">
        <div class="c-map-page__summaryItem">
          <span class="c-map-page__summaryValue">{{ routePoints.length }}</span>
          <span class="c-map-page__summaryLabel">точек</span>
        </div>
        <div class="c-map-page__summaryItem">
          <span class="c-map-page__summaryValue">{{ freeTime }}</span>
          <span class="c-map-page__summaryLabel">часа</span>
        </div>
        <div class="c-map-page__summaryItem">
          <span class="c-map-page__summaryValue">{{ humanSpeed }}</span>
          <span class="c-map-page__summaryLabel">км/ч</span>
        </div>
      </div>

      <div class="c-map-page__toolRow">
        <button class="c-map-page__infoToggle" type="button" @click="() => toggleInfo()">
          <span class="c-map-page__infoToggleText">{{ sideOpen ? 'Скрыть подсказки' : 'Подсказки маршрута' }}</span>
          <span class="c-map-page__infoToggleIcon">{{ sideOpen ? 'x' : 'i' }}</span>
        </button>

        <button class="c-map-page__settingsBtn" type="button" @click="() => toggleSettings()">
          <span class="c-map-page__settingsIcon">?</span>
        </button>
      </div>

      <div v-if="sideOpen" class="c-map-page__info">
        <button class="c-map-page__infoClose" type="button" @click="() => toggleInfo()">+</button>
        <h2 class="c-map-page__infoTitle">Как успеть посмотреть больше</h2>
        <p class="c-map-page__infoText">ЛКМ по карте добавляет достопримечательность в план прогулки.</p>
        <p class="c-map-page__infoText">ПКМ по метке убирает место из маршрута.</p>
        <p class="c-map-page__infoText">Цель страницы - помочь успеть посетить максимум интересных мест за доступное время. В будущем сюда можно добавить покупку билетов и бронирование экскурсий.</p>
      </div>

      <div class="c-map-page__controls">
        <div class="c-map-page__fieldGrid">
          <label class="c-map-page__field">
            <span class="c-map-page__label">Время на прогулку</span>
            <span class="c-map-page__inputWrap">
              <input class="c-map-page__input" type="number" min="0" step="1" v-model.number="freeTimeValue">
              <span class="c-map-page__inputUnit">ч</span>
            </span>
          </label>

          <label class="c-map-page__field">
            <span class="c-map-page__label">Скорость</span>
            <span class="c-map-page__inputWrap">
              <input class="c-map-page__input" type="number" min="1" step="1" v-model.number="humanSpeedValue">
              <span class="c-map-page__inputUnit">км/ч</span>
            </span>
          </label>
        </div>

        <div class="c-map-page__buttonGrid">
          <button
            v-for="button in actionButtons"
            :key="button.method"
            class="c-map-page__button"
            :class="button.className"
            type="button"
            @click="() => runAction(button.method)"
          >
            <span class="c-map-page__buttonIcon">{{ button.icon }}</span>
            <span class="c-map-page__buttonText">{{ button.text }}</span>
          </button>
        </div>
      </div>

      <div class="c-map-page__status" :class="routeInfoClass">
        <span class="c-map-page__statusLabel">Успеешь посмотреть</span>
        <span class="c-map-page__statusText">{{ routeInfo.message }}</span>
      </div>

      <div v-if="alternativeDistance" class="c-map-page__distance">
        {{ alternativeDistance }}
      </div>

      <button
        v-if="alternativeRoutes.length"
        class="c-map-page__savedToggle"
        type="button"
        @click="() => toggleSavedRoutes()"
      >
        <span class="c-map-page__savedToggleText">Сохраненные планы</span>
        <span class="c-map-page__savedToggleCount">{{ alternativeRoutes.length }}</span>
      </button>
    </section>

    <section class="c-map-page__mapWrap">
      <div class="c-map-page__mapFrame">
        <div v-if="isMapLoading" class="c-map-page__loader">Карта загружается...</div>
        <div v-if="mapError" class="c-map-page__error">{{ mapError }}</div>
        <div class="c-map-page__mapOverlay">
          <span class="c-map-page__mapHint">Добавляй достопримечательности прямо на карте</span>
        </div>
        <div ref="map" class="c-map-page__map"></div>
      </div>
    </section>

    <section
      v-if="settingsOpen"
      ref="settingsWindow"
      class="c-map-page__settingsWindow"
      :style="settingsPanelStyle"
    >
      <header class="c-map-page__settingsHead" @mousedown="(event) => startSettingsDrag(event)">
        <div>
          <span class="c-map-page__settingsKicker">visual settings</span>
          <h2 class="c-map-page__settingsTitle">Оформление карты</h2>
        </div>
        <button class="c-map-page__settingsClose" type="button" @click="() => closeSettings()">×</button>
      </header>

      <div class="c-map-page__settingsBody">
        <p class="c-map-page__settingsText">Выбери настроение интерфейса. Это меняет акцентные цвета панели, не ломая карту и маршруты.</p>

        <div class="c-map-page__themeList">
          <button
            v-for="(theme, index) in themes"
            :key="theme.name"
            class="c-map-page__themeBtn"
            :class="{ 'c-map-page__themeBtn--active': selectedThemeIndex === index }"
            type="button"
            @click="() => selectTheme(index)"
          >
            <span class="c-map-page__themeSwatch" :style="{ background: theme.accent }"></span>
            <span class="c-map-page__themeName">{{ theme.name }}</span>
          </button>
        </div>

        <div class="c-map-page__futureBox">
          <span class="c-map-page__futureLabel">Будущая функция</span>
          <span class="c-map-page__futureText">Билеты, расписания музеев и подбор экскурсий рядом с выбранным маршрутом.</span>
        </div>
      </div>
    </section>

    <section
      v-if="savedRoutesOpen && alternativeRoutes.length"
      ref="savedRoutesWindow"
      class="c-map-page__savedWindow"
      :style="savedRoutesPanelStyle"
    >
      <header class="c-map-page__savedHead" @mousedown="(event) => startSavedRoutesDrag(event)">
        <div>
          <span class="c-map-page__savedKicker">route archive</span>
          <h2 class="c-map-page__savedTitle">Сохраненные планы</h2>
        </div>
        <button class="c-map-page__savedClose" type="button" @click="() => closeSavedRoutes()">×</button>
      </header>

      <div class="c-map-page__saved">
        <button
          v-for="(route, index) in alternativeRoutes"
          :key="route.id"
          class="c-map-page__savedBtn"
          :class="{ 'c-map-page__savedBtn--active': selectedRouteIndex === index }"
          type="button"
          @click="() => selectAlternativeRoute(index)"
        >
          <span class="c-map-page__savedName">{{ route.title }}</span>
          <span class="c-map-page__savedMeta">{{ route.timeText }} · {{ route.distanceText }}</span>
        </button>
      </div>
    </section>
  </main>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import { MAP_COLORS, ROUTE_DEFAULTS } from '@/constants/travelConfig.js'
import { TRAVEL_MARKS } from '@/constants/travelMarks.js'
import {
  createDijkstraRoute,
  createRotatedRoute,
  createRouteResult,
  findNearestUnusedMark,
  getRoadRoute,
  parseHumanDistance
} from '@/utils/travelRoute.js'

export default {
  name: 'MapPage',
  data() {
    return {
      sideOpen: false,
      isMapLoading: true,
      mapError: '',
      settingsOpen: false,
      selectedThemeIndex: 0,
      settingsPosition: {
        x: 430,
        y: 88
      },
      settingsDrag: null,
      savedRoutesOpen: false,
      savedRoutesPosition: {
        x: 430,
        y: 90
      },
      savedRoutesDrag: null,
      themes: [
        {
          name: 'Мятный вечер',
          accent: '#6df2cd',
          warm: '#ffb15e',
          panel: 'rgba(9, 12, 11, 0.88)'
        },
        {
          name: 'Кремлевский янтарь',
          accent: '#ffbd5a',
          warm: '#ff6f61',
          panel: 'rgba(18, 12, 8, 0.88)'
        },
        {
          name: 'Ночная Нева',
          accent: '#8eb7ff',
          warm: '#c7ff6d',
          panel: 'rgba(8, 11, 20, 0.88)'
        }
      ],
      actionButtons: [
        {
          method: 'calculateRoute',
          text: 'Рассчитать маршрут',
          icon: '01',
          className: 'c-map-page__button--primary'
        },
        {
          method: 'showAlternativeRoute',
          text: 'Альтернативный план',
          icon: 'A',
          className: ''
        },
        {
          method: 'showDijkstraRoute',
          text: 'Маршрут Дейкстры',
          icon: 'D',
          className: ''
        },
        {
          method: 'optimizeRoutePlus',
          text: 'Добавить точку умно',
          icon: '+',
          className: ''
        },
        {
          method: 'optimizeRouteMinus',
          text: 'Сократить маршрут',
          icon: '−',
          className: ''
        },
        {
          method: 'resetMap',
          text: 'Очистить карту',
          icon: '×',
          className: 'c-map-page__button--ghost'
        }
      ]
    }
  },
  computed: {
    ...mapGetters('travel', {
      landmarks: 'getLandmarks',
      routePoints: 'getRoutePoints',
      freeTime: 'getFreeTime',
      humanSpeed: 'getHumanSpeed',
      routeInfo: 'getRouteInfo',
      alternativeDistance: 'getAlternativeDistance',
      alternativeRoutes: 'getAlternativeRoutes',
      selectedRouteIndex: 'getSelectedRouteIndex'
    }),
    freeTimeValue: {
      get() {
        return this.freeTime
      },
      set(value) {
        this.setFreeTime(value)
      }
    },
    humanSpeedValue: {
      get() {
        return this.humanSpeed
      },
      set(value) {
        this.setHumanSpeed(value)
      }
    },
    routeInfoClass() {
      return {
        'c-map-page__status--success': this.routeInfo.canVisit === true,
        'c-map-page__status--warning': this.routeInfo.canVisit === false
      }
    },
    savedRoutesPanelStyle() {
      return {
        left: `${this.savedRoutesPosition.x}px`,
        top: `${this.savedRoutesPosition.y}px`
      }
    },
    settingsPanelStyle() {
      return {
        left: `${this.settingsPosition.x}px`,
        top: `${this.settingsPosition.y}px`
      }
    },
    selectedTheme() {
      return this.themes[this.selectedThemeIndex]
    },
    themeStyle() {
      return {
        '--c-map-accent': this.selectedTheme.accent,
        '--c-map-warm': this.selectedTheme.warm,
        '--c-map-panel': this.selectedTheme.panel
      }
    }
  },
  mounted() {
    this.loadYandexApi()
      .then(() => this.initMap())
      .catch(() => {
        this.isMapLoading = false
        this.mapError = 'Не удалось загрузить Яндекс.Карты. Проверьте интернет и API-ключ.'
      })
  },
  beforeUnmount() {
    window.removeEventListener('mousemove', this.onSavedRoutesDrag)
    window.removeEventListener('mouseup', this.stopSavedRoutesDrag)
    window.removeEventListener('mousemove', this.onSettingsDrag)
    window.removeEventListener('mouseup', this.stopSettingsDrag)

    if (this._map) {
      this._map.destroy()
    }
  },
  methods: {
    ...mapActions('travel', [
      'addLandmark',
      'removeLandmark',
      'setLandmarks',
      'setFreeTime',
      'setHumanSpeed',
      'setRouteInfo',
      'setAlternativeDistance',
      'addAlternativeRoute',
      'setSelectedRouteIndex',
      'resetTravel'
    ]),
    runAction(methodName) {
      this[methodName]()
    },
    toggleInfo() {
      this.sideOpen = !this.sideOpen
    },
    toggleSettings() {
      this.settingsOpen = !this.settingsOpen
    },
    closeSettings() {
      this.settingsOpen = false
    },
    selectTheme(index) {
      this.selectedThemeIndex = index
    },
    startSettingsDrag(event) {
      if (event.target.closest('.c-map-page__settingsClose')) {
        return
      }

      this.settingsDrag = {
        shiftX: event.clientX - this.settingsPosition.x,
        shiftY: event.clientY - this.settingsPosition.y
      }

      window.addEventListener('mousemove', this.onSettingsDrag)
      window.addEventListener('mouseup', this.stopSettingsDrag)
    },
    onSettingsDrag(event) {
      if (!this.settingsDrag) {
        return
      }

      this.settingsPosition = this.getLimitedPanelPosition(
        event.clientX - this.settingsDrag.shiftX,
        event.clientY - this.settingsDrag.shiftY,
        this.$refs.settingsWindow,
        360,
        390
      )
    },
    stopSettingsDrag() {
      this.settingsDrag = null
      window.removeEventListener('mousemove', this.onSettingsDrag)
      window.removeEventListener('mouseup', this.stopSettingsDrag)
    },
    toggleSavedRoutes() {
      this.savedRoutesOpen = !this.savedRoutesOpen
    },
    closeSavedRoutes() {
      this.savedRoutesOpen = false
    },
    startSavedRoutesDrag(event) {
      if (event.target.closest('.c-map-page__savedClose')) {
        return
      }

      this.savedRoutesDrag = {
        shiftX: event.clientX - this.savedRoutesPosition.x,
        shiftY: event.clientY - this.savedRoutesPosition.y
      }

      window.addEventListener('mousemove', this.onSavedRoutesDrag)
      window.addEventListener('mouseup', this.stopSavedRoutesDrag)
    },
    onSavedRoutesDrag(event) {
      if (!this.savedRoutesDrag) {
        return
      }

      this.savedRoutesPosition = this.getLimitedSavedRoutesPosition(
        event.clientX - this.savedRoutesDrag.shiftX,
        event.clientY - this.savedRoutesDrag.shiftY
      )
    },
    stopSavedRoutesDrag() {
      this.savedRoutesDrag = null
      window.removeEventListener('mousemove', this.onSavedRoutesDrag)
      window.removeEventListener('mouseup', this.stopSavedRoutesDrag)
    },
    getLimitedSavedRoutesPosition(x, y) {
      return this.getLimitedPanelPosition(x, y, this.$refs.savedRoutesWindow, 460, 420)
    },
    getLimitedPanelPosition(x, y, panel, defaultWidth, defaultHeight) {
      const width = panel ? panel.offsetWidth : defaultWidth
      const height = panel ? panel.offsetHeight : defaultHeight
      const maxX = Math.max(12, window.innerWidth - width - 12)
      const maxY = Math.max(12, window.innerHeight - height - 12)

      return {
        x: Math.min(Math.max(12, x), maxX),
        y: Math.min(Math.max(12, y), maxY)
      }
    },
    loadYandexApi() {
      if (window.ymaps) {
        return Promise.resolve()
      }

      return new Promise((resolve, reject) => {
        let attempts = 0
        const timerId = window.setInterval(() => {
          attempts += 1

          if (window.ymaps) {
            window.clearInterval(timerId)
            resolve()
            return
          }

          if (attempts > 80) {
            window.clearInterval(timerId)
            reject()
          }
        }, 100)
      })
    },
    initMap() {
      window.ymaps.ready(() => {
        this._map = new window.ymaps.Map(this.$refs.map, {
          center: ROUTE_DEFAULTS.center,
          zoom: ROUTE_DEFAULTS.zoom,
          controls: ['zoomControl', 'searchControl', 'typeSelector']
        })

        this._map.events.add('click', (event) => this.handleMapClick(event))
        this.isMapLoading = false
        this.refreshMapObjects()
      })
    },
    handleMapClick(event) {
      const coords = event.get('coords')
      this.addLandmark(coords)
      this.refreshMapObjects()
    },
    removeMapPoint(index) {
      this.removeLandmark(index)
      this.refreshMapObjects()
    },
    validateRoute() {
      if (this.routePoints.length >= 2) {
        return true
      }

      this.setRouteInfo({
        canVisit: null,
        distanceKm: 0,
        totalSeconds: 0,
        message: 'Для прогулки нужно выбрать минимум две достопримечательности.'
      })

      return false
    },
    refreshMapObjects(points = this.routePoints) {
      if (!this._map) {
        return
      }

      this._map.geoObjects.removeAll()
      this.renderPlacemarks(points)
    },
    renderPlacemarks(points) {
      points.forEach((coords, index) => {
        const placemark = new window.ymaps.Placemark(coords, {
          iconContent: String(index + 1)
        }, {
          preset: 'islands#blueStretchyIcon'
        })

        placemark.events.add('contextmenu', (event) => {
          event.preventDefault()
          this.removeMapPoint(index)
        })

        this._map.geoObjects.add(placemark)
      })
    },
    getYandexPoints(points) {
      return points.map((point) => [Number(point[0]), Number(point[1])])
    },
    buildRoute(points, color) {
      this._map.geoObjects.removeAll()
      const yandexPoints = this.getYandexPoints(points)

      return getRoadRoute(yandexPoints).then((roadRoute) => {
        const polyline = new window.ymaps.Polyline(roadRoute.points, {}, {
          strokeColor: color,
          strokeWidth: 5,
          strokeOpacity: 0.84
        })

        this._map.geoObjects.add(polyline)
        this.renderPlacemarks(yandexPoints)
        this._map.setBounds(polyline.geometry.getBounds(), {
          checkZoomRange: true,
          zoomMargin: 40
        })

        return roadRoute
      })
    },
    buildLocalRoute(points, color) {
      this._map.geoObjects.removeAll()
      const yandexPoints = this.getYandexPoints(points)

      const polyline = new window.ymaps.Polyline(yandexPoints, {}, {
        strokeColor: color,
        strokeWidth: 5,
        strokeOpacity: 0.72
      })

      this._map.geoObjects.add(polyline)
      this.renderPlacemarks(yandexPoints)
      this._map.setBounds(polyline.geometry.getBounds(), {
        checkZoomRange: true,
        zoomMargin: 40
      })
    },
    getRouteErrorText(error) {
      if (!error) {
        return 'Яндекс.Карты не вернули подробность ошибки.'
      }

      if (typeof error === 'string') {
        return error
      }

      return error.message || error.statusText || JSON.stringify(error)
    },
    getRouteDistance(route) {
      if (route && typeof route.distanceKm === 'number') {
        return route.distanceKm
      }

      if (route && typeof route.getLength === 'function') {
        return route.getLength() / 1000
      }

      if (route && typeof route.getHumanLength === 'function') {
        return parseHumanDistance(route.getHumanLength())
      }

      return null
    },
    saveRoute(points, info, title) {
      const routeKey = JSON.stringify(points)
      const isSaved = this.alternativeRoutes.some((route) => route.key === routeKey && route.title === title)

      if (isSaved) {
        return
      }

      this.addAlternativeRoute({
        id: Date.now() + Math.random(),
        key: routeKey,
        title,
        points,
        timeText: info.timeText,
        distanceText: info.distanceText
      })
    },
    drawRoute(points, color, title = '') {
      if (!this._map || points.length < 2) {
        return Promise.resolve(null)
      }

      return this.buildRoute(points, color)
        .then((route) => {
          const routeDistance = this.getRouteDistance(route)
          const info = createRouteResult(
            points,
            this.freeTime,
            this.humanSpeed,
            ROUTE_DEFAULTS.visitAverageMinutes,
            routeDistance
          )

          this.setRouteInfo(info)

          if (title) {
            this.saveRoute(points, info, title)
          }

          return info
        })
        .catch((error) => {
          const info = createRouteResult(points, this.freeTime, this.humanSpeed, ROUTE_DEFAULTS.visitAverageMinutes)
          const errorText = this.getRouteErrorText(error)
          this.buildLocalRoute(points, color)
          this.setRouteInfo({
            ...info,
            canVisit: false,
            message: `Не удалось построить маршрут по дорогам, поэтому показана локальная линия по точкам. Ошибка: ${errorText}`
          })

          if (title) {
            this.saveRoute(points, info, title)
          }

          return info
        })
    },
    calculateRoute() {
      if (!this.validateRoute()) {
        return
      }

      this.setAlternativeDistance('')
      this.drawRoute(this.routePoints, MAP_COLORS.main, 'Основной план прогулки')
    },
    showAlternativeRoute() {
      if (!this.validateRoute()) {
        return
      }

      const points = createRotatedRoute(this.routePoints)
      this.drawRoute(points, MAP_COLORS.alternative, 'Альтернативный план')
        .then((info) => {
          if (info) {
            this.setAlternativeDistance(`Дистанция альтернативного плана: ${info.distanceText}`)
          }
        })
    },
    showDijkstraRoute() {
      if (!this.validateRoute()) {
        return
      }

      const points = createDijkstraRoute(this.routePoints)
      this.drawRoute(points, MAP_COLORS.dijkstra, 'Маршрут Дейкстры')
    },
    optimizeRoutePlus() {
      if (!this.validateRoute()) {
        return
      }

      const mark = findNearestUnusedMark(this.routePoints, TRAVEL_MARKS)

      if (!mark) {
        this.setRouteInfo({
          canVisit: null,
          distanceKm: 0,
          totalSeconds: 0,
          message: 'Нет свободных дополнительных достопримечательностей для оптимизации.'
        })
        return
      }

      this.addLandmark(mark)
      this.$nextTick(() => this.calculateRoute())
    },
    optimizeRouteMinus() {
      if (this.routePoints.length <= 2) {
        this.setRouteInfo({
          canVisit: null,
          distanceKm: 0,
          totalSeconds: 0,
          message: 'Нельзя удалить место: для прогулки нужны минимум две достопримечательности.'
        })
        return
      }

      this.removeLandmark(this.routePoints.length - 1)
      this.$nextTick(() => this.calculateRoute())
    },
    selectAlternativeRoute(index) {
      const route = this.alternativeRoutes[index]

      if (!route) {
        return
      }

      this.setSelectedRouteIndex(index)
      this.setLandmarks(route.points)
      this.$nextTick(() => {
        this.drawRoute(route.points, MAP_COLORS.alternative)
      })
    },
    resetMap() {
      this.resetTravel()
      this.refreshMapObjects()
    }
  }
}
</script>

<style lang="scss">
.c-map-page {
  --c-map-bg: #050706;
  --c-map-card: rgba(255, 255, 255, 0.07);
  --c-map-line: rgba(240, 231, 210, 0.16);
  --c-map-text: #f7eedc;
  --c-map-muted: rgba(247, 238, 220, 0.64);
  --c-map-accent: #6df2cd;
  --c-map-warm: #ffb15e;
  display: grid;
  grid-template-columns: minmax(320px, 390px) minmax(0, 1fr);
  height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 12% 16%, rgba(109, 242, 205, 0.18), transparent 28%),
    radial-gradient(circle at 90% 6%, rgba(255, 177, 94, 0.16), transparent 22%),
    linear-gradient(140deg, #101315 0%, #050706 66%);
  color: var(--c-map-text);
  font-family: Arial, sans-serif;

  &__panel {
    position: relative;
    z-index: 4;
    display: flex;
    flex-direction: column;
    gap: 12px;
    height: 100vh;
    padding: 14px 16px;
    overflow-x: hidden;
    overflow-y: auto;
    border-right: 1px solid var(--c-map-line);
    background:
      linear-gradient(180deg, var(--c-map-panel), rgba(9, 12, 11, 0.84)),
      radial-gradient(circle at 30% 0%, rgba(109, 242, 205, 0.16), transparent 34%);
    box-shadow: 24px 0 80px rgba(0, 0, 0, 0.34);
    backdrop-filter: blur(18px);
  }

  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  &__brandBlock {
    min-width: 0;
  }

  &__eyebrow {
    margin: 0 0 8px;
    color: var(--c-map-accent);
    font-size: 11px;
    font-weight: 800;
    letter-spacing: 0.2em;
    text-transform: uppercase;
  }

  &__title {
    max-width: 270px;
    margin: 0;
    font-family: Georgia, serif;
    font-size: clamp(30px, 3vw, 42px);
    line-height: 0.95;
    letter-spacing: -0.06em;
  }

  &__back {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid var(--c-map-line);
    border-radius: 999px;
    color: var(--c-map-text);
    text-decoration: none;
    transition: all 0.22s ease;

    &:hover {
      border-color: var(--c-map-accent);
      background: rgba(109, 242, 205, 0.12);
      transform: translateY(-2px);
    }
  }

  &__backIcon {
    color: var(--c-map-accent);
  }

  &__backText {
    font-size: 13px;
    font-weight: 800;
  }

  &__summary {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  &__summaryItem {
    display: grid;
    gap: 4px;
    padding: 10px;
    border: 1px solid var(--c-map-line);
    border-radius: 18px;
    background: var(--c-map-card);
  }

  &__summaryValue {
    color: var(--c-map-accent);
    font-family: Georgia, serif;
    font-size: 22px;
    line-height: 1;
  }

  &__summaryLabel {
    color: var(--c-map-muted);
    font-size: 11px;
    text-transform: uppercase;
  }

  &__infoToggle,
  &__settingsBtn,
  &__button,
  &__savedToggle,
  &__savedBtn,
  &__themeBtn {
    border: 1px solid var(--c-map-line);
    background: rgba(255, 255, 255, 0.06);
    color: var(--c-map-text);
    cursor: pointer;
    transition: all 0.22s ease;

    &:hover {
      border-color: rgba(109, 242, 205, 0.7);
      background: rgba(109, 242, 205, 0.12);
      transform: translateY(-2px);
    }
  }

  &__infoToggle {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 48px;
    padding: 0 10px 0 16px;
    border-radius: 16px;
    font-weight: 800;
  }

  &__infoToggleIcon {
    display: grid;
    place-items: center;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: var(--c-map-accent);
    color: #06100d;
  }

  &__toolRow {
    display: grid;
    grid-template-columns: 1fr 48px;
    gap: 8px;
  }

  &__settingsBtn {
    display: grid;
    place-items: center;
    min-height: 48px;
    border-radius: 16px;
  }

  &__settingsIcon {
    display: grid;
    place-items: center;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: var(--c-map-warm);
    color: #06100d;
    font-size: 16px;
    transition: all 0.22s ease;
  }

  &__settingsBtn:hover &__settingsIcon {
    transform: rotate(45deg);
  }

  &__info {
    position: absolute;
    left: 20px;
    right: 20px;
    top: 172px;
    z-index: 5;
    padding: 24px;
    border: 1px solid rgba(109, 242, 205, 0.34);
    border-radius: 24px;
    background: rgba(246, 238, 220, 0.96);
    color: #0a0f0e;
    box-shadow: 0 24px 70px rgba(0, 0, 0, 0.42);
    animation: c-map-panel-in 0.24s ease;
  }

  &__infoClose {
    position: absolute;
    top: 8px;
    right: 12px;
    border: none;
    background: transparent;
    color: #0a0f0e;
    cursor: pointer;
    font-size: 30px;
    transform: rotate(45deg);
  }

  &__infoTitle {
    margin: 0 0 12px;
    font-family: Georgia, serif;
    font-size: 24px;
    letter-spacing: -0.04em;
  }

  &__infoText {
    margin: 8px 0;
    color: rgba(10, 15, 14, 0.72);
    line-height: 1.45;
  }

  &__controls {
    display: flex;
    flex-direction: column;
    gap: 10px;
    min-height: auto;
    overflow: visible;
  }

  &__fieldGrid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  &__field {
    display: grid;
    gap: 8px;
  }

  &__label {
    color: var(--c-map-muted);
    font-size: 12px;
    font-weight: 700;
  }

  &__inputWrap {
    position: relative;
    display: block;
  }

  &__input {
    width: 100%;
    min-height: 42px;
    padding: 0 42px 0 14px;
    border: 1px solid rgba(247, 238, 220, 0.2);
    border-radius: 16px;
    outline: none;
    background: rgba(247, 238, 220, 0.94);
    color: #0a0f0e;
    font-size: 18px;
    font-weight: 800;
    transition: all 0.22s ease;

    &:focus {
      border-color: var(--c-map-accent);
      box-shadow: 0 0 0 4px rgba(109, 242, 205, 0.16);
    }
  }

  &__inputUnit {
    position: absolute;
    top: 50%;
    right: 12px;
    color: rgba(10, 15, 14, 0.48);
    font-size: 12px;
    font-weight: 800;
    transform: translateY(-50%);
  }

  &__buttonGrid {
    display: grid;
    gap: 8px;
  }

  &__button {
    position: relative;
    display: grid;
    grid-template-columns: 30px 1fr;
    align-items: center;
    gap: 10px;
    min-height: 44px;
    padding: 7px 12px;
    border-radius: 16px;
    overflow: hidden;
    text-align: left;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      opacity: 0;
      background: linear-gradient(90deg, rgba(109, 242, 205, 0.22), transparent);
      transition: all 0.22s ease;
    }

    &:hover::before {
      opacity: 1;
    }

    &--primary {
      border-color: rgba(109, 242, 205, 0.62);
      background: linear-gradient(120deg, rgba(109, 242, 205, 0.2), rgba(255, 177, 94, 0.1));
    }

    &--ghost {
      border-color: rgba(255, 112, 112, 0.34);
      color: rgba(255, 216, 216, 0.86);
    }
  }

  &__buttonIcon,
  &__buttonText {
    position: relative;
    z-index: 1;
  }

  &__buttonIcon {
    display: grid;
    place-items: center;
    width: 30px;
    height: 30px;
    border-radius: 12px;
    background: rgba(247, 238, 220, 0.1);
    color: var(--c-map-accent);
    font-size: 12px;
    font-weight: 900;
  }

  &__buttonText {
    font-weight: 800;
  }

  &__status,
  &__distance,
  &__savedToggle,
  &__saved {
    border: 1px solid var(--c-map-line);
    border-radius: 18px;
    background: var(--c-map-card);
  }

  &__status {
    display: grid;
    gap: 7px;
    padding: 14px;
    min-height: 84px;

    &--success {
      border-color: rgba(109, 242, 205, 0.42);
      background: rgba(49, 208, 132, 0.13);
    }

    &--warning {
      border-color: rgba(255, 112, 112, 0.38);
      background: rgba(255, 93, 93, 0.14);
    }
  }

  &__statusLabel {
    color: var(--c-map-muted);
    font-size: 11px;
    font-weight: 900;
    letter-spacing: 0.14em;
    text-transform: uppercase;
  }

  &__statusText {
    color: rgba(247, 238, 220, 0.9);
    font-size: 13px;
    line-height: 1.45;
  }

  &__distance {
    padding: 12px 14px;
    color: rgba(247, 238, 220, 0.86);
    font-size: 13px;
    line-height: 1.35;
  }

  &__savedToggle {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 48px;
    padding: 0 12px 0 16px;
    font-weight: 900;
  }

  &__savedToggleCount {
    display: grid;
    place-items: center;
    min-width: 30px;
    height: 30px;
    padding: 0 8px;
    border-radius: 999px;
    background: var(--c-map-warm);
    color: #0a0f0e;
  }

  &__settingsWindow {
    position: fixed;
    z-index: 21;
    width: min(360px, calc(100vw - 24px));
    overflow: hidden;
    border: 1px solid rgba(247, 238, 220, 0.22);
    border-radius: 26px;
    background:
      linear-gradient(145deg, rgba(8, 16, 14, 0.86), rgba(8, 16, 14, 0.66)),
      radial-gradient(circle at 12% 0%, rgba(109, 242, 205, 0.2), transparent 40%);
    box-shadow: 0 30px 90px rgba(0, 0, 0, 0.44);
    backdrop-filter: blur(18px);
    animation: c-map-panel-in 0.24s ease;
  }

  &__settingsHead {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    padding: 18px 18px 12px;
    border-bottom: 1px solid rgba(247, 238, 220, 0.12);
    cursor: grab;
    user-select: none;

    &:active {
      cursor: grabbing;
    }
  }

  &__settingsKicker {
    display: block;
    margin-bottom: 6px;
    color: var(--c-map-accent);
    font-size: 10px;
    font-weight: 900;
    letter-spacing: 0.18em;
    text-transform: uppercase;
  }

  &__settingsTitle {
    margin: 0;
    font-family: Georgia, serif;
    font-size: 24px;
    line-height: 1;
    letter-spacing: -0.04em;
  }

  &__settingsClose {
    display: grid;
    place-items: center;
    width: 36px;
    height: 36px;
    border: 1px solid rgba(247, 238, 220, 0.18);
    border-radius: 50%;
    background: rgba(247, 238, 220, 0.1);
    color: var(--c-map-text);
    cursor: pointer;
    font-size: 24px;
    line-height: 1;
    transition: all 0.22s ease;

    &:hover {
      border-color: rgba(255, 112, 112, 0.52);
      background: rgba(255, 112, 112, 0.18);
      transform: rotate(90deg);
    }
  }

  &__settingsBody {
    display: grid;
    gap: 14px;
    padding: 14px;
  }

  &__settingsText,
  &__futureText {
    margin: 0;
    color: var(--c-map-muted);
    font-size: 13px;
    line-height: 1.45;
  }

  &__themeList {
    display: grid;
    gap: 8px;
  }

  &__themeBtn {
    display: grid;
    grid-template-columns: 26px 1fr;
    align-items: center;
    gap: 10px;
    min-height: 44px;
    padding: 8px 10px;
    border-radius: 16px;
    text-align: left;

    &--active {
      border-color: var(--c-map-accent);
      background: rgba(109, 242, 205, 0.14);
    }
  }

  &__themeSwatch {
    width: 26px;
    height: 26px;
    border: 2px solid rgba(247, 238, 220, 0.34);
    border-radius: 50%;
  }

  &__themeName {
    font-weight: 900;
  }

  &__futureBox {
    display: grid;
    gap: 6px;
    padding: 12px;
    border: 1px solid rgba(255, 177, 94, 0.24);
    border-radius: 18px;
    background: rgba(255, 177, 94, 0.08);
  }

  &__futureLabel {
    color: var(--c-map-warm);
    font-size: 11px;
    font-weight: 900;
    letter-spacing: 0.12em;
    text-transform: uppercase;
  }

  &__savedWindow {
    position: fixed;
    z-index: 20;
    display: grid;
    grid-template-rows: auto minmax(0, 1fr);
    width: min(520px, calc(100vw - 24px));
    max-height: min(520px, calc(100vh - 24px));
    overflow: hidden;
    border: 1px solid rgba(247, 238, 220, 0.22);
    border-radius: 28px;
    background:
      linear-gradient(145deg, rgba(8, 16, 14, 0.84), rgba(8, 16, 14, 0.62)),
      radial-gradient(circle at 12% 0%, rgba(109, 242, 205, 0.22), transparent 36%);
    box-shadow: 0 30px 90px rgba(0, 0, 0, 0.44);
    backdrop-filter: blur(18px);
    animation: c-map-panel-in 0.24s ease;
  }

  &__savedHead {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    padding: 18px 18px 12px;
    border-bottom: 1px solid rgba(247, 238, 220, 0.12);
    cursor: grab;
    user-select: none;

    &:active {
      cursor: grabbing;
    }
  }

  &__savedKicker {
    display: block;
    margin-bottom: 6px;
    color: var(--c-map-accent);
    font-size: 10px;
    font-weight: 900;
    letter-spacing: 0.18em;
    text-transform: uppercase;
  }

  &__savedClose {
    display: grid;
    place-items: center;
    width: 36px;
    height: 36px;
    border: 1px solid rgba(247, 238, 220, 0.18);
    border-radius: 50%;
    background: rgba(247, 238, 220, 0.1);
    color: var(--c-map-text);
    cursor: pointer;
    font-size: 24px;
    line-height: 1;
    transition: all 0.22s ease;

    &:hover {
      border-color: rgba(255, 112, 112, 0.52);
      background: rgba(255, 112, 112, 0.18);
      transform: rotate(90deg);
    }
  }

  &__saved {
    display: grid;
    gap: 10px;
    min-height: 0;
    padding: 14px;
    overflow: auto;
    border: none;
    border-radius: 0;
    background: transparent;
  }

  &__savedTitle {
    margin: 0;
    font-family: Georgia, serif;
    font-size: 24px;
    line-height: 1;
    letter-spacing: -0.04em;
  }

  &__savedBtn {
    display: grid;
    gap: 5px;
    padding: 14px;
    border-radius: 18px;
    text-align: left;

    &--active {
      border-color: var(--c-map-accent);
      background: rgba(109, 242, 205, 0.14);
    }
  }

  &__savedName {
    font-size: 13px;
    font-weight: 900;
  }

  &__savedMeta {
    color: var(--c-map-muted);
    font-size: 12px;
  }

  &__mapWrap {
    position: relative;
    min-width: 0;
    height: 100vh;
    padding: 18px 18px 18px 0;
  }

  &__mapFrame {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    border: 1px solid rgba(247, 238, 220, 0.18);
    border-radius: 30px;
    background: #101315;
    box-shadow: 0 30px 100px rgba(0, 0, 0, 0.4);
  }

  &__map {
    width: 100%;
    height: 100%;
  }

  &__mapOverlay {
    position: absolute;
    top: 18px;
    left: 18px;
    z-index: 2;
    display: flex;
    align-items: center;
    gap: 10px;
    max-width: min(520px, calc(100% - 36px));
    padding: 9px 12px;
    border: 1px solid rgba(9, 12, 11, 0.14);
    border-radius: 999px;
    background: rgba(247, 238, 220, 0.86);
    color: #08100e;
    box-shadow: 0 12px 34px rgba(0, 0, 0, 0.18);
    pointer-events: none;
  }


  &__mapHint {
    color: rgba(8, 16, 14, 0.7);
    font-size: 13px;
    font-weight: 800;
  }

  &__loader,
  &__error {
    position: absolute;
    left: 50%;
    top: 22px;
    z-index: 3;
    transform: translateX(-50%);
    padding: 12px 18px;
    border-radius: 999px;
    background: rgba(8, 16, 14, 0.84);
    color: var(--c-map-text);
    box-shadow: 0 12px 34px rgba(0, 0, 0, 0.18);
  }

  &__error {
    background: rgba(128, 24, 24, 0.9);
  }
}

@keyframes c-map-panel-in {
  0% {
    opacity: 0;
    transform: translateY(-10px) scale(0.98);
  }

  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 1040px) {
  .c-map-page {
    grid-template-columns: 340px minmax(0, 1fr);
  }
}

@media (max-width: 820px) {
  .c-map-page {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 100vh;
    overflow: auto;

    &__panel {
      height: auto;
      min-height: auto;
      border-right: none;
      border-bottom: 1px solid var(--c-map-line);
    }

    &__controls {
      overflow: visible;
    }

    &__mapWrap {
      height: 70vh;
      min-height: 560px;
      padding: 0 16px 16px;
    }

    &__mapOverlay {
      align-items: flex-start;
      flex-direction: column;
      border-radius: 20px;
    }

    &__settingsWindow,
    &__savedWindow {
      left: 12px !important;
      right: 12px;
      top: 12px !important;
      width: auto;
    }
  }
}

@media (max-width: 520px) {
  .c-map-page {
    &__header,
    &__summary,
    &__fieldGrid {
      grid-template-columns: 1fr;
    }

    &__header {
      display: grid;
    }

    &__back {
      justify-self: start;
    }
  }
}
</style>
