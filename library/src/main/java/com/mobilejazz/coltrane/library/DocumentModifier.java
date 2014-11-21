package com.mobilejazz.coltrane.library;

import java.io.FileNotFoundException;

public interface DocumentModifier {

    public String renameDocument(String documentId, String displayName) throws FileNotFoundException;

    public void deleteDocument(String documentId) throws FileNotFoundException;

}
