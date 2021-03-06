package de.hilling.graylog.plugins.multimatch;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.graylog.plugins.pipelineprocessor.ast.functions.Function;
import org.graylog2.plugin.Message;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

class MultiMatcher {
    private static final String MESSAGE_KEY = "message";
    private static final Cache<String, Pattern> PATTERN_CACHE = Caffeine.newBuilder()
                                                                        .maximumSize(2000)
                                                                        .build();
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
        final String matcherString = mapEntry.getValue();
        if (!message.hasField(key)) {
            return false;
        }
        if (Objects.equals(key, MESSAGE_KEY)) {
            return matchMessage(matcherString);
        } else {
            return Objects.equals(matcherString, message.getField(key));
        }
    }

    private Pattern fromCache(String matcherString) {
        return PATTERN_CACHE.get(matcherString, t -> Pattern.compile(matcherString, Pattern.DOTALL | Pattern.MULTILINE));
    }

    private boolean matchMessage(String matcherString) {
        String messageField = message.getFieldAs(String.class, MESSAGE_KEY);
        return fromCache(matcherString).matcher(messageField)
                                       .matches();
    }
}
