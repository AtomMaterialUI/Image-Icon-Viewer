package com.mallowigi.imageicon;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IconUtil;
import com.intellij.util.ImageLoader;
import com.intellij.util.SVGLoader;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;


/**
 * Created by David Sommer on 19.05.17.
 *
 * @author davidsommer
 */
public class ImageIconProvider extends IconProvider {

  private static final int IMG_WIDTH = 16;
  private static final int IMG_HEIGHT = 16;
  private static final int MAX = 64;

  @Override
  public Icon getIcon(@NotNull final PsiElement psiElement, final int flags) {
    final PsiFile containingFile = psiElement.getContainingFile();
    if (ImageIconProvider.checkImagePath(containingFile)) {
      Image image = null;
      final String canonicalPath = Objects.requireNonNull(containingFile.getVirtualFile().getCanonicalFile()).getCanonicalPath();
      final String fileName = containingFile.getName();

      try {
        if (ImageUtils.isImage(fileName)) {
          image = ImageLoader.loadFromStream(new BufferedInputStream(new FileInputStream(canonicalPath)));
        } else if (ImageUtils.isSvg(fileName)) {
          image = loadSVG(containingFile, canonicalPath);
        }
      } catch (final IOException e) {
        e.printStackTrace();
        throw new IllegalStateException("Error loading preview Icon - " + canonicalPath);
      }

      if (image != null &&
          image.getWidth(null) <= ImageIconProvider.MAX &&
          image.getHeight(null) <= ImageIconProvider.MAX) {
        return IconUtil.toSize(IconUtil.createImageIcon(image), ImageIconProvider.IMG_WIDTH, ImageIconProvider.IMG_HEIGHT);
      }
    }
    return null;
  }

  private BufferedImage loadSVG(final PsiFile containingFile, final String canonicalPath) throws IOException {
    final VirtualFile file = containingFile.getVirtualFile();
    final Ref<URL> url = Ref.create();
    try {
      //      final byte[] content = file.contentsToByteArray();
      url.set(new File(file.getPath()).toURI().toURL());
    } catch (final MalformedURLException ex) {
      ex.printStackTrace();
    }
    return SVGLoader.loadHiDPI(url.get(), new FileInputStream(canonicalPath), JBUI.ScaleContext.create());
  }

  private static boolean checkImagePath(final PsiFile containingFile) {
    return containingFile != null &&
        containingFile.getVirtualFile() != null &&
        containingFile.getVirtualFile().getCanonicalFile() != null &&
        containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath() != null &&
        !containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath().contains(".jar");
  }
}