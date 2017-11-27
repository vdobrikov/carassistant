package com.carassistant.carbot.slack.handler.interactivemessage;

import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackInteractiveMessageActionHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionValue;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.RidesView;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.RIDE_LIST)
public class RidesListHandler {
    private String botAccessToken;
    private int pageSize;
    private RideService rideService;
    private UserContext userContext;

    @Autowired
    public RidesListHandler(@Value("${slack.bot.access.token}") String botAccessToken,
                            @Value("${slack.pagination.size}") int pageSize,
                            RideService rideService, UserContext userContext) {

        this.botAccessToken = botAccessToken;
        this.pageSize = pageSize;
        this.rideService = rideService;
        this.userContext = userContext;
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.NEXT)
    public void onNext(ActionPayload payload) throws IOException, SlackApiException {
        int pageNum = Integer.parseInt(payload.getActions().get(0).getName());
        pageNum++;
        sendRidesList(payload, pageNum);

    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.PREV)
    public void onPrev(ActionPayload payload) throws IOException, SlackApiException {
        int pageNum = Integer.parseInt(payload.getActions().get(0).getName());
        pageNum--;
        sendRidesList(payload, pageNum);

    }

    private void sendRidesList(ActionPayload payload, int pageNum) throws IOException, SlackApiException {
        User user = userContext.getUser();
        Page<Ride> ridesPage = rideService.findAllByStatusAndLocation(Ride.Status.READY, user.getLocation(),
            new PageRequest(pageNum, pageSize, Sort.Direction.DESC, "createdDate"));

        Slack.getInstance().methods()
            .chatUpdate(RidesView.createUpdateRequest(botAccessToken, CallbackId.RIDE_LIST, payload, ridesPage, user));
    }
}
