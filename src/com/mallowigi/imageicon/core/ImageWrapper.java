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

package com.mallowigi.imageicon.core;

import com.intellij.util.ImageLoader;
import com.intellij.util.RetinaImage;

import java.awt.*;

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public final class ImageWrapper {
  private final IconType iconType;
  private final Image image;
  private final byte[] imageBytes;

  public ImageWrapper(final IconType iconType, final Image image, final byte[] imageBytes) {
    this.iconType = iconType;
    this.image = scaleImage(image);
    this.imageBytes = imageBytes;
  }

  private static Image scaleImage(final Image image) {
    final int width = image.getWidth(null);
    final int height = image.getHeight(null);

    if (width != height) {
      throw new IllegalArgumentException("Image should be square.");
    }

    if (width <= 0) {
      throw new IllegalArgumentException("Width and height are unknown.");
    }

    if (width == 16) {
      return image;
    }

    if (width == 32) {
      return RetinaImage.createFrom(image);
    }

    float widthToScaleTo = 16.0f;
    boolean retina = false;

    if (width >= 32) {
      widthToScaleTo = 32.0f;
      retina = true;
    }

    final Image scaledImage = ImageLoader.scaleImage(image, widthToScaleTo / width);

    if (retina) {
      return RetinaImage.createFrom(scaledImage);
    }

    return scaledImage;
  }

  public Image getImage() {
    return image;
  }

  public byte[] getImageBytes() {
    return imageBytes;
  }

  IconType getIconType() {
    return iconType;
  }

}
