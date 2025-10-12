export const defaultBusinessHour = {
  day: '',
  time: '',
  close: '',
};

export function createEmptyStore() {
  return {
    name: '',                 // required
    description: '',
    phone: '',
    email: '',
    address: '',              // required
    coordinates: [0, 0],      // [lng, lat], required
    businessHours: [],        // { day, time, close }[]
    isActive: true,
    updatedAt: new Date(),    // 送出時更新
  };
}
