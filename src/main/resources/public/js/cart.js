// Gerenciamento de carrinho com localStorage
class Cart {
  constructor() {
    this.items = this.loadCart();
  }

  loadCart() {
    const cartData = localStorage.getItem('cart');
    // Garante que sempre retorne um array, mesmo se o JSON estiver corrompido
    try {
      return cartData ? JSON.parse(cartData) : [];
    } catch (e) {
      return [];
    }
  }

  saveCart() {
    localStorage.setItem('cart', JSON.stringify(this.items));
    this.updateCartUI();
  }

  addItem(product, quantity = 1) {
    const existingItem = this.items.find(item => item.id === product.id);
    
    // Normaliza campos para suportar tanto o formato fake API quanto o Java
    const name = product.nome || product.name;
    const price = product.preco || product.price;
    const imageUrl = (product.imagensUrls && product.imagensUrls.length > 0) ? product.imagensUrls[0] : product.imageUrl;
    const estoqueDisponivel = product.quantidadeEstoque;

    const quantidadeNoCarrinho = existingItem ? existingItem.quantity : 0;
    const quantidadeFutura = quantidadeNoCarrinho + quantity;

    // VALIDAÇÃO DE ESTOQUE (Usa a função do seu utils.js)
    if (typeof validarEstoque === 'function') {
      if (!validarEstoque(quantidadeFutura, estoqueDisponivel)) {
        return; 
      }
    }

    if (existingItem) {
      existingItem.quantity += quantity;
    } else {
      this.items.push({
        id: product.id,
        name: name,
        price: price, // Salvando como price para manter o padrão do getTotal
        imageUrl: imageUrl,
        quantity: quantity,
        quantidadeEstoque: estoqueDisponivel 
      });
    }

    this.saveCart();
    showToast(`${name} adicionado ao carrinho`);
  }

  removeItem(productId) {
    this.items = this.items.filter(item => item.id !== productId);
    this.saveCart();
    showToast('Produto removido do carrinho');
  }

  updateQuantity(productId, quantity) {
    const item = this.items.find(item => item.id === productId);
    if (item) {
      if (quantity <= 0) {
        this.removeItem(productId);
      } else {
        // TRAVA DE ESTOQUE NO BOTÃO "+"
        if (quantity > item.quantity) { // Se está tentando aumentar
          if (typeof validarEstoque === 'function') {
            if (!validarEstoque(quantity, item.quantidadeEstoque)) {
              return; 
            }
          }
        }
        item.quantity = quantity;
        this.saveCart();
      }
    }
  }

  getTotal() {
    // Corrigido para garantir que use o campo 'price' que salvamos no addItem
    return this.items.reduce((total, item) => {
      const valor = item.price || item.preco || 0;
      return total + (valor * item.quantity);
    }, 0);
  }

  getItemCount() {
    return this.items.reduce((count, item) => count + item.quantity, 0);
  }

  clear() {
    this.items = [];
    this.saveCart();
  }

  updateCartUI() {
    const cartCount = document.getElementById('cartCount');
    if (cartCount) {
      const count = this.getItemCount();
      cartCount.textContent = count;
      cartCount.style.display = count > 0 ? 'inline-block' : 'none';
    }
    this.renderCart();
  }

  renderCart() {
    const cartContainer = document.getElementById('cartItems');
    if (!cartContainer) return;

    if (this.items.length === 0) {
      cartContainer.innerHTML = '<p class="text-center text-muted">Carrinho vazio</p>';
      const cartTotal = document.getElementById('cartTotal');
      if (cartTotal) cartTotal.textContent = formatPrice(0);
      return;
    }

    cartContainer.innerHTML = this.items.map(item => `
      <div class="card mb-2">
        <div class="card-body p-2">
          <div class="row align-items-center">
            <div class="col-3">
              ${item.imageUrl ? `<img src="${item.imageUrl}" alt="${item.name}" class="img-fluid rounded" style="max-height: 60px;">` : '<div class="bg-light rounded d-flex align-items-center justify-content-center" style="height: 60px;"><i class="bi bi-image text-muted"></i></div>'}
            </div>
            <div class="col-4">
              <h6 class="mb-1 text-truncate" title="${item.name}">${item.name}</h6>
              <p class="mb-0 text-muted small">${formatPrice(item.price || item.preco)}</p>
            </div>
            <div class="col-3">
              <div class="input-group input-group-sm">
                <button class="btn btn-outline-secondary px-1" onclick="cart.updateQuantity(${item.id}, ${item.quantity - 1})">-</button>
                <input type="text" class="form-control text-center p-0" value="${item.quantity}" readonly>
                <button class="btn btn-outline-secondary px-1" onclick="cart.updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
              </div>
            </div>
            <div class="col-2 text-end">
              <button class="btn btn-sm text-danger" onclick="cart.removeItem(${item.id})">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    `).join('');

    const cartTotal = document.getElementById('cartTotal');
    if (cartTotal) {
      cartTotal.textContent = formatPrice(this.getTotal());
    }
  }
}

// Instância global do carrinho
const cart = new Cart();