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

    float widthToScaleTo = 16f;
    boolean retina = false;

    if (width >= 32) {
      widthToScaleTo = 32f;
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
