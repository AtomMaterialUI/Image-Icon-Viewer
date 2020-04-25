package com.mallowigi.imageicon.converters;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Base64;
import com.mallowigi.imageicon.core.IconType;
import com.mallowigi.imageicon.core.ImageWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

public interface ImageToIconConverter {
  /**
   * List of supported extensions
   *
   * @return
   */
  Set<String> getExtensions();

  /**
   * Type of icon
   */
  IconType getIconType();

  /**
   * load an image from a byte stream
   */
  Image loadImage(final ByteArrayInputStream byteArrayInputStream) throws IOException;

  /**
   * Convert a virtual file into an icon
   *
   * @param canonicalFile the file containing the image
   * @param canonicalPath the path of the image
   * @return an icon if convertible
   * @throws IOException
   */
  Icon convert(VirtualFile canonicalFile, final String canonicalPath) throws IOException;

  /**
   * Convert the image of an image wrapper in base64
   */
  String toBase64(final ImageWrapper imageWrapper);

  /**
   * Wrap an image as an ImageWrapper
   */
  default ImageWrapper getImageWrapper(final VirtualFile virtualFile) {
    final Image image;
    final byte[] fileContents;

    try {
      fileContents = virtualFile.contentsToByteArray();
      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileContents);

      image = loadImage(byteArrayInputStream);
    }
    catch (final IOException ignored) {
      throw new IllegalArgumentException("IOException while trying to load image.");
    }

    if (image == null) {
      throw new IllegalArgumentException("Could not load image properly.");
    }
    return new ImageWrapper(getIconType(), image, fileContents);
  }

  /**
   * Load an image as base64
   */
  @Nullable
  default ImageWrapper fromBase64(final String base64, final IconType iconType) {
    final byte[] decodedBase64 = Base64.decode(base64);
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBase64);
    final Image image;

    try {
      image = loadImage(byteArrayInputStream);
    }
    catch (final IOException ex) {
      return null;
    }
    return new ImageWrapper(iconType, image, decodedBase64);
  }

  /**
   * Checks if filename is accepted by this converter
   */
  default boolean isAccepted(final String fileName) {
    final String fileExt = FileUtilRt.getExtension(fileName);
    return getExtensions().stream().anyMatch(fileExt::equalsIgnoreCase);
  }
}
