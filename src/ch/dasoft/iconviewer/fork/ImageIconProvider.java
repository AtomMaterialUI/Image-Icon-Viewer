package ch.dasoft.iconviewer.fork;

import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IconUtil;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * Created by David Sommer on 19.05.17.
 *
 * @author davidsommer
 */
public class ImageIconProvider extends IconProvider {

  private static final int IMG_WIDTH = JBUI.scale(16);
  private static final int IMG_HEIGHT = JBUI.scale(16);

  public Icon getIcon(@NotNull PsiElement psiElement, int flags) {
    PsiFile containingFile = psiElement.getContainingFile();
    if (checkImagePath(containingFile)) {
      Image image;
      final String canonicalPath = containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath();
      try {
        image = ImageLoader.loadFromStream(new BufferedInputStream(new FileInputStream(canonicalPath)));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new IllegalStateException("Error loading preview Icon - " + containingFile.getVirtualFile().getCanonicalFile()
                                                                                        .getCanonicalPath());
      }

      if (image != null) {
        return IconUtil.toSize(IconUtil.createImageIcon(image), IMG_WIDTH, IMG_HEIGHT);
      }
    }
    return null;
  }

  private boolean checkImagePath(PsiFile containingFile) {
    return containingFile != null && containingFile.getVirtualFile() != null && containingFile.getVirtualFile().getCanonicalFile() !=
        null && containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath() != null && UIUtils.isImageFile(containingFile
        .getName()) && !containingFile.getVirtualFile().getCanonicalFile().getCanonicalPath().contains(".jar");
  }
}