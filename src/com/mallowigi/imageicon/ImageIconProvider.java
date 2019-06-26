/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 David Sommer and Elior Boukhobza
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
 *
 *
 */

package com.mallowigi.imageicon;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.util.IconUtil;
import com.intellij.util.ImageLoader;
import com.intellij.util.SVGLoader;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings({"OverlyBroadCatchBlock",
    "OverlyBroadThrowsClause"})
public class ImageIconProvider extends IconProvider implements DumbAware {
  private static final int IMG_WIDTH = 16;
  private static final int IMG_HEIGHT = 16;
  private static final int MAX = 64;
  private static final String[] IMAGE_EXTENSIONS = {
      "*.jpeg",
      "jpg",
      "png",
      "wbmp",
      "gif",
      "bmp"
  };

  private static boolean isSvg(final String filename) {
    @NonNls final String fileExt = FileUtilRt.getExtension(filename);
    return "svg".equals(fileExt);
  }

  private static boolean isImage(final String filename) {
    final String fileExt = FileUtilRt.getExtension(filename);
    return Arrays.stream(IMAGE_EXTENSIONS).anyMatch(fileExt::equalsIgnoreCase);
  }

  @Nullable
  @Override
  public final Icon getIcon(@NotNull final PsiElement element, final int flags) {
    final PsiFile containingFile = element.getContainingFile();
    if (checkImagePath(containingFile)) {
      Image image = null;
      final String canonicalPath = Objects.requireNonNull(containingFile.getVirtualFile().getCanonicalFile()).getCanonicalPath();
      final String fileName = containingFile.getName();

      try {
        if (isImage(fileName)) {
          assert canonicalPath != null;
          image = ImageLoader.loadFromStream(new BufferedInputStream(new FileInputStream(canonicalPath)));
        } else if (isSvg(fileName)) {
          image = loadSVG(containingFile, canonicalPath);
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }

      if (image != null && image.getWidth(null) <= MAX && image.getHeight(null) <= MAX) {
        return IconUtil.toSize(IconUtil.createImageIcon(image), IMG_WIDTH, IMG_HEIGHT);
      }
    }
    return null;
  }

  private static BufferedImage loadSVG(final PsiFileSystemItem containingFile, final String canonicalPath) throws IOException {
    final VirtualFile file = containingFile.getVirtualFile();
    final Ref<URL> url = Ref.create();
    try {
      url.set(new File(file.getPath()).toURI().toURL());
    } catch (final MalformedURLException ex) {
      ex.printStackTrace();
    }
    return SVGLoader.loadHiDPI(url.get(), new FileInputStream(canonicalPath), JBUI.ScaleContext.create());
  }

  @SuppressWarnings({"OverlyComplexBooleanExpression",
      "HardCodedStringLiteral",
      "MethodWithMoreThanThreeNegations"})
  private static boolean checkImagePath(final PsiFileSystemItem containingFile) {
    return containingFile != null &&
        containingFile.getVirtualFile() != null &&
        containingFile.getVirtualFile().getCanonicalFile() != null &&
        containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath() != null &&
        !containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath().contains(".jar");
  }
}