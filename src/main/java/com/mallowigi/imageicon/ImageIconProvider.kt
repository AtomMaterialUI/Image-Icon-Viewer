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
package com.mallowigi.imageicon

import com.intellij.ide.IconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.IconUtil
import com.intellij.util.indexing.FileBasedIndex
import com.mallowigi.imageicon.converters.SVGImageConverter
import com.mallowigi.imageicon.core.IconType
import javax.swing.Icon

class ImageIconProvider : IconProvider(), DumbAware {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        val fileBasedIndex = FileBasedIndex.getInstance()
        var base64: String? = null
        val project = element.project
        val file = element.containingFile?.virtualFile ?: return null

        fileBasedIndex.processValues(
            /* indexId = */ ImageIconIndex.NAME,
            /* dataKey = */ file.path,
            /* inFile = */ null,
            /* processor = */ { _, value ->
                base64 = value
                false
            },
            /* filter = */ GlobalSearchScope.projectScope(project)
        )

        if (base64 != null) {
            val converter = SVGImageConverter()
            val fromBase64 = converter.fromBase64(base64, IconType.SVG, file, true) ?: return null
            return IconUtil.createImageIcon(fromBase64.image)
        }
        return null
    }

}
