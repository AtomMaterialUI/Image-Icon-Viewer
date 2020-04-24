package com.mallowigi.imageicon;

import com.google.common.collect.Sets;
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
      new SVGImageConverter()
    )
  );

  static ImageToIconConverter create(final String fileName) {
    final Optional<ImageToIconConverter> first = CONVERTERS.stream()
      .filter(converter -> converter.isAccepted(fileName))
      .findFirst();
    return first.orElse(null);
  }
}
