import React, { useState } from 'react';

const GuestAccessSystem: React.FC = () => {
  const [guestMenus, setGuestMenus] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 게스트 접근 가능 메뉴 조회
  const fetchGuestAccessibleMenus = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // 실제 API 호출 로직 (예시)
      // const response = await axios.get('/menus/guest-accessible');
      // setGuestMenus(response.data);
      
      // 테스트 데이터
      setTimeout(() => {
        setGuestMenus([
          { id: 1, name: '공지사항', path: '/notice', sort: 1, state: 'A' },
          { id: 2, name: '자료실', path: '/documents', sort: 2, state: 'A' },
          { id: 3, name: 'FAQ', path: '/faq', sort: 3, state: 'A' }
        ]);
        setLoading(false);
      }, 500);
    } catch (err) {
      setError('게스트 접근 가능 메뉴 조회 실패');
      setLoading(false);
    }
  };

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">게스트 접근 가능 메뉴</h2>
      
      <div className="mb-4 flex space-x-2">
        <button 
          onClick={fetchGuestAccessibleMenus}
          disabled={loading}
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50"
        >
          {loading ? '로딩 중...' : '메뉴 목록 불러오기'}
        </button>
      </div>

      {error && <div className="text-red-500 mb-4">{error}</div>}

      <div className="border rounded-lg overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">메뉴명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">경로</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">정렬</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {guestMenus.map((menu) => (
              <tr key={menu.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{menu.name}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{menu.path}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{menu.sort}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{menu.state}</td>
              </tr>
            ))}
          </tbody>
        </table>
        
        {guestMenus.length === 0 && !loading && (
          <div className="text-center py-8 text-gray-500">
            게스트가 접근 가능한 메뉴가 없습니다.
          </div>
        )}
      </div>
    </div>
  );
};

export default GuestAccessSystem;