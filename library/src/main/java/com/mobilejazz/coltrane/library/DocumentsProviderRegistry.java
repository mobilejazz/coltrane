/*
 * Copyright (C) 2014 Mobilejazz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilejazz.coltrane.library;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DocumentsProviderRegistry {

    private Map<String, DocumentsProvider> mProviders;
    private DocumentsProvider mDefaultProvider;

    public DocumentsProviderRegistry() {
        mProviders = new HashMap<String, DocumentsProvider>();
    }

    public DocumentsProvider getProvider(String id) {
        return mProviders.get(id);
    }

    public void register(String id, DocumentsProvider provider) {
        mProviders.put(id, provider);
    }

    public Collection<DocumentsProvider> getAll() {
        return mProviders.values();
    }

    public void registerAsDefault(String id, DocumentsProvider provider) {
        if (mDefaultProvider == null) {
            register(id, provider);
            mDefaultProvider = provider;
        } else {
            throw new IllegalStateException("Could not register " + provider.getId() + ". There is already a provider registered: " + mDefaultProvider.getId());
        }
    }

    public DocumentsProvider getDefault() {
        if (mDefaultProvider != null) {
            return mDefaultProvider;
        } else if (mProviders.size() > 0) {
            return mProviders.values().iterator().next();
        } else {
            throw new NoSuchElementException("No provider registered.");
        }
    }

    private static DocumentsProviderRegistry instance;

    public static DocumentsProviderRegistry get() {
        if (instance == null) {
            instance = new DocumentsProviderRegistry();
        }
        return instance;
    }

}
