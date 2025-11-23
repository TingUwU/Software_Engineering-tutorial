export default {
  namespaced: true,
  state: {
    allShops: [
      {
        id: "store001",
        name: "小王豆漿",
        description: "傳統中式早餐店",
        address: "台北市大同區重慶北路一段123號",
        businessHours: [
          { day: "Mon", start: "06:00", close: "14:00" },
          { day: "Tue", start: "06:00", close: "14:00" },
          { day: "Wed", start: "06:00", close: "14:00" },
          { day: "Thu", start: "06:00", close: "14:00" },
          { day: "Fri", start: "06:00", close: "14:00" },
          { day: "Sat", start: "06:00", close: "12:00" },
        ],
        menu: [
          { id: "item101", itemName: "豆漿", price: 20, description: "香濃豆漿", imgUrl: "", isAvailable: true, tag: "飲品" },
          { id: "item102", itemName: "燒餅", price: 25, description: "現烤燒餅", imgUrl: "", isAvailable: true, tag: "主食" },
          { id: "item103", itemName: "油條", price: 15, description: "酥脆油條", imgUrl: "", isAvailable: true, tag: "主食" },
          { id: "item104", itemName: "飯糰", price: 30, description: "古早味飯糰", imgUrl: "", isAvailable: true, tag: "主食" },
        ]
      },
      {
        id: "store002",
        name: "阿婆飯糰",
        description: "古早味飯糰專賣",
        address: "台北市大安區羅斯福路三段456號",
        businessHours: [
          { day: "Mon", start: "06:30", close: "13:00" },
          { day: "Tue", start: "06:30", close: "13:00" },
          { day: "Wed", start: "06:30", close: "13:00" },
          { day: "Thu", start: "06:30", close: "13:00" },
          { day: "Fri", start: "06:30", close: "13:00" },
        ],
        menu: [
          { id: "item201", itemName: "綜合飯糰", price: 35, description: "料多實在", imgUrl: "", isAvailable: true, tag: "主食" },
          { id: "item202", itemName: "素食飯糰", price: 30, description: "健康素食", imgUrl: "", isAvailable: true, tag: "主食" },
          { id: "item203", itemName: "米漿", price: 25, description: "濃郁米漿", imgUrl: "", isAvailable: true, tag: "飲品" },
        ]
      },
      {
        id: "store003",
        name: "早安美芝城",
        description: "西式早餐連鎖店",
        address: "台北市中正區中山南路789號",
        businessHours: [
          { day: "Mon", start: "06:00", close: "14:00" },
          { day: "Tue", start: "06:00", close: "14:00" },
          { day: "Wed", start: "06:00", close: "14:00" },
          { day: "Thu", start: "06:00", close: "14:00" },
          { day: "Fri", start: "06:00", close: "14:00" },
          { day: "Sat", start: "07:00", close: "14:00" },
          { day: "Sun", start: "07:00", close: "14:00" },
        ],
        menu: [
          { id: "item301", itemName: "火腿蛋吐司", price: 40, description: "經典組合", imgUrl: "", isAvailable: true, tag: "吐司" },
          { id: "item302", itemName: "奶茶", price: 30, description: "香濃奶茶", imgUrl: "", isAvailable: true, tag: "飲品" },
          { id: "item303", itemName: "薯餅", price: 25, description: "酥脆薯餅", imgUrl: "", isAvailable: true, tag: "配餐" },
          { id: "item304", itemName: "總匯三明治", price: 55, description: "豐盛總匯", imgUrl: "", isAvailable: true, tag: "三明治" },
        ]
      },
      {
        id: "store004",
        name: "拉亞漢堡",
        description: "美式漢堡專賣",
        address: "台北市信義區信義路五段321號",
        businessHours: [
          { day: "Mon", start: "07:00", close: "22:00" },
          { day: "Tue", start: "07:00", close: "22:00" },
          { day: "Wed", start: "07:00", close: "22:00" },
          { day: "Thu", start: "07:00", close: "22:00" },
          { day: "Fri", start: "07:00", close: "22:00" },
          { day: "Sat", start: "08:00", close: "22:00" },
          { day: "Sun", start: "08:00", close: "22:00" },
        ],
        menu: [
          { id: "item401", itemName: "豬肉漢堡", price: 50, description: "招牌豬肉漢堡", imgUrl: "", isAvailable: true, tag: "漢堡" },
          { id: "item402", itemName: "雞塊", price: 35, description: "炸雞塊", imgUrl: "", isAvailable: true, tag: "配餐" },
          { id: "item403", itemName: "薯條", price: 30, description: "黃金薯條", imgUrl: "", isAvailable: true, tag: "配餐" },
          { id: "item404", itemName: "可樂", price: 25, description: "冰涼可樂", imgUrl: "", isAvailable: true, tag: "飲品" },
        ]
      },
      {
        id: "c1",
        name: "大同包子",
        description: "傳統手工包子",
        address: "台北市大同區迪化街一段234號",
        businessHours: [
          { day: "Mon", start: "05:30", close: "12:00" },
          { day: "Tue", start: "05:30", close: "12:00" },
          { day: "Wed", start: "05:30", close: "12:00" },
          { day: "Thu", start: "05:30", close: "12:00" },
          { day: "Fri", start: "05:30", close: "12:00" },
          { day: "Sat", start: "05:30", close: "13:00" },
        ],
        menu: [
          { id: "c1d1", itemName: "肉包", price: 20, description: "傳統肉包", imgUrl: "", isAvailable: true, tag: "包子" },
          { id: "c1d2", itemName: "菜包", price: 18, description: "素菜包", imgUrl: "", isAvailable: true, tag: "包子" },
          { id: "c1d3", itemName: "豆漿", price: 15, description: "無糖豆漿", imgUrl: "", isAvailable: true, tag: "飲品" },
        ]
      },
      {
        id: "c2",
        name: "晨光饅頭",
        description: "手工饅頭專賣",
        address: "台北市中山區民生東路二段567號",
        businessHours: [
          { day: "Mon", start: "06:00", close: "13:00" },
          { day: "Tue", start: "06:00", close: "13:00" },
          { day: "Wed", start: "06:00", close: "13:00" },
          { day: "Thu", start: "06:00", close: "13:00" },
          { day: "Fri", start: "06:00", close: "13:00" },
        ],
        menu: [
          { id: "c2d1", itemName: "白饅頭", price: 12, description: "傳統白饅頭", imgUrl: "", isAvailable: true, tag: "饅頭" },
          { id: "c2d2", itemName: "黑糖饅頭", price: 15, description: "香甜黑糖", imgUrl: "", isAvailable: true, tag: "饅頭" },
          { id: "c2d3", itemName: "芝麻包", price: 18, description: "芝麻餡", imgUrl: "", isAvailable: true, tag: "包子" },
        ]
      },
      {
        id: "w1",
        name: "美式早餐坊",
        description: "道地美式早午餐",
        address: "台北市松山區南京東路三段890號",
        businessHours: [
          { day: "Mon", start: "07:00", close: "15:00" },
          { day: "Tue", start: "07:00", close: "15:00" },
          { day: "Wed", start: "07:00", close: "15:00" },
          { day: "Thu", start: "07:00", close: "15:00" },
          { day: "Fri", start: "07:00", close: "15:00" },
          { day: "Sat", start: "08:00", close: "16:00" },
          { day: "Sun", start: "08:00", close: "16:00" },
        ],
        menu: [
          { id: "w1d1", itemName: "培根蛋捲", price: 80, description: "美式蛋捲", imgUrl: "", isAvailable: true, tag: "早午餐" },
          { id: "w1d2", itemName: "法式吐司", price: 75, description: "經典法式", imgUrl: "", isAvailable: true, tag: "早午餐" },
          { id: "w1d3", itemName: "美式咖啡", price: 50, description: "香濃咖啡", imgUrl: "", isAvailable: true, tag: "飲品" },
        ]
      },
      {
        id: "w2",
        name: "早午餐咖啡館",
        description: "精緻早午餐與咖啡",
        address: "台北市大安區敦化南路一段654號",
        businessHours: [
          { day: "Mon", start: "08:00", close: "17:00" },
          { day: "Tue", start: "08:00", close: "17:00" },
          { day: "Wed", start: "08:00", close: "17:00" },
          { day: "Thu", start: "08:00", close: "17:00" },
          { day: "Fri", start: "08:00", close: "17:00" },
          { day: "Sat", start: "09:00", close: "18:00" },
          { day: "Sun", start: "09:00", close: "18:00" },
        ],
        menu: [
          { id: "w2d1", itemName: "班尼迪克蛋", price: 120, description: "經典班尼迪克", imgUrl: "", isAvailable: true, tag: "早午餐" },
          { id: "w2d2", itemName: "酪梨沙拉", price: 100, description: "新鮮酪梨", imgUrl: "", isAvailable: true, tag: "沙拉" },
          { id: "w2d3", itemName: "拿鐵咖啡", price: 80, description: "義式拿鐵", imgUrl: "", isAvailable: true, tag: "飲品" },
        ]
      }
    ]
  },
  getters: {
    // 獲取所有店家
    allShops: state => state.allShops,
    // 根據ID獲取單個店家
    getShopById: state => id => {
      return state.allShops.find(shop => shop.id === id)
    },
    // 根據菜單項目ID獲取商品信息
    getMenuItem: state => (shopId, itemId) => {
      const shop = state.allShops.find(s => s.id === shopId)
      if (shop) {
        return shop.menu.find(item => item.id === itemId)
      }
      return null
    }
  },
  mutations: {},
  actions: {}
}

