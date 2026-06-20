<template>
  <main class="c-map-page" :style="themeStyle">
    <section class="c-map-page__panel">
      <header class="c-map-page__header">
        <div class="c-map-page__brandBlock">
          <div class="c-map-page__topLine">
            <p class="c-map-page__eyebrow">Moscow Sightseeing Guide</p>

            <RouterLink class="c-map-page__back" :to="{ name: $routes.INDEX }">
              <span class="c-map-page__backIcon">←</span>
              <span class="c-map-page__backText">Старт</span>
            </RouterLink>
          </div>
          <h1 class="c-map-page__title">Достопримечательности Москвы</h1>
        </div>
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

      <div class="c-map-page__toolGrid">
        <button class="c-map-page__toolBtn c-map-page__toolBtn--catalog" type="button" @click="() => toggleCatalog()">
          <span class="c-map-page__toolIcon">+</span>
          <span class="c-map-page__toolText">Каталог мест</span>
        </button>

        <button class="c-map-page__toolBtn" type="button" @click="() => toggleInfo()">
          <span class="c-map-page__toolIcon">i</span>
          <span class="c-map-page__toolText">Подсказки</span>
        </button>

        <button
          class="c-map-page__toolBtn"
          :disabled="!alternativeRoutes.length"
          type="button"
          @click="() => toggleSavedRoutes()"
        >
          <span class="c-map-page__toolIcon">{{ alternativeRoutes.length }}</span>
          <span class="c-map-page__toolText">Планы</span>
        </button>

        <button class="c-map-page__toolBtn" type="button" @click="() => toggleSettings()">
          <span class="c-map-page__toolIcon">&#9881;</span>
          <span class="c-map-page__toolText">Настройки</span>
        </button>
      </div>

      <div class="c-map-page__status" :class="routeInfoClass">
        <span class="c-map-page__statusLabel">Успеешь посмотреть</span>
        <span class="c-map-page__statusText">{{ routeInfo.message }}</span>
      </div>

      <div v-if="alternativeDistance" class="c-map-page__distance">
        {{ alternativeDistance }}
      </div>

    </section>

    <section class="c-map-page__mapWrap">
      <div class="c-map-page__mapFrame">
        <div v-if="isMapLoading" class="c-map-page__loader">Карта загружается...</div>
        <div v-if="mapError" class="c-map-page__error">{{ mapError }}</div>
        <div class="c-map-page__mapOverlay">
          <form class="c-map-page__search" @submit.prevent="() => searchMapLocation()">
            <input
              class="c-map-page__searchInput"
              type="text"
              placeholder="Найти достопримечательность"
              v-model.trim="searchQuery"
            >
            <button class="c-map-page__searchBtn" type="submit">Найти</button>
          </form>
          <span class="c-map-page__mapHint">Добавляй достопримечательности прямо на карте</span>
        </div>
        <div ref="map" class="c-map-page__map"></div>
      </div>
    </section>

    <section
      v-if="sideOpen"
      ref="infoWindow"
      class="c-map-page__infoWindow"
      :style="infoPanelStyle"
    >
      <header class="c-map-page__infoHead" @mousedown="(event) => startInfoDrag(event)">
        <div>
          <span class="c-map-page__infoKicker">route tips</span>
          <h2 class="c-map-page__infoTitle">Как успеть посмотреть больше</h2>
        </div>
        <button class="c-map-page__infoClose" type="button" @click="() => closeInfo()">&times;</button>
      </header>

      <div class="c-map-page__infoBody">
        <p class="c-map-page__infoText">ЛКМ по карте добавляет достопримечательность в план прогулки.</p>
        <p class="c-map-page__infoText">ПКМ по метке убирает место из маршрута.</p>
        <p class="c-map-page__infoText">Цель страницы - помочь успеть посетить максимум интересных мест за доступное время. В будущем сюда можно добавить покупку билетов и бронирование экскурсий.</p>
      </div>
    </section>

    <section
      v-if="catalogOpen"
      ref="catalogWindow"
      class="c-map-page__catalogWindow"
      :style="catalogPanelStyle"
    >
      <header class="c-map-page__catalogHead" @mousedown="(event) => startCatalogDrag(event)">
        <div>
          <span class="c-map-page__catalogKicker">каталог мест</span>
          <h2 class="c-map-page__catalogTitle">Выбери достопримечательности</h2>
        </div>
        <div class="c-map-page__catalogControls">
          <span class="c-map-page__catalogCounter">{{ routePoints.length }}/{{ places.length }}</span>
          <button class="c-map-page__catalogClose" type="button" @click="() => closeCatalog()">&times;</button>
        </div>
      </header>

      <div class="c-map-page__catalog">
        <div class="c-map-page__categoryList">
          <button
            v-for="category in placeCategories"
            :key="category"
            class="c-map-page__categoryBtn"
            :class="{ 'c-map-page__categoryBtn--active': activeCategory === category }"
            type="button"
            @click="() => setActiveCategory(category)"
          >
            {{ category }}
          </button>
        </div>

        <div class="c-map-page__placeList">
          <article
            v-for="place in filteredPlaces"
            :key="place.id"
            class="c-map-page__placeCard"
            :class="{ 'c-map-page__placeCard--selected': isPlaceSelected(place) }"
          >
            <div class="c-map-page__placeTop">
              <div>
                <h3 class="c-map-page__placeTitle">{{ place.title }}</h3>
                <p class="c-map-page__placeCategory">{{ place.category }} · {{ place.visitMinutes }} мин</p>
              </div>
            </div>
            <p class="c-map-page__placeDescription">{{ place.description }}</p>
            <div class="c-map-page__placeMeta">
              <span class="c-map-page__placeBadge">{{ place.ticketStatus }}</span>
              <span class="c-map-page__placeHighlight">{{ place.highlight }}</span>
            </div>
            <div class="c-map-page__placeActions">
              <button class="c-map-page__placeBtn" type="button" @click="() => togglePlace(place)">
                {{ isPlaceSelected(place) ? 'Убрать' : 'В маршрут' }}
              </button>
              <button class="c-map-page__placeBtn" type="button" @click="() => focusPlace(place)">
                На карте
              </button>
              <button class="c-map-page__placeBtn c-map-page__placeBtn--ticket" type="button" @click="() => showTicketStub(place)">
                Билеты
              </button>
            </div>
          </article>
        </div>
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
        <button class="c-map-page__settingsClose" type="button" @click="() => closeSettings()">&times;</button>
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

        <div class="c-map-page__routeColorList">
          <h3 class="c-map-page__settingsSubtitle">Цвета маршрутов</h3>
          <label class="c-map-page__routeColorField">
            <span class="c-map-page__routeColorText">Основной</span>
            <input class="c-map-page__routeColorInput" type="color" v-model="routeColors.main">
          </label>
          <label class="c-map-page__routeColorField">
            <span class="c-map-page__routeColorText">Альтернативный</span>
            <input class="c-map-page__routeColorInput" type="color" v-model="routeColors.alternative">
          </label>
          <label class="c-map-page__routeColorField">
            <span class="c-map-page__routeColorText">Дейкстра</span>
            <input class="c-map-page__routeColorInput" type="color" v-model="routeColors.dijkstra">
          </label>
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
        <button class="c-map-page__savedClose" type="button" @click="() => closeSavedRoutes()">&times;</button>
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
import { ROUTE_DEFAULTS } from '@/constants/travelConfig.js'
import { TRAVEL_MARKS, TRAVEL_PLACES } from '@/constants/travelMarks.js'
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
      mapErrorTimer: null,
      searchQuery: '',
      places: TRAVEL_PLACES,
      activeCategory: 'Все',
      focusedPlaceId: '',
      catalogOpen: false,
      catalogPosition: {
        x: 380,
        y: 110
      },
      catalogDrag: null,
      infoPosition: {
        x: 380,
        y: 126
      },
      infoDrag: null,
      settingsOpen: false,
      selectedThemeIndex: 0,
      settingsPosition: {
        x: 760,
        y: 96
      },
      settingsDrag: null,
      savedRoutesOpen: false,
      savedRoutesPosition: {
        x: 760,
        y: 318
      },
      savedRoutesDrag: null,
      routeColors: {
        main: '#00d084',
        alternative: '#ff9f1c',
        dijkstra: '#2f80ff'
      },
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
          text: 'Рассчитать план',
          icon: '01',
          className: 'c-map-page__button--primary'
        },
        {
          method: 'showAlternativeRoute',
          text: 'Альтернативный',
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
          text: 'Добавить место',
          icon: '+',
          className: ''
        },
        {
          method: 'optimizeRouteMinus',
          text: 'Сократить',
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
    catalogPanelStyle() {
      return {
        left: `${this.catalogPosition.x}px`,
        top: `${this.catalogPosition.y}px`
      }
    },
    infoPanelStyle() {
      return {
        left: `${this.infoPosition.x}px`,
        top: `${this.infoPosition.y}px`
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
        '--c-map-panel': this.selectedTheme.panel,
        '--c-route-main': this.routeColors.main,
        '--c-route-alternative': this.routeColors.alternative,
        '--c-route-dijkstra': this.routeColors.dijkstra
      }
    },
    placeCategories() {
      return ['Все', ...new Set(this.places.map((place) => place.category))]
    },
    filteredPlaces() {
      if (this.activeCategory === 'Все') {
        return this.places
      }

      return this.places.filter((place) => place.category === this.activeCategory)
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
    window.clearTimeout(this.mapErrorTimer)
    window.removeEventListener('mousemove', this.onCatalogDrag)
    window.removeEventListener('mouseup', this.stopCatalogDrag)
    window.removeEventListener('mousemove', this.onInfoDrag)
    window.removeEventListener('mouseup', this.stopInfoDrag)
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
    setActiveCategory(category) {
      this.activeCategory = category
    },
    isSamePoint(firstPoint, secondPoint) {
      return Math.abs(firstPoint[0] - secondPoint[0]) < 0.000001 && Math.abs(firstPoint[1] - secondPoint[1]) < 0.000001
    },
    isPlaceSelected(place) {
      return this.routePoints.some((point) => this.isSamePoint(point, place.coordinates))
    },
    getPlaceByCoords(coords) {
      return this.places.find((place) => this.isSamePoint(place.coordinates, coords))
    },
    togglePlace(place) {
      const index = this.routePoints.findIndex((point) => this.isSamePoint(point, place.coordinates))

      if (index >= 0) {
        this.removeLandmark(index)
      } else {
        this.addLandmark(place.coordinates)
      }

      this.refreshMapObjects()
    },
    focusPlace(place) {
      if (!this._map) {
        return
      }

      this.focusedPlaceId = place.id
      this._map.setCenter(place.coordinates, 16, { duration: 300 })
      this.renderFocusedPlace()
    },
    showTicketStub(place) {
      this.setRouteInfo({
        canVisit: null,
        distanceKm: 0,
        totalSeconds: 0,
        message: `Билеты для «${place.title}» пока в режиме заглушки. Позже сюда можно подключить афиши, экскурсии и бронирование.`
      })
    },
    toggleInfo() {
      this.sideOpen = !this.sideOpen
      this.$nextTick(() => {
        this.infoPosition = this.getLimitedPanelPosition(
          this.infoPosition.x,
          this.infoPosition.y,
          this.$refs.infoWindow,
          380,
          260
        )
      })
    },
    closeInfo() {
      this.sideOpen = false
    },
    startInfoDrag(event) {
      if (event.target.closest('.c-map-page__infoClose')) {
        return
      }

      this.infoDrag = {
        shiftX: event.clientX - this.infoPosition.x,
        shiftY: event.clientY - this.infoPosition.y
      }

      window.addEventListener('mousemove', this.onInfoDrag)
      window.addEventListener('mouseup', this.stopInfoDrag)
    },
    onInfoDrag(event) {
      if (!this.infoDrag) {
        return
      }

      this.infoPosition = this.getLimitedPanelPosition(
        event.clientX - this.infoDrag.shiftX,
        event.clientY - this.infoDrag.shiftY,
        this.$refs.infoWindow,
        380,
        260
      )
    },
    stopInfoDrag() {
      this.infoDrag = null
      window.removeEventListener('mousemove', this.onInfoDrag)
      window.removeEventListener('mouseup', this.stopInfoDrag)
    },
    toggleCatalog() {
      this.catalogOpen = !this.catalogOpen
      this.$nextTick(() => {
        this.catalogPosition = this.getLimitedPanelPosition(
          this.catalogPosition.x,
          this.catalogPosition.y,
          this.$refs.catalogWindow,
          720,
          620
        )
      })
    },
    closeCatalog() {
      this.catalogOpen = false
    },
    startCatalogDrag(event) {
      if (event.target.closest('.c-map-page__catalogClose')) {
        return
      }

      this.catalogDrag = {
        shiftX: event.clientX - this.catalogPosition.x,
        shiftY: event.clientY - this.catalogPosition.y
      }

      window.addEventListener('mousemove', this.onCatalogDrag)
      window.addEventListener('mouseup', this.stopCatalogDrag)
    },
    onCatalogDrag(event) {
      if (!this.catalogDrag) {
        return
      }

      this.catalogPosition = this.getLimitedPanelPosition(
        event.clientX - this.catalogDrag.shiftX,
        event.clientY - this.catalogDrag.shiftY,
        this.$refs.catalogWindow,
        720,
        560
      )
    },
    stopCatalogDrag() {
      this.catalogDrag = null
      window.removeEventListener('mousemove', this.onCatalogDrag)
      window.removeEventListener('mouseup', this.stopCatalogDrag)
    },
    toggleSettings() {
      this.settingsOpen = !this.settingsOpen
      this.$nextTick(() => {
        this.settingsPosition = this.getLimitedPanelPosition(
          this.settingsPosition.x,
          this.settingsPosition.y,
          this.$refs.settingsWindow,
          360,
          390
        )
      })
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
      this.$nextTick(() => {
        this.savedRoutesPosition = this.getLimitedSavedRoutesPosition(this.savedRoutesPosition.x, this.savedRoutesPosition.y)
      })
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
    showMapError(message) {
      window.clearTimeout(this.mapErrorTimer)
      this.mapError = message
      const timerId = window.setTimeout(() => {
        if (this.mapErrorTimer === timerId) {
          this.mapError = ''
          this.mapErrorTimer = null
        }
      }, 3600)
      this.mapErrorTimer = timerId
    },
    clearMapError() {
      window.clearTimeout(this.mapErrorTimer)
      this.mapError = ''
      this.mapErrorTimer = null
    },
    loadYandexApi() {
      if (window.ymaps) {
        return Promise.resolve()
      }

      const mapsKey = import.meta.env.VITE_YANDEX_MAPS_API_KEY

      if (!mapsKey) {
        return Promise.reject(new Error('VITE_YANDEX_MAPS_API_KEY is empty'))
      }

      const existingScript = document.getElementById('yandex-map-api')

      if (existingScript) {
        return this.waitYandexApi()
      }

      return new Promise((resolve, reject) => {
        const params = new URLSearchParams({
          apikey: mapsKey,
          lang: 'ru_RU',
          load: 'package.full',
          mode: import.meta.env.VITE_YANDEX_MAPS_MODE || 'release'
        })
        const suggestKey = import.meta.env.VITE_YANDEX_SUGGEST_API_KEY

        if (suggestKey) {
          params.set('suggest_apikey', suggestKey)
        }

        const script = document.createElement('script')
        script.id = 'yandex-map-api'
        script.type = 'text/javascript'
        script.src = `https://api-maps.yandex.ru/2.1/?${params.toString()}`
        script.onload = () => this.waitYandexApi().then(resolve).catch(reject)
        script.onerror = () => reject(new Error('Yandex Maps script failed'))
        document.head.appendChild(script)
      })
    },
    waitYandexApi() {
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
            reject(new Error('Yandex Maps API timeout'))
          }
        }, 100)
      })
    },
    initMap() {
      window.ymaps.ready(() => {
        this._map = new window.ymaps.Map(this.$refs.map, {
          center: ROUTE_DEFAULTS.center,
          zoom: ROUTE_DEFAULTS.zoom,
          controls: ['zoomControl', 'typeSelector']
        })

        this._map.events.add('click', (event) => this.handleMapClick(event))
        this.isMapLoading = false
        this.refreshMapObjects()
      })
    },
    searchMapLocation() {
      if (!this.searchQuery || !this._map || !window.ymaps) {
        return
      }

      window.ymaps.geocode(this.searchQuery, {
        results: 1,
        boundedBy: [[55.45, 37.25], [56.05, 38.05]]
      })
        .then((result) => {
          const item = result.geoObjects.get(0)

          if (!item) {
            this.showMapError('Место не найдено. Попробуй уточнить запрос.')
            return
          }

          const coords = item.geometry.getCoordinates()
          this.clearMapError()
          this._map.setCenter(coords, 15, { duration: 300 })
          this.addLandmark(coords)
          this.refreshMapObjects()
        })
        .catch(() => {
          this.showMapError('Поиск не сработал. Проверь API-ключ и подключение к интернету.')
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
      this.renderFocusedPlace()
      this.renderPlacemarks(points)
    },
    renderFocusedPlace() {
      const place = this.places.find((item) => item.id === this.focusedPlaceId)

      if (!place || !window.ymaps) {
        return
      }

      if (this._focusedMarker) {
        this._map.geoObjects.remove(this._focusedMarker)
      }

      const marker = new window.ymaps.Placemark(place.coordinates, {
        iconContent: '●',
        balloonContentHeader: place.title,
        balloonContentBody: `${place.category} · ${place.visitMinutes} мин<br>${place.description}`,
        balloonContentFooter: place.highlight
      }, {
        preset: 'islands#redStretchyIcon'
      })

      this._map.geoObjects.add(marker)
      this._focusedMarker = marker
      window.setTimeout(() => marker.balloon.open(), 120)
    },
    renderPlacemarks(points) {
      points.forEach((coords, index) => {
        const place = this.getPlaceByCoords(coords)
        const isFocused = place && place.id === this.focusedPlaceId
        const placemark = new window.ymaps.Placemark(coords, {
          iconContent: String(index + 1),
          balloonContentHeader: place ? place.title : `Точка ${index + 1}`,
          balloonContentBody: place ? `${place.description}<br><strong>${place.ticketStatus}</strong>` : 'Пользовательская точка маршрута',
          balloonContentFooter: place ? place.highlight : 'Можно убрать правой кнопкой мыши'
        }, {
          preset: isFocused ? 'islands#redStretchyIcon' : place ? 'islands#greenStretchyIcon' : 'islands#blueStretchyIcon'
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

      return this.buildYandexRoute(yandexPoints, color)
        .catch((yandexError) => {
          console.warn('Yandex route build failed', {
            error: yandexError,
            points: yandexPoints,
            routeFunctionExists: Boolean(window.ymaps && window.ymaps.route)
          })

          return this.buildOsrmRoute(yandexPoints, color)
        })
    },
    buildYandexRoute(points, color) {
      if (!window.ymaps || typeof window.ymaps.route !== 'function') {
        return Promise.reject(new Error('ymaps.route is unavailable'))
      }

      return window.ymaps.route(points, {
        multiRoute: true,
        routingMode: 'pedestrian',
        mapStateAutoApply: false
      }).then((route) => {
        this._map.geoObjects.add(route)
        this.styleYandexRoute(route, color)
        this.renderPlacemarks(points)

        if (typeof route.getBounds === 'function') {
          this._map.setBounds(route.getBounds(), {
            checkZoomRange: true,
            zoomMargin: 48
          })
        }

        return route
      })
    },
    styleYandexRoute(route, color) {
      if (route.options && typeof route.options.set === 'function') {
        route.options.set({
          routeActiveStrokeColor: color,
          routeStrokeColor: color,
          routeActiveStrokeWidth: 6,
          routeStrokeWidth: 5
        })
      }

      if (route.getPaths && route.getPaths().options) {
        route.getPaths().options.set({
          strokeColor: color,
          strokeWidth: 5,
          strokeOpacity: 0.9
        })
      }
    },
    buildOsrmRoute(points, color) {
      return getRoadRoute(points).then((roadRoute) => {
        const polyline = new window.ymaps.Polyline(roadRoute.points, {}, {
          strokeColor: color,
          strokeWidth: 5,
          strokeOpacity: 0.84
        })

        this._map.geoObjects.add(polyline)
        this.renderPlacemarks(points)
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

      if (route && typeof route.getActiveRoute === 'function') {
        const activeRoute = route.getActiveRoute()
        const distance = activeRoute && activeRoute.properties && activeRoute.properties.get('distance')

        if (distance && typeof distance.value === 'number') {
          return distance.value / 1000
        }
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
      this.drawRoute(this.routePoints, this.routeColors.main, 'Основной план прогулки')
    },
    showAlternativeRoute() {
      if (!this.validateRoute()) {
        return
      }

      const points = createRotatedRoute(this.routePoints)
      this.drawRoute(points, this.routeColors.alternative, 'Альтернативный план')
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
      this.drawRoute(points, this.routeColors.dijkstra, 'Маршрут Дейкстры')
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
        this.drawRoute(route.points, this.routeColors.alternative)
      })
    },
    resetMap() {
      this.focusedPlaceId = ''
      this._focusedMarker = null
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
  grid-template-columns: minmax(344px, 380px) minmax(0, 1fr);
  height: 100dvh;
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
    height: 100dvh;
    padding: 18px;
    overflow: hidden;
    border-right: 1px solid var(--c-map-line);
    background:
      linear-gradient(180deg, var(--c-map-panel), rgba(9, 12, 11, 0.84)),
      radial-gradient(circle at 30% 0%, rgba(109, 242, 205, 0.16), transparent 34%);
    box-shadow: 24px 0 80px rgba(0, 0, 0, 0.34);
    backdrop-filter: blur(18px);
  }

  &__header {
    display: block;
  }

  &__brandBlock {
    min-width: 0;
  }

  &__topLine {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
    margin-bottom: 7px;
  }

  &__eyebrow {
    margin: 0;
    color: var(--c-map-accent);
    font-size: 11px;
    font-weight: 800;
    letter-spacing: 0.2em;
    text-transform: uppercase;
  }

  &__title {
    max-width: 320px;
    margin: 0;
    font-family: Georgia, serif;
    font-size: 30px;
    line-height: 1.02;
    letter-spacing: 0;
  }

  &__back {
    display: inline-flex;
    flex-shrink: 0;
    align-items: center;
    gap: 8px;
    min-height: 34px;
    padding: 0 10px;
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
    font-size: 12px;
    font-weight: 800;
  }

  &__summary {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  &__summaryItem {
    display: grid;
    gap: 2px;
    min-width: 0;
    padding: 9px 10px;
    border: 1px solid var(--c-map-line);
    border-radius: 18px;
    background: var(--c-map-card);
  }

  &__summaryValue {
    color: var(--c-map-accent);
    font-family: Georgia, serif;
    font-size: 21px;
    line-height: 1;
  }

  &__summaryLabel {
    color: var(--c-map-muted);
    font-size: 11px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-transform: uppercase;
  }

  &__infoToggle,
  &__settingsBtn,
  &__toolBtn,
  &__button,
  &__savedToggle,
  &__savedBtn,
  &__themeBtn,
  &__searchBtn,
  &__routeColorField,
  &__catalogClose,
  &__infoClose {
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

  &__toolGrid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  &__toolBtn {
    display: grid;
    grid-template-columns: 30px minmax(0, 1fr);
    align-items: center;
    gap: 8px;
    min-height: 42px;
    padding: 6px 10px;
    border-radius: 16px;
    text-align: left;

    &:disabled {
      cursor: not-allowed;
      opacity: 0.48;
      transform: none;
    }

    &--catalog {
      border-color: rgba(109, 242, 205, 0.48);
      background: rgba(109, 242, 205, 0.12);
    }
  }

  &__toolIcon {
    display: grid;
    place-items: center;
    width: 30px;
    height: 30px;
    border-radius: 12px;
    background: rgba(247, 238, 220, 0.12);
    color: var(--c-map-accent);
    font-size: 12px;
    font-weight: 900;
  }

  &__toolText {
    min-width: 0;
    font-size: 12px;
    font-weight: 900;
    overflow-wrap: anywhere;
  }

  &__infoToggle {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 42px;
    padding: 0 8px 0 14px;
    border-radius: 16px;
    font-weight: 800;
  }

  &__infoToggleIcon {
    display: grid;
    place-items: center;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: var(--c-map-accent);
    color: #06100d;
  }

  &__toolRow {
    display: grid;
    grid-template-columns: 1fr 42px;
    gap: 8px;
  }

  &__settingsBtn {
    display: grid;
    place-items: center;
    min-height: 42px;
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

  &__infoWindow {
    position: fixed;
    z-index: 22;
    width: min(380px, calc(100vw - 24px));
    overflow: hidden;
    border: 1px solid rgba(247, 238, 220, 0.22);
    border-radius: 24px;
    background:
      linear-gradient(145deg, rgba(8, 16, 14, 0.9), rgba(8, 16, 14, 0.72)),
      radial-gradient(circle at 18% 0%, rgba(255, 177, 94, 0.18), transparent 40%);
    box-shadow: 0 30px 90px rgba(0, 0, 0, 0.44);
    backdrop-filter: blur(18px);
    animation: c-map-panel-in 0.24s ease;
  }

  &__infoHead {
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

  &__infoKicker {
    display: block;
    margin-bottom: 6px;
    color: var(--c-map-accent);
    font-size: 10px;
    font-weight: 900;
    letter-spacing: 0.18em;
    text-transform: uppercase;
  }

  &__infoTitle {
    margin: 0;
    font-family: Georgia, serif;
    font-size: 24px;
    line-height: 1;
  }

  &__infoClose {
    display: grid;
    place-items: center;
    flex: 0 0 auto;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    cursor: pointer;
    font-size: 24px;
    line-height: 1;
  }

  &__infoBody {
    display: grid;
    gap: 10px;
    padding: 16px 18px 18px;
  }

  &__infoText {
    margin: 0;
    color: var(--c-map-muted);
    font-size: 13px;
    line-height: 1.45;
  }

  &__controls {
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: auto;
    overflow: visible;
  }

  &__fieldGrid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  &__field {
    display: grid;
    gap: 5px;
  }

  &__label {
    color: var(--c-map-muted);
    font-size: 11px;
    font-weight: 700;
  }

  &__inputWrap {
    position: relative;
    display: block;
  }

  &__input {
    width: 100%;
    min-height: 40px;
    padding: 0 40px 0 12px;
    border: 1px solid rgba(247, 238, 220, 0.2);
    border-radius: 16px;
    outline: none;
    background: rgba(247, 238, 220, 0.94);
    color: #0a0f0e;
    font-size: 19px;
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
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  &__button {
    position: relative;
    display: grid;
    grid-template-columns: 28px minmax(0, 1fr);
    align-items: center;
    gap: 9px;
    min-height: 44px;
    padding: 7px 10px;
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
      grid-column: 1 / -1;
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
    width: 28px;
    height: 28px;
    border-radius: 12px;
    background: rgba(247, 238, 220, 0.1);
    color: var(--c-map-accent);
    font-size: 12px;
    font-weight: 900;
  }

  &__buttonText {
    min-width: 0;
    font-size: 13px;
    font-weight: 800;
    line-height: 1.15;
    white-space: normal;
    overflow-wrap: anywhere;
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
    gap: 6px;
    min-height: 72px;
    max-height: 118px;
    padding: 12px;
    overflow: auto;

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
    line-height: 1.35;
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
    min-height: 40px;
    padding: 0 10px 0 14px;
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
    max-height: min(560px, calc(100dvh - 24px));
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
    max-height: calc(100dvh - 126px);
    padding: 14px;
    overflow: auto;
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

  &__settingsSubtitle {
    margin: 0;
    color: var(--c-map-text);
    font-size: 13px;
    font-weight: 900;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }

  &__routeColorList {
    display: grid;
    gap: 8px;
  }

  &__routeColorField {
    display: grid;
    grid-template-columns: 1fr 42px;
    align-items: center;
    gap: 10px;
    min-height: 42px;
    padding: 8px 10px;
    border-radius: 16px;
  }

  &__routeColorText {
    min-width: 0;
    font-weight: 900;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__routeColorInput {
    width: 42px;
    height: 30px;
    padding: 0;
    border: 0;
    border-radius: 10px;
    overflow: hidden;
    background: transparent;
    cursor: pointer;
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
    max-height: min(520px, calc(100dvh - 24px));
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

  &__catalogWindow {
    position: fixed;
    z-index: 19;
    display: grid;
    grid-template-rows: auto minmax(0, 1fr);
    width: min(720px, calc(100vw - 48px));
    max-height: min(620px, calc(100dvh - 32px));
    overflow: hidden;
    border: 1px solid rgba(247, 238, 220, 0.22);
    border-radius: 28px;
    background:
      linear-gradient(145deg, rgba(8, 16, 14, 0.86), rgba(8, 16, 14, 0.68)),
      radial-gradient(circle at 12% 0%, rgba(109, 242, 205, 0.2), transparent 38%);
    box-shadow: 0 30px 90px rgba(0, 0, 0, 0.44);
    backdrop-filter: blur(18px);
    animation: c-map-panel-in 0.24s ease;
  }


  &__catalog {
    display: grid;
    gap: 12px;
    min-height: 0;
    padding: 16px;
    overflow: hidden;
  }

  &__catalogHead {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 14px;
    padding: 18px 20px 14px;
    border-bottom: 1px solid rgba(247, 238, 220, 0.12);
    cursor: grab;
    user-select: none;

    &:active {
      cursor: grabbing;
    }
  }

  &__catalogKicker {
    color: var(--c-map-accent);
    font-size: 10px;
    font-weight: 900;
    letter-spacing: 0.16em;
    text-transform: uppercase;
  }

  &__catalogTitle {
    margin: 4px 0 0;
    color: var(--c-map-text);
    font-size: 24px;
    line-height: 1.05;
  }

  &__catalogControls {
    display: flex;
    flex: 0 0 auto;
    align-items: center;
    gap: 8px;
  }

  &__catalogCounter {
    padding: 7px 10px;
    border-radius: 999px;
    background: rgba(109, 242, 205, 0.14);
    color: var(--c-map-accent);
    font-weight: 900;
  }

  &__catalogClose {
    display: grid;
    place-items: center;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    cursor: pointer;
    font-size: 24px;
    line-height: 1;
  }

  &__categoryList {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(118px, 1fr));
    gap: 8px;
    overflow: visible;
  }

  &__categoryBtn {
    min-height: 38px;
    padding: 8px 10px;
    border: 1px solid rgba(255, 255, 255, 0.14);
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.06);
    color: var(--c-map-muted);
    cursor: pointer;
    font-size: 13px;
    font-weight: 800;
    line-height: 1.15;
    transition: all 0.24s ease;

    &--active,
    &:hover {
      border-color: var(--c-map-accent);
      background: rgba(109, 242, 205, 0.15);
      color: var(--c-map-text);
    }
  }

  &__placeList {
    display: grid;
    gap: 10px;
    min-height: 0;
    overflow-y: auto;
    padding-right: 6px;
  }

  &__placeCard {
    display: grid;
    gap: 9px;
    padding: 14px;
    border: 1px solid rgba(255, 255, 255, 0.11);
    border-radius: 18px;
    background: linear-gradient(145deg, rgba(255, 255, 255, 0.085), rgba(255, 255, 255, 0.035));
    transition: all 0.24s ease;

    &--selected {
      border-color: var(--c-map-accent);
      box-shadow: inset 0 0 0 1px rgba(109, 242, 205, 0.12), 0 16px 40px rgba(0, 0, 0, 0.2);
    }
  }

  &__placeTop {
    display: block;
  }

  &__placeTitle {
    margin: 0;
    color: var(--c-map-text);
    font-size: 16px;
    line-height: 1.08;
  }

  &__placeCategory,
  &__placeDescription,
  &__placeHighlight,
  &__placeBadge {
    margin: 0;
    color: var(--c-map-muted);
    font-size: 12px;
    line-height: 1.35;
  }

  &__placeMeta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  &__placeBadge {
    flex: 0 0 auto;
    padding: 5px 8px;
    border-radius: 999px;
    background: rgba(255, 177, 94, 0.1);
    color: rgba(255, 202, 133, 0.94);
    font-size: 10px;
    font-weight: 900;
    letter-spacing: 0.03em;
  }

  &__placeHighlight {
    flex: 1 1 220px;
    color: var(--c-map-accent);
    font-weight: 800;
  }

  &__placeActions {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 8px;
  }

  &__placeBtn {
    min-height: 34px;
    padding: 7px 9px;
    border: 1px solid rgba(255, 255, 255, 0.14);
    border-radius: 12px;
    background: rgba(0, 0, 0, 0.22);
    color: var(--c-map-text);
    cursor: pointer;
    font-size: 11px;
    font-weight: 900;
    transition: all 0.24s ease;

    &:hover {
      border-color: var(--c-map-accent);
      background: rgba(109, 242, 205, 0.14);
    }

    &--ticket {
      color: var(--c-map-warm);
    }
  }

  &__mapWrap {
    position: relative;
    min-width: 0;
    height: 100dvh;
    padding: 0;
  }

  &__mapFrame {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: visible;
    border: none;
    border-radius: 0;
    background: #101315;
    box-shadow: none;
  }

  &__map {
    width: 100%;
    height: 100%;
    border-radius: 0;
  }

  &__mapOverlay {
    position: absolute;
    top: 20px;
    left: 22px;
    z-index: 2;
    display: flex;
    align-items: center;
    gap: 10px;
    max-width: min(560px, calc(100% - 44px));
    min-height: 52px;
    padding: 8px 10px;
    border: 1px solid rgba(9, 12, 11, 0.14);
    border-radius: 999px;
    background: rgba(247, 238, 220, 0.86);
    color: #08100e;
    box-shadow: 0 12px 34px rgba(0, 0, 0, 0.18);
    pointer-events: auto;
  }


  &__mapHint {
    color: rgba(8, 16, 14, 0.7);
    max-width: 160px;
    font-size: 12px;
    font-weight: 800;
    line-height: 1.15;
  }

  &__search {
    display: grid;
    grid-template-columns: minmax(180px, 1fr) auto;
    gap: 8px;
    width: min(360px, calc(100vw - 36px));
  }

  &__searchInput {
    min-width: 0;
    min-height: 38px;
    padding: 0 14px;
    border: 1px solid rgba(8, 16, 14, 0.16);
    border-radius: 999px;
    outline: none;
    background: rgba(255, 255, 255, 0.88);
    color: #08100e;
    font-weight: 800;

    &:focus {
      border-color: var(--c-map-accent);
      box-shadow: 0 0 0 4px rgba(109, 242, 205, 0.2);
    }
  }

  &__searchBtn {
    min-height: 38px;
    padding: 0 14px;
    border-radius: 999px;
    background: #08100e;
    color: var(--c-map-accent);
    font-weight: 900;
  }

  &__loader,
  &__error {
    position: absolute;
    top: 86px;
    right: 28px;
    z-index: 6;
    max-width: min(460px, calc(100% - 56px));
    padding: 14px 18px;
    border-radius: 18px;
    background: rgba(8, 16, 14, 0.84);
    color: var(--c-map-text);
    box-shadow: 0 18px 44px rgba(0, 0, 0, 0.26);
    font-size: 14px;
    font-weight: 800;
    line-height: 1.35;
  }

  &__loader {
    left: 50%;
    right: auto;
    top: 24px;
    transform: translateX(-50%);
    border-radius: 999px;
  }

  &__error {
    background: rgba(128, 24, 24, 0.92);
    animation: c-map-toast-in 0.18s ease;
  }
}

@keyframes c-map-toast-in {
  0% {
    opacity: 0;
    transform: translateY(-8px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
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
    grid-template-columns: 330px minmax(0, 1fr);

    &__title {
      font-size: 27px;
    }

    &__buttonGrid,
    &__toolGrid {
      grid-template-columns: 1fr;
    }

    &__button--primary {
      grid-column: auto;
    }
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
      overflow: visible;
      border-right: none;
      border-bottom: 1px solid var(--c-map-line);
    }

    &__controls {
      overflow: visible;
    }

    &__mapWrap {
      height: 70vh;
      min-height: 560px;
      padding: 0;
    }

    &__mapOverlay {
      align-items: stretch;
      flex-direction: column;
      left: 12px;
      right: 12px;
      max-width: none;
      border-radius: 20px;
    }

    &__mapHint {
      max-width: none;
    }

    &__search {
      width: 100%;
      grid-template-columns: 1fr;
    }

    &__settingsWindow,
    &__savedWindow,
    &__catalogWindow,
    &__infoWindow {
      left: 12px !important;
      right: 12px;
      top: 12px !important;
      width: auto;
    }

    &__catalogWindow {
      max-height: calc(100dvh - 24px);
    }

    &__error {
      top: 122px;
      right: 12px;
      max-width: calc(100% - 24px);
    }
  }
}

@media (max-width: 520px) {
  .c-map-page {
    &__header,
    &__summary,
    &__fieldGrid,
    &__toolGrid {
      grid-template-columns: 1fr;
    }

    &__topLine {
      align-items: flex-start;
      flex-direction: column;
      gap: 8px;
    }

    &__toolBtn--catalog {
      grid-column: auto;
    }

    &__placeTop,
    &__catalogHead {
      display: grid;
    }

    &__catalogControls {
      justify-content: space-between;
    }

    &__placeActions {
      grid-template-columns: 1fr;
    }

    &__categoryList {
      grid-template-columns: 1fr;
    }
  }
}
</style>
