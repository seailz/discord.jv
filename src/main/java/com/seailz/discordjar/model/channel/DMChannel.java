package com.seailz.discordjar.model.channel;

import com.seailz.discordjar.DiscordJar;
import com.seailz.discordjar.action.message.MessageCreateAction;
import com.seailz.discordjar.model.channel.interfaces.MessageRetrievable;
import com.seailz.discordjar.model.channel.interfaces.Messageable;
import com.seailz.discordjar.model.channel.interfaces.Typeable;
import com.seailz.discordjar.model.channel.internal.DMChannelImpl;
import com.seailz.discordjar.model.channel.utils.ChannelType;
import com.seailz.discordjar.model.component.DisplayComponent;
import com.seailz.discordjar.model.user.User;
import com.seailz.discordjar.utils.json.SJSONArray;
import com.seailz.discordjar.utils.json.SJSONObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a DM channel.
 * <br>A DM channel is a private channel between two users, or in the case of a group DM, between multiple users.
 * <p>
 * You can send messages to a DM channel by using {@link #sendMessage(String)}.
 * <br>To create a DM channel, see {@link User#createDM()}
 *
 * @author Seailz
 * @since  1.0
 * @see    User
 * @see    User#createDM()
 * @see    MessageCreateAction
 * @see    DisplayComponent
 * @see    ChannelType#DM
 * @see    ChannelType#GROUP_DM
 */
public interface DMChannel extends Channel, Typeable, Messageable, MessageRetrievable {

    String lastMessageId();
    List<User> recipients();
    DiscordJar discordJv();

    @NotNull
    @Override
    default ChannelType type() {
        return ChannelType.DM;
    }

    default MessageCreateAction sendMessage(String text) {
        return new MessageCreateAction(text, id(), discordJv());
    }
    default MessageCreateAction sendComponents(DisplayComponent... components) {
        return new MessageCreateAction(new ArrayList<>(List.of(components)), id(), discordJv());
    }

    @NotNull
    @Contract("_, _ -> new")
    static DMChannel decompile(@NotNull SJSONObject obj, @NotNull DiscordJar djv) {
        String lastMessageId = obj.has("last_message_id") && !obj.get("last_message_id").equals(SJSONObject.NULL) ? obj.getString("last_message_id") : null;

        List<User> recipients = new ArrayList<>();
        SJSONArray recipientsArray = obj.getJSONArray("recipients");
        recipientsArray.forEach(o -> recipients.add(User.decompile((SJSONObject) o, djv)));

        String name = obj.has("name") ? obj.getString("name") : recipients.get(0).username();
        return new DMChannelImpl(obj.getString("id"), ChannelType.DM, name, lastMessageId ,recipients, djv, obj);
    }

    /**
     * Compiles this object
     */
    @Override
    default SJSONObject compile() {
        SJSONObject obj = new SJSONObject();
        obj.put("id", id());
        obj.put("type", type().getCode());
        if (lastMessageId() != null) obj.put("last_message_id", lastMessageId());

        SJSONArray array = new SJSONArray();
        for (User user : recipients())
            array.put(user.compile());

        obj.put("recipients", array);
        return obj;
    }
}
