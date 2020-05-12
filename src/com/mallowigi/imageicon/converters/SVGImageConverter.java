package com.mallowigi.imageicon.converters;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.Base64;
import com.intellij.util.IconUtil;
import com.intellij.util.SVGLoader;
import com.mallowigi.imageicon.core.IconType;
import com.mallowigi.imageicon.core.ImageWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

public final class SVGImageConverter implements ImageToIconConverter {
  private final int WIDTH = 16;
  private final int HEIGHT = 16;

  @SuppressWarnings("HardCodedStringLiteral")
  @NonNls
  @Override
  public Set<String> getExtensions() {
    return Collections.synchronizedSet(
      Sets.newHashSet("svg")
    );
  }

  @Override
  public IconType getIconType() {
    return IconType.SVG;
  }

  @Override
  public Image loadImage(final ByteArrayInputStream byteArrayInputStream) throws IOException {
    return SVGLoader.load(byteArrayInputStream, 1.0f);
  }

  @NotNull
  @Override
  public Icon convert(final VirtualFile canonicalFile, final String canonicalPath) throws IOException {
    final Ref<URL> url = Ref.create();
    try {
      url.set(new File(canonicalFile.getPath()).toURI().toURL());
    }
    catch (final MalformedURLException ex) {
      //
    }
    final Image bufferedImage = SVGLoader.loadHiDPI(url.get(), new FileInputStream(canonicalPath), ScaleContext.create());

    return IconUtil.toSize(IconUtil.createImageIcon(bufferedImage), WIDTH, HEIGHT);
  }

  @Override
  public String toBase64(final ImageWrapper imageWrapper) {
    return Base64.encode(imageWrapper.getImageBytes());
  }
}
