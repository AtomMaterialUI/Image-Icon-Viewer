/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2015-2022 Elior "Mallowigi" Boukhobza, David Sommer and Jonathan Lermitage.
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
package com.mallowigi.imageicon.converters

import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.mallowigi.imageicon.core.Base64
import com.mallowigi.imageicon.core.IconType
import com.mallowigi.imageicon.core.ImageWrapper
import java.awt.Image
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.swing.Icon

interface ImageToIconConverter {
  /**
   * List of supported extensions
   *
   * @return
   */
  val extensions: Set<String?>

  /** Type of icon. */
  val iconType: IconType?

  /** load an image from a byte stream */
  @Throws(IOException::class)
  fun loadImage(byteArrayInputStream: ByteArrayInputStream?, virtualFile: VirtualFile?): Image?

  /**
   * Convert a virtual file into an icon
   *
   * @param canonicalFile the file containing the image
   * @param canonicalPath the path of the image
   * @return an icon if convertible
   * @throws IOException if error
   */
  @Throws(IOException::class)
  fun convert(canonicalFile: VirtualFile?, canonicalPath: String?): Icon?

  /** Convert the image of an image wrapper in base64. */
  fun toBase64(imageWrapper: ImageWrapper?): String?

  /** Wrap an image as an ImageWrapper. */
  fun getImageWrapper(virtualFile: VirtualFile): ImageWrapper? {
    val image: Image?
    val fileContents: ByteArray
    try {
      fileContents = virtualFile.contentsToByteArray()
      val byteArrayInputStream = ByteArrayInputStream(fileContents)
      image = loadImage(byteArrayInputStream, virtualFile)
    } catch (ignored: IOException) {
      // do nothing
      return null
    }
    requireNotNull(image) { "Could not load image properly." }
    return ImageWrapper(iconType!!, image, fileContents)
  }

  /** Load an image as base64. */
  fun fromBase64(
    base64: String?,
    iconType: IconType?,
    canonicalFile: VirtualFile?,
    isSvg: Boolean = true,
  ): ImageWrapper? {
    val decodedBase64 = Base64.decode(base64)
    val byteArrayInputStream = ByteArrayInputStream(decodedBase64)
    val image: Image? = try {
      loadImage(byteArrayInputStream, canonicalFile)
    } catch (ex: IOException) {
      return null
    }
    return ImageWrapper(iconType!!, image!!, decodedBase64, isSvg)
  }

  /** Checks if filename is accepted by this converter. */
  fun isAccepted(fileName: String?): Boolean {
    val fileExt = FileUtilRt.getExtension(fileName!!)
    return extensions.stream().anyMatch { anotherString: String? -> fileExt.equals(anotherString, ignoreCase = true) }
  }
}
