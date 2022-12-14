package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import edu.usfca.cs272.InvertedWordIndex.SearchResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SearchServlet extends HttpServlet {

    public static final String title = "SUPASEARCH";

    private final String htmlTemplate;

    public static final Path baseDir = Path.of("src", "main", "resources", "html");

    ThreadSafeQueryFileHandler queryFileHandler;

    private static final Logger log = LogManager.getLogger();

    public SearchServlet(ThreadSafeQueryFileHandler queryFileHandler) throws IOException {
        super();
        this.queryFileHandler = queryFileHandler;
        this.htmlTemplate = Files.readString(baseDir.resolve("index.html"), StandardCharsets.UTF_8);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        DatabaseConnector connector = new DatabaseConnector("database.properties");

        if (!connector.testConnection()) {
            //todo: safely do not use db OR display html error, move to main server
           return;
        }


        // generate html
        Map<String, String> values = new HashMap<>();
        values.put("title", title);
        values.put("thread", Thread.currentThread().getName());

        values.put("method", "GET");
        values.put("action", "search");

        String query = request.getParameter("query");

        if (query == null){
            query = "";
        }
        // avoid xss
        query = StringEscapeUtils.escapeHtml4(query);
        var results = queryFileHandler.parseQueryGetResults(query, false);

        try (
                StringWriter stringWriter = new StringWriter();
                Connection db = connector.getConnection();
                PreparedStatement statement = db.prepareStatement(
                        "SELECT * from crawler_stats WHERE page_url LIKE ?"
                );
        ) {
            for (SearchResult result : results) {
                statement.setString(1, result.getWhere());
                String snippet = "";
                String title = "      ";
                String contentLength = "";
                String timestampCrawled = "";
                try (ResultSet dbResults = statement.executeQuery()){
                    //todo: escape to avoid xss
                    if (dbResults.next()) {
                        snippet = dbResults.getString("snippet");
                        title = dbResults.getString("title");
                        contentLength = dbResults.getString("content_length");
                        timestampCrawled = dbResults.getString("timestamp_crawled");
                    } else {
                        System.out.println("no metadata found");
                    }
                }
                stringWriter.write("<p>");
                String whereLink = String.join("", "<a href=\"", result.getWhere(), "\">",
                        title, "</a>");
                stringWriter.write(whereLink);
                stringWriter.write(":<br>\n");
                stringWriter.write("Matches: ");
                stringWriter.write(String.valueOf(result.getCount()));
                stringWriter.write("<br>\n");
                // get page stats
                stringWriter.write("Snippet: ");
                stringWriter.write(snippet);
                stringWriter.write("<br>\n");
                stringWriter.write("Content Length: ");
                stringWriter.write(contentLength);
                stringWriter.write(" | Timestamp Crawled: ");
                stringWriter.write(timestampCrawled);
                stringWriter.write("<br>\n");
                stringWriter.write("----------------------------------------");
                stringWriter.write("<br><br>\n\n");
                stringWriter.write("</p>");



            }
            if (results.isEmpty()){
                values.put("results", "No results found.");
            } else {
                values.put("results", stringWriter.toString());
            }
        } catch (SQLException e) {
            //todo : log.warn
            throw new RuntimeException(e);
        }

        String html = StringSubstitutor.replace(htmlTemplate, values);

        // http response
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        // output html
        PrintWriter out = response.getWriter();
        out.println(html);
        out.flush();
    }
}
