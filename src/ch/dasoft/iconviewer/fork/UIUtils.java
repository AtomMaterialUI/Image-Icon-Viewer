package ch.dasoft.iconviewer.fork;

import com.intellij.openapi.util.io.FileUtilRt;

import java.util.Arrays;

/**
 * Created by David Sommer on 19.05.17.
 *
 * @author davidsommer
 */
class UIUtils {

  private static final String[] IMAGE_EXTENSIONS = {"*.jpeg",
      "jpg",
      "png",
      "wbmp",
      "gif",
      "bmp"
  };


  static boolean isImage(final String filename) {
    final String fileExt = FileUtilRt.getExtension(filename);
    return Arrays.stream(UIUtils.IMAGE_EXTENSIONS).anyMatch(fileExt::equalsIgnoreCase);
  }

  static boolean isSvg(final String filename) {
    final String fileExt = FileUtilRt.getExtension(filename);
    return fileExt.equals("svg");
  }

}
