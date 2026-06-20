export const toRadians = (degrees) => degrees * (Math.PI / 180)

export const getDistanceKm = (firstPoint, secondPoint) => {
  const [lat1, lon1] = firstPoint
  const [lat2, lon2] = secondPoint
  const earthRadiusKm = 6371
  const dLat = toRadians(lat2 - lat1)
  const dLon = toRadians(lon2 - lon1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * Math.sin(dLon / 2) ** 2
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

  return earthRadiusKm * c
}

export const createDistanceMatrix = (points) => points.map((firstPoint, firstIndex) => {
  return points.map((secondPoint, secondIndex) => {
    return firstIndex === secondIndex ? 0 : getDistanceKm(firstPoint, secondPoint)
  })
})

export const getRouteDistanceKm = (points) => {
  return points.reduce((distance, point, index) => {
    if (index === 0) {
      return distance
    }

    return distance + getDistanceKm(points[index - 1], point)
  }, 0)
}

export const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const restSeconds = Math.floor(seconds % 60)
  const formattedHours = String(hours).padStart(2, '0')
  const formattedMinutes = String(minutes).padStart(2, '0')
  const formattedSeconds = String(restSeconds).padStart(2, '0')

  return `${formattedHours}:${formattedMinutes}:${formattedSeconds}`
}

export const formatDistance = (distanceKm) => `${distanceKm.toFixed(1)} км`

export const parseHumanDistance = (value) => {
  const normalized = String(value).replace(',', '.').replace(/[^0-9.]/g, '')
  const distance = Number(normalized)

  return Number.isFinite(distance) ? distance : 0
}

export const createRouteResult = (points, freeTime, humanSpeed, visitAverageMinutes, externalDistance = null) => {
  const safeSpeed = humanSpeed > 0 ? humanSpeed : 1
  const distanceKm = typeof externalDistance === 'number' && externalDistance > 0 ? externalDistance : getRouteDistanceKm(points)
  const walkSeconds = (distanceKm / safeSpeed) * 3600
  const visitSeconds = points.length * visitAverageMinutes * 60
  const totalSeconds = walkSeconds + visitSeconds
  const canVisit = totalSeconds <= freeTime * 3600

  return {
    canVisit,
    distanceKm,
    totalSeconds,
    timeText: formatTime(totalSeconds),
    distanceText: formatDistance(distanceKm),
    message: canVisit
      ? `Вы успеваете пройти выбранный маршрут. Время: ${formatTime(totalSeconds)}. Дистанция: ${formatDistance(distanceKm)}.`
      : `Свободного времени недостаточно. Нужно: ${formatTime(totalSeconds)}. Дистанция: ${formatDistance(distanceKm)}.`
  }
}

export const createRotatedRoute = (points) => {
  if (points.length < 2) {
    return points
  }

  return [...points.slice(1), points[0]]
}

export const createDijkstraRoute = (points) => {
  if (points.length < 3) {
    return points
  }

  const matrix = createDistanceMatrix(points)
  const result = [0]
  const visited = new Set(result)

  while (result.length < points.length) {
    const currentIndex = result[result.length - 1]
    let nextIndex = -1
    let nextDistance = Infinity

    for (let i = 0; i < points.length; i += 1) {
      if (!visited.has(i) && matrix[currentIndex][i] < nextDistance) {
        nextDistance = matrix[currentIndex][i]
        nextIndex = i
      }
    }

    if (nextIndex === -1) {
      break
    }

    visited.add(nextIndex)
    result.push(nextIndex)
  }

  return result.map((index) => points[index])
}

export const findNearestUnusedMark = (points, marks) => {
  if (!points.length) {
    return null
  }

  const lastPoint = points[points.length - 1]
  let nearestMark = null
  let nearestDistance = Infinity

  for (let i = 0; i < marks.length; i += 1) {
    const mark = marks[i]
    const isUsed = points.some((point) => point[0] === mark[0] && point[1] === mark[1])

    if (!isUsed) {
      const distance = getDistanceKm(lastPoint, mark)

      if (distance < nearestDistance) {
        nearestDistance = distance
        nearestMark = mark
      }
    }
  }

  return nearestMark
}

export const getRoadRoute = (points) => {
  const coordinates = points
    .map((point) => `${Number(point[1])},${Number(point[0])}`)
    .join(';')
  const routeBaseUrl = import.meta.env.VITE_OSRM_ROUTE_URL || 'https://router.project-osrm.org'
  const url = `${routeBaseUrl}/route/v1/foot/${coordinates}?overview=full&geometries=geojson`

  return fetch(url)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`OSRM route status ${response.status}`)
      }

      return response.json()
    })
    .then((data) => {
      const route = data.routes && data.routes[0]

      if (!route) {
        throw new Error(data.message || 'OSRM route not found')
      }

      return {
        distanceKm: route.distance / 1000,
        durationSeconds: route.duration,
        points: route.geometry.coordinates.map((point) => [point[1], point[0]])
      }
    })
}
