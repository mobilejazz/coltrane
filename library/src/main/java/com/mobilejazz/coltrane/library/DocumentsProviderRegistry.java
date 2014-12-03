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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class DocumentsProviderRegistry {

    private Map<String, DocumentsProvider> mProviders;

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

    public List<Root> getAllRoots() {
        List<Root> result = new ArrayList<Root>();
        for (DocumentsProvider p : getAll()) {
            result.addAll(p.getRoots());
        }
        return result;
    }

    private static DocumentsProviderRegistry instance;

    public static DocumentsProviderRegistry get() {
        if (instance == null) {
            instance = new DocumentsProviderRegistry();
        }
        return instance;
    }

}
