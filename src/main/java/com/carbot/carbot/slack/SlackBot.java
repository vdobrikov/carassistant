package com.carbot.carbot.slack;

import com.carbot.carbot.event.RideCancelledEvent;
import com.carbot.carbot.event.RideJoinedEvent;
import com.carbot.carbot.event.RideSharedEvent;
import com.carbot.carbot.model.Ride;
import com.carbot.carbot.service.RideService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Component
public class SlackBot extends Bot {
    private static final Logger LOG = LoggerFactory.getLogger(SlackBot.class);

    private RideService rideService;

    private ApplicationEventPublisher eventPublisher;

    private LoadingCache<String, Ride> userIdsRidesCache;

    private String botToken;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM, dd HH:mm");

    @PostConstruct
    public void init() {
        CacheLoader<String, Ride> loader = new CacheLoader<String, Ride>() {
            @Override
            public Ride load(String key) throws Exception {
                Ride ride =  new Ride();
                ride.setOwnerId(key);
                ride.setState(Ride.State.ASK_FROM);
                return ride;
            }
        };
        userIdsRidesCache = CacheBuilder.newBuilder()
            .expireAfterAccess(12L, TimeUnit.HOURS)
            .build(loader);
    }

    @Autowired
    public SlackBot(RideService rideService, ApplicationEventPublisher eventPublisher, @Value("${slackBotToken}") String botToken) {
        this.rideService = rideService;
        this.eventPublisher = eventPublisher;
        this.botToken = botToken;
    }

    @Override
    public String getSlackToken() {
        return botToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE}, pattern = "^(show|list|ls)\\s+rides$")
    public void onListRides(WebSocketSession session, Event event) {
        LOG.debug("onListRides: session={}; event={}; text={}", session, event.getType(), event.getText());
        List<Ride> readyRides = rideService.findAllByState(Ride.State.READY);
        LOG.debug("Listing all ready rides: {}", readyRides);
        String ridesMessage;
        if (readyRides.isEmpty()) {
            ridesMessage = "Sorry, but there are no shared rides for the moment. Try again later";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            readyRides.stream().forEach(readyRide -> stringBuilder.append(String.format("id: `%s` driver: *%s* from: *%s* to: *%s* at: *%s* seats: *%s*\n",
                readyRide.getId(), readyRide.getOwnerId(), readyRide.getPointFrom(), readyRide.getPointTo(), readyRide.getDate().format(dateFormatter), readyRide.getSeats())));
            ridesMessage = stringBuilder.toString();
        }
        reply(session, event, new Message(ridesMessage));
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE}, pattern = "^(join ride\\s+[0-9a-f]{10,}|ride join\\s+[0-9a-f]{10,})$")
    public void onJoinRide(WebSocketSession session, Event event) {
        LOG.debug("onJoinRide: session={}; event={}; text={}", session, event.getType(), event.getText());

        String userId = event.getUserId();
        String text = event.getText();
        try {
            String rideId = getIdFromText(text);
            LOG.debug("Joining ride {}", rideId);
            Optional<Ride> rideOptional = rideService.findById(rideId);
            if(!rideOptional.isPresent()) {
                throw new SlackBotException(String.format("Ride %s cannot be found", rideId));
            }

            Ride rideToJoin = rideOptional.get();
            if (rideToJoin.getSeats() < 1) {
                throw new SlackBotException(String.format("No free seats for ride %s", rideId));
            }
            if (rideToJoin.getCompanions().contains(userId)) {
                throw new SlackBotException(String.format("You already joined ride %s", rideId));
            }
            rideToJoin.addCompanion(userId);
            rideService.save(rideToJoin);
            eventPublisher.publishEvent(new RideJoinedEvent(rideToJoin, userId));
            LOG.debug("User joined ride. userId={}, rideId={}", userId, rideId);
            reply(session, event, new Message("Joined!"));
        } catch (SlackBotException e) {
            LOG.warn("Failed to join ride", e);
            reply(session, event, new Message(String.format("Cannot join shared ride: %s", e.getMessage())));
        } catch (Exception e) {
            LOG.warn("Failed to join ride", e);
            reply(session, event, new Message("Cannot join shared ride"));
        }
    }

    private String getIdFromText(String text) {
        return text.replaceAll(".*\\s+([0-9a-f]{10,}).*", "$1");
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE}, pattern = "^(cancel)$")
    public void onCancel(WebSocketSession session, Event event) {
        LOG.debug("onCancel: session={}; event={}; text={}", session, event.getType(), event.getText());
        if (userIdsRidesCache.getIfPresent(event.getUserId()) == null) {
            reply(session, event, new Message("Nothing to cancel"));
            return;
        }
        userIdsRidesCache.invalidate(event.getUserId());
        reply(session, event, new Message("Cancelled"));
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE}, pattern = "^(cancel ride\\s+[0-9a-f]{10,}|ride cancel\\s+[0-9a-f]{10,})$")
    public void onCancelRide(WebSocketSession session, Event event) {
        LOG.debug("onCancelRide: session={}; event={}; text={}", session, event.getType(), event.getText());
        String rideId = getIdFromText(event.getText());
        try {
            Optional<Ride> rideOptional = rideService.findById(rideId);
            if (!rideOptional.isPresent()) {
                throw new SlackBotException(String.format("Ride with id %s cannot be found", rideId));
            }
            Ride ride = rideOptional.get();
            if (!event.getUserId().equals(ride.getOwnerId())) {
                throw new SlackBotException("Only owner can cancel the ride");
            }
            rideService.delete(ride);
            eventPublisher.publishEvent(new RideCancelledEvent(ride));
            reply(session, event, new Message("Ride was cancelled"));
        } catch (SlackBotException e) {
            LOG.warn("Failed to cancel ride", e);
            reply(session, event, new Message(String.format("Cannot cancel ride: %s", e.getMessage())));
        } catch (Exception e) {
            LOG.warn("Failed to cancel ride", e);
            reply(session, event, new Message("Cannot cancel ride"));
        }
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE}, pattern = "^(share ride|ride share)$")
    public void onNewShareRide(WebSocketSession session, Event event) {
        LOG.debug("onNewShareRide: session={}; event={}; text={}", session, event.getType(), event.getText());

        String userId = event.getUserId();
        String text = event.getText();

        MDC.put("userId", userId);
        MDC.put("text", text);

        Ride ride = userIdsRidesCache.getIfPresent(userId);
        if (ride == null) {
            LOG.debug("Sharing new ride for userId={}", userId);
            ride = userIdsRidesCache.getUnchecked(userId);
        }

        ride.setState(Ride.State.SAVE_FROM_AND_ASK_TO);

        reply(session, event, new Message("Cool! What is the starting point?"));
    }


    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE})
    public void onDefaultMessage(WebSocketSession session, Event event) {
        LOG.debug("onDefaultMessage: session={}; event={}; text={}", session, event.getType(), event.getText());

        String userId = event.getUserId();
        String text = event.getText();

        Ride ride = userIdsRidesCache.getIfPresent(userId);

        String message = null;
        if (ride != null) {
            message = processShareRide(ride, text, userId);
        }
        reply(session, event, new Message(message));

    }

    private String processShareRide(Ride ride, String text, String userId) {
            LOG.debug("Continue with sharing ride. userId={}; ride.state={}", userId, ride.getState());
            switch (ride.getState()) {
                case ASK_FROM:
                    ride.setState(Ride.State.SAVE_FROM_AND_ASK_TO);
                    return "Cool! What is the starting point?";
                case SAVE_FROM_AND_ASK_TO:
                    ride.setPointFrom(text);
                    ride.setState(Ride.State.SAVE_TO_AND_ASK_DATE);
                    return String.format("Your ride starting point is *%s*. What is the destination?", text);
                case SAVE_TO_AND_ASK_DATE:
                    ride.setPointTo(text);
                    ride.setState(Ride.State.SAVE_DATE_AND_ASK_SEATS);
                    return String.format("Your destination is *%s*. At what time do you plan to depart?", text);
                case SAVE_DATE_AND_ASK_SEATS:
                    LocalDateTime departureDate;
                    try {
                        Parser timeParser = new Parser();
                        List<DateGroup> dateGroups = timeParser.parse(text, new Date());
                        Assert.isTrue(dateGroups.size() == 1, "dateGroups.size should be 1");
                        List<Date> dates = dateGroups.get(0).getDates();
                        Assert.isTrue(dates.size() == 1, "dates.size should be 1");
                        Date date = dates.get(0);
                        departureDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                        if (departureDate.isBefore(LocalDateTime.now())) {
                            throw new SlackBotException(String.format("Departure date is in the past: %s", departureDate.format(dateFormatter)));
                        }
                    } catch (SlackBotException e) {
                        LOG.warn("Failed to parse date. text={}", text, e);
                        return String.format("Failed to configure date: %s. Please try again", e.getMessage());
                    } catch (Exception e) {
                        LOG.warn("Failed to parse date. text={}", text, e);
                        return "Sorry but I cannot understand departure time. Please rephrase";
                    }

                    ride.setDate(departureDate);
                    ride.setState(Ride.State.SAVE_SEATS_AND_COMPLETE);
                    return String.format("You depart at *%s*. How many free seats do you have?",
                        ride.getDate().format(dateFormatter));
                case SAVE_SEATS_AND_COMPLETE:
                    int seats;
                    try {
                        seats = Integer.parseInt(text);
                        if (seats < 1) {
                            throw new SlackBotException("Seats number cannot be less than 1");
                        }
                    } catch (NumberFormatException e) {
                        LOG.warn("Failed to parse seats. text={}", text, e);
                        return "Sorry but I cannot understand how many free seats do you have. Please provide numeric value";
                    } catch (SlackBotException e) {
                        LOG.warn("Failed to configure seats. text={}", text, e);
                        return String.format("Failed to configure free seats count: %s. Please try again", e.getMessage());
                    }
                    ride.setSeats(seats);
                    ride.setState(Ride.State.READY);
                    userIdsRidesCache.invalidate(userId);
                    ride = rideService.save(ride);
                    eventPublisher.publishEvent(new RideSharedEvent(ride));
                    LOG.debug("Sharing ride finished. userId={}; ride.id={}", userId, ride.getId());
                    return String.format("Your ride is shared:\n id: *%s* from: *%s* to: *%s* at: *%s* seats: *%s*\n",
                        ride.getId(), ride.getPointFrom(), ride.getPointTo(), ride.getDate().format(dateFormatter), ride.getSeats());
                default:
                    throw new RuntimeException("Unknown ride state: " + ride.getState());
            }
    }
}
