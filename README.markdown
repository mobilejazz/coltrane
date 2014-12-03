# Coltrane - An Android File Picker

This library provides functionality for browsing through files and selecting a file on Android. It
is inspired by Android's recently introduced [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider.html), but
it supports API 14 and higher.

## How to use

Apps might have different requirements on file picking. This section is going
to outline three of them, and how they can be easily set up with the help of this library. Note that
all three scenarios are implemented in the example application that is part of this repository.

The functionality of this library is distributed among multiple modules in order to support an extensible
plugin-architecture right from the start. There is a basic library that defines the interface and then
there are modules adding document providers and browser ui. For more information please refer to the section on Modular Architecture.

### Custom UI

In order to provide a user with a custom file picker UI, you need to include the main library, the
providers you want to present the user and select a picker ui. If you go with the default modules
your gradle build file should have the following dependencies:

```gradle
compile 'com.mobilejazz:coltrane:0.1.0-SNAPSHOT@aar'
compile 'com.mobilejazz:coltrane-provider-filesystem:0.1.0-SNAPSHOT@aar'
compile 'com.mobilejazz:coltrane-ui:0.1.0-SNAPSHOT@aar'
```

Currently only these modules are available.

In your `AndroidManifest.xml` you need to add the picker UI as an activity:

```xml
<activity
    android:name="com.mobilejazz.coltrane.ui.DocumentBrowserActivity"
    android:label="@string/choose_file"
    android:theme="@style/Theme.Lollipop" >
</activity>
```

You also need to register your providers. The recommended way of doing this, is to add them to your
Application's `onCreate()` method:

```java
FileSystemProvider.register(getApplicationContext());
```

No everything is set up and you can open the file picker with `startActivityOnResult`:

```java
startActivityForResult(new Intent(this, DocumentBrowserActivity.class), REQUEST_CODE);
```

and receive the results as follows:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
        case REQUEST_CODE:
            // If the file selection was successful
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Get the URI of the selected file
                    Uri documentUri = data.getData();
                    try {
                        Toast.makeText(FileChooserExampleActivity.this,
                                "File Selected: " + documentUri, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("FileSelectorTestActivity", "File select error", e);
                    }
                }
            }
            break;
    }
    super.onActivityResult(requestCode, resultCode, data);
}
```

### Storage Access Framework with fallback

If you want to use the native storage access framework if available, you follow the process described
for the custom UI in order to set up a fallback solution. The only difference is, that when you are
sending the intent to open the file, you need to check the API version and open the SAF if possible.

You can use this code to open a native storage access framework dialog:

```java
@TargetApi(Build.VERSION_CODES.KITKAT)
private void openSafUI() {
    Intent selectFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    selectFile.addCategory(Intent.CATEGORY_OPENABLE);
    selectFile.setType("*/*");
}
```

An implement the fallback like this:

```java
public void openSafWithFallback(View view) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        openSafUI();
    } else {
        // Fallback to custom UI:
        openCustomUI();
    }
}
```

where `openCustomUI()` is implemented as in the previous section. Note that by default the coltrane
providers will not be available to the storage access framework. There is, however, an easy way to
add those providers due to the similarity of the interfaces. The `NativeDocumentsProvider` provides glue
code to wrap any coltrane DocumentsProvider to act as a native SAF DocumentsProvider. Provider developers
will normally provide you with a native version.

For example, the file system provider has an `NativeFileSystemProvider` that allows you to add it's functionality to SAF.
You can enable it by adding this to your application's `AndroidManifest.xml`:

```xml
<provider
    android:name="com.mobilejazz.coltrane.provider.filesystem.NativeFileSystemProvider"
    android:authorities="com.mobilejazz.coltrane.provider.filesystem"
    android:enabled="@bool/use_provider"
    android:exported="true"
    android:grantUriPermissions="true"
    android:permission="android.permission.MANAGE_DOCUMENTS" >
    <intent-filter>
        <action android:name="android.content.action.DOCUMENTS_PROVIDER" />
    </intent-filter>
</provider>
```

### Native UI

This is implemented in the same way as the fallback solution, but in this case the fallback is the
native chooser dialog.

```java
public void openSafWithFallback(View view) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        openSafUI();
    } else {
        // Fallback to custom UI:
        openChooser();
    }
}
```

with

```java
private void openChooser() {
    final Intent selectFile = new Intent(Intent.ACTION_GET_CONTENT);
    // The MIME data type filter
    selectFile.setType("*/*");
    // Only return URIs that can be opened with ContentResolver
    selectFile.addCategory(Intent.CATEGORY_OPENABLE);
    startActivityForResult(selectFile, REQUEST_CODE);
}
```

Note that in order for the custom UI to appear as one option in the chooser, you need to add an
intent filter to the document browser activity element in the application's `AndroidManifest.xml`:

```xml
<intent-filter>
    <action android:name="android.intent.action.GET_CONTENT" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.OPENABLE" />

    <data android:mimeType="*/*" />
</intent-filter>
```

## Modular Architecture

One of the main design goals of the library is *modularization*. Many apps have different use cases for
selecting a file and a modularized approach allows to address most of them with a single library while
still providing a possibility to extend the functionality by writing *plugins*. In order to inherently
support plugins right from the start, most code of this library is distributed within their own plugins.

There are two types of plugins:

### Providers

Providers are a compatible version of the Storage Access Framework's [Documents Provider](https://developer.android.com/reference/android/provider/DocumentsProvider.html). They
implement the functionality to browse and open files for a certain type of file provider, such as
the SD Card or a cloud provider.

Currently the library provides out of the box plugins for the following providers:

#### File System Provider

This provider allows to browse and access files from the device's sd card. It can be setup by adding:

```gradle
compile 'com.mobilejazz:coltrane-provider-filesystem:0.1.0-SNAPSHOT@aar'
```

to your gradle build file and

```java
FileSystemProvider.register(getApplicationContext());
```

to your Application's `onCreate()`.

### UI

UI plugins provide users with a way to select a provider, browse it's files and select a file. This
library provides only one default UI out of the box. It resembles the behaviour of the native
storage access framework UI on Lollipop devices. You can add it by adding

```gradle
compile 'com.mobilejazz:coltrane-ui:0.1.0-SNAPSHOT@aar'
```

to your gradle build file and

```xml
<activity
    android:name="com.mobilejazz.coltrane.ui.DocumentBrowserActivity"
    android:label="@string/choose_file"
    android:theme="@style/Theme.Lollipop" >
</activity>
```

to your applications `AndroidManifest.xml`.

Note, how you can easily modify the ui's title or theme by changing the respective entries in the
activitie's attributes. Opening the file picker can be done easily by sending an intent:

```java
Intent selectFile = new Intent(this, DocumentBrowserActivity.class);
startActivityForResult(selectFile, REQUEST_CODE);
```

and receiving the file id in `onActivityResult`:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
        case REQUEST_CODE:
            // If the file selection was successful
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Get the URI of the selected file
                    String documentId = data.getStringExtra(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                    try {
                        Toast.makeText(FileChooserExampleActivity.this,
                                "File Selected: " + documentId, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("FileSelectorTestActivity", "File select error", e);
                    }
                }
            }
            break;
    }
    super.onActivityResult(requestCode, resultCode, data);
}
```

### Main library

The main library defines the interfaces and compatibility classes. Irrespective of what plugins
you decide to use, it must be always added to the project by adding:

```gradle
compile 'com.mobilejazz:coltrane:0.1.0-SNAPSHOT@aar'
```

to your gradle build file.

## Credits

This project is based on [aFileChooser](https://github.com/iPaulPro/aFileChooser). Though, not much
of the original code is present in the library, it helped guiding the right way and providing resources
such as translated strings and icons.

The file system provider implementation is based on the [Local Storage Provider](https://github.com/ianhanniballake/LocalStorage)
by Ian Lake, but has been adapted for this libraries interface.
