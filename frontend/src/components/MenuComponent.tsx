import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { MenuService } from '../services/menuService';
import { setMenus, setLoading, setError } from '../store/menuSlice';
import { RootState } from '../store';

const MenuComponent: React.FC = () => {
  const dispatch = useDispatch();
  const menuState = useSelector((state: RootState) => state.menu);

  useEffect(() => {
    const fetchMenus = async () => {
      try {
        dispatch(setLoading(true));
        const menus = await MenuService.getMenuList();
        dispatch(setMenus(menus));
        dispatch(setLoading(false));
      } catch (error) {
        dispatch(setError('메뉴 로딩 실패'));
        dispatch(setLoading(false));
      }
    };

    fetchMenus();
  }, [dispatch]);

  if (menuState.loading) {
    return <div className="p-4">로딩 중...</div>;
  }

  if (menuState.error) {
    return <div className="p-4 text-red-500">{menuState.error}</div>;
  }

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">메뉴 목록</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {menuState.menus.map(menu => (
          <div key={menu.id} className="border rounded p-4 shadow-sm hover:shadow-md transition-shadow">
            <h3 className="font-semibold text-blue-600">{menu.name}</h3>
            <p className="text-gray-600 text-sm mt-1">{menu.path}</p>
            <p className="text-gray-500 text-xs mt-2">정렬: {menu.sort}, 상태: {menu.state}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MenuComponent;