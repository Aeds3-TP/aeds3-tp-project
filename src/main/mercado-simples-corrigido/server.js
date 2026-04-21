const express = require('express');
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = 3001;

// Middleware
app.use(cors());
app.use(express.static(path.join(__dirname)));
app.use(express.json());

// Servir arquivos estáticos
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.get('/pages/:page', (req, res) => {
  res.sendFile(path.join(__dirname, 'pages', `${req.params.page}.html`));
});

app.get('/admin', (req, res) => {
  res.sendFile(path.join(__dirname, 'admin', 'index.html'));
});

// Mock API para demonstração
app.get('/api/categories', (req, res) => {
  res.json({
    items: [
      { id: 1, name: 'Eletrônicos', slug: 'eletronicos', icon: 'laptop' },
      { id: 2, name: 'Alimentos', slug: 'alimentos', icon: 'basket' },
      { id: 3, name: 'Roupas', slug: 'roupas', icon: 'bag' },
      { id: 4, name: 'Livros', slug: 'livros', icon: 'book' },
    ]
  });
});

app.get('/api/products', (req, res) => {
  const products = [
    {
      id: 1,
      name: 'Notebook Dell',
      price: 2499.90,
      originalPrice: 3299.90,
      imageUrl: 'https://via.placeholder.com/300x200?text=Notebook',
      stock: 5,
      categoryId: 1,
      categoryName: 'Eletrônicos',
      featured: true,
      createdAt: new Date()
    },
    {
      id: 2,
      name: 'Mouse Logitech',
      price: 89.90,
      originalPrice: 129.90,
      imageUrl: 'https://via.placeholder.com/300x200?text=Mouse',
      stock: 20,
      categoryId: 1,
      categoryName: 'Eletrônicos',
      featured: false,
      createdAt: new Date()
    },
    {
      id: 3,
      name: 'Teclado Mecânico',
      price: 349.90,
      originalPrice: 449.90,
      imageUrl: 'https://via.placeholder.com/300x200?text=Teclado',
      stock: 10,
      categoryId: 1,
      categoryName: 'Eletrônicos',
      featured: true,
      createdAt: new Date()
    },
    {
      id: 4,
      name: 'Monitor LG 24"',
      price: 799.90,
      originalPrice: 999.90,
      imageUrl: 'https://via.placeholder.com/300x200?text=Monitor',
      stock: 8,
      categoryId: 1,
      categoryName: 'Eletrônicos',
      featured: false,
      createdAt: new Date()
    },
    {
      id: 5,
      name: 'Webcam HD',
      price: 199.90,
      originalPrice: 279.90,
      imageUrl: 'https://via.placeholder.com/300x200?text=Webcam',
      stock: 15,
      categoryId: 1,
      categoryName: 'Eletrônicos',
      featured: false,
      createdAt: new Date()
    },
    {
      id: 6,
      name: 'Arroz Integral 5kg',
      price: 24.90,
      originalPrice: null,
      imageUrl: 'https://via.placeholder.com/300x200?text=Arroz',
      stock: 50,
      categoryId: 2,
      categoryName: 'Alimentos',
      featured: false,
      createdAt: new Date()
    },
    {
      id: 7,
      name: 'Feijão Carioca 1kg',
      price: 8.90,
      originalPrice: null,
      imageUrl: 'https://via.placeholder.com/300x200?text=Feijao',
      stock: 100,
      categoryId: 2,
      categoryName: 'Alimentos',
      featured: false,
      createdAt: new Date()
    },
    {
      id: 8,
      name: 'Camiseta Básica',
      price: 49.90,
      originalPrice: 79.90,
      imageUrl: 'https://via.placeholder.com/300x200?text=Camiseta',
      stock: 30,
      categoryId: 3,
      categoryName: 'Roupas',
      featured: false,
      createdAt: new Date()
    },
    {
      id: 9,
      name: 'Calça Jeans',
      price: 129.90,
      originalPrice: 179.90,
      imageUrl: 'https://via.placeholder.com/300x200?text=Calca',
      stock: 20,
      categoryId: 3,
      categoryName: 'Roupas',
      featured: true,
      createdAt: new Date()
    },
    {
      id: 10,
      name: 'Clean Code',
      price: 89.90,
      originalPrice: null,
      imageUrl: 'https://via.placeholder.com/300x200?text=Livro',
      stock: 12,
      categoryId: 4,
      categoryName: 'Livros',
      featured: false,
      createdAt: new Date()
    },
  ];

  const { search, categoryId, minPrice, maxPrice, sortBy = 'newest', limit = 24 } = req.query;

  let filtered = [...products];

  if (search) {
    filtered = filtered.filter(p => p.name.toLowerCase().includes(search.toLowerCase()));
  }

  if (categoryId) {
    filtered = filtered.filter(p => p.categoryId === parseInt(categoryId));
  }

  if (minPrice) {
    filtered = filtered.filter(p => p.price >= parseFloat(minPrice));
  }

  if (maxPrice) {
    filtered = filtered.filter(p => p.price <= parseFloat(maxPrice));
  }

  // Ordenação
  if (sortBy === 'price_asc') {
    filtered.sort((a, b) => a.price - b.price);
  } else if (sortBy === 'price_desc') {
    filtered.sort((a, b) => b.price - a.price);
  } else if (sortBy === 'name') {
    filtered.sort((a, b) => a.name.localeCompare(b.name));
  }

  const total = filtered.length;
  const items = filtered.slice(0, parseInt(limit));

  res.json({ items, total });
});

// Armazenar usuários em memória (para demo)
let users = [
  {
    id: 1,
    name: 'Admin User',
    email: 'admin@example.com',
    password: '123456',
    role: 'admin'
  },
  {
    id: 2,
    name: 'João Silva',
    email: 'joao@example.com',
    password: '123456',
    role: 'user'
  }
];

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  
  const user = users.find(u => u.email === email && u.password === password);
  
  if (!user) {
    return res.status(401).json({ error: 'Email ou senha inválidos' });
  }
  
  res.json({
    token: 'mock-token-' + Date.now(),
    user: {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role
    }
  });
});

app.post('/api/auth/register', (req, res) => {
  const { name, email, password } = req.body;
  
  if (users.find(u => u.email === email)) {
    return res.status(400).json({ error: 'Email já registrado' });
  }
  
  const newUser = {
    id: users.length + 1,
    name,
    email,
    password,
    role: 'user'
  };
  
  users.push(newUser);
  
  res.json({
    token: 'mock-token-' + Date.now(),
    user: {
      id: newUser.id,
      name: newUser.name,
      email: newUser.email,
      role: newUser.role
    }
  });
});

app.get('/api/favorites', (req, res) => {
  res.json({ items: [] });
});

app.post('/api/favorites/toggle', (req, res) => {
  res.json({ success: true });
});

app.get('/api/orders', (req, res) => {
  res.json({ items: [] });
});

app.post('/api/orders', (req, res) => {
  res.json({
    id: Math.floor(Math.random() * 10000),
    success: true
  });
});

app.get('/api/admin/dashboard', (req, res) => {
  res.json({
    totalProducts: 10,
    pendingOrders: 3,
    totalRevenue: 15000,
    totalUsers: 150
  });
});

app.get('/api/admin/orders', (req, res) => {
  res.json({
    items: [
      {
        id: 1,
        fullName: 'João Silva',
        email: 'joao@example.com',
        total: 299.90,
        status: 'pending',
        createdAt: new Date(),
        address: 'Rua X, 123'
      }
    ]
  });
});

app.listen(PORT, () => {
  console.log(`\n✅ Servidor rodando em http://localhost:${PORT}\n`);
  console.log(`📱 Acesse: http://localhost:${PORT}\n`);
});
