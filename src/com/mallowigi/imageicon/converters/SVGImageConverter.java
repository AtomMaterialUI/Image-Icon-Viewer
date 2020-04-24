package com.mallowigi.imageicon.converters;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Base64;
import com.intellij.util.IconUtil;
import com.intellij.util.SVGLoader;
import com.mallowigi.imageicon.core.IconType;
import com.mallowigi.imageicon.core.ImageWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class SVGImageConverter implements ImageToIconConverter {

  @NonNls
  @Override
  public String[] getExtensions() {
    return new String[]{
      "svg"
    };
  }

  @Override
  public IconType getIconType() {
    return IconType.SVG;
  }

  @Override
  public Image loadImage(final ByteArrayInputStream byteArrayInputStream) throws IOException {
    return SVGLoader.load(byteArrayInputStream, 1.0f);
  }

  @Nullable
  @Override
  public Icon convert(final VirtualFile canonicalFile, final String canonicalPath) {
    //    final Ref<URL> url = Ref.create();
    //    try {
    //      url.set(new File(canonicalFile.getPath()).toURI().toURL());
    //    } catch (final MalformedURLException ex) {
    //      ex.printStackTrace();
    //    }

    //    final BufferedImage image = SVGLoader.loadHiDPI(url.get(), new FileInputStream(canonicalPath), ScaleContext.create());
    //    if (image != null && image.getWidth(null) <= MAX && image.getHeight(null) <= MAX) {
    //      return IconUtil.toSize(
    //        IconUtil.createImageIcon(image),
    //        WIDTH,
    //        HEIGHT);

    // Convert image to base64
    final ImageWrapper imageWrapper = getImageWrapper(canonicalFile);
    if (imageWrapper == null) {
      return null;
    }
    final ImageWrapper fromBase64 = fromBase64(toBase64(imageWrapper), getIconType());
    if (fromBase64 == null) {
      return null;
    }
    return IconUtil.createImageIcon(fromBase64.getImage());
  }

  @Override
  public String toBase64(final ImageWrapper imageWrapper) {
    return Base64.encode(imageWrapper.getImageBytes());
  }
}
