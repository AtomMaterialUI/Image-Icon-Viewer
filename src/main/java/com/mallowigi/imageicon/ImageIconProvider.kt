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
package com.mallowigi.imageicon

import com.intellij.ide.IconProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import java.io.IOException
import java.util.*
import javax.swing.Icon

class ImageIconProvider : IconProvider(), DumbAware {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        val containingFile = element.containingFile
        if (isValidImagePath(containingFile)) {
            val canonicalFile = Objects.requireNonNull(containingFile.virtualFile.canonicalFile)!!
            val fileName = containingFile.name
            val converter = ImageConverterFactory.create(fileName)
            if (converter != null) {
                try {
                    return converter.convert(canonicalFile, canonicalFile.canonicalPath)
                } catch (e: IOException) {
                    LOG.warn(e.message)
                }
            }
        }
        return null
    }

    companion object {
        private val LOG = Logger.getInstance(ImageIconProvider::class.java)
        private fun isValidImagePath(containingFile: PsiFileSystemItem?): Boolean {
            return containingFile != null &&
                containingFile.virtualFile != null &&
                containingFile.virtualFile.canonicalFile != null &&
                containingFile.virtualFile.canonicalFile!!.canonicalPath != null &&
                !containingFile.virtualFile.canonicalFile!!.canonicalPath!!.contains(".jar")
        }
    }
}
