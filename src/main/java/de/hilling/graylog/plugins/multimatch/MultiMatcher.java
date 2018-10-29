package de.hilling.graylog.plugins.multimatch;

import org.graylog.plugins.pipelineprocessor.ast.functions.Function;
import org.graylog2.plugin.Message;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class MultiMatcher {
    public static final String MESSAGE_KEY = "message";
    private final List<Map<String, String>> matchers;
    private final Message message;

    MultiMatcher(@Nonnull List<Map<String, String>> matchers, @Nonnull Message message) {
        this.matchers = matchers;
        this.message = message;
    }

    boolean invoke() {
        if (!typesCorrect(matchers)) {
            Function.log.error("wrong type of configuration, expecting a list of maps");
            return false;
        }
        return matchers.stream()
                       .anyMatch(this::matchAllMapPredicates);
    }

    private boolean typesCorrect(List<?> matchers) {
        return matchers.stream()
                       .allMatch(this::assertStringMap);
    }

    private boolean assertStringMap(Object o) {
        if (o instanceof Map) {
            Map<?, ?> map = (Map) o;
            return map.entrySet()
                      .stream()
                      .allMatch(this::assertStrings);
        } else {
            return false;
        }
    }

    private boolean assertStrings(Map.Entry entry) {
        return entry.getKey() instanceof String && entry.getValue() instanceof String;
    }

    private boolean matchAllMapPredicates(Map<String, String> map) {
        Function.log.debug("found object {}/{}", map, map.getClass());
        return map.entrySet()
                  .stream()
                  .allMatch(this::match);
    }

    private boolean match(Map.Entry<String, String> mapEntry) {
        final String key = mapEntry.getKey();
        final String value = mapEntry.getValue();
        if (!message.hasField(key)) {
            return false;
        }
        if (Objects.equals(key, MESSAGE_KEY)) {
            return matchMessage(value);
        } else {
            return Objects.equals(value, message.getField(key));
        }
    }

    private boolean matchMessage(String value) {
        String messageField = message.getFieldAs(String.class, MESSAGE_KEY);
        return messageField.matches(value);
    }
}
