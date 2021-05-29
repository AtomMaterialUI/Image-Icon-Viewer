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
package com.mallowigi.imageicon.core

import com.intellij.util.ImageLoader
import com.intellij.util.RetinaImage
import java.awt.Image

class ImageWrapper(val iconType: IconType, image: Image, val imageBytes: ByteArray) {
    val image: Image = scaleImage(image)

    private fun scaleImage(image: Image): Image {
        val width = image.getWidth(null)
        val height = image.getHeight(null)
        require(width == height) { "Image should be square." }
        require(width > 0) { "Width and height are unknown." }

        if (width == 16) return image
        if (width == 32) return RetinaImage.createFrom(image)

        var widthToScaleTo = 16.0f
        var retina = false

        if (width >= 32) {
            widthToScaleTo = 32.0f
            retina = true
        }
        val scaledImage = ImageLoader.scaleImage(image, (widthToScaleTo / width).toDouble())
        return if (retina) RetinaImage.createFrom(scaledImage) else scaledImage
    }
}
