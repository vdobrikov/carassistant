package com.carassistant.bot.slack.handler;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface CallbackId {
    String RIDE_SHARE_DIALOG = "/rides/new";
    String USER_DETAILS_DIALOG = "/users/{id}/submission";
    String RIDE_SHARE_CONFIRMATION = "/rides/{id}/submission";
    String RIDE_INITIAL_PROMPT = "/welcome/rides";
    String USER_INITIAL_PROMPT = "/welcome/users";
    String RIDE_ACTIONS = "/rides/{id}/actions";
    String RIDE_LIST = "/rides/pages/{page}";
}
