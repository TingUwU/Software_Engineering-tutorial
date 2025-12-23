export const defaultBusinessHour = {
  day: '',           // 例如：星期一
  start: '',    // HH:MM
  end: '',      // HH:MM
  note: '',         // 備註/公休
};

export function createEmptyStore() {
  return {
    name: '',                 // required
    description: '',
    ownerId: '',              // required (from logged-in user)
    phone: '',
    email: '',
    address: '',              // required
    category: '',             // 店家類別（中式/西式）
    coordinates: [0, 0],      // [lng, lat], required
    businessHours: [],        // { day, start, end, note }[]
    avatar: '',               // 商家頭貼
    menu: [],                 // 菜單（MenuItem[]）
    isActive: true,
    updatedAt: new Date(),    // 送出時更新
  };
}
