package de.hilling.graylog.plugins.multimatch;

import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.graylog2.plugin.Message;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiMatcherTest {

    private static final String SAMPLE_TEXT = "sample text";
    private static final String SAMPLE_MULTILINE;

    static {
        try {
            SAMPLE_MULTILINE = IOUtils.toString(MultiMatcherTest.class.getResourceAsStream("/sample-message.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message testMessage;
    private JSONArray jsonArray;
    private HashMap<String, String> matcherMap;

    public static void main(String[] args) {
        Message testMessage = new Message(SAMPLE_TEXT, "easy", DateTime.now());
        AtomicLong invocations = new AtomicLong(0);
        final long startTime = System.currentTimeMillis();
        Thread last = null;
        for (int i = 0; i < 10; i++) {
            last = new Thread(() -> {
                while (true) {
                    long n = invocations.incrementAndGet();
                    JSONArray jsonArray = new JSONArray();
                    HashMap<String, String> matcherMap = new HashMap<>();
                    jsonArray.appendElement(matcherMap);
                    matcherMap.put("room", "0816");
                    if (n % 2 == 0) {
                        matcherMap.put("message", "^.*FEHLER.*$");
                    } else {
                        final int epsilon = new Random().nextInt(1000);
                        matcherMap.put("message", "^.*FEHLER" + epsilon + ".*$");
                    }
                    testMessage.addField("room", "0816");
                    new MultiMatcher((List) jsonArray, testMessage).invoke();
                    if (n % 1_000_000 == 0) {
                        long timediff = System.currentTimeMillis() - startTime;
                        System.out.println("n: " + n + " in " + timediff + "ms (" + n/timediff + "kmsg/s");
                    }
                }
            }, "worker-" + i);
            last.start();
        }
        try {
            last.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp() {
        testMessage = new Message(SAMPLE_TEXT, "easy", DateTime.now());
        jsonArray = new JSONArray();
        matcherMap = new HashMap<>();
    }

    @Test
    public void emtpyNoMatch() {
        assertFalse(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void matchMessage() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("message", SAMPLE_TEXT);
        assertTrue(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void unknownFieldMatchesFalse() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0815");
        matcherMap.put("message", SAMPLE_TEXT);
        assertFalse(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void matchOnMultipleConditions() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0815");
        matcherMap.put("message", SAMPLE_TEXT);
        testMessage.addField("room", "0815");
        assertTrue(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void matchFalseOnMultipleConditions() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0815");
        matcherMap.put("message", SAMPLE_TEXT);
        testMessage.addField("room", "0816");
        assertFalse(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void matchOnMultipleConditionsWithRegexp() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0816");
        matcherMap.put("message", "^.*text.*$");
        testMessage.addField("room", "0816");
        assertTrue(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void matchOnMultipleConditionsWithRegexpMultiLine() {
        testMessage = new Message(SAMPLE_MULTILINE, "easy", DateTime.now());
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0816");
        matcherMap.put("message", "^.*FEHLER.*$");
        testMessage.addField("room", "0816");
        assertTrue(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void ignoreUnknownField() {
        jsonArray.appendElement("hello");
        assertFalse(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }

    @Test
    public void matchCustomField() {
        jsonArray.appendElement(matcherMap);
        matcherMap.put("room", "0815");
        testMessage.addField("room", "0815");
        assertTrue(new MultiMatcher((List) jsonArray, testMessage).invoke());
    }
}