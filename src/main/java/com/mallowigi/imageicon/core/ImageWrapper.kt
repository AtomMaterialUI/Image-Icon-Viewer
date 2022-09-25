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
package com.mallowigi.imageicon.core

import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ImageLoader
import com.intellij.util.RetinaImage
import java.awt.Image

private const val AT2X = 32.0f

private const val AT1X = 16.0f

class ImageWrapper(val iconType: IconType, image: Image, val imageBytes: ByteArray, val isSvg: Boolean = false) {
  val image: Image = scaleImage(image)

  private fun scaleImage(image: Image): Image {
    if (isSvg) return scaleSvg(image)

    val width = image.getWidth(null)

    require(width > 0) { "Width and height are unknown." }

    if (width == SIZE) return image
    if (width == RETINA) return RetinaImage.createFrom(image)

    var widthToScaleTo = JBUIScale.scale(AT1X)
    var retina = false

    if (width >= RETINA) {
      widthToScaleTo = JBUIScale.scale(AT2X)
      retina = true
    }
    val scaledImage = ImageLoader.scaleImage(image, (widthToScaleTo / width).toDouble())
    return if (retina) RetinaImage.createFrom(scaledImage) else scaledImage
  }

  private fun scaleSvg(image: Image): Image {
    val width = image.getWidth(null)
    if (width == SIZE) return image

    return ImageLoader.scaleImage(image, SIZE, SIZE)
  }

  companion object {
    val SIZE = JBUIScale.scale(16)
    val RETINA = JBUIScale.scale(32)
  }
}
