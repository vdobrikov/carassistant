package com.carassistant.bot.slack.handler.message;

import com.carassistant.bot.slack.framework.SlackApiClient;
import com.carassistant.bot.slack.framework.annotation.SlackHandler;
import com.carassistant.bot.slack.framework.annotation.SlackMessageActionHandler;
import com.carassistant.bot.slack.framework.model.ActionPayload;
import com.carassistant.bot.slack.handler.ActionName;
import com.carassistant.bot.slack.handler.CallbackId;
import com.carassistant.bot.slack.message.RidesView;
import com.carassistant.bot.slack.framework.UserContext;
import com.carassistant.bot.slack.framework.annotation.CallbackVariable;
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
    public RidesListHandler(@Value("${carassistant.slack.pagination_size}") int pageSize,
                            RideService rideService, UserContext userContext,
                            SlackApiClient slackApiClient) {

        this.pageSize = pageSize;
        this.rideService = rideService;
        this.userContext = userContext;
        this.slackApiClient = slackApiClient;
    }

    @SlackMessageActionHandler(actionName = ActionName.NEXT)
    public void onNext(ActionPayload payload,
                       @CallbackVariable(name = "page") String pageNumStr) throws IOException, SlackApiException {

        int pageNum = Integer.parseInt(pageNumStr);
        pageNum++;
        sendRidesList(payload, pageNum);

    }

    @SlackMessageActionHandler(actionName = ActionName.PREV)
    public void onPrev(ActionPayload payload,
                       @CallbackVariable(name = "page") String pageNumStr) throws IOException, SlackApiException {

        int pageNum = Integer.parseInt(pageNumStr);
        pageNum--;
        sendRidesList(payload, pageNum);

    }

    private void sendRidesList(ActionPayload payload, int pageNum) throws IOException, SlackApiException {
        User user = userContext.getUser();
        Page<Ride> ridesPage = rideService.findAllByStatusAndLocation(Ride.Status.READY, user.getLocation(),
            new PageRequest(pageNum, pageSize, Sort.Direction.DESC, "createdDate"));

        slackApiClient.send(RidesView.createUpdateRequest(payload, ridesPage, user));
    }
}
