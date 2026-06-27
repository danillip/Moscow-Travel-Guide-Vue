const DEFAULT_API_BASE_URL = '/api/v1'
const ACCESS_TOKEN_KEY = 'mtg_access_token'
const REFRESH_TOKEN_KEY = 'mtg_refresh_token'

export class ApiRequestError extends Error {
  constructor(message, response, errorBody = null) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = response.status
    this.code = errorBody?.error?.code || 'API_ERROR'
    this.details = errorBody?.error?.details || []
  }
}

const getApiBaseUrl = () => {
  return (import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL).replace(/\/$/, '')
}

const getStoredToken = (key) => {
  if (typeof window === 'undefined') {
    return ''
  }

  return window.localStorage.getItem(key) || ''
}

const setStoredToken = (key, token) => {
  if (typeof window !== 'undefined' && token) {
    window.localStorage.setItem(key, token)
  }
}

export const getAccessToken = () => getStoredToken(ACCESS_TOKEN_KEY)

export const getRefreshToken = () => getStoredToken(REFRESH_TOKEN_KEY)

export const hasApiAccessToken = () => Boolean(getAccessToken())

export const storeAuthTokens = ({ accessToken, refreshToken }) => {
  setStoredToken(ACCESS_TOKEN_KEY, accessToken)
  setStoredToken(REFRESH_TOKEN_KEY, refreshToken)
}

export const clearAuthTokens = () => {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.removeItem(ACCESS_TOKEN_KEY)
  window.localStorage.removeItem(REFRESH_TOKEN_KEY)
}

const buildQueryString = (params = {}) => {
  const query = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      query.set(key, String(value))
    }
  })

  const text = query.toString()
  return text ? `?${text}` : ''
}

const parseResponseJson = (response) => {
  if (response.status === 204) {
    return Promise.resolve(null)
  }

  return response.text().then((text) => {
    if (!text) {
      return null
    }

    return JSON.parse(text)
  })
}

const requestJson = (path, options = {}) => {
  const {
    body,
    headers = {},
    auth = false,
    ...fetchOptions
  } = options

  const token = getAccessToken()
  const requestHeaders = {
    Accept: 'application/json',
    ...headers
  }

  const requestOptions = {
    ...fetchOptions,
    headers: requestHeaders
  }

  if (body !== undefined) {
    requestHeaders['Content-Type'] = 'application/json'
    requestOptions.body = typeof body === 'string' ? body : JSON.stringify(body)
  }

  if (auth && token) {
    requestHeaders.Authorization = `Bearer ${token}`
  }

  return fetch(`${getApiBaseUrl()}${path}`, requestOptions)
    .then((response) => {
      return parseResponseJson(response)
        .catch(() => null)
        .then((data) => {
          if (!response.ok) {
            const message = data?.error?.message || `Ошибка API: ${response.status}`
            throw new ApiRequestError(message, response, data)
          }

          return data
        })
    })
}

export const fetchTravelPlaces = (params = {}) => {
  return requestJson(`/places${buildQueryString({ limit: 100, ...params })}`)
    .then((data) => data?.items || [])
}

export const fetchPlaceCategories = () => {
  return requestJson('/place-categories').then((data) => data?.items || [])
}

export const fetchPlaceById = (placeId) => {
  return requestJson(`/places/${encodeURIComponent(placeId)}`)
}

export const signUp = (payload) => {
  return requestJson('/sign-up', {
    method: 'POST',
    body: payload
  }).then((data) => {
    storeAuthTokens(data)
    return data
  })
}

export const signIn = (payload) => {
  return requestJson('/sign-in', {
    method: 'POST',
    body: payload
  }).then((data) => {
    storeAuthTokens(data)
    return data
  })
}

export const refreshSession = () => {
  return requestJson('/refresh', {
    method: 'POST',
    body: {
      refreshToken: getRefreshToken()
    }
  }).then((data) => {
    storeAuthTokens(data)
    return data
  })
}

export const fetchCurrentUser = () => {
  return requestJson('/me', { auth: true }).then((data) => data?.user || null)
}

export const signOut = () => {
  return requestJson('/sign-out', {
    method: 'POST',
    auth: true,
    body: {
      refreshToken: getRefreshToken()
    }
  }).finally(() => {
    clearAuthTokens()
  })
}

export const fetchSavedRoutes = (params = {}) => {
  return requestJson(`/routes${buildQueryString({ limit: 20, ...params })}`, { auth: true })
    .then((data) => data?.items || [])
}

export const saveTravelRoute = (route) => {
  return requestJson('/routes', {
    method: 'POST',
    auth: true,
    body: route
  })
}
