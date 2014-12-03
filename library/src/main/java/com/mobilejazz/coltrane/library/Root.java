package com.mobilejazz.coltrane.library;

public class Root {

    private DocumentsProvider provider;

    private String id;
    private String documentId;
    private String title;
    private int icon;

    public Root(DocumentsProvider provider, String id, String documentId, String title, int icon) {
        this.provider = provider;
        this.id = id;
        this.documentId = documentId;
        this.title = title;
        this.icon = icon;
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

    // TODO: complete


}
