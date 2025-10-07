# InkFlow API - Backend

API REST em Spring Boot para o estúdio de tatuagem InkFlow.

## 🚀 Tecnologias

- **Spring Boot 3.2** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **PostgreSQL** - Banco de dados (Supabase)
- **Maven** - Gerenciamento de dependências

## 📁 Estrutura

```
src/main/java/com/inkflow/api/
├── config/          # Configurações (CORS, Security)
├── controller/      # Controllers REST
├── dto/            # Data Transfer Objects
├── entity/         # Entidades JPA
├── repository/     # Repositórios JPA
└── service/        # Serviços de negócio
```

## 🛠️ Configuração

### 1. Variáveis de Ambiente

Copie `.env.example` para `.env` e configure:

```env
SUPABASE_DB_URL=jdbc:postgresql://db.your-project.supabase.co:5432/postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your-password
JWT_SECRET=your-jwt-secret-key
```

### 2. Executar

```bash
.\gradlew bootRun
```

**Ou se tiver Java instalado:**
```bash
java -jar build/libs/inkflow-api-1.0.0.jar
```

## 🌐 Endpoints

### Autenticação
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Cadastro

### Agendamentos
- `GET /api/bookings` - Listar agendamentos
- `POST /api/bookings` - Criar agendamento
- `PUT /api/bookings/{id}` - Atualizar agendamento
- `DELETE /api/bookings/{id}` - Excluir agendamento
- `PUT /api/bookings/{id}/status` - Atualizar status

## 🔧 CORS

Configurado para aceitar requisições de:
- `http://localhost:5173` (desenvolvimento)
- `https://*.vercel.app` (produção)
- `https://inkflow-studios.vercel.app` (domínio específico)

## 🗄️ Banco de Dados

### Tabelas criadas automaticamente:
- `users` - Usuários do sistema
- `bookings` - Agendamentos

### Usuário Admin padrão:
- Email: `admin@inkflow.com`
- Senha: `admin123`