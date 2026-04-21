
function formatPrice(price) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(price || 0);
}

// --- TOAST ---

function showToast(message, type = 'success') {
  let toastContainer = document.getElementById('toastContainer');

  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toastContainer';
    toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    document.body.appendChild(toastContainer);
  }

  const toastId = 'toast-' + Date.now();

  const toastHtml = `
    <div id="${toastId}" class="toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0">
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    </div>
  `;

  toastContainer.insertAdjacentHTML('beforeend', toastHtml);

  const toastElement = document.getElementById(toastId);
  const toast = new bootstrap.Toast(toastElement);
  toast.show();

  toastElement.addEventListener('hidden.bs.toast', () => {
    toastElement.remove();
  });
}

// --- VALIDAÇÃO DE ESTOQUE ---

function validarEstoque(qtdDesejada, qtdEstoque) {
  const desejada = parseInt(qtdDesejada);
  const disponivel = parseInt(qtdEstoque);

  if (desejada > disponivel) {
    showToast(`Estoque insuficiente! Temos apenas ${disponivel} unidades.`, 'error');
    return false;
  }

  if (desejada <= 0) {
    showToast("A quantidade deve ser maior que zero.", 'error');
    return false;
  }

  return true;
}

// --- AUTENTICAÇÃO ---

function isAuthenticated() {
  return !!localStorage.getItem('user');
}

function getCurrentUser() {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

function isAdmin() {
  const user = getCurrentUser();
  return user && (user.role === 'ADMIN' || user.role === 'admin');
}

async function logout() {
  try {
    await apiPost('/logout');
  } catch (error) {
    console.error('Erro no logout:', error);
  } finally {
    localStorage.removeItem('user');
    const basePath = window.location.pathname.includes('/pages/') ? '../' : '';
    window.location.href = basePath + 'pages/login.html';
  }
}

function requireAuth() {
  if (!isAuthenticated()) {
    const basePath = window.location.pathname.includes('/pages/') ? '../' : '';
    window.location.href = basePath + 'pages/login.html';
  }
}

function requireAdmin() {
  if (!isAdmin()) {
    const basePath = window.location.pathname.includes('/pages/') ? '../' : '';
    window.location.href = basePath + 'index.html';
  }
}

// --- UI USUÁRIO ---

document.addEventListener('DOMContentLoaded', () => {
  const user = getCurrentUser();

  if (user) {
    const userBtn = document.getElementById('userDropdown') || document.querySelector('.nav-link.dropdown-toggle');
    if (userBtn) userBtn.innerText = user.displayName || user.nome || user.login;

    const adminLink = document.getElementById('adminLink');
    if (isAdmin() && adminLink) {
      adminLink.classList.remove('d-none');
      adminLink.style.display = 'block';
    }
  }
});
async function apiPost(endpoint, data) {
    const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.erro || 'Erro na requisição');
    }

    return response.json();
}