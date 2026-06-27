<template>
  <main class="c-intro" :class="introThemeClass" @mousemove="(event) => onMouseMove(event)">
    <section class="c-intro__layers">
      <div class="c-intro__container" :style="containerStyle">
        <div
          v-for="layer in layers"
          :key="layer.name"
          class="c-intro__layer"
          :class="layer.className"
          :style="{ backgroundImage: `url(${layer.image})` }"
        ></div>

        <div class="c-intro__layer c-intro__layer--content">
          <div class="c-intro__shell">
            <header class="c-intro__nav">
              <span class="c-intro__brand">Moscow Travel Guide</span>
              <span class="c-intro__navLine"></span>
              <span class="c-intro__navText">городской маршрутный конструктор</span>
            </header>

            <div class="c-intro__content">
              <div class="c-intro__badge">
                <span class="c-intro__badgeDot"></span>
                маршрут без лишних экранов
              </div>

              <h1 class="c-intro__title">
                Москва в ритме
                <span class="c-intro__titleAccent">твоего маршрута</span>
              </h1>

              <p class="c-intro__lead">
                Ставь точки на карте, сравнивай варианты пути и собирай прогулку по городу без ручного пересчета времени.
              </p>

              <div class="c-intro__actions">
                <button
                  class="c-intro__start"
                  type="button"
                  @mouseenter="(event) => animateStartButton(event)"
                  @click="() => startTravel()"
                >
                  <span class="c-intro__startText">Открыть карту</span>
                  <span class="c-intro__startIcon">→</span>
                </button>
                <span class="c-intro__hint">ЛКМ по карте ставит метки маршрута</span>
              </div>

              <dl class="c-intro__projectStats">
                <div class="c-intro__projectStat">
                  <dt class="c-intro__projectValue">{{ placesCount }}</dt>
                  <dd class="c-intro__projectLabel">доступных мест</dd>
                </div>
                <div class="c-intro__projectStat">
                  <dt class="c-intro__projectValue">{{ categoriesCount }}</dt>
                  <dd class="c-intro__projectLabel">категорий</dd>
                </div>
                <div class="c-intro__projectStat">
                  <dt class="c-intro__projectValue">{{ averageVisitMinutes }}</dt>
                  <dd class="c-intro__projectLabel">мин. на точку</dd>
                </div>
              </dl>
            </div>

            <aside class="c-intro__routeCard">
              <header class="c-intro__routeHead">
                <span class="c-intro__routeKicker">пример маршрута</span>
                <h2 class="c-intro__routeTitle">Прогулка на сегодня</h2>
              </header>

              <ol class="c-intro__routeList">
                <li
                  v-for="(place, index) in featuredRoute.places"
                  :key="place.id"
                  class="c-intro__routeItem"
                >
                  <span class="c-intro__routeNumber">{{ getRouteNumber(index) }}</span>
                  <span class="c-intro__routePlace">
                    <span class="c-intro__routeText">{{ place.title }}</span>
                    <span class="c-intro__routeMeta">{{ place.category }} · {{ place.visitMinutes }} мин</span>
                  </span>
                </li>
              </ol>

              <div class="c-intro__routeSummary">
                <span class="c-intro__routeTime">{{ formattedRouteTime }}</span>
                <span class="c-intro__routeSummaryText">включая прогулку между точками</span>
              </div>
            </aside>
          </div>
        </div>

        <div class="c-intro__layer c-intro__layer--rain">
          <canvas ref="rainCanvas" class="c-intro__rain"></canvas>
        </div>
      </div>
    </section>
  </main>
</template>

<script>
import { animate, stagger } from 'motion'
import { ROUTES } from '@/router/index.js'
import { TRAVEL_PLACES } from '@/constants/travelMarks.js'
import { getStoredUiTheme, getUiThemeClass } from '@/utils/uiTheme.js'
import layerBack from '@/assets/travel/images/layer-1.jpg'
import layerMiddle from '@/assets/travel/images/layer-2.png'
import layerFront from '@/assets/travel/images/layer-5.png'
import layerFog from '@/assets/travel/images/layer-6.png'

const FEATURED_ROUTE_GROUPS = [
  ['kremlin', 'red-square', 'st-basil'],
  ['kremlin', 'zaryadye', 'tretyakov'],
  ['cathedral', 'pushkin', 'arbat'],
  ['bolshoi', 'red-square', 'zaryadye'],
  ['gorky-park', 'tretyakov', 'cathedral']
]

export default {
  name: 'IndexPage',
  data() {
    return {
      moveX: 0,
      moveY: 0,
      rainItems: [],
      rainRafId: null,
      motionControls: [],
      uiTheme: getStoredUiTheme(),
      featuredRoute: {
        places: [],
        totalMinutes: 0
      },
      layers: [
        {
          name: 'back',
          image: layerBack,
          className: 'c-intro__layer--back'
        },
        {
          name: 'middle',
          image: layerMiddle,
          className: 'c-intro__layer--middle'
        },
        {
          name: 'front',
          image: layerFront,
          className: 'c-intro__layer--front'
        },
        {
          name: 'fog',
          image: layerFog,
          className: 'c-intro__layer--fog'
        }
      ]
    }
  },
  computed: {
    containerStyle() {
      return {
        transform: `rotateX(${this.moveY}deg) rotateY(${this.moveX}deg)`
      }
    },
    placesCount() {
      return TRAVEL_PLACES.length
    },
    categoriesCount() {
      return new Set(TRAVEL_PLACES.map((place) => place.category)).size
    },
    averageVisitMinutes() {
      const totalMinutes = TRAVEL_PLACES.reduce((sum, place) => sum + place.visitMinutes, 0)

      return Math.round(totalMinutes / TRAVEL_PLACES.length)
    },
    formattedRouteTime() {
      const hours = Math.floor(this.featuredRoute.totalMinutes / 60)
      const minutes = this.featuredRoute.totalMinutes % 60

      if (!hours) {
        return `${minutes} мин`
      }

      return `около ${hours} ч ${minutes} мин`
    },
    introThemeClass() {
      return getUiThemeClass(this.uiTheme, 'c-intro')
    }
  },
  created() {
    this.featuredRoute = this.createFeaturedRoute()
  },
  mounted() {
    this.setupRain()
    this.$nextTick(() => this.animateIntroPanel())
    window.addEventListener('resize', this.setupRain)
    window.addEventListener('mtg-ui-theme-change', this.syncUiTheme)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.setupRain)
    window.removeEventListener('mtg-ui-theme-change', this.syncUiTheme)
    this.motionControls.forEach((control) => {
      if (control && control.stop) {
        control.stop()
      }
    })

    if (this.rainRafId) {
      window.cancelAnimationFrame(this.rainRafId)
    }
  },
  methods: {
    onMouseMove(event) {
      this.moveX = (event.clientX - window.innerWidth / 2) * -0.005
      this.moveY = (event.clientY - window.innerHeight / 2) * 0.01
    },
    syncUiTheme(event) {
      this.uiTheme = event?.detail?.theme || getStoredUiTheme()
    },
    startTravel() {
      this.$router.push({ name: ROUTES.MAP })
    },
    createFeaturedRoute() {
      const routeIds = FEATURED_ROUTE_GROUPS[Math.floor(Math.random() * FEATURED_ROUTE_GROUPS.length)]
      const places = routeIds
        .map((id) => TRAVEL_PLACES.find((place) => place.id === id))
        .filter(Boolean)
      const visitMinutes = places.reduce((sum, place) => sum + place.visitMinutes, 0)
      const walkingMinutes = (places.length - 1) * (14 + Math.floor(Math.random() * 7))

      return {
        places,
        totalMinutes: visitMinutes + walkingMinutes
      }
    },
    getRouteNumber(index) {
      return String(index + 1).padStart(2, '0')
    },
    shouldReduceMotion() {
      return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
    },
    animateIntroPanel() {
      if (this.shouldReduceMotion()) {
        return
      }

      this.motionControls = [
        animate(
          '.c-intro__shell',
          { opacity: [0, 1], y: [18, 0] },
          { duration: 0.54, easing: 'ease-out' }
        ),
        animate(
          '.c-intro__content > *, .c-intro__routeCard',
          { opacity: [0, 1], y: [16, 0] },
          { duration: 0.46, delay: stagger(0.055), easing: 'ease-out' }
        )
      ]
    },
    animateStartButton(event) {
      if (this.shouldReduceMotion() || !event.currentTarget) {
        return
      }

      animate(
        event.currentTarget,
        { scale: [1, 1.025, 1], boxShadow: ['0 18px 46px rgba(120, 243, 211, 0.2)', '0 20px 58px rgba(120, 243, 211, 0.34)', '0 18px 46px rgba(120, 243, 211, 0.2)'] },
        { duration: 0.38, easing: 'ease-out' }
      )
    },
    getRandomNumber(max, min) {
      return Math.floor(Math.random() * max) + min
    },
    setupRain() {
      const canvas = this.$refs.rainCanvas

      if (!canvas) {
        return
      }

      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
      this.rainItems = []

      for (let i = 0; i < 120; i += 1) {
        this.rainItems.push({
          x: Math.floor(Math.random() * window.innerWidth) + 1,
          y: Math.random() * -500,
          height: this.getRandomNumber(14, 4),
          velocity: this.getRandomNumber(16, 2),
          opacity: Math.random() * 0.46
        })
      }

      if (!this.rainRafId) {
        this.drawRain()
      }
    },
    drawRain() {
      const canvas = this.$refs.rainCanvas

      if (!canvas) {
        return
      }

      const context = canvas.getContext('2d')
      context.clearRect(0, 0, canvas.width, canvas.height)

      this.rainItems.forEach((drop) => {
        context.beginPath()
        context.moveTo(drop.x, drop.y)
        context.lineTo(drop.x, drop.y - drop.height)
        context.lineWidth = 1
        context.strokeStyle = `rgba(255, 255, 255, ${drop.opacity})`
        context.stroke()

        if (drop.y >= window.innerHeight + 100) {
          drop.y = drop.height - 100
          return
        }

        drop.y += drop.velocity
      })

      this.rainRafId = window.requestAnimationFrame(() => this.drawRain())
    }
  }
}
</script>

<style lang="scss">
@font-face {
  font-family: 'KamerikTravel';
  src: url('@/assets/travel/fonts/kamerik205-heavy.woff2') format('woff2');
  font-weight: 900;
}

@font-face {
  font-family: 'MerriweatherTravel';
  src: url('@/assets/travel/fonts/merriweather-regular-italic.woff2') format('woff2');
}

.c-intro {
  --c-intro-ink: #f8f0df;
  --c-intro-muted: rgba(248, 240, 223, 0.7);
  --c-intro-neon: #78f3d3;
  --c-intro-ember: #ff8b4a;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 10% 14%, rgba(120, 243, 211, 0.18), transparent 30%),
    linear-gradient(140deg, #050708 0%, #10100d 42%, #040505 100%);
  color: var(--c-intro-ink);
  font-family: 'KamerikTravel', Georgia, serif;

  &__layers {
    min-height: 100vh;
    overflow: hidden;
    perspective: 1100px;
  }

  &__container {
    position: relative;
    min-height: 100vh;
    transform-style: preserve-3d;
    transition: transform 1.4s cubic-bezier(0.05, 0.5, 0, 1);
    will-change: transform;
  }

  &__layer {
    position: absolute;
    inset: -5vw;
    display: flex;
    align-items: center;
    justify-content: center;
    background-position: center;
    background-size: cover;

    &--back {
      filter: saturate(0.86) contrast(1.08) brightness(0.82);
      transform: translateZ(-70px) scale(1.08);
    }

    &--middle {
      opacity: 0.92;
      transform: translateZ(78px) scale(0.9);
    }

    &--content {
      z-index: 3;
      transform: translateZ(170px) scale(0.82);
    }

    &--rain {
      z-index: 4;
      pointer-events: none;
      transform: translateZ(220px) scale(0.92);
    }

    &--front {
      z-index: 5;
      pointer-events: none;
      transform: translateZ(300px) scale(0.9);
    }

    &--fog {
      z-index: 6;
      opacity: 0.88;
      pointer-events: none;
      transform: translateZ(380px);
    }
  }

  &__shell {
    position: relative;
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(280px, 330px);
    align-items: end;
    gap: 34px;
    width: min(1180px, calc(100vw - 48px));
    min-height: min(720px, calc(100vh - 64px));
    padding: clamp(24px, 4vw, 52px);
    border: 1px solid rgba(248, 240, 223, 0.18);
    border-radius: 34px;
    background:
      linear-gradient(118deg, rgba(5, 7, 8, 0.62), rgba(5, 7, 8, 0.24)),
      radial-gradient(circle at 88% 12%, rgba(255, 139, 74, 0.24), transparent 32%),
      radial-gradient(circle at 6% 96%, rgba(120, 243, 211, 0.18), transparent 30%);
    box-shadow:
      0 30px 120px rgba(0, 0, 0, 0.42),
      inset 0 1px 0 rgba(255, 255, 255, 0.13);
    backdrop-filter: blur(10px) saturate(1.08);

    &::before {
      content: '';
      position: absolute;
      inset: 18px;
      border: 1px solid rgba(248, 240, 223, 0.1);
      border-radius: 26px;
      pointer-events: none;
    }

    &::after {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: inherit;
      pointer-events: none;
      background:
        linear-gradient(135deg, rgba(255, 255, 255, 0.12), transparent 28%),
        linear-gradient(315deg, rgba(120, 243, 211, 0.08), transparent 34%);
      mix-blend-mode: screen;
    }
  }

  &__nav {
    position: absolute;
    top: 28px;
    left: clamp(24px, 4vw, 52px);
    right: clamp(24px, 4vw, 52px);
    display: flex;
    align-items: center;
    gap: 14px;
    color: var(--c-intro-muted);
    font-family: 'MerriweatherTravel', Georgia, serif;
    font-size: 13px;
  }

  &__brand {
    color: var(--c-intro-neon);
    font-family: 'KamerikTravel', Georgia, serif;
    letter-spacing: 0.22em;
    text-transform: uppercase;
  }

  &__navLine {
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, rgba(120, 243, 211, 0.55), transparent);
  }

  &__navText {
    text-align: right;
  }

  &__content {
    position: relative;
    z-index: 1;
    max-width: 760px;
    padding-top: 110px;
  }

  &__badge {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    padding: 9px 12px;
    border: 1px solid rgba(120, 243, 211, 0.38);
    border-radius: 999px;
    background: rgba(5, 7, 8, 0.46);
    color: var(--c-intro-muted);
    font-size: 12px;
    letter-spacing: 0.12em;
    text-transform: uppercase;
  }

  &__badgeDot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--c-intro-neon);
    box-shadow: 0 0 18px var(--c-intro-neon);
  }

  &__title {
    max-width: 880px;
    margin: 0;
    font-size: clamp(48px, 8.5vw, 128px);
    letter-spacing: -0.075em;
    line-height: 0.9;
    text-transform: uppercase;
  }

  &__titleAccent {
    display: block;
    color: transparent;
    -webkit-text-stroke: 1px rgba(248, 240, 223, 0.82);
    text-shadow: 0 0 34px rgba(120, 243, 211, 0.24);
  }

  &__lead {
    max-width: 610px;
    margin: 26px 0 0;
    color: var(--c-intro-muted);
    font-family: Arial, sans-serif;
    font-size: clamp(16px, 1.5vw, 20px);
    line-height: 1.6;
  }

  &__actions {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
    margin-top: 32px;
  }

  &__start {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 16px;
    min-height: 58px;
    padding: 0 10px 0 24px;
    border: 1px solid rgba(248, 240, 223, 0.42);
    border-radius: 999px;
    overflow: hidden;
    background: rgba(248, 240, 223, 0.08);
    color: var(--c-intro-ink);
    cursor: pointer;
    font-family: Arial, sans-serif;
    font-size: 14px;
    font-weight: 800;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    transition: all 0.28s ease;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      opacity: 0;
      background: linear-gradient(100deg, var(--c-intro-neon), var(--c-intro-ember));
      transition: all 0.28s ease;
    }

    &:hover {
      border-color: transparent;
      color: #05100d;
      transform: translateY(-3px);
      box-shadow: 0 18px 46px rgba(120, 243, 211, 0.2);
    }

    &:hover::before {
      opacity: 1;
    }
  }

  &__startText,
  &__startIcon {
    position: relative;
    z-index: 1;
  }

  &__startIcon {
    display: grid;
    place-items: center;
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: var(--c-intro-ink);
    color: #07100e;
    transition: all 0.28s ease;
  }

  &__start:hover &__startIcon {
    transform: translateX(4px);
  }

  &__hint {
    max-width: 240px;
    color: rgba(248, 240, 223, 0.56);
    font-family: Arial, sans-serif;
    font-size: 13px;
    line-height: 1.4;
  }

  &__projectStats {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 150px));
    gap: 10px;
    max-width: 500px;
    margin: 20px 0 0;
  }

  &__projectStat {
    display: grid;
    gap: 3px;
    min-height: 72px;
    margin: 0;
    padding: 12px 14px;
    border: 1px solid rgba(248, 240, 223, 0.15);
    border-radius: 18px;
    background: rgba(5, 7, 8, 0.38);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
  }

  &__projectValue {
    color: var(--c-intro-neon);
    font-size: 25px;
    line-height: 1;
  }

  &__projectLabel {
    margin: 0;
    color: rgba(248, 240, 223, 0.66);
    font-family: Arial, sans-serif;
    font-size: 11px;
    font-weight: 800;
    line-height: 1.2;
    text-transform: uppercase;
  }

  &__routeCard {
    position: relative;
    z-index: 1;
    display: grid;
    gap: 18px;
    align-self: center;
    padding: 22px;
    border: 1px solid rgba(248, 240, 223, 0.22);
    border-radius: 28px;
    background:
      linear-gradient(155deg, rgba(248, 240, 223, 0.12), rgba(5, 7, 8, 0.42)),
      rgba(5, 7, 8, 0.54);
    box-shadow:
      0 20px 54px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.12);
  }

  &__routeHead {
    display: grid;
    gap: 8px;
  }

  &__routeKicker {
    color: var(--c-intro-neon);
    font-size: 10px;
    letter-spacing: 0.18em;
    text-transform: uppercase;
  }

  &__routeTitle {
    margin: 0;
    font-family: 'MerriweatherTravel', Georgia, serif;
    font-size: 26px;
    line-height: 1.08;
  }

  &__routeList {
    display: grid;
    gap: 14px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  &__routeItem {
    position: relative;
    display: grid;
    grid-template-columns: 34px minmax(0, 1fr);
    gap: 12px;

    &:not(:last-child)::after {
      content: '';
      position: absolute;
      top: 29px;
      bottom: -13px;
      left: 16px;
      width: 1px;
      background: linear-gradient(var(--c-intro-neon), rgba(248, 240, 223, 0.12));
    }
  }

  &__routeNumber {
    display: grid;
    place-items: center;
    width: 34px;
    height: 28px;
    border-radius: 999px;
    background: rgba(120, 243, 211, 0.12);
    color: var(--c-intro-neon);
    font-size: 12px;
    letter-spacing: 0.18em;
  }

  &__routePlace {
    display: grid;
    gap: 4px;
    min-width: 0;
  }

  &__routeText {
    overflow: hidden;
    font-family: Arial, sans-serif;
    font-size: 14px;
    font-weight: 700;
    line-height: 1.25;
    text-overflow: ellipsis;
  }

  &__routeMeta {
    overflow: hidden;
    color: rgba(248, 240, 223, 0.56);
    font-family: Arial, sans-serif;
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__routeSummary {
    display: grid;
    gap: 4px;
    padding: 14px;
    border: 1px solid rgba(255, 139, 74, 0.24);
    border-radius: 18px;
    background: rgba(255, 139, 74, 0.1);
  }

  &__routeTime {
    color: var(--c-intro-ember);
    font-size: 20px;
    line-height: 1;
  }

  &__routeSummaryText {
    color: rgba(248, 240, 223, 0.62);
    font-family: Arial, sans-serif;
    font-size: 12px;
    font-weight: 700;
    line-height: 1.3;
  }

  &__rain {
    width: 100%;
    height: 100%;
  }

  &--pixel {
    --c-intro-ink: #fff7d6;
    --c-intro-muted: #c8c1e8;
    --c-intro-neon: #39ff88;
    --c-intro-ember: #ffd23f;
    background:
      linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px),
      linear-gradient(135deg, #07071d 0%, #17123d 50%, #07071d 100%);
    background-size: 12px 12px, 12px 12px, auto;
    font-family: 'Courier New', Consolas, monospace;
    image-rendering: pixelated;

    &::after {
      content: '';
      position: fixed;
      inset: 0;
      z-index: 10;
      pointer-events: none;
      opacity: 0.16;
      background: repeating-linear-gradient(
        180deg,
        rgba(255, 255, 255, 0.15) 0,
        rgba(255, 255, 255, 0.15) 1px,
        transparent 1px,
        transparent 5px
      );
      mix-blend-mode: soft-light;
    }

    .c-intro__layer--back {
      filter: saturate(1.18) contrast(1.22) brightness(0.72);
    }

    .c-intro__shell,
    .c-intro__routeCard,
    .c-intro__projectStat,
    .c-intro__routeSummary,
    .c-intro__badge {
      border: 2px solid #fff7d6;
      border-radius: 0;
      background:
        linear-gradient(180deg, rgba(24, 20, 65, 0.92), rgba(8, 9, 31, 0.92)),
        linear-gradient(90deg, rgba(57, 255, 136, 0.12), transparent);
      box-shadow:
        5px 5px 0 #000,
        inset 0 0 0 2px rgba(57, 255, 136, 0.13);
      backdrop-filter: none;
    }

    .c-intro__shell {
      min-height: min(700px, calc(100vh - 64px));

      &::before {
        inset: 12px;
        border: 2px dashed rgba(57, 255, 136, 0.44);
        border-radius: 0;
      }

      &::after {
        border-radius: 0;
        background:
          linear-gradient(135deg, rgba(57, 255, 136, 0.13), transparent 32%),
          linear-gradient(315deg, rgba(255, 210, 63, 0.12), transparent 34%);
      }
    }

    .c-intro__brand,
    .c-intro__badge,
    .c-intro__routeKicker,
    .c-intro__routeNumber,
    .c-intro__projectValue {
      color: var(--c-intro-neon);
      text-shadow: 2px 2px 0 #000;
    }

    .c-intro__title,
    .c-intro__routeTitle,
    .c-intro__routeTime {
      font-family: 'Courier New', Consolas, monospace;
      font-weight: 900;
      letter-spacing: 0;
      text-shadow: 3px 3px 0 #000;
    }

    .c-intro__title {
      font-size: clamp(34px, 7vw, 92px);
      line-height: 0.98;
    }

    .c-intro__titleAccent {
      color: var(--c-intro-ember);
      -webkit-text-stroke: 0;
      text-shadow: 3px 3px 0 #000;
    }

    .c-intro__lead,
    .c-intro__hint,
    .c-intro__nav,
    .c-intro__projectLabel,
    .c-intro__routeText,
    .c-intro__routeMeta,
    .c-intro__routeSummaryText {
      font-family: 'Courier New', Consolas, monospace;
    }

    .c-intro__start {
      border: 2px solid #fff7d6;
      border-radius: 0;
      background: #17123d;
      box-shadow: 4px 4px 0 #000;
      color: var(--c-intro-ink);

      &::before {
        background: var(--c-intro-neon);
      }

      &:hover {
        border-color: #fff7d6;
        color: #07071d;
        transform: translate(-1px, -1px);
        box-shadow: 6px 6px 0 #000;
      }
    }

    .c-intro__startIcon {
      border-radius: 0;
      background: var(--c-intro-ember);
      box-shadow: 3px 3px 0 #000;
    }

    .c-intro__badgeDot {
      border-radius: 0;
      box-shadow: 0 0 0 2px #000, 0 0 18px var(--c-intro-neon);
    }

    .c-intro__routeNumber {
      border: 2px solid var(--c-intro-neon);
      border-radius: 0;
      background: #07071d;
      letter-spacing: 0;
    }

    .c-intro__routeItem:not(:last-child)::after {
      width: 2px;
      background: repeating-linear-gradient(
        180deg,
        var(--c-intro-neon) 0,
        var(--c-intro-neon) 5px,
        transparent 5px,
        transparent 9px
      );
    }
  }
}

@media (max-width: 860px) {
  .c-intro {
    overflow: auto;

    &__shell {
      grid-template-columns: 1fr;
      align-items: center;
      min-height: calc(100vh - 32px);
      margin: 16px 0;
    }

    &__nav {
      align-items: flex-start;
      flex-direction: column;
    }

    &__navLine {
      width: 96px;
      flex: none;
    }

    &__routeCard {
      align-self: stretch;
    }
  }
}
</style>
