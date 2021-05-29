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
package com.mallowigi.imageicon.converters

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ImageLoader
import com.intellij.util.ui.JBImageIcon
import com.mallowigi.imageicon.core.IconType
import com.mallowigi.imageicon.core.ImageWrapper
import org.jetbrains.annotations.NonNls
import java.awt.Image
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.lang.Boolean.FALSE
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.imageio.ImageIO
import javax.swing.Icon

class ExtendedImageConverter : ImageToIconConverter, Disposable {
    // Load TwelveMonkeys library to add all supported extensions
    @get:NonNls
    override val extensions: Set<String>
        get() {
            // Load TwelveMonkeys library to add all supported extensions
            if (FALSE == CONTEXT_UPDATED.get()) {
                Thread.currentThread().contextClassLoader = ExtendedImageConverter::class.java.classLoader
                ImageIO.scanForPlugins()
                val otherSupportedExts = Stream.of(*ImageIO.getReaderFormatNames())
                    .map { obj: String -> obj.toLowerCase() }
                    .collect(Collectors.toSet())
                EXTENSIONS.addAll(otherSupportedExts)
                CONTEXT_UPDATED.set(true)
            }
            return Collections.synchronizedSet(EXTENSIONS)
        }

    override val iconType: IconType
        get() = IconType.EXTENDED

    override fun loadImage(byteArrayInputStream: ByteArrayInputStream?, virtualFile: VirtualFile?): Image? {
        return ImageLoader.loadFromStream(byteArrayInputStream!!)
    }

    override fun convert(canonicalFile: VirtualFile?, canonicalPath: String?): Icon? {
        try {
            ImageIO.createImageInputStream(File(canonicalPath)).use { imageInputStream ->
                val imageReaders = ImageIO.getImageReaders(imageInputStream)
                while (imageReaders.hasNext()) {
                    val imageReader = imageReaders.next()
                    try {
                        imageReader.input = imageInputStream
                        val bufferedImage = imageReader.read(0)
                        val image = bufferedImage.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH)
                        if (image != null) return JBImageIcon(image)
                    } catch (e: IOException) {
                        LOG.warn(e.message)
                    } finally {
                        imageReader.dispose()
                    }
                }
            }
        } catch (e: IOException) {
            LOG.warn(e.message)
            return null
        }
        return null
    }

    override fun toBase64(imageWrapper: ImageWrapper?): String? = null

    override fun dispose() = CONTEXT_UPDATED.remove()

    companion object {
        private val EXTENSIONS: MutableSet<String> = mutableSetOf("bigtiff",
            "dcx",
            "icns",
            "ico",
            "jbig2",
            "pam",
            "pbm",
            "pcx",
            "pgm",
            "pnm",
            "ppm",
            "psd",
            "rgbe",
            "tga",
            "tif",
            "tiff",
            "wbmp",
            "xbm",
            "xpm")

        /**
         * Thread that we use to preload TwelveMonkeys extensions
         */
        private val CONTEXT_UPDATED = ThreadLocal.withInitial { false }
        private const val WIDTH = 16
        private const val HEIGHT = 16
        private val LOG = Logger.getInstance(RegularImageConverter::class.java)
    }
}
