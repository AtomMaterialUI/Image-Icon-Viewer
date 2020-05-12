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

package com.mallowigi.imageicon;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.mallowigi.imageicon.converters.ImageToIconConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class ImageIconProvider extends IconProvider implements DumbAware {
  private static final Logger LOG = Logger.getInstance(ImageIconProvider.class);

  @SuppressWarnings({"OverlyComplexBooleanExpression",
                      "HardCodedStringLiteral",
                      "MethodWithMoreThanThreeNegations"})
  private static boolean isValidImagePath(final PsiFileSystemItem containingFile) {
    return containingFile != null &&
      containingFile.getVirtualFile() != null &&
      containingFile.getVirtualFile().getCanonicalFile() != null &&
      containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath() != null &&
      !containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath().contains(".jar");
  }

  @Nullable
  @Override
  public final Icon getIcon(@NotNull final PsiElement element, final int flags) {
    final PsiFile containingFile = element.getContainingFile();

    if (isValidImagePath(containingFile)) {
      final VirtualFile canonicalFile = Objects.requireNonNull(containingFile.getVirtualFile().getCanonicalFile());
      final String fileName = containingFile.getName();
      final ImageToIconConverter converter = ImageConverterFactory.create(fileName);

      if (converter != null) {
        try {
          return converter.convert(canonicalFile, canonicalFile.getCanonicalPath());
        }
        catch (final IOException e) {
          LOG.warn(e.getMessage());
        }
      }
    }
    return null;
  }
}