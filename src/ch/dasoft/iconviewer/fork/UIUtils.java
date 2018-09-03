package ch.dasoft.iconviewer.fork;

/**
 * Created by David Sommer on 19.05.17.
 * @author davidsommer
 */
class UIUtils {

    private static final String[] IMAGE_EXTENSIONS  = {"*.jpeg", "*.jpg", "*.png", "*.wbmp", "*.gif", "*.bmp", "*.svg"};

    static boolean isImageFile(String fileName) {
        int dot = fileName.lastIndexOf(".");
        if (dot==-1) {
            return false;
        }
        String fileExt = fileName.substring(dot,fileName.length());
        for (String extension : IMAGE_EXTENSIONS){
            extension = extension.substring(1);
            if (fileExt.equalsIgnoreCase(extension)){
                return true;
            }
        }
        return false;
    }

}
