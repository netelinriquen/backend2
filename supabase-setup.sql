-- Script para configurar banco InkFlow no Supabase
-- Execute no SQL Editor do Supabase

-- Criar tabela de usuários
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Criar tabela de agendamentos
CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    servico VARCHAR(255) NOT NULL,
    data DATE NOT NULL,
    horario TIME NOT NULL,
    descricao TEXT,
    status VARCHAR(20) DEFAULT 'PENDENTE',
    user_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Criar usuário administrador
INSERT INTO users (nome, email, senha, is_admin) 
VALUES ('Administrador', 'admin@inkflow.com', 'admin123', true)
ON CONFLICT (email) DO NOTHING;

-- Criar índices para performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_bookings_data ON bookings(data);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);

-- Comentários das tabelas
COMMENT ON TABLE users IS 'Usuários do sistema InkFlow';
COMMENT ON TABLE bookings IS 'Agendamentos de tatuagens';

-- Verificar se tudo foi criado
SELECT 'Tabelas criadas com sucesso!' as status;