package com.mallowigi.imageicon.converters;

import com.google.common.collect.Sets;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.JBImageIcon;
import com.mallowigi.imageicon.core.IconType;
import com.mallowigi.imageicon.core.ImageWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ExtendedImageConverter implements ImageToIconConverter {
  private static final Set<String> EXTENSIONS =
    Sets.newHashSet("bigtiff",
                    "dcx",
                    "icns",
                    "ico",
                    "jbig2",
                    "pam",
                    "pbm",
                    "pcx",
                    "pgm",
                    "pnm",
                    "ppm",
                    "psd",
                    "rgbe",
                    "tga",
                    "tif",
                    "tiff",
                    "wbmp",
                    "xbm",
                    "xpm");
  /**
   * Thread that we use to preload TwelveMonkeys extensions
   */
  private static final ThreadLocal<Boolean> CONTEXT_UPDATED = ThreadLocal.withInitial(() -> false);
  private static final int WIDTH = 16;
  private static final int HEIGHT = 16;

  @NonNls
  @Override
  public Set<String> getExtensions() {
    // Load TwelveMonkeys library to add all supported extensions
    if (!CONTEXT_UPDATED.get()) {
      Thread.currentThread().setContextClassLoader(ExtendedImageConverter.class.getClassLoader());
      ImageIO.scanForPlugins();

      final Set<String> otherSupportedExts = Stream.of(ImageIO.getReaderFormatNames())
        .map(String::toLowerCase)
        .collect(Collectors.toSet());

      EXTENSIONS.addAll(otherSupportedExts);

      CONTEXT_UPDATED.set(true);
    }

    return Collections.synchronizedSet(EXTENSIONS);
  }

  @Override
  public IconType getIconType() {
    return IconType.EXTENDED;
  }

  @Override
  public Image loadImage(final ByteArrayInputStream byteArrayInputStream) {
    return ImageLoader.loadFromStream(byteArrayInputStream);
  }

  @Override
  @Nullable
  public Icon convert(final VirtualFile canonicalFile, final String canonicalPath) {
    try {
      final ImageInputStream imageInputStream = ImageIO.createImageInputStream(new File(canonicalPath));
      final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

      while (imageReaders.hasNext()) {
        final ImageReader imageReader = imageReaders.next();

        try {
          imageReader.setInput(imageInputStream);
          final BufferedImage bufferedImage = imageReader.read(0);
          final Image image = bufferedImage.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);

          if (image != null) {
            return new JBImageIcon(image);
          }
        }
        finally {
          imageReader.dispose();
        }
      }
    }
    catch (final IOException e) {
      return null;
    }
    return null;
  }

  @Nullable
  @Override
  public String toBase64(final ImageWrapper imageWrapper) {
    return null;
  }

}
