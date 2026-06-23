const DEFAULT_API_BASE_URL = 'http://localhost:8080/api/v1'

const getApiBaseUrl = () => {
  return (import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL).replace(/\/$/, '')
}

const requestJson = (path, options = {}) => {
  return fetch(`${getApiBaseUrl()}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  }).then((response) => {
    if (!response.ok) {
      return response.json()
        .catch(() => null)
        .then((errorBody) => {
          const message = errorBody?.error?.message || `API request failed: ${response.status}`
          throw new Error(message)
        })
    }

    return response.json()
  })
}

export const fetchTravelPlaces = () => {
  return requestJson('/places?limit=100').then((data) => data.items || [])
}

export const fetchPlaceCategories = () => {
  return requestJson('/place-categories').then((data) => data.items || [])
}

export const fetchSavedRoutes = () => {
  return requestJson('/routes').then((data) => data.items || [])
}

export const saveTravelRoute = (route) => {
  return requestJson('/routes', {
    method: 'POST',
    body: JSON.stringify(route)
  })
}
