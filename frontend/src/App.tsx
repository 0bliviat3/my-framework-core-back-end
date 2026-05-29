import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MenuComponent from './components/MenuComponent';
import GuestAccessSystem from './components/GuestAccessSystem';
import MainPageSetting from './components/MainPageSetting';

const App: React.FC = () => {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <nav className="bg-blue-600 text-white p-4 shadow-md">
          <div className="container mx-auto flex justify-between items-center">
            <h1 className="text-xl font-bold">Framework Core Back-end</h1>
            <div className="flex space-x-4">
              <a href="/" className="hover:bg-blue-700 px-3 py-1 rounded">홈</a>
              <a href="/guest-access" className="hover:bg-blue-700 px-3 py-1 rounded">게스트 접근</a>
              <a href="/main-setting" className="hover:bg-blue-700 px-3 py-1 rounded">메인화면 설정</a>
            </div>
          </div>
        </nav>
        
        <main className="container mx-auto p-4">
          <Routes>
            <Route path="/" element={<MenuComponent />} />
            <Route path="/guest-access" element={<GuestAccessSystem />} />
            <Route path="/main-setting" element={<MainPageSetting />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
};

export default App;