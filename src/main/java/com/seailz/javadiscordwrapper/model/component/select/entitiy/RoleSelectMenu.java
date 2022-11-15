package com.seailz.javadiscordwrapper.model.component.select.entitiy;

import com.seailz.javadiscordwrapper.model.component.ActionComponent;
import com.seailz.javadiscordwrapper.model.component.ComponentType;
import com.seailz.javadiscordwrapper.model.component.select.SelectMenu;
import org.json.JSONObject;

/**
 * Represents a role select menu
 *
 * @author Seailz
 * @since  1.0
 * @see    com.seailz.javadiscordwrapper.model.component.select.SelectMenu
 */
public class RoleSelectMenu implements SelectMenu {

    private String customId;
    private String placeholder;
    private int minValues;
    private int maxValues;
    private boolean isDisabled;

    /**
     * Creates a new role select menu
     *
     * @param customId The custom id of the select menu
     */
    public RoleSelectMenu(String customId) {
        this.customId = customId;
    }

    /**
     * Creates a new role select menu
     *
     * @param customId    The custom id of the select menu
     * @param placeholder The placeholder of the select menu
     * @param minValues   The minimum amount of values that can be selected
     * @param maxValues   The maximum amount of values that can be selected
     * @param disabled    If the select menu is disabled
     */
    public RoleSelectMenu(String customId, String placeholder, int minValues, int maxValues, boolean disabled) {
        this.customId = customId;
        this.placeholder = placeholder;
        this.minValues = minValues;
        this.maxValues = maxValues;
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

    public RoleSelectMenu setCustomId(String customId) {
        this.customId = customId;
        return this;
    }

    public RoleSelectMenu setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public RoleSelectMenu setMinValues(int minValues) {
        this.minValues = minValues;
        return this;
    }

    public RoleSelectMenu setMaxValues(int maxValues) {
        this.maxValues = maxValues;
        return this;
    }

    @Override
    public ActionComponent setDisabled(boolean disabled) {
        return new RoleSelectMenu(
                customId, placeholder, minValues, maxValues, disabled
        );
    }

    @Override
    public ComponentType type() {
        return ComponentType.ROLE_SELECT;
    }

    @Override
    public boolean isSelect() {
        return true;
    }

    @Override
    public JSONObject compile() {
        JSONObject obj = new JSONObject();
        obj.put("type", type().getCode());
        obj.put("custom_id", customId);
        if (placeholder != null)
            obj.put("placeholder", placeholder);

        if (minValues != 0)
            obj.put("min_values", minValues);

        if (maxValues != 0)
            obj.put("max_values", maxValues);

        if (isDisabled)
            obj.put("disabled", true);
        return obj;
    }

    public static RoleSelectMenu decompile(JSONObject json) {
        String customId = json.has("custom_id") ? json.getString("custom_id") : null;
        String placeholder = json.has("placeholder") ? json.getString("placeholder") : null;
        int minValues = json.has("min_values") ? json.getInt("min_values") : 0;
        int maxValues = json.has("max_values") ? json.getInt("max_values") : 25;
        boolean disabled = json.has("disabled") && json.getBoolean("disabled");

        return new RoleSelectMenu(customId, placeholder, minValues, maxValues, disabled);
    }


}
