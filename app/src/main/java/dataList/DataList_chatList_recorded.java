package dataList;

public class DataList_chatList_recorded {
    String nickname;
    String content;
    int chatTime;

    public static final int HOST =0;
    public static final int VIEWER =1;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getChatTime() {
        return chatTime;
    }

    public void setChatTime(int chatTime) {
        this.chatTime = chatTime;
    }


}
