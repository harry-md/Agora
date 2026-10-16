package com.agora.media.internal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

class CloudinaryStorageServiceImplTest {

    private static final String URL =
            "https://res.cloudinary.com/demo/image/upload/v123/manufacturers/apple.png";

    private final Cloudinary cloudinary = mock(Cloudinary.class);
    private final Uploader uploader = mock(Uploader.class);
    private final CloudinaryStorageServiceImpl service =
            new CloudinaryStorageServiceImpl(cloudinary, mock(Tika.class));

    @Test
    void deleteUsesPublicIdFromUrl() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);

        service.delete(URL);

        verify(uploader).destroy("manufacturers/apple", Map.of("resource_type", "image"));
    }

    @Test
    void deletePropagatesOriginalIOException() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        var failure = new IOException("Cloudinary unavailable");
        when(uploader.destroy(eq("manufacturers/apple"), anyMap())).thenThrow(failure);

        assertSame(failure, assertThrows(IOException.class, () -> service.delete(URL)));
    }

    @Test
    void deletePropagatesOriginalRuntimeException() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        var failure = new IllegalStateException("Cloudinary unavailable");
        when(uploader.destroy(eq("manufacturers/apple"), anyMap())).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.delete(URL)));
    }

    @Test
    void nullUrlDoesNotCallCloudinary() throws IOException {
        service.delete(null);

        verifyNoInteractions(cloudinary, uploader);
    }
}
