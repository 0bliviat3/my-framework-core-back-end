import axios from 'axios';
import { MenuItem, MenuTreeItem } from '../types/menuTypes';

const API_BASE_URL = 'http://localhost:8080'; // 백엔드 API 주소

export class MenuService {
  // 메뉴 목록 조회
  static async getMenuList(): Promise<MenuItem[]> {
    try {
      const response = await axios.get<MenuItem[]>(`${API_BASE_URL}/menus`);
      return response.data;
    } catch (error) {
      console.error('메뉴 목록 조회 실패:', error);
      throw error;
    }
  }

  // 메뉴 트리 구조 조회
  static async getMenuTree(): Promise<MenuTreeItem[]> {
    try {
      const response = await axios.get<MenuTreeItem[]>(`${API_BASE_URL}/menus/tree`);
      return response.data;
    } catch (error) {
      console.error('메뉴 트리 조회 실패:', error);
      throw error;
    }
  }

  // 게스트 접근 가능 메뉴 조회
  static async getGuestAccessibleMenus(): Promise<MenuItem[]> {
    try {
      const response = await axios.get<MenuItem[]>(`${API_BASE_URL}/menus/guest-accessible`);
      return response.data;
    } catch (error) {
      console.error('게스트 접근 가능 메뉴 조회 실패:', error);
      throw error;
    }
  }
}