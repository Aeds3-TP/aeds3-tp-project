# MercadoFácil - Frontend Simples

Frontend simples em HTML, CSS, JavaScript e Bootstrap para conectar ao seu backend.

## Estrutura de Pastas

```
mercado-simples/
├── index.html              # Página principal (home)
├── css/
│   └── style.css          # Estilos globais
├── js/
│   ├── config.js          # Configuração da API
│   ├── utils.js           # Funções utilitárias
│   └── cart.js            # Gerenciamento do carrinho
├── pages/
│   ├── login.html         # Página de login
│   ├── checkout.html      # Página de checkout
│   ├── favorites.html     # Página de favoritos
│   └── orders.html        # Histórico de pedidos
└── admin/
    └── index.html         # Painel administrativo
```

## Como Usar

### 1. Configurar a URL da API

Abra o arquivo `js/config.js` e altere a URL da API:

```javascript
const API_BASE_URL = 'http://localhost:3000/api'; // Mude para sua URL
```

### 2. Servir os arquivos

Você pode servir os arquivos de várias formas:

**Opção 1: Python (simples)**
```bash
cd /home/ubuntu/mercado-simples
python3 -m http.server 8000
```

**Opção 2: Node.js (http-server)**
```bash
npm install -g http-server
cd /home/ubuntu/mercado-simples
http-server -p 8000
```

**Opção 3: Live Server (VS Code)**
- Instale a extensão "Live Server"
- Clique com botão direito em `index.html` → "Open with Live Server"

### 3. Acessar a aplicação

Abra no navegador: `http://localhost:8000`

## Endpoints da API Esperados

O frontend espera os seguintes endpoints no seu backend:

### Autenticação
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout

### Produtos
- `GET /api/products` - Listar produtos
- `GET /api/products/:id` - Obter produto
- `POST /api/products` - Criar produto (admin)
- `PUT /api/products/:id` - Atualizar produto (admin)
- `DELETE /api/products/:id` - Deletar produto (admin)

### Categorias
- `GET /api/categories` - Listar categorias
- `POST /api/categories` - Criar categoria (admin)
- `PUT /api/categories/:id` - Atualizar categoria (admin)
- `DELETE /api/categories/:id` - Deletar categoria (admin)

### Favoritos
- `GET /api/favorites` - Listar favoritos
- `POST /api/favorites/toggle` - Adicionar/remover favorito
- `DELETE /api/favorites/:productId` - Remover favorito

### Pedidos
- `GET /api/orders` - Listar pedidos do usuário
- `POST /api/orders` - Criar pedido
- `GET /api/admin/orders` - Listar todos os pedidos (admin)

### Admin
- `GET /api/admin/dashboard` - Dados do dashboard

## Formato de Resposta Esperado

### Produtos
```json
{
  "items": [
    {
      "id": 1,
      "name": "Produto",
      "price": 99.90,
      "originalPrice": 129.90,
      "imageUrl": "https://...",
      "stock": 10,
      "categoryId": 1,
      "categoryName": "Categoria",
      "featured": false,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "total": 25
}
```

### Categorias
```json
{
  "items": [
    {
      "id": 1,
      "name": "Eletrônicos",
      "slug": "eletronicos",
      "icon": "laptop"
    }
  ]
}
```

### Pedidos
```json
{
  "items": [
    {
      "id": 1,
      "fullName": "João Silva",
      "email": "joao@example.com",
      "total": 299.90,
      "status": "pending",
      "createdAt": "2024-01-01T00:00:00Z",
      "address": "Rua X, 123",
      "items": [
        {
          "productId": 1,
          "quantity": 2,
          "price": 99.90
        }
      ]
    }
  ]
}
```

## Autenticação

O frontend armazena o token no `localStorage`:

```javascript
localStorage.setItem('authToken', token);
localStorage.setItem('user', JSON.stringify(user));
```

O token é enviado em todas as requisições no header:
```
Authorization: Bearer <token>
```

## Carrinho

O carrinho é armazenado no `localStorage` e sincronizado automaticamente:

```javascript
// Adicionar item
cart.addItem({ id: 1, name: 'Produto', price: 99.90 });

// Remover item
cart.removeItem(1);

// Atualizar quantidade
cart.updateQuantity(1, 3);

// Obter total
cart.getTotal();
```

## Customização

### Cores
Edite `css/style.css`:
```css
:root {
  --primary-color: #B8860B;    /* Cor principal */
  --secondary-color: #DAA520;  /* Cor secundária */
  --dark-color: #2c3e50;
  --light-color: #ecf0f1;
}
```

### Fontes
Edite `css/style.css` para mudar a fonte:
```css
body {
  font-family: 'Sua Fonte', sans-serif;
}
```

## Funcionalidades

✅ Login/Logout
✅ Catálogo de produtos com busca
✅ Filtros por categoria e preço
✅ Carrinho com localStorage
✅ Checkout simples
✅ Favoritos
✅ Histórico de pedidos
✅ Painel administrativo
✅ Responsivo (mobile-friendly)

## Notas Importantes

1. **CORS**: Se o backend estiver em outro domínio, configure CORS no backend
2. **Token**: O token deve ser válido por toda a sessão
3. **Carrinho**: É armazenado localmente, não sincroniza com o servidor automaticamente
4. **Admin**: Apenas usuários com `role: 'admin'` podem acessar o painel

## Suporte

Para dúvidas ou problemas, verifique:
1. Se a URL da API está correta em `js/config.js`
2. Se o backend está rodando
3. Se o CORS está configurado
4. Se os endpoints retornam o formato esperado

---

**MercadoFácil** - Loja Virtual Simples
