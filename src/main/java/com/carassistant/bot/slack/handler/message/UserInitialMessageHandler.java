package com.carassistant.bot.slack.handler.message;

import com.carassistant.bot.slack.framework.annotation.SlackHandler;
import com.carassistant.bot.slack.framework.annotation.SlackMessageActionHandler;
import com.carassistant.bot.slack.handler.ActionName;
import com.carassistant.bot.slack.handler.CallbackId;
import com.carassistant.bot.slack.framework.SlackApiClient;
import com.carassistant.bot.slack.framework.UserContext;
import com.carassistant.bot.slack.framework.model.ActionPayload;
import com.carassistant.bot.slack.message.CommonRequests;
import com.carassistant.model.User;
import com.carassistant.service.ConfigService;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.dialog.DialogOpenRequest;
import com.github.seratch.jslack.api.model.dialog.Dialog;
import com.github.seratch.jslack.api.model.dialog.DialogOption;
import com.github.seratch.jslack.api.model.dialog.DialogSelectElement;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.USER_INITIAL_PROMPT)
public class UserInitialMessageHandler {
    private ConfigService configService;
    private UserContext userContext;
    private SlackApiClient slackApiClient;

    @Autowired
    public UserInitialMessageHandler(ConfigService configService,
                                     UserContext userContext,
                                     SlackApiClient slackApiClient) {
        this.configService = configService;
        this.userContext = userContext;
        this.slackApiClient = slackApiClient;
    }

    @SlackMessageActionHandler(actionName = ActionName.CANCEL)
    public SlackApiResponse onCancel(ActionPayload payload) throws IOException, SlackApiException {
        return slackApiClient.send(CommonRequests.deleteOriginalMessage(payload));
    }

    @SlackMessageActionHandler(actionName = ActionName.USER_DETAILS)
    public SlackApiResponse onMyDetails(ActionPayload payload) throws IOException, SlackApiException {
        User user = userContext.getUser();

        return slackApiClient.send(DialogOpenRequest.builder()
                .triggerId(payload.getTriggerId())
                .dialog(Dialog.builder()
                    .title("User Details")
                    .callbackId(String.format(CallbackId.USER_DETAILS_DIALOG.replaceAll("\\{.+?\\}", "%s"), user.getId()))
                    .elements(Lists.newArrayList(DialogSelectElement.builder()
                        .name("location")
                        .label("Location")
                        .options(createOptions())
                        .value(user != null ? user.getLocation() : null)
                        .build()))
                    .submitLabel("Submit")
                    .build())
                .build());
    }

    private List<DialogOption> createOptions() {
        return configService.getLocations().keySet().stream()
            .map(location -> DialogOption.builder()
                .value(location)
                .label(StringUtils.capitalize(location))
                .build())
            .collect(Collectors.toList());
    }
}
