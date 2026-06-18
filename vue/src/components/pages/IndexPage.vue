<template>
  <main class="c-intro" @mousemove="(event) => onMouseMove(event)">
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
                <button class="c-intro__start" type="button" @click="() => startTravel()">
                  <span class="c-intro__startText">Открыть карту</span>
                  <span class="c-intro__startIcon">→</span>
                </button>
                <span class="c-intro__hint">ЛКМ по карте ставит метки маршрута</span>
              </div>
            </div>

            <aside class="c-intro__routeCard">
              <span class="c-intro__routeNumber">01</span>
              <span class="c-intro__routeText">Выбирай точки</span>
              <span class="c-intro__routeLine"></span>
              <span class="c-intro__routeNumber">02</span>
              <span class="c-intro__routeText">Строй путь</span>
              <span class="c-intro__routeLine"></span>
              <span class="c-intro__routeNumber">03</span>
              <span class="c-intro__routeText">Сравнивай альтернативы</span>
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
import { ROUTES } from '@/router/index.js'
import layerBack from '@/assets/travel/images/layer-1.jpg'
import layerMiddle from '@/assets/travel/images/layer-2.png'
import layerFront from '@/assets/travel/images/layer-5.png'
import layerFog from '@/assets/travel/images/layer-6.png'

export default {
  name: 'IndexPage',
  data() {
    return {
      moveX: 0,
      moveY: 0,
      rainItems: [],
      rainRafId: null,
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
    }
  },
  mounted() {
    this.setupRain()
    window.addEventListener('resize', this.setupRain)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.setupRain)

    if (this.rainRafId) {
      window.cancelAnimationFrame(this.rainRafId)
    }
  },
  methods: {
    onMouseMove(event) {
      this.moveX = (event.clientX - window.innerWidth / 2) * -0.005
      this.moveY = (event.clientY - window.innerHeight / 2) * 0.01
    },
    startTravel() {
      this.$router.push({ name: ROUTES.MAP })
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
    grid-template-columns: minmax(0, 1fr) 230px;
    align-items: end;
    gap: 34px;
    width: min(1180px, calc(100vw - 48px));
    min-height: min(720px, calc(100vh - 64px));
    padding: clamp(24px, 4vw, 52px);
    border: 1px solid rgba(248, 240, 223, 0.18);
    border-radius: 34px;
    background:
      linear-gradient(110deg, rgba(5, 7, 8, 0.48), rgba(5, 7, 8, 0.18)),
      radial-gradient(circle at 88% 12%, rgba(255, 139, 74, 0.2), transparent 32%);
    box-shadow: 0 30px 120px rgba(0, 0, 0, 0.42);
    backdrop-filter: blur(4px);

    &::before {
      content: '';
      position: absolute;
      inset: 18px;
      border: 1px solid rgba(248, 240, 223, 0.1);
      border-radius: 26px;
      pointer-events: none;
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
    max-width: 820px;
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

  &__routeCard {
    position: relative;
    z-index: 1;
    display: grid;
    gap: 10px;
    align-self: center;
    padding: 20px;
    border: 1px solid rgba(248, 240, 223, 0.16);
    border-radius: 24px;
    background: rgba(5, 7, 8, 0.5);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
  }

  &__routeNumber {
    color: var(--c-intro-neon);
    font-size: 12px;
    letter-spacing: 0.18em;
  }

  &__routeText {
    font-family: Arial, sans-serif;
    font-size: 14px;
    font-weight: 700;
  }

  &__routeLine {
    width: 1px;
    height: 34px;
    margin-left: 8px;
    background: linear-gradient(var(--c-intro-neon), rgba(248, 240, 223, 0.18));
  }

  &__rain {
    width: 100%;
    height: 100%;
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
      grid-template-columns: auto 1fr;
      align-self: stretch;
    }

    &__routeLine {
      display: none;
    }
  }
}
</style>
