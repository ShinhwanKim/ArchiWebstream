package dataList;

public class DataList_chatList_viewStream {
    private String id;
    private String content;
    private String nickname;

    public static final int HOST =0;
    public static final int VIEWER =1;
    public static final int ME =2;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
