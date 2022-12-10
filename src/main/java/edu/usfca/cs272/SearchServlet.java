package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import edu.usfca.cs272.InvertedWordIndex.SearchResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SearchServlet extends HttpServlet {

    public static final String title = "SUPASEARCH";

    private final String htmlTemplate;

    public static final Path baseDir = Path.of("src", "main", "resources", "html");

    ThreadSafeQueryFileHandler queryFileHandler;

    public SearchServlet(ThreadSafeQueryFileHandler queryFileHandler) throws IOException {
        super();
        this.queryFileHandler = queryFileHandler;
        this.htmlTemplate = Files.readString(baseDir.resolve("index.html"), StandardCharsets.UTF_8);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

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

        try (StringWriter stringWriter = new StringWriter()) {
            Iterator<SearchResult> resultsIterator = results.iterator();
            for (int i = 1; resultsIterator.hasNext(); i++) {
                SearchResult result = resultsIterator.next();
                stringWriter.write(String.valueOf(i));
                stringWriter.write(":<br>\n");
                stringWriter.write(result.getWhere());
                stringWriter.write(":<br>\n");
                stringWriter.write("Matches: ");
                stringWriter.write(String.valueOf(result.getCount()));
                stringWriter.write("<br>\n");
            }
            if (results.isEmpty()){
                values.put("results", "No results found.");
            } else {
                values.put("results", stringWriter.toString());
            }
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
