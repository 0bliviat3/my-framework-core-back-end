import { configureStore } from '@reduxjs/toolkit';
import menuReducer from './menuSlice';

export const store = configureStore({
  reducer: {
    menu: menuReducer,
  },
});

// 추출된 타입들
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;