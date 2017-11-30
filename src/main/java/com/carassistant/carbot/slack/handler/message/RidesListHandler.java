package com.carassistant.carbot.slack.handler.message;

import com.carassistant.carbot.slack.framework.SlackApiClient;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackMessageActionHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionName;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.RidesView;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
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
    private int pageSize;
    private RideService rideService;
    private UserContext userContext;
    private SlackApiClient slackApiClient;

    @Autowired
    public RidesListHandler(@Value("${slack.pagination.size}") int pageSize,
                            RideService rideService, UserContext userContext,
                            SlackApiClient slackApiClient) {

        this.pageSize = pageSize;
        this.rideService = rideService;
        this.userContext = userContext;
        this.slackApiClient = slackApiClient;
    }

    @SlackMessageActionHandler(actionName = ActionName.NEXT)
    public void onNext(ActionPayload payload) throws IOException, SlackApiException {
        int pageNum = Integer.parseInt(payload.getActions().get(0).getValue());
        pageNum++;
        sendRidesList(payload, pageNum);

    }

    @SlackMessageActionHandler(actionName = ActionName.PREV)
    public void onPrev(ActionPayload payload) throws IOException, SlackApiException {
        int pageNum = Integer.parseInt(payload.getActions().get(0).getValue());
        pageNum--;
        sendRidesList(payload, pageNum);

    }

    private void sendRidesList(ActionPayload payload, int pageNum) throws IOException, SlackApiException {
        User user = userContext.getUser();
        Page<Ride> ridesPage = rideService.findAllByStatusAndLocation(Ride.Status.READY, user.getLocation(),
            new PageRequest(pageNum, pageSize, Sort.Direction.DESC, "createdDate"));

        slackApiClient.send(RidesView.createUpdateRequest(CallbackId.RIDE_LIST, payload, ridesPage, user));
    }
}
