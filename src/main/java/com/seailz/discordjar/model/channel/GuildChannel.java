package com.seailz.discordjar.model.channel;

import com.seailz.discordjar.DiscordJar;
import com.seailz.discordjar.action.channel.invites.CreateChannelInviteAction;
import com.seailz.discordjar.model.channel.internal.GuildChannelImpl;
import com.seailz.discordjar.model.channel.utils.ChannelType;
import com.seailz.discordjar.model.guild.Guild;
import com.seailz.discordjar.model.permission.PermissionOverwrite;
import com.seailz.discordjar.utils.Checker;
import com.seailz.discordjar.utils.URLS;
import com.seailz.discordjar.utils.json.SJSONArray;
import com.seailz.discordjar.utils.json.SJSONObject;
import com.seailz.discordjar.utils.rest.DiscordRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface GuildChannel extends Channel {

    @NotNull
    Guild guild();

    // Will return 0 if not found
    int position();

    @Nullable
    List<PermissionOverwrite> permissionOverwrites();

    boolean nsfw();

    @NotNull SJSONObject raw();

    @Override
    default SJSONObject compile() {
        SJSONObject obj = new SJSONObject();
        obj.put("id", id());
        obj.put("type", type().getCode());
        obj.put("name", name());
        obj.put("guild_id", guild().id());
        obj.put("position", position());
        obj.put("nsfw", nsfw());

        if (permissionOverwrites() != null) {
            SJSONArray array = new SJSONArray();
            for (PermissionOverwrite overwrite : permissionOverwrites())
                array.put(overwrite.compile());
        }

        obj.put("permission_overwrites", permissionOverwrites());
        return obj;
    }

    /**
     * Decompile a {@link SJSONObject} into a {@link GuildChannel}
     *
     * @param obj The {@link SJSONObject} to decompile
     * @param discordJar The {@link DiscordJar} instance
     *
     * @return The {@link GuildChannel} instance
     */
    @NotNull
    @Contract("_, _ -> new")
    static GuildChannel decompile(@NotNull SJSONObject obj, @NotNull DiscordJar discordJar) {
        String id = obj.getString("id");
        ChannelType type = ChannelType.fromCode(obj.getInt("type"));
        String name = obj.getString("name");
        Guild guild = obj.has("guild_id") ? discordJar.getGuildById(obj.getString("guild_id")) : null;
        int position = obj.has("position") ? obj.getInt("position") : 0;
        boolean nsfw = obj.has("nsfw") && obj.getBoolean("nsfw");

        List<PermissionOverwrite> permissionOverwrites = new ArrayList<>();
        if (obj.has("permission_overwrites")) {
            SJSONArray array = obj.getJSONArray("permission_overwrites");
            for (int i = 0; i < array.length(); i++) {
                SJSONObject overwrite = array.getJSONObject(i);
                permissionOverwrites.add(PermissionOverwrite.decompile(overwrite));
            }
        }

        return new GuildChannelImpl(id, type, name, guild, position, permissionOverwrites, nsfw, obj, discordJar);
    }

    @NotNull
    DiscordJar discordJv();

    /**
     * Returns this class as a {@link MessagingChannel}, or null if it is not a messaging channel.
     * @throws IllegalArgumentException If the channel is not a messaging channel
     */
    @Nullable
    default MessagingChannel asMessagingChannel() {
        try {
            return MessagingChannel.decompile(raw(), discordJv());
        } catch (Exception e) {
            Checker.check(true, "This channel is not a messaging channel");
        }
        return null;
    }

    /**
     * Adds a {@link PermissionOverwrite} to this channel
     * @param overwrite The {@link PermissionOverwrite} to add
     */
    default void addPermissionOverwrite(@NotNull PermissionOverwrite overwrite) {
        permissionOverwrites().add(overwrite);
        modify().setPermissionOverwrites(permissionOverwrites()).run();
    }

    default CreateChannelInviteAction createInvite() {
        return new CreateChannelInviteAction(discordJv(), id());
    }

    default void editChannelPermissions(PermissionOverwrite ov) {
        DiscordRequest req = new DiscordRequest(
                ov.compile(),
                new HashMap<>(),
                URLS.PUT.CHANNELS.PERMISSIONS.EDIT_CHANNEL_PERMS.replace("{channel.id}", id()).replace("{overwrite.id}", ov.id()),
                discordJv(),
                URLS.PUT.CHANNELS.PERMISSIONS.EDIT_CHANNEL_PERMS,
                RequestMethod.PUT
        );
        try {
            req.invoke();
        } catch (DiscordRequest.UnhandledDiscordAPIErrorException e) {
            throw new DiscordRequest.DiscordAPIErrorException(e);
        }
    }
}
