package com.example.webstream;

/*
방송 목록에 출력될 데이터 + 기타 방송 정보 데이터
 */
public class DataList_broadcastList {

    private int number;  //방송 번호
    private String title;   //방송 제목
    private String host;   //방송자 명
    private int viewer;   //시청자 수
    private String routeThumbnail;
    private String routeStream;

    public String getRouteStream() {
        return routeStream;
    }

    public void setRouteStream(String routeStream) {
        this.routeStream = routeStream;
    }

    public String getRouteThumbnail() {
        return routeThumbnail;
    }

    public void setRouteThumbnail(String routeThumbnail) {
        this.routeThumbnail = routeThumbnail;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getViewer() {
        return viewer;
    }

    public void setViewer(int viewer) {
        this.viewer = viewer;
    }
}
