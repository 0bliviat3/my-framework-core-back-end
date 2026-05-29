import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { MenuItem } from '../types/menuTypes';

interface MenuState {
  menus: MenuItem[];
  loading: boolean;
  error: string | null;
}

const initialState: MenuState = {
  menus: [],
  loading: false,
  error: null,
};

const menuSlice = createSlice({
  name: 'menu',
  initialState,
  reducers: {
    setMenus: (state, action: PayloadAction<MenuItem[]>) => {
      state.menus = action.payload;
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
  },
});

export const { setMenus, setLoading, setError } = menuSlice.actions;
export default menuSlice.reducer;