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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.paint.PaintUtil
import com.intellij.util.ImageLoader
import com.intellij.util.JBHiDPIScaledImage
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.JBImageIcon
import com.intellij.util.ui.UIUtil
import com.mallowigi.imageicon.core.Base64
import com.mallowigi.imageicon.core.IconType
import com.mallowigi.imageicon.core.ImageWrapper
import org.jetbrains.annotations.NonNls
import sun.awt.image.ToolkitImage
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.awt.image.renderable.RenderableImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

class RegularImageConverter : ImageToIconConverter {
  @get:NonNls
  override val extensions: Set<String>
    get() = setOf("jpeg", "jpg", "png", "wbmp", "gif", "bmp")

  override val iconType: IconType
    get() = IconType.IMG

  override fun loadImage(byteArrayInputStream: ByteArrayInputStream?, virtualFile: VirtualFile?): Image? =
    ImageLoader.loadFromStream(byteArrayInputStream!!)

  override fun convert(canonicalFile: VirtualFile?, canonicalPath: String?): Icon? {
    val imageIcon = loadImageIcon(canonicalFile)
    return imageIcon ?: try {
      ImageIcon(ImageIO.read(File(canonicalFile!!.path)).getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH))
    } catch (_: IOException) {
      null
    }
  }

  @Suppress("UnstableApiUsage")
  override fun toBase64(imageWrapper: ImageWrapper?): String? {
    val outputStream = ByteArrayOutputStream()
    try {
      var image = imageWrapper!!.image

      when (image) {
        is JBHiDPIScaledImage -> image = image.delegate!!
        is ToolkitImage       -> image = image.bufferedImage
        !is RenderableImage   -> {
          val bufferedImage = UIUtil.createImage(
            GRAPHICS_CFG,
            image.getWidth(null).toDouble(),
            image.getHeight(null).toDouble(),
            BufferedImage.TYPE_INT_RGB,
            PaintUtil.RoundingMode.ROUND
          )
          bufferedImage.graphics.drawImage(image, 0, 0, null)
          image = bufferedImage
        }
      }
      ImageIO.write(image as RenderedImage, "png", outputStream)
    } catch (e: IOException) {
      LOG.warn(e.message)
    }
    return Base64.encode(outputStream.toByteArray())
  }

  companion object {
    private val GRAPHICS_CFG = when {
      GraphicsEnvironment.isHeadless() -> null
      else                             -> GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice.defaultConfiguration
    }

    private const val WIDTH = 16
    private const val HEIGHT = 16
    private val LOG = Logger.getInstance(RegularImageConverter::class.java)

    private fun loadImageIcon(virtualFile: VirtualFile?): Icon? {
      try {
        val bytes = ImageLoader.loadFromBytes(virtualFile!!.contentsToByteArray())
        val image = ImageUtil.scaleImage(bytes, WIDTH, HEIGHT)
        if (image != null) {
          val imageIcon = JBImageIcon(image)
          if (imageIcon.image != null && imageIcon.iconHeight > 0 && imageIcon.iconWidth > 0) {
            return imageIcon
          }
        }
      } catch (e: IOException) {
        LOG.warn(e.message)
      }
      return null
    }
  }
}
