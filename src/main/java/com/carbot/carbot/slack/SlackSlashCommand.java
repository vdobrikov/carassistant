package com.carbot.carbot.slack;

import com.carbot.carbot.model.Ride;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ramswaroop.jbot.core.slack.models.Attachment;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@RestController
public class SlackSlashCommand {
    private static final Logger LOG = LoggerFactory.getLogger(SlackSlashCommand.class);

    private String slashToken;

    public SlackSlashCommand(@Value("${slashCommandToken}") String slashToken) {
        this.slashToken = slashToken;
    }

    //  /carbot share ride from office to somewhere

    @RequestMapping(value = "slash-command",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommand(@RequestParam("token") String token,
                                             @RequestParam("team_id") String teamId,
                                             @RequestParam("team_domain") String teamDomain,
                                             @RequestParam("channel_id") String channelId,
                                             @RequestParam("channel_name") String channelName,
                                             @RequestParam("user_id") String userId,
                                             @RequestParam("user_name") String userName,
                                             @RequestParam("command") String command,
                                             @RequestParam("text") String text,
                                             @RequestParam("response_url") String responseUrl) {

        LOG.debug("token={}; teamId={}; teamDomain={}; channelId={}; channelName={}; userId={}; userName={}; command={}, text={}, responseUrl={}",
            token, teamId, teamDomain, channelId, channelName, userId, userName, command, text, responseUrl);

        // validate token
        if (!token.equals(slashToken)) {
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }


        /** build response */
        RichMessage richMessage = new RichMessage("The is Slash Commander!");
        richMessage.setResponseType("in_channel");
        // set attachments
        Attachment[] attachments = new Attachment[1];
        attachments[0] = new Attachment();
        attachments[0].setText("I will perform all tasks for you.");
        richMessage.setAttachments(attachments);

        // For debugging purpose only
        if (LOG.isDebugEnabled()) {
            try {
                LOG.debug("Reply (RichMessage): {}", new ObjectMapper().writeValueAsString(richMessage));
            } catch (JsonProcessingException e) {
                LOG.debug("Error parsing RichMessage: ", e);
            }
        }

        return richMessage.encodedMessage(); // don't forget to send the encoded message to Slack
    }

    private Ride parseNewRideCommand(String userName, String text) {
        // /carbot share ride from office to Tairovo at 18:00 3 seats
        return null;
    }

    private String getFrom(String text) {
        return trim(substringBetween(text, "from ", " to"));
    }

    private String getTo(String text) {
        return trim(substringBetween(text, "to ", " at"));
    }

    private String getDate(String text) {
        return trim(substringBetween(text, "at  ", " at"));
    }
}
