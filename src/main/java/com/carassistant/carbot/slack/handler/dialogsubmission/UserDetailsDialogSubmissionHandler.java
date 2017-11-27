package com.carassistant.carbot.slack.handler.dialogsubmission;

import com.carassistant.model.User;
import com.carassistant.service.UserService;
import com.carassistant.carbot.slack.framework.annotation.SlackDialogSubmissionHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.framework.model.DialogSubmissionError;
import com.carassistant.carbot.slack.framework.model.DialogSubmissionErrorsResponse;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.InitialMessage;
import com.carassistant.service.ConfigService;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.carassistant.utilities.ExecutionHelpers.executeWithRetries;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler
public class UserDetailsDialogSubmissionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserDetailsDialogSubmissionHandler.class);

    private String botAccessToken;
    private UserService userService;
    private ConfigService configService;

    @Autowired
    public UserDetailsDialogSubmissionHandler(@Value("${slack.bot.access.token}") String botAccessToken,
                                              UserService userService,
                                              ConfigService configService) {
        this.botAccessToken = botAccessToken;
        this.userService = userService;
        this.configService = configService;
    }

    @SlackDialogSubmissionHandler(callbackId = CallbackId.USER_DETAILS_DIALOG)
    public Object onSubmission(ActionPayload payload) throws Exception {
        List<DialogSubmissionError> errors = new ArrayList<>();

        String location = payload.getSubmission().get("location");

        if (!configService.getLocations().keySet().contains(location)) {
            errors.add(new DialogSubmissionError("location", "Failed to parse location"));
        }

        if (!errors.isEmpty()) {
            return new DialogSubmissionErrorsResponse(errors);
        }

        LOG.info("Passed validation");

        User user = createNewUser(payload);

        return Slack.getInstance()
            .methods().chatPostMessage(InitialMessage.createMessage(botAccessToken, payload.getChannel().getId(), user));
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
        UsersInfoResponse usersInfoResponse = Slack.getInstance().methods().usersInfo(UsersInfoRequest.builder()
            .token(botAccessToken)
            .user(slackUserId)
            .build());
        return usersInfoResponse.getUser();
    }
}
