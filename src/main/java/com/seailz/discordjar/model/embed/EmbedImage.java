package com.seailz.discordjar.model.embed;

import com.seailz.discordjar.core.Compilerable;
import com.seailz.discordjar.utils.json.SJSONObject;
import org.springframework.lang.NonNull;

public record EmbedImage(
        String url,
        String proxyUrl,
        int height,
        int width
) implements Compilerable {

    @Override
    public SJSONObject compile() {
        SJSONObject obj = new SJSONObject();
        obj.put("url", url);
        obj.put("proxy_url", proxyUrl);
        if (height != 0) obj.put("height", height);
        if (width != 0) obj.put("width", width);
        return obj;
    }

    @NonNull
    public static EmbedImage decompile(SJSONObject obj) {
        String url;
        String proxyUrl;
        int height;
        int width;

        try {
            url = obj.getString("url");
        } catch (Exception e) {
            url = null;
        }

        try {
            proxyUrl = obj.getString("proxy_url");
        } catch (Exception e) {
            proxyUrl = null;
        }

        try {
            height = obj.getInt("height");
        } catch (Exception e) {
            height = 0;
        }

        try {
            width = obj.getInt("width");
        } catch (Exception e) {
            width = 0;
        }
        return new EmbedImage(url, proxyUrl, height, width);
    }

}
