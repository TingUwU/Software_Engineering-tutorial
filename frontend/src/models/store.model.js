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
    phone: '',
    email: '',
    address: '',              // required
    coordinates: [0, 0],      // [lng, lat], required
    businessHours: [],        // { day, start, end, note }[]
    isActive: true,
    updatedAt: new Date(),    // 送出時更新
  };
}
