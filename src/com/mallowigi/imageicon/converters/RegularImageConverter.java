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

import com.google.common.collect.Sets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.paint.PaintUtil;
import com.intellij.util.Base64;
import com.intellij.util.ImageLoader;
import com.intellij.util.JBHiDPIScaledImage;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;
import com.intellij.util.ui.UIUtil;
import com.mallowigi.imageicon.core.IconType;
import com.mallowigi.imageicon.core.ImageWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import sun.awt.image.ToolkitImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public final class RegularImageConverter implements ImageToIconConverter {
  private static final GraphicsConfiguration GRAPHICS_CFG =
    GraphicsEnvironment.isHeadless() ? null // some Gradle tasks run IDE in headless
                                     :
    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
  private static final int WIDTH = 16;
  private static final int HEIGHT = 16;

  private static final Logger LOG = Logger.getInstance(RegularImageConverter.class);

  @Nullable
  private static Icon loadImageIcon(final VirtualFile virtualFile) {
    try {
      final Image bytes = com.intellij.util.ImageLoader.loadFromBytes(virtualFile.contentsToByteArray());
      final Image image = ImageUtil.scaleImage(bytes, WIDTH, HEIGHT);

      if (image != null) {
        final JBImageIcon imageIcon = new JBImageIcon(image);
        if (imageIcon.getImage() != null && imageIcon.getIconHeight() > 0 && imageIcon.getIconWidth() > 0) {
          return imageIcon;
        }
      }
    }
    catch (final IOException e) {
      LOG.warn(e.getMessage());
    }
    return null;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @NonNls
  @Override
  public Set<String> getExtensions() {
    return Collections.synchronizedSet(
      Sets.newHashSet("jpeg",
                      "jpg",
                      "png",
                      "wbmp",
                      "gif",
                      "bmp")
    );
  }

  @Override
  public IconType getIconType() {
    return IconType.IMG;
  }

  @Override
  public Image loadImage(final ByteArrayInputStream byteArrayInputStream) {
    return ImageLoader.loadFromStream(byteArrayInputStream);
  }

  @Override
  @Nullable
  public Icon convert(final VirtualFile canonicalFile, final String canonicalPath) {
    final Icon imageIcon = loadImageIcon(canonicalFile);
    if (imageIcon != null) {
      return imageIcon;
    }

    try {
      return new ImageIcon(ImageIO.read(new File(canonicalFile.getPath()))
                             .getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH));
    }
    catch (final IOException e) {
      return null;
    }

  }

  @SuppressWarnings("InstanceofIncompatibleInterface")
  @Override
  public String toBase64(final ImageWrapper imageWrapper) {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
      Image image = imageWrapper.getImage();

      if (image instanceof JBHiDPIScaledImage) {
        image = ((JBHiDPIScaledImage) image).getDelegate();
      }
      else if (image instanceof ToolkitImage) {
        image = ((ToolkitImage) image).getBufferedImage();
      }
      else if (!(image instanceof RenderableImage)) {
        final BufferedImage bufferedImage = UIUtil.createImage(
          GRAPHICS_CFG,
          image.getWidth(null),
          image.getHeight(null),
          BufferedImage.TYPE_INT_RGB,
          PaintUtil.RoundingMode.ROUND);
        bufferedImage.getGraphics().drawImage(image, 0, 0, null);
        image = bufferedImage;
      }

      ImageIO.write((RenderedImage) image, "png", outputStream);
    }
    catch (final IOException e) {
      LOG.warn(e.getMessage());
    }

    return Base64.encode(outputStream.toByteArray());
  }

}
