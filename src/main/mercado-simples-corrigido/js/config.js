const API_BASE_URL = 'http://localhost:3000'; 

async function apiCall(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  try {
    const response = await fetch(url, {
      ...options,
      headers,
      credentials: 'include',
    });

    if (response.status === 204) return null;

    let data = {};
    try {
      data = await response.json();
    } catch (e) {
      data = {};
    }

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('user');
        const basePath = window.location.pathname.includes('/pages/') ? '../' : '';
        window.location.href = basePath + 'pages/login.html';
        return;
      }

      throw new Error(data.erro || data.message || `Erro ${response.status}`);
    }

    return data;
  } catch (error) {
    console.error('Erro na requisição:', error);
    throw error;
  }
}

async function apiGet(endpoint) {
  return apiCall(endpoint, { method: 'GET' });
}

async function apiPost(endpoint, data) {
  return apiCall(endpoint, {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

async function apiPut(endpoint, data) {
  return apiCall(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data)
  });
}

async function apiDelete(endpoint) {
  return apiCall(endpoint, {
    method: 'DELETE'
  });
}