package com.mallowigi.imageicon.converters;

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

public final class RegularImageConverter implements ImageToIconConverter {
  private static final GraphicsConfiguration GRAPHICS_CFG =
    GraphicsEnvironment.isHeadless() ? null // some Gradle tasks run IDE in headless
                                     :
    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
  private final int WIDTH = 16;
  private final int HEIGHT = 16;
  private final IconType iconType = IconType.IMG;

  @NonNls
  @Override
  public String[] getExtensions() {
    return new String[]{
      "*.jpeg",
      "jpg",
      "png",
      "wbmp",
      "gif",
      "bmp"
    };
  }

  @Override
  public IconType getIconType() {
    return iconType;
  }

  @Override
  public Image loadImage(final ByteArrayInputStream byteArrayInputStream) {
    return ImageLoader.loadFromStream(byteArrayInputStream);
  }

  @Override
  @Nullable
  public Icon convert(final VirtualFile canonicalFile, final String canonicalPath) {
    final Icon imageIcon = loadFromJetBrains(canonicalFile);
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
      } else if (image instanceof ToolkitImage) {
        image = ((ToolkitImage) image).getBufferedImage();
      } else if (!(image instanceof RenderableImage)) {
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
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return Base64.encode(outputStream.toByteArray());
  }

  @Nullable
  private Icon loadFromJetBrains(final VirtualFile virtualFile) {
    try {
      final Image bytes = com.intellij.util.ImageLoader.loadFromBytes(virtualFile.contentsToByteArray());
      final Image image = ImageUtil.scaleImage(bytes, WIDTH, HEIGHT);

      if (image != null) {
        final JBImageIcon imageIcon = new JBImageIcon(image);
        if (imageIcon.getImage() != null && imageIcon.getIconHeight() > 0 && imageIcon.getIconWidth() > 0) {
          return imageIcon;
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
