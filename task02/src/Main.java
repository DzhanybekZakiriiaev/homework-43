import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = makeServer();
            server.start();
            initRoutes(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpServer makeServer() throws IOException {
        String host = "localhost";
        InetSocketAddress address = new InetSocketAddress(host, 9881);
        String msg = "запускаем сервер по адресу" + " http://%s:%s/%n";
        System.out.printf(msg, address.getHostName(), address.getPort());
        HttpServer server = HttpServer.create(address, 50);
        System.out.println("удачно!");
        return server;
    }

    private static void initRoutes(HttpServer server) {
        server.createContext("/", Main::handleRootRequest);
        server.createContext("/apps/", Main::handleAppRequest);
        server.createContext("/apps/profile", Main::handleProfileRequest);
        server.createContext("/index.html", Main::handleHtmlRequest);
    }

    private static void handleHtmlRequest(HttpExchange exchange) throws IOException {
        String htmlContent = Files.readString(Paths.get("homework/index.html"));
        String cssContent = Files.readString(Paths.get("homework/css/forms.css"));
        htmlContent = htmlContent.replaceFirst("<head>", "<head><style>" + cssContent + "</style>");
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            try (PrintWriter writer = getWriterFrom(exchange)) {
                write(writer, "", htmlContent);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readHtml() {
        String html = "";
        try{
            Path path = Paths.get("homework/index.html");
            html = Files.readString(path);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return html;
    }
    private static void handleProfileRequest(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            try (PrintWriter writer = getWriterFrom(exchange)) {
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                write(writer, "HTTP Метод", method);
                write(writer, "Запрос", uri.toString());
                write(writer, "Просмотр профиля","Профиль пока что пуст. Попробуйте позже");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void handleAppRequest(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            try (PrintWriter writer = getWriterFrom(exchange)) {
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                write(writer, "HTTP Метод", method);
                write(writer, "Запрос", uri.toString());
                writeHeaders(writer, "Заголовки запроса",
                        exchange.getRequestHeaders());
                write(writer, "Вы зашли в приложение: ", "Тут пусто");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void handleRootRequest(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            try (PrintWriter writer = getWriterFrom(exchange)) {
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String ctxPath = exchange.getHttpContext()
                        .getPath();
                write(writer, "HTTP Метод", method);
                write(writer, "Запрос", uri.toString());
                write(writer, "Обработан через", ctxPath);
                writeHeaders(writer, "Заголовки запроса",
                        exchange.getRequestHeaders());
                writeData(writer, exchange);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static PrintWriter getWriterFrom(HttpExchange exchange) {
        OutputStream output = exchange.getResponseBody();
        Charset charset = StandardCharsets.UTF_8;
        return new PrintWriter(output, false, charset);
    }

    private static void write(Writer writer, String msg, String method) {
        String data = String.format("%s: %s%n%n", msg, method);
        try {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } }
    private static void writeHeaders(Writer writer, String type, Headers headers) {
        write(writer, type, "");
        headers.forEach((k, v) -> write(writer, "\t" + k, v.toString()));
    }
    private static BufferedReader getReader(HttpExchange exchange) {
        InputStream input = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader isr = new InputStreamReader(input, charset);
        return new BufferedReader(isr);
    }
    private static void writeData(Writer writer,  HttpExchange exchange) {
        try (BufferedReader reader = getReader(exchange)) {
            if (!reader.ready()) {
                return;
            }
            write(writer, "Блок данных", "");
            reader.lines().forEach(v -> write(writer, "\t", v));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}