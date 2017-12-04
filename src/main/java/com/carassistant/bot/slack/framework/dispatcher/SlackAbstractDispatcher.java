package com.carassistant.bot.slack.framework.dispatcher;

import com.carassistant.bot.slack.framework.CallbackMatcher;
import com.carassistant.bot.slack.framework.model.ActionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public abstract class SlackAbstractDispatcher implements SlackDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SlackAbstractDispatcher.class);

    @Autowired
    protected CallbackMatcher callbackMatcher;

    protected void validate(ActionPayload payload) {
        Assert.notNull(payload, "'payload' cannot be null");

        Assert.isTrue(getDispatchedType().equals(payload.getType()), String.format("type should be '%s'", getDispatchedType()));

        String callbackId = payload.getCallbackId();
        Assert.hasLength(callbackId, "callback id cannot be null or empty");
    }

    protected Map<String, String> extractCallbackVariables(String pattern, String callback) {
        return callbackMatcher.extractUriTemplateVariables(pattern, callback);
    }

    protected Optional<String> findMostMatchingPattern(String path, Collection<String> patterns) {
        LOG.debug("findMostMatchingPattern: path={}", path);
        if (path == null || patterns == null) {
            return Optional.empty();
        }

        List<String> matchingPatterns = patterns.stream()
            .filter(pattern -> callbackMatcher.match(pattern, path))
            .collect(Collectors.toList());

        Comparator<String> comparator = callbackMatcher.getPatternComparator(path);
        matchingPatterns.sort(comparator);

        Optional<String> matchedPattern = matchingPatterns.isEmpty() ? Optional.empty() : Optional.of(matchingPatterns.get(0));
        LOG.debug("findMostMatchingPattern: matchedPattern={}", matchedPattern);
        return matchedPattern;
    }
}
