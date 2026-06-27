const UI_THEME_COOKIE = 'mtg_ui_theme'
const UI_THEME_MAX_AGE = 60 * 60 * 24 * 365

export const UI_THEMES = [
  {
    value: 'glass',
    title: 'Стекло',
    description: 'Основной'
  },
  {
    value: 'pixel',
    title: '8-bit',
    description: 'Arcade'
  }
]

export const DEFAULT_UI_THEME = UI_THEMES[0].value

export const getStoredUiTheme = () => {
  if (typeof document === 'undefined') {
    return DEFAULT_UI_THEME
  }

  const cookie = document.cookie
    .split('; ')
    .find((item) => item.startsWith(`${UI_THEME_COOKIE}=`))

  const value = cookie ? decodeURIComponent(cookie.split('=').slice(1).join('=')) : DEFAULT_UI_THEME
  return UI_THEMES.some((theme) => theme.value === value) ? value : DEFAULT_UI_THEME
}

export const setStoredUiTheme = (value) => {
  if (typeof document === 'undefined') {
    return
  }

  const theme = UI_THEMES.some((item) => item.value === value) ? value : DEFAULT_UI_THEME
  document.cookie = `${UI_THEME_COOKIE}=${encodeURIComponent(theme)}; max-age=${UI_THEME_MAX_AGE}; path=/; SameSite=Lax`
  window.dispatchEvent(new CustomEvent('mtg-ui-theme-change', { detail: { theme } }))
}

export const getUiThemeClass = (value, blockName) => {
  return value === 'pixel' ? `${blockName}--pixel` : ''
}
