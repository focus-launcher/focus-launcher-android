package io.focuslauncher.phone.token;

/**
 * Created by Shahab on 2/16/2017.
 */

public class TokenItem {

    public boolean isChipable;
    private TokenItemType itemType;
    private String title;
    private String extra1;
    private String extra2;
    private TokenCompleteType completeType;

    public TokenItem(TokenItemType itemType) {
        this.itemType = itemType;

        switch (itemType) {
            case CONTACT:
                init("@", true, TokenCompleteType.DEFAULT);
                break;
            case DATA:
                init("", true, TokenCompleteType.DEFAULT);
                break;
            case END_OP:
                init("", false, TokenCompleteType.FULL);
                break;
            case APPS:
                init("/", false, TokenCompleteType.FULL);
        }
    }

    private void init(String title, boolean isChipable, TokenCompleteType completeType) {
        this.title = title;
        this.isChipable = isChipable;
        this.completeType = completeType;
    }

    public TokenItemType getItemType() {
        return itemType;
    }

    public boolean isChipable() {
        return isChipable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public TokenCompleteType getCompleteType() {
        return completeType;
    }

    public void setCompleteType(TokenCompleteType completeType) {
        this.completeType = completeType;
    }

    @Override
    public String toString() {
        return "TokenItem{" +
                "itemType=" + itemType +
                ", isChipable=" + isChipable +
                ", title='" + title + '\'' +
                ", extra1='" + extra1 + '\'' +
                ", extra2='" + extra2 + '\'' +
                ", completeType=" + completeType +
                '}';
    }
}
