package com.umnvd.sensetestapp.models;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Objects;

public class FileItem {
    public final String name;
    public final Uri uri;

    public FileItem(@NonNull String name, @NonNull Uri uri) {
        this.name = name;
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileItem fileItem = (FileItem) o;
        return name.equals(fileItem.name) && uri.equals(fileItem.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uri);
    }
}
