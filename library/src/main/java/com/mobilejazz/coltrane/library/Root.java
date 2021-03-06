package com.mobilejazz.coltrane.library;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Root {

    private DocumentsProvider provider;

    private String id;
    private String documentId;
    private String title;
    private int icon;
    private long availableBytes;
    private int flags;

    public Root() {}

    public Root(DocumentsProvider provider, String id, String documentId, String title, int icon, long availableBytes, int flags) {
        this.provider = provider;
        this.id = id;
        this.documentId = documentId;
        this.title = title;
        this.icon = icon;
        this.availableBytes = availableBytes;
        this.flags = flags;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        try {
            Root r = (Root)o;
            return id.equals(r.id);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public DocumentsProvider getProvider() {
        return provider;
    }

    public String getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public long getAvailableBytes() {
        return availableBytes;
    }

    public int getFlags() {
        return flags;
    }

    public void setProvider(DocumentsProvider provider) {
        this.provider = provider;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setAvailableBytes(long availableBytes) {
        this.availableBytes = availableBytes;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    // TODO: complete

    public void update() {}

}
