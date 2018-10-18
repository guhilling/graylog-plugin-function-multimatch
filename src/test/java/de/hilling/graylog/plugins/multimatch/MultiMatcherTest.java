package de.hilling.graylog.plugins.multimatch;

import net.minidev.json.JSONArray;
import org.graylog2.plugin.Message;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiMatcherTest {

    public static final String SAMPLE_TEXT = "sample text";
    private Message emptyMessage;
    private JSONArray jsonArray;
    private HashMap<String, String> matcherMap;

    @BeforeEach
    public void setUp() {
        emptyMessage = new Message(SAMPLE_TEXT, "easy", DateTime.now());
        jsonArray = new JSONArray();
        matcherMap = new HashMap<>();
    }

    @Test
    public void emtpyNoMatch() {
        assertFalse(new MultiMatcher((List)jsonArray, emptyMessage).invoke());
    }

    @Test
    public void matchMessage() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("message", SAMPLE_TEXT);
        assertTrue(new MultiMatcher((List)jsonArray, emptyMessage).invoke());
    }

    @Test
    public void unknownFieldMatchesFalse() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0815");
        matcherMap.put("message", SAMPLE_TEXT);
        assertFalse(new MultiMatcher((List)jsonArray, emptyMessage).invoke());
    }

    @Test
    public void ignoreUnknownField() {
        jsonArray.appendElement("hello");
        assertFalse(new MultiMatcher((List)jsonArray, emptyMessage).invoke());
    }

    @Test
    public void matchCustomField() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0815");
        emptyMessage.addField("room", "0815");
        assertTrue(new MultiMatcher((List)jsonArray, emptyMessage).invoke());
    }
}