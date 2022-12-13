package edu.usfca.cs272;

import org.apache.commons.text.StringEscapeUtils;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans simple, validating HTML 4/5 into plain text. For simplicity, this
 * class cleans already validating HTML, it does not validate the HTML itself.
 * For example, the {@link #stripEntities(String)} method removes HTML entities
 * but does not check that the removed entity was valid.
 *
 * <p>
 * Look at the "See Also" section for useful classes and methods for
 * implementing this class.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * @see String#replaceAll(String, String)
 * @see Pattern#DOTALL
 * @see Pattern#CASE_INSENSITIVE
 * @see StringEscapeUtils#unescapeHtml4(String)
 */
public class HtmlCleaner {
    /**
     * Replaces all HTML tags with an empty string. For example, the html
     * {@code A<b>B</b>C} will become {@code ABC}.
     *
     * <p>
     * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
     *
     * @param html text including HTML tags to remove
     * @return text without any HTML tags
     * @see String#replaceAll(String, String)
     */
    public static String stripTags(String html) {
        // regex: https://regex101.com/r/VjrqzO/1
        final String regex = "<[^<]+?>";
        return html.replaceAll(regex, "");
    }

    /**
     * Replaces all HTML 4 entities with their Unicode character equivalent or, if
     * unrecognized, replaces the entity code with an empty string. For example:,
     * {@code 2010&ndash;2012} will become {@code 2010–2012} and
     * {@code &gt;&dash;x} will become {@code >x} with the unrecognized
     * {@code &dash;} entity getting removed. (The {@code &dash;} entity is valid
     * HTML 5, but not HTML 4 which this code uses.) Should also work for entities
     * that use decimal syntax like {@code &#8211;} or {@code &#x2013}.
     *
     * <p>
     * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
     *
     * @param html text including HTML entities to remove
     * @return text with all HTML entities converted or removed
     * @see StringEscapeUtils#unescapeHtml4(String)
     * @see String#replaceAll(String, String)
     */
    public static String stripEntities(String html) {
        //regex: https://regex101.com/r/mXSE7f/1
        final String regex = "&\\S+?;";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(html);
        HashSet<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group()); // get unique results
        }

        for (String match : matches) {
            String escaped = StringEscapeUtils.unescapeHtml4(match);
            if (escaped.equals(match)) {
                escaped = "";
            }
            html = html.replaceAll(match, escaped);
        }
        return html;
    }

    /**
     * Replaces all HTML comments with an empty string. For example:
     *
     * <pre>
     * A&lt;!-- B --&gt;C
     * </pre>
     *
     * ...and this HTML:
     *
     * <pre>
     * A&lt;!--
     * B --&gt;C
     * </pre>
     *
     * ...will both become "AC" after stripping comments.
     *
     * <p>
     * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
     *
     * @param html text including HTML comments to remove
     * @return text without any HTML comments
     * @see String#replaceAll(String, String)
     */
    public static String stripComments(String html) {
        // regex: https://regex101.com/r/SnRdmc/1
        final String regex = "(?s)<!--.+?-->";
        return html.replaceAll(regex, "");
    }

    /**
     * Replaces everything between the element tags and the element tags
     * themselves with an empty string. For example, consider the html code:
     *
     * <pre>
     * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
     * </pre>
     * <p>
     * If removing the "style" element, all of the above code will be removed, and
     * replaced with an empty string.
     *
     * <p>
     * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
     *
     * @param html text including HTML elements to remove
     * @param name name of the HTML element (like "style" or "script")
     * @return text without that HTML element
     * @see String#formatted(Object...)
     * @see String#format(String, Object...)
     * @see String#replaceAll(String, String)
     */
    public static String stripElement(String html, String name) {
        // regex: https://regex101.com/r/QMzPD4/1
        final String regex = "(?i)(?s)(<head|<head\\s[^>]*)>.*?<\\/head\\s*?>".replaceAll("head", name);
        return html.replaceAll(regex, "");
    }

    /**
     * Removes comments and certain block elements from the provided html. The
     * block elements removed include: head, style, script, noscript, iframe, and
     * svg.
     *
     * @param html the HTML to strip comments and block elements from
     * @return text clean of any comments and certain HTML block elements
     */
    public static String stripBlockElements(String html) {
        html = stripComments(html);
        html = stripElement(html, "head");
        html = stripElement(html, "style");
        html = stripElement(html, "script");
        html = stripElement(html, "noscript");
        html = stripElement(html, "iframe");
        html = stripElement(html, "svg");
        return html;
    }

    /**
     * Removes all HTML tags and certain block elements from the provided text.
     *
     * @param html the HTML to strip tags and elements from
     * @return text clean of any HTML tags and certain block elements
     * @see #stripBlockElements(String)
     * @see #stripTags(String)
     */
    public static String stripHtml(String html) {
        html = stripBlockElements(html);
        html = stripTags(html);
        html = stripEntities(html);
        return html;
    }

    /**
     * Gets the title of a given page or null if the page does not have a title tag
     * Also Escapes HTML tags to avoid XSS
     * @param html the html to find the title of
     * @return the HTML escaped title or null if the title is not found
     */
    public static String getTitle(String html) {
        final String regex = "(?i)(?s)(<title|<title\\s[^>]*)>(.*?)<\\/title>";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(html);
        if (matcher.find()){
            return StringEscapeUtils.escapeHtml4(matcher.group(2));
        }
        return null;

    }
}
