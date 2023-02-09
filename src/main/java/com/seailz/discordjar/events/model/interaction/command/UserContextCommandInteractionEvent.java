package com.seailz.discordjar.events.model.interaction.command;

import com.seailz.discordjar.DiscordJar;
import com.seailz.discordjar.model.user.User;
import com.seailz.discordjar.utils.URLS;
import com.seailz.discordjar.rest.DiscordRequest;
import com.seailz.discordjar.rest.DiscordResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;

public class UserContextCommandInteractionEvent extends CommandInteractionEvent {
    public UserContextCommandInteractionEvent(@NotNull DiscordJar bot, long sequence, @NotNull JSONObject data) {
        super(bot, sequence, data);
    }

    /**
     * Returns the context menu command's target as a {@link User}.
     * This will return null if the targeted entity is not a user.
     *
     * @return {@link User} object containing the user data.
     */
    @Nullable
    public User getTarget() {
        DiscordResponse response =
                new DiscordRequest(
                        new JSONObject(),
                        new HashMap<>(),
                        URLS.GET.USER.GET_USER.replace("{user_id}", String.valueOf(getCommandData().targetId())),
                        getBot(),
                        URLS.GET.USER.GET_USER,
                        RequestMethod.GET
                ).invoke();
        return User.decompile(response.body(), getBot());
    }

}