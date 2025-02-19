package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class ArrayHelper {
  private ArrayHelper() {}

  /**
   * Checks if the given arrays are the same length
   * @param arrays the arrays whose length should be checked
   * @return {true} if length of arrays matches, false otherwise
   */
  public static boolean allHaveEqualLength(String[]... arrays) {
    if(ArrayUtils.isNotEmpty(arrays)) {
      return Arrays.stream(arrays).allMatch(arr -> arr != null && arr.length == arrays[0].length);
    }
    return false;
  }
}
