package com.capitalone.creditocr.util;

import org.springframework.util.DigestUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to compute a 128-bit simhash digest for a String.
 */
public class Simhash {

    /**
     * Compute a 128-bit simhash digest of a string.
     * @param str The string
     * @return The hash digest
     */
    public static byte[] hash(String str) {
        // MD5 has 16 bytes = 128 bits. The n'th dimension in the vector represents the n'th bit.
        int[] weightVector = new int[128];

        // Use 3-grams as tokens so that some context is maintained between individual words.
        // While not strictly necessary, it does improve accuracy.
        String[] words = ngram(str, 3);

        for (String word : words) {
            byte[] hash = DigestUtils.md5Digest(word.getBytes());

            // get bits of every byte of the hash and add them to the weight Vector
            for (int j = 0; j < hash.length; j++) {
                for (int k = 0; k < 8; k++) {
                    if ((hash[j] >> (7 - k) & 0x01) == 1) {
                        weightVector[(j * 8) + k] += 1;
                    } else {
                        weightVector[(j * 8) + k] -= 1;
                    }
                }
            }
        }

        byte[] result = new byte[16];
        /*
         * Convert weightVector to hash number by setting every bit >0 to 1
         * and all the others to 0
         */
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < 8; j++) {
                if (weightVector[(i << 3) + j] > 0) {
                    result[i] |= 1 << (7 - j);
                }
            }
        }

        return result;
//        StringBuilder out = new StringBuilder(128);
//        for (int i : weightVector) {
//            if (i > 0) {
//                out.append('1');
//            } else {
//                out.append('0');
//            }
//        }
//        return new BigInteger(out.toString(), 2);
    }

    private static String[] ngram(String str, int n) {
        List<String> ngrams = new ArrayList<>();
        String[] words = str.split(" ");

        for (int i = 0; i < words.length - n + 1; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = 0; j < n; j++) {
                ngram.append(' ');
                ngram.append(words[i + j]);
            }
            ngrams.add(ngram.toString().trim());
        }

        return ngrams.toArray(new String[] {});
    }

    /**
     * Extract the hash into longs. Return length will always be 2
     */
    public static long[] splitHash(byte[] hash) {
        if (hash == null || hash.length != 16) {
            return new long[]{0L, 0L};
        }

        ByteBuffer buffer = ByteBuffer.wrap( hash );
        long[] ret = new long[2];
        ret[0] = buffer.getLong();
        ret[1] = buffer.getLong();


        return ret;
    }


    public static byte[] unsplitHash(long[] split) {
        ByteBuffer buffer = ByteBuffer.allocate( 16 );
        buffer.putLong( split[0] );
        buffer.putLong( split[1] );

        byte[] retBuf = new byte[16];
        System.arraycopy( buffer.array(), 0, retBuf, 0, retBuf.length );
        return retBuf;
    }

}
