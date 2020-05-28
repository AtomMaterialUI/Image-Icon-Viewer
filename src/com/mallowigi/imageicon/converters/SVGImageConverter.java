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
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.Base64;
import com.intellij.util.IconUtil;
import com.intellij.util.SVGLoader;
import com.mallowigi.imageicon.core.IconType;
import com.mallowigi.imageicon.core.ImageWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

public final class SVGImageConverter implements ImageToIconConverter {
  private static final int WIDTH = 16;
  private static final int HEIGHT = 16;

  private static final Logger LOG = Logger.getInstance(SVGImageConverter.class);

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
  public Image loadImage(final ByteArrayInputStream byteArrayInputStream, final VirtualFile canonicalFile) throws IOException {
    final Ref<URL> url = Ref.create();
    try {
      url.set(new File(canonicalFile.getPath()).toURI().toURL());
    }
    catch (final MalformedURLException ex) {
      LOG.warn(ex.getMessage());
    }
    //    final Image bufferedImage = SVGLoader.loadHiDPI(url.get(), new FileInputStream(canonicalPath), ScaleContext.create());
    return SVGLoader.loadHiDPI(url.get(), new FileInputStream(canonicalFile.getPath()), ScaleContext.create());
  }

  @Nullable
  @Override
  public Icon convert(final VirtualFile canonicalFile, final String canonicalPath) throws IOException {
    //    final Ref<URL> url = Ref.create();
    //    try {
    //      url.set(new File(canonicalFile.getPath()).toURI().toURL());
    //    }
    //    catch (final MalformedURLException ex) {
    //      LOG.warn(ex.getMessage());
    //    }
    //    final Image bufferedImage = SVGLoader.loadHiDPI(url.get(), new FileInputStream(canonicalPath), ScaleContext.create());
    //
    //    return IconUtil.toSize(IconUtil.createImageIcon(bufferedImage), WIDTH, HEIGHT);
    final ImageWrapper imageWrapper = getImageWrapper(canonicalFile);
    if (imageWrapper == null) {
      return null;
    }
    final ImageWrapper fromBase64 = fromBase64(toBase64(imageWrapper), getIconType(), canonicalFile);
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
