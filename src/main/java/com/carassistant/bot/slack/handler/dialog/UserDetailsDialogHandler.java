package com.carassistant.bot.slack.handler.dialog;

import com.carassistant.bot.slack.framework.annotation.SlackHandler;
import com.carassistant.bot.slack.framework.SlackApiClient;
import com.carassistant.bot.slack.framework.annotation.CallbackVariable;
import com.carassistant.bot.slack.framework.annotation.SlackDialogHandler;
import com.carassistant.bot.slack.framework.model.ActionPayload;
import com.carassistant.bot.slack.framework.model.DialogSubmissionError;
import com.carassistant.bot.slack.framework.model.DialogSubmissionErrorsResponse;
import com.carassistant.bot.slack.handler.CallbackId;
import com.carassistant.exception.UserNotFoundException;
import com.carassistant.model.User;
import com.carassistant.service.ConfigService;
import com.carassistant.service.UserService;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.carassistant.utilities.ExecutionHelpers.executeWithRetries;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler
public class UserDetailsDialogHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserDetailsDialogHandler.class);

    private UserService userService;
    private ConfigService configService;
    private SlackApiClient slackApiClient;

    @Autowired
    public UserDetailsDialogHandler(UserService userService,
                                    ConfigService configService, SlackApiClient slackApiClient) {
        this.userService = userService;
        this.configService = configService;
        this.slackApiClient = slackApiClient;
    }

    @SlackDialogHandler(callbackId = CallbackId.USER_DETAILS_DIALOG)
    public Object onSubmission(ActionPayload payload,
                               @CallbackVariable(name = "id") String userId) throws Exception {
        List<DialogSubmissionError> errors = new ArrayList<>();

        String location = payload.getSubmission().get("location");

        if (!configService.getLocations().keySet().contains(location)) {
            errors.add(new DialogSubmissionError("location", "Failed to parse location"));
        }

        if (!errors.isEmpty()) {
            return new DialogSubmissionErrorsResponse(errors);
        }

        LOG.info("Passed validation");

        if (userId == null) {
            createNewUser(payload);
        } else {
            updateUser(userId, location);
        }
        return null;
    }

    private User updateUser(String userId, String location) {
        User user = userService.findById(userId).orElseThrow(() -> new UserNotFoundException("id=" + userId));
        user.setLocation(location);
        return userService.save(user);
    }

    private User createNewUser(ActionPayload payload) throws Exception {
        String slackUserId = payload.getUser().getId();

        com.github.seratch.jslack.api.model.User slackUser = executeWithRetries(() -> getSlackUser(slackUserId), newArrayList(IOException.class, SlackApiException.class), 3);

        User user = new User();
        user.getSlackInfo().setUserId(slackUserId);
        user.getSlackInfo().setChannelId(payload.getChannel().getId());
        user.setEmail(slackUser.getProfile().getEmail());
        user.setLocation(payload.getSubmission().get("location"));
        return userService.save(user);
    }

    private com.github.seratch.jslack.api.model.User getSlackUser(String slackUserId) throws IOException, SlackApiException {
        UsersInfoResponse usersInfoResponse = slackApiClient.send(UsersInfoRequest.builder()
            .user(slackUserId)
            .build());
        return usersInfoResponse.getUser();
    }
}
