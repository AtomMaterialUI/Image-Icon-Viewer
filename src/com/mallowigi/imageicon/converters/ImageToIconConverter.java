/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2020 Elior "Mallowigi" Boukhobza, David Sommer and Jonathan Lermitage.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
   * @throws IOException if error
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
