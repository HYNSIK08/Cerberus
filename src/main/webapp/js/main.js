// CSRF 토큰을 AJAX fetch 요청에 자동 포함
(function () {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    if (!csrfMeta) return;
    const token = csrfMeta.getAttribute('content');

    const origFetch = window.fetch;
    window.fetch = function (input, init = {}) {
        const method = (init.method || 'GET').toUpperCase();
        if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
            init.headers = init.headers || {};
            init.headers['X-CSRF-Token'] = token;
        }
        return origFetch(input, init);
    };
})();

// 테이블 행 클릭 시 상세 정보 표시 (선택적)
document.querySelectorAll('.data-table tbody tr').forEach(row => {
    row.style.cursor = 'default';
});
