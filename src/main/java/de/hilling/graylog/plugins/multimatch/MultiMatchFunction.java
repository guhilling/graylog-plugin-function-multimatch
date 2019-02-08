package de.hilling.graylog.plugins.multimatch;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.Function;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.graylog2.plugin.Message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor.type;

public class MultiMatchFunction implements Function<Boolean> {

    public static final String NAME = "multimatch";
    private static final String MESSAGE_PARAM = "message";
    private static final String MATCHER_PARAM = "matcherMap";

    private final ParameterDescriptor<Message, Message> messageParam = type(MESSAGE_PARAM, Message.class).optional()
                                                                                                         .description("The message to verify, defaults to '$message'")
                                                                                                         .build();

    private final ParameterDescriptor<Map, Map> matcherParam = type(MATCHER_PARAM, Map.class).description("The map conditions to evaluate")
                                                                                             .build();


    @Override
    public Object preComputeConstantArgument(FunctionArgs functionArgs, String s, Expression expression) {
        return expression.evaluateUnsafe(EvaluationContext.emptyContext());
    }

    @Override
    public Boolean evaluate(FunctionArgs args, EvaluationContext context) {
        Message message = messageParam.optional(args, context)
                                      .orElse(context.currentMessage());
        //noinspection unchecked
        Map<String, Object> matcherParams = matcherParam.optional(args, context).orElse(Collections.emptyMap());
        log.debug("message {}", message);
        List matchers = (List) matcherParams.get("value");

        if (matchers == null) {
            return false;
        }
        return new MultiMatcher(matchers, message).invoke();
    }

    @Override
    public FunctionDescriptor<Boolean> descriptor() {
        return FunctionDescriptor.<Boolean>builder()
                .name(NAME)
                .description("Matches multiple fields of the message to multiple conditions")
                .params(of(messageParam, matcherParam))
                .returnType(Boolean.class)
                .build();
    }

}
