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
        return matchers.stream()
                       .anyMatch(map -> {
                           Function.log.debug("found object {}/{}", map, map.getClass());
                           if (map instanceof Map) {
                               return map.entrySet().stream().allMatch(this::match);
                           } else {
                               Function.log.error("object should be a map, found {}", map.getClass());
                               return false;
                           }
                       });
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
