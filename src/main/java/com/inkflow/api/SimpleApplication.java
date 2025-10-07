package com.inkflow.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleApplication {
    private static Connection db;
    private static Map<String, User> users = new ConcurrentHashMap<>();
    private static Map<Long, Booking> bookings = new ConcurrentHashMap<>();
    private static Long bookingIdCounter = 1L;

    public static void main(String[] args) throws Exception {
        connectToSupabase();
        createTables();
        createAdminUser();
        
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/", new CorsHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/bookings", new BookingsHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("InkFlow API rodando na porta " + port);
    }

    static void connectToSupabase() {
        try {
            String url = System.getenv().getOrDefault("SUPABASE_DB_URL", "jdbc:postgresql://localhost:5432/inkflow");
            String user = System.getenv().getOrDefault("SUPABASE_DB_USER", "postgres");
            String password = System.getenv().getOrDefault("SUPABASE_DB_PASSWORD", "password");
            
            Class.forName("org.postgresql.Driver");
            db = DriverManager.getConnection(url, user, password);
            System.out.println("Conectado ao Supabase!");
        } catch (Exception e) {
            System.out.println("Erro ao conectar Supabase: " + e.getMessage());
            System.out.println("Usando memória local...");
        }
    }

    static void createTables() {
        if (db == null) return;
        try {
            Statement stmt = db.createStatement();
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY," +
                "nome VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) UNIQUE NOT NULL," +
                "senha VARCHAR(255) NOT NULL," +
                "telefone VARCHAR(20)," +
                "is_admin BOOLEAN DEFAULT FALSE" +
                ")"
            );
            
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "id SERIAL PRIMARY KEY," +
                "nome VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) NOT NULL," +
                "telefone VARCHAR(20) NOT NULL," +
                "servico VARCHAR(255) NOT NULL," +
                "data DATE NOT NULL," +
                "horario TIME NOT NULL," +
                "descricao TEXT," +
                "status VARCHAR(20) DEFAULT 'PENDENTE'" +
                ")"
            );
            
            System.out.println("Tabelas criadas/verificadas!");
        } catch (Exception e) {
            System.out.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    static void createAdminUser() {
        if (db == null) {
            users.put("admin@inkflow.com", new User("Administrador", "admin@inkflow.com", "admin123", "", true));
            return;
        }
        
        try {
            PreparedStatement stmt = db.prepareStatement(
                "INSERT INTO users (nome, email, senha, is_admin) VALUES (?, ?, ?, ?) ON CONFLICT (email) DO NOTHING"
            );
            stmt.setString(1, "Administrador");
            stmt.setString(2, "admin@inkflow.com");
            stmt.setString(3, "admin123");
            stmt.setBoolean(4, true);
            stmt.executeUpdate();
            System.out.println("Usuário admin criado/verificado!");
        } catch (Exception e) {
            System.out.println("Erro ao criar admin: " + e.getMessage());
        }
    }

    static class CorsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }
            
            String response = "InkFlow API";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }
            
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
                return;
            }

            String body = readBody(exchange);
            Map<String, String> loginData = parseJson(body);
            
            String email = loginData.get("email");
            String senha = loginData.get("senha");
            
            User user = getUserFromDB(email);
            if (user != null && user.senha.equals(senha)) {
                String response = String.format(
                    "{\"token\":\"fake-jwt-%s\",\"user\":{\"id\":1,\"nome\":\"%s\",\"email\":\"%s\",\"isAdmin\":%s}}",
                    email, user.nome, user.email, user.isAdmin
                );
                sendJsonResponse(exchange, 200, response);
            } else {
                sendJsonResponse(exchange, 400, "{\"message\":\"Credenciais inválidas\"}");
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }
            
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
                return;
            }

            String body = readBody(exchange);
            Map<String, String> userData = parseJson(body);
            
            String email = userData.get("email");
            if (getUserFromDB(email) != null) {
                sendJsonResponse(exchange, 400, "{\"message\":\"Email já cadastrado\"}");
                return;
            }
            
            User user = new User(userData.get("nome"), email, userData.get("senha"), userData.get("telefone"), false);
            saveUserToDB(user);
            
            String response = String.format(
                "{\"token\":\"fake-jwt-%s\",\"user\":{\"id\":1,\"nome\":\"%s\",\"email\":\"%s\",\"isAdmin\":false}}",
                email, user.nome, user.email
            );
            sendJsonResponse(exchange, 200, response);
        }
    }

    static class BookingsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }
            
            String method = exchange.getRequestMethod();
            
            if ("GET".equals(method)) {
                String bookingsJson = getBookingsFromDB();
                sendJsonResponse(exchange, 200, bookingsJson);
                
            } else if ("POST".equals(method)) {
                String body = readBody(exchange);
                Map<String, String> bookingData = parseJson(body);
                
                Booking booking = new Booking();
                booking.nome = bookingData.get("nome");
                booking.email = bookingData.get("email");
                booking.telefone = bookingData.get("telefone");
                booking.servico = bookingData.get("servico");
                booking.data = bookingData.get("data");
                booking.horario = bookingData.get("horario");
                booking.descricao = bookingData.get("descricao");
                booking.status = "PENDENTE";
                
                saveBookingToDB(booking);
                sendJsonResponse(exchange, 200, bookingToJson(booking));
            }
        }
    }

    static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
    }

    static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes());
    }

    static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim().substring(1, json.length() - 1);
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    static void sendJsonResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    static String bookingToJson(Booking booking) {
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"email\":\"%s\",\"telefone\":\"%s\",\"servico\":\"%s\",\"data\":\"%s\",\"horario\":\"%s\",\"descricao\":\"%s\",\"status\":\"%s\"}",
            booking.id, booking.nome, booking.email, booking.telefone, booking.servico, booking.data, booking.horario, booking.descricao, booking.status
        );
    }

    static User getUserFromDB(String email) {
        if (db == null) return users.get(email);
        
        try {
            PreparedStatement stmt = db.prepareStatement("SELECT * FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("senha"),
                    rs.getString("telefone"),
                    rs.getBoolean("is_admin")
                );
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuário: " + e.getMessage());
        }
        return null;
    }
    
    static void saveUserToDB(User user) {
        if (db == null) {
            users.put(user.email, user);
            return;
        }
        
        try {
            PreparedStatement stmt = db.prepareStatement(
                "INSERT INTO users (nome, email, senha, telefone, is_admin) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setString(1, user.nome);
            stmt.setString(2, user.email);
            stmt.setString(3, user.senha);
            stmt.setString(4, user.telefone);
            stmt.setBoolean(5, user.isAdmin);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erro ao salvar usuário: " + e.getMessage());
        }
    }
    
    static void saveBookingToDB(Booking booking) {
        if (db == null) {
            booking.id = bookingIdCounter++;
            bookings.put(booking.id, booking);
            return;
        }
        
        try {
            PreparedStatement stmt = db.prepareStatement(
                "INSERT INTO bookings (nome, email, telefone, servico, data, horario, descricao, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id"
            );
            stmt.setString(1, booking.nome);
            stmt.setString(2, booking.email);
            stmt.setString(3, booking.telefone);
            stmt.setString(4, booking.servico);
            stmt.setString(5, booking.data);
            stmt.setString(6, booking.horario);
            stmt.setString(7, booking.descricao);
            stmt.setString(8, booking.status);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                booking.id = rs.getLong("id");
            }
        } catch (Exception e) {
            System.out.println("Erro ao salvar agendamento: " + e.getMessage());
        }
    }
    
    static String getBookingsFromDB() {
        if (db == null) {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (Booking booking : bookings.values()) {
                if (!first) json.append(",");
                json.append(bookingToJson(booking));
                first = false;
            }
            json.append("]");
            return json.toString();
        }
        
        try {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM bookings ORDER BY data DESC, horario DESC");
            
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            
            while (rs.next()) {
                if (!first) json.append(",");
                json.append(String.format(
                    "{\"id\":%d,\"nome\":\"%s\",\"email\":\"%s\",\"telefone\":\"%s\",\"servico\":\"%s\",\"data\":\"%s\",\"horario\":\"%s\",\"descricao\":\"%s\",\"status\":\"%s\"}",
                    rs.getLong("id"), rs.getString("nome"), rs.getString("email"), rs.getString("telefone"),
                    rs.getString("servico"), rs.getString("data"), rs.getString("horario"), 
                    rs.getString("descricao"), rs.getString("status")
                ));
                first = false;
            }
            json.append("]");
            return json.toString();
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos: " + e.getMessage());
            return "[]";
        }
    }

    static class User {
        String nome, email, senha, telefone;
        boolean isAdmin;
        
        User(String nome, String email, String senha, String telefone, boolean isAdmin) {
            this.nome = nome;
            this.email = email;
            this.senha = senha;
            this.telefone = telefone;
            this.isAdmin = isAdmin;
        }
    }

    static class Booking {
        Long id;
        String nome, email, telefone, servico, data, horario, descricao, status;
    }
}