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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.Base64
import com.intellij.util.IconUtil
import com.intellij.util.SVGLoader
import com.mallowigi.imageicon.core.IconType
import com.mallowigi.imageicon.core.ImageWrapper
import org.jetbrains.annotations.NonNls
import java.awt.Image
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.swing.Icon

class SVGImageConverter : ImageToIconConverter {
    @get:NonNls
    override val extensions: Set<String>
        get() = setOf("svg")

    override val iconType: IconType
        get() = IconType.SVG

    @Throws(IOException::class)
    override fun loadImage(byteArrayInputStream: ByteArrayInputStream?, virtualFile: VirtualFile?): Image? {
        val url = Ref.create<URL>()
        try {
            url.set(File(virtualFile!!.path).toURI().toURL())
        } catch (ex: MalformedURLException) {
            LOG.warn(ex.message)
        }
        return SVGLoader.loadHiDPI(url.get(), FileInputStream(virtualFile!!.path), ScaleContext.create())
    }

    @Throws(IOException::class)
    override fun convert(canonicalFile: VirtualFile?, canonicalPath: String?): Icon? {
        val imageWrapper = getImageWrapper(canonicalFile!!) ?: return null
        val fromBase64 = fromBase64(toBase64(imageWrapper), iconType, canonicalFile) ?: return null
        return IconUtil.createImageIcon(fromBase64.image)
    }

    override fun toBase64(imageWrapper: ImageWrapper?): String? = Base64.encode(imageWrapper!!.imageBytes)

    companion object {
        private val LOG = Logger.getInstance(SVGImageConverter::class.java)
    }
}
