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

package com.mallowigi.imageicon.core;

import com.intellij.util.ArrayUtilRt;

public final class Base64 {
  private Base64() {
  }

  public static String encode(final byte[] bytes) {
    return Base64.encode(bytes, 0, bytes.length);
  }

  public static String encode(final byte[] bytes, final int offset, final int length) {
    if (length == 0) {
      return "";
    }

    final StringBuilder builder = new StringBuilder();
    for (int i = offset; i < length; i += 3) {
      builder.append(Base64.encodeBlock(bytes, i));
    }
    return builder.toString();
  }

  private static char[] encodeBlock(final byte[] bytes, final int offset) {
    int j = 0;
    final int s = bytes.length - offset - 1;
    final int l = Math.min(s, 2);
    for (int i = 0; i <= l; i++) {
      final byte b = bytes[offset + i];
      final int n = b >= 0 ? ((int) (b)) : b + 256;
      j += n << 8 * (2 - i);
    }
    final char[] ac = new char[4];
    for (int k = 0; k < 4; k++) {
      final int l1 = j >>> 6 * (3 - k) & 0x3f;
      ac[k] = Base64.getChar(l1);
    }
    if (s < 1) {
      ac[2] = '=';
    }
    if (s < 2) {
      ac[3] = '=';
    }
    return ac;
  }

  private static char getChar(final int i) {
    if (i >= 0 && i <= 25) {
      return (char) (65 + i);
    }
    if (i >= 26 && i <= 51) {
      return (char) (97 + (i - 26));
    }
    if (i >= 52 && i <= 61) {
      return (char) (48 + (i - 52));
    }
    if (i == 62) {
      return '+';
    }
    return i != 63 ? '?' : '/';
  }

  public static byte[] decode(final String s) {
    if (s.length() == 0) {
      return ArrayUtilRt.EMPTY_BYTE_ARRAY;
    }

    int i = 0;
    for (int j = s.length() - 1; j > 0 && s.charAt(j) == '='; j--) {
      i++;
    }

    final int len = (s.length() * 6) / 8 - i;
    final byte[] raw = new byte[len];
    int l = 0;
    for (int i1 = 0; i1 < s.length(); i1 += 4) {
      final int n = s.length() - i1;
      if (n == 1) {
        throw new IllegalArgumentException("Invalid Base64 string");
      }
      final int j1 = (Base64.getValue(s.charAt(i1)) << 18) +
        (Base64.getValue(s.charAt(i1 + 1)) << 12) +
        (n > 2 ? (Base64.getValue(s.charAt(i1 + 2)) << 6) : 0) +
        (n > 3 ? (Base64.getValue(s.charAt(i1 + 3))) : 0);
      for (int k = 0; k < 3 && l + k < raw.length; k++) {
        raw[l + k] = (byte) (j1 >> 8 * (2 - k) & 0xff);
      }
      l += 3;
    }
    return raw;
  }

  private static int getValue(final char c) {
    if (c >= 'A' && c <= 'Z') {
      return c - 65;
    }
    if (c >= 'a' && c <= 'z') {
      return (c - 97) + 26;
    }
    if (c >= '0' && c <= '9') {
      return (c - 48) + 52;
    }
    if (c == '+') {
      return 62;
    }
    if (c == '/') {
      return 63;
    }
    return c != '=' ? -1 : 0;
  }
}
