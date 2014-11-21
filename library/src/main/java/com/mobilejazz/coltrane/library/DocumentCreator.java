package com.mobilejazz.coltrane.library;

import java.io.FileNotFoundException;

public interface DocumentCreator {

    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException;

}
