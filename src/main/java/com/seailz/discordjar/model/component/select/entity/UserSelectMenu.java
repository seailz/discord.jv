package com.seailz.discordjar.model.component.select.entity;

import com.seailz.discordjar.model.component.ActionComponent;
import com.seailz.discordjar.model.component.ComponentType;
import com.seailz.discordjar.model.component.select.SelectMenu;
import com.seailz.discordjar.utils.json.SJSONObject;

/**
 * Represents a user select menu
 *
 * @author Seailz
 * @see com.seailz.discordjar.model.component.select.SelectMenu
 * @since 1.0
 */
public class UserSelectMenu implements SelectMenu {

    private String customId;
    private String placeholder;
    private int minValues;
    private int maxValues;
    private boolean isDisabled;

    private SJSONObject raw;

    /**
     * Creates a new user select menu
     *
     * @param customId The custom id of the select menu
     */
    public UserSelectMenu(String customId) {
        this.customId = customId;
    }

    /**
     * Creates a new user select menu
     *
     * @param customId    The custom id of the select menu
     * @param placeholder The placeholder of the select menu
     * @param minValues   The minimum amount of values that can be selected
     * @param maxValues   The maximum amount of values that can be selected
     */
    public UserSelectMenu(String customId, String placeholder, int minValues, int maxValues, boolean isDisabled, SJSONObject raw) {
        this.customId = customId;
        this.placeholder = placeholder;
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.isDisabled = isDisabled;
        this.raw = raw;
    }

    @Override
    public String customId() {
        return customId;
    }

    @Override
    public String placeholder() {
        return placeholder;
    }

    @Override
    public int minValues() {
        return minValues;
    }

    @Override
    public int maxValues() {
        return maxValues;
    }

    public UserSelectMenu setCustomId(String customId) {
        this.customId = customId;
        return this;
    }

    public UserSelectMenu setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public UserSelectMenu setMinValues(int minValues) {
        this.minValues = minValues;
        return this;
    }

    public UserSelectMenu setMaxValues(int maxValues) {
        this.maxValues = maxValues;
        return this;
    }

    @Override
    public ActionComponent setDisabled(boolean disabled) {
        return new UserSelectMenu(
                customId, placeholder, minValues, maxValues, disabled, raw
        );
    }

    @Override
    public ComponentType type() {
        return ComponentType.USER_SELECT;
    }

    @Override
    public boolean isSelect() {
        return true;
    }

    @Override
    public SJSONObject compile() {
        if (minValues > maxValues)
            throw new IllegalArgumentException("Min values cannot be greater than max values");

        SJSONObject obj = new SJSONObject();
        obj.put("type", type().getCode());
        obj.put("custom_id", customId);
        if (placeholder != null) obj.put("placeholder", placeholder);
        if (minValues != 0) obj.put("min_values", minValues);
        if (maxValues != 0) obj.put("max_values", maxValues);
        if (isDisabled) obj.put("disabled", true);
        return obj;
    }

    public static UserSelectMenu decompile(SJSONObject json) {
        String customId = json.has("custom_id") ? json.getString("custom_id") : null;
        String placeholder = json.has("placeholder") ? json.getString("placeholder") : null;
        int minValues = json.has("min_values") ? json.getInt("min_values") : 0;
        int maxValues = json.has("max_values") ? json.getInt("max_values") : 25;
        boolean isDisabled = json.has("disabled") && json.getBoolean("disabled");

        return new UserSelectMenu(customId, placeholder, minValues, maxValues, isDisabled, json);
    }

    @Override
    public SJSONObject raw() {
        return raw;
    }

    @Override
    public void setRaw(SJSONObject raw) {
          this.raw = raw;
    }
}


