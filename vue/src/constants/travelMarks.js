export const TRAVEL_PLACES = [
  {
    id: 'kremlin',
    title: 'Московский Кремль',
    category: 'История',
    coordinates: [55.752023, 37.617499],
    visitMinutes: 90,
    ticketStatus: 'Нужен билет',
    description: 'Главный исторический комплекс Москвы: соборы, стены, башни и Оружейная палата рядом.',
    highlight: 'Лучше закладывать больше часа'
  },
  {
    id: 'red-square',
    title: 'Красная площадь',
    category: 'Символ города',
    coordinates: [55.75393, 37.620795],
    visitMinutes: 35,
    ticketStatus: 'Свободный вход',
    description: 'Открытое сердце столицы между Кремлем, ГУМом и храмом Василия Блаженного.',
    highlight: 'Хорошая стартовая точка'
  },
  {
    id: 'st-basil',
    title: 'Храм Василия Блаженного',
    category: 'Архитектура',
    coordinates: [55.7525, 37.623056],
    visitMinutes: 50,
    ticketStatus: 'Нужен билет',
    description: 'Один из самых узнаваемых памятников русской архитектуры с музейной экспозицией внутри.',
    highlight: 'Яркая фото-точка'
  },
  {
    id: 'bolshoi',
    title: 'Большой театр',
    category: 'Культура',
    coordinates: [55.760133, 37.61865],
    visitMinutes: 30,
    ticketStatus: 'Билеты на спектакли',
    description: 'Историческая сцена, фасад и театральная площадь. Подходит для короткой остановки или вечера.',
    highlight: 'Можно добавить билетный модуль'
  },
  {
    id: 'tretyakov',
    title: 'Третьяковская галерея',
    category: 'Музей',
    coordinates: [55.741389, 37.620833],
    visitMinutes: 120,
    ticketStatus: 'Нужен билет',
    description: 'Главная коллекция русского искусства: иконы, классика, передвижники и авангард.',
    highlight: 'Лучше бронировать заранее'
  },
  {
    id: 'zaryadye',
    title: 'Парк Зарядье',
    category: 'Парк',
    coordinates: [55.751111, 37.628611],
    visitMinutes: 45,
    ticketStatus: 'Свободный вход',
    description: 'Современный парк рядом с Кремлем, парящий мост и видовые точки на центр Москвы.',
    highlight: 'Хорошо между Кремлем и набережной'
  },
  {
    id: 'pushkin',
    title: 'ГМИИ им. Пушкина',
    category: 'Музей',
    coordinates: [55.747222, 37.605],
    visitMinutes: 95,
    ticketStatus: 'Нужен билет',
    description: 'Крупный музей мирового искусства с постоянной коллекцией и временными выставками.',
    highlight: 'Удобно рядом с храмом Христа Спасителя'
  },
  {
    id: 'cathedral',
    title: 'Храм Христа Спасителя',
    category: 'Архитектура',
    coordinates: [55.744722, 37.605556],
    visitMinutes: 35,
    ticketStatus: 'Свободный вход',
    description: 'Крупнейший кафедральный храм Москвы и видовая зона у Патриаршего моста.',
    highlight: 'Отличная связка с Пушкиным'
  },
  {
    id: 'arbat',
    title: 'Старый Арбат',
    category: 'Прогулка',
    coordinates: [55.749444, 37.591389],
    visitMinutes: 60,
    ticketStatus: 'Свободный вход',
    description: 'Пешеходная улица с уличными музыкантами, кафе, сувенирами и старой городской атмосферой.',
    highlight: 'Хорош для финала маршрута'
  },
  {
    id: 'vdnh',
    title: 'ВДНХ',
    category: 'Парк',
    coordinates: [55.829823, 37.633086],
    visitMinutes: 140,
    ticketStatus: 'Есть платные павильоны',
    description: 'Большой выставочный парк с павильонами, фонтанами, музеями и Москвариумом.',
    highlight: 'Лучше строить отдельным блоком'
  },
  {
    id: 'moscow-city',
    title: 'Москва-Сити',
    category: 'Смотровая',
    coordinates: [55.749792, 37.536992],
    visitMinutes: 75,
    ticketStatus: 'Есть смотровые билеты',
    description: 'Деловой квартал с небоскребами, набережной и смотровыми площадками.',
    highlight: 'Красиво на закате'
  },
  {
    id: 'gorky-park',
    title: 'Парк Горького',
    category: 'Парк',
    coordinates: [55.7298, 37.6019],
    visitMinutes: 80,
    ticketStatus: 'Свободный вход',
    description: 'Большой городской парк на набережной с прогулочными зонами, музеоном и прокатами.',
    highlight: 'Хорошо для длинной прогулки'
  }
]

export const TRAVEL_MARKS = TRAVEL_PLACES.map((place) => place.coordinates)
