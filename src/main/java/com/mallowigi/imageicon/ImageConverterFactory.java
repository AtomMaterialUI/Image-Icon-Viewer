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

package com.mallowigi.imageicon;

import com.google.common.collect.Sets;
import com.mallowigi.imageicon.converters.ExtendedImageConverter;
import com.mallowigi.imageicon.converters.ImageToIconConverter;
import com.mallowigi.imageicon.converters.RegularImageConverter;
import com.mallowigi.imageicon.converters.SVGImageConverter;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

enum ImageConverterFactory {
  ;
  private static final Set<ImageToIconConverter> CONVERTERS = Collections.unmodifiableSet(
    Sets.newHashSet(
      new RegularImageConverter(),
      new SVGImageConverter(),
      new ExtendedImageConverter()
    )
  );

  static ImageToIconConverter create(final String fileName) {
    final Optional<ImageToIconConverter> first = CONVERTERS.stream()
      .filter(converter -> converter.isAccepted(fileName))
      .findFirst();
    return first.orElse(null);
  }
}
