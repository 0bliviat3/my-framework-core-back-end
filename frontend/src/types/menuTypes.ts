export interface MenuItem {
  id: number;
  name: string;
  path: string;
  icon?: string;
  sort: number;
  state: string;
  children?: MenuItem[];
}

export interface MenuTreeItem {
  id: number;
  name: string;
  path: string;
  icon?: string;
  sort: number;
  state: string;
  children: MenuTreeItem[];
}