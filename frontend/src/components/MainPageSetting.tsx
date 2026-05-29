import React, { useState } from 'react';

const MainPageSetting: React.FC = () => {
  const [mainPageSetting, setMainPageSetting] = useState({
    title: '내 홈페이지',
    description: '나만의 특별한 공간',
    theme: 'light'
  });
  
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setMainPageSetting(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    
    try {
      // 실제 API 호출 로직 (예시)
      // await axios.post('/main-setting', mainPageSetting);
      
      // 테스트 응답
      setTimeout(() => {
        setSaveSuccess(true);
        setSaving(false);
        
        // 성공 메시지 자동 숨기기
        setTimeout(() => {
          setSaveSuccess(false);
        }, 3000);
      }, 500);
    } catch (error) {
      console.error('메인화면 설정 저장 실패:', error);
      setSaving(false);
    }
  };

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">메인화면 설정</h2>
      
      <form onSubmit={handleSave} className="bg-white shadow-md rounded-lg p-6 max-w-2xl">
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="title">
            페이지 제목
          </label>
          <input
            type="text"
            id="title"
            name="title"
            value={mainPageSetting.title}
            onChange={handleInputChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            placeholder="페이지 제목을 입력하세요"
          />
        </div>

        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="description">
            페이지 설명
          </label>
          <textarea
            id="description"
            name="description"
            value={mainPageSetting.description}
            onChange={handleInputChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            placeholder="페이지 설명을 입력하세요"
            rows={3}
          />
        </div>

        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="theme">
            테마
          </label>
          <select
            id="theme"
            name="theme"
            value={mainPageSetting.theme}
            onChange={handleInputChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          >
            <option value="light">라이트 테마</option>
            <option value="dark">다크 테마</option>
            <option value="blue">블루 테마</option>
          </select>
        </div>

        <div className="flex items-center justify-between">
          <button
            type="submit"
            disabled={saving}
            className={`bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline ${saving ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {saving ? '저장 중...' : '설정 저장'}
          </button>
          
          {saveSuccess && (
            <div className="text-green-500 ml-4">
              설정이 성공적으로 저장되었습니다!
            </div>
          )}
        </div>
      </form>

      <div className="mt-8 bg-gray-50 p-4 rounded-lg">
        <h3 className="font-semibold mb-2">설정 미리보기</h3>
        <div className="border rounded p-4 bg-white">
          <h4 className="text-lg font-bold">{mainPageSetting.title}</h4>
          <p className="text-gray-600 mt-1">{mainPageSetting.description}</p>
          <div className="mt-2 text-sm text-gray-500">
            현재 테마: <span className="font-medium">{mainPageSetting.theme}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MainPageSetting;