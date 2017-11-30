package com.carassistant.carbot.slack.framework;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatDeleteRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.methods.request.dialog.DialogOpenRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatDeleteResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatUpdateResponse;
import com.github.seratch.jslack.api.methods.response.dialog.DialogOpenResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
public class SlackApiClient {
    private String botAccessToken;
    private Slack slack = Slack.getInstance();

    public SlackApiClient(@Value("${slack.bot.access.token}") String botAccessToken) {
        this.botAccessToken = botAccessToken;
    }

    public ChatPostMessageResponse send(ChatPostMessageRequest request) throws IOException, SlackApiException {
        Assert.notNull(request, "'request' cannot be null");

        request.setToken(defaultIfEmpty(request.getToken(), botAccessToken));
        return slack.methods().chatPostMessage(request);
    }

    public ChatUpdateResponse send(ChatUpdateRequest request) throws IOException, SlackApiException {
        Assert.notNull(request, "'request' cannot be null");

        request.setToken(defaultIfEmpty(request.getToken(), botAccessToken));
        return slack.methods().chatUpdate(request);
    }

    public ChatDeleteResponse send(ChatDeleteRequest request) throws IOException, SlackApiException {
        Assert.notNull(request, "'request' cannot be null");

        request.setToken(defaultIfEmpty(request.getToken(), botAccessToken));
        return slack.methods().chatDelete(request);
    }

    public DialogOpenResponse send(DialogOpenRequest request) throws IOException, SlackApiException {
        Assert.notNull(request, "'request' cannot be null");

        request.setToken(defaultIfEmpty(request.getToken(), botAccessToken));
        return slack.methods().dialogOpen(request);
    }

    public UsersInfoResponse send(UsersInfoRequest request) throws IOException, SlackApiException {
        Assert.notNull(request, "'request' cannot be null");

        request.setToken(defaultIfEmpty(request.getToken(), botAccessToken));
        return slack.methods().usersInfo(request);
    }
}
