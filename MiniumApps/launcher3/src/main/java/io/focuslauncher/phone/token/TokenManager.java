package io.focuslauncher.phone.token;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Shahab on 2/16/2017.
 */
public class TokenManager {
    private static TokenManager tokenManager;
    private List<TokenItem> items = new ArrayList<>();
    private String patternString;


    public TokenManager() {
        init();
    }

    public static TokenManager getInstance() {
        if (tokenManager == null) {
            tokenManager = new TokenManager();
        }
        return tokenManager;
    }

    public void init() {
        items.clear();
        items.add(new TokenItem(TokenItemType.DATA));
    }

    public void clear() {
        init();
        EventBus.getDefault().post(new TokenUpdateEvent());
    }

    public TokenItem get(int pos) {
        return items.get(pos);
    }

    public TokenItem get(TokenItemType type) {
        for (TokenItem token : items) {
            if (token.getItemType() == type) return token;
        }
        return null;
    }

    public void add(TokenItem item) {
        items.add(item);
    }

    public void deleteLast() {
        if (!items.isEmpty()) items.remove(items.size() - 1);
        if (!items.isEmpty()) items.remove(items.size() - 1);
        if (items.isEmpty()) clear();
    }

    public TokenItem getCurrent() {
        return items.get(items.size() - 1);
    }

    public void setCurrent(TokenItem item) {
        items.set(items.size() - 1, item);
    }

    public List<TokenItem> getItems() {
        return items;
    }

    public boolean has(TokenItemType type) {
        for (TokenItem token : items) {
            if (token.getItemType() == type) return true;
        }
        return false;
    }

    public boolean hasCompleted(TokenItemType type) {
        for (TokenItem token : items) {
            if (token.getItemType() == type && token.getCompleteType() == TokenCompleteType.FULL)
                return true;
        }
        return false;
    }

    public int getLength() {
        return items.size();
    }

    public String getPattern() {
        return patternString;
    }

    public void setPattern(String pattern) {
        this.patternString = pattern;
    }
}
