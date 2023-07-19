package org.nwolfhub;

public class Note {
    public String name;
    public String content;
    public Boolean isOnline = false;

    public Note() {

    }

    public Note(String name, String content, Boolean isOnline) {
        this.name = name;
        this.content = content;
        this.isOnline = isOnline;
    }

    public String getName() {
        return name;
    }

    public Note setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Note setContent(String content) {
        this.content = content;
        return this;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public Note setOnline(Boolean online) {
        isOnline = online;
        return this;
    }
}
