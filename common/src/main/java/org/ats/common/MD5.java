/**
 * 
 */
package org.ats.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 19, 2015
 */
public class MD5 {

  /** .*/
  public final static int DATA_LENGTH = 16;

  /** .*/
  private static final MessageDigest DIGESTER;

  /** .*/
  private byte[] digest;

  static {
    try {
      DIGESTER = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
  
  public MD5() { 
    this.digest = new byte[DATA_LENGTH]; 
  }

  public MD5(String hex) { 
    setDigest(hex); 
  }

  /** Constructs an MD5Hash with a specified value. */
  public MD5(byte[] digest) {
    if (digest.length != DATA_LENGTH) {
      throw new IllegalArgumentException("Wrong length: " + digest.length);
    }
    this.digest = digest;
  }

  public void set(MD5 that) {
    System.arraycopy(that.digest, 0, this.digest, 0, DATA_LENGTH);
  }

  public byte[] getDigest() { 
    return digest; 
  }
  
  public void setDigest(byte[] digest) { 
    this.digest = digest ; 
  }

  public int getMaxLength() { 
    return DATA_LENGTH ; 
  }

  /**
   * Return a 32-bit digest of the MD5.
   * @return the first 4 bytes of the md5
   */
  public int quarterDigest() {
    int value = 0;
    for (int i = 0; i < 4; i++)
      value |= ((digest[i] & 0xff) << (8*(3-i)));
    return value;    
  }

  /** Returns true iff <code>o</code> is an MD5Hash whose digest contains the same values.  */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MD5)) return false;
    MD5 other = (MD5)o;
    return Arrays.equals(this.digest, other.digest);
  }

  /** Returns a hash code value for this object.
   * Only uses the first 4 bytes, since md5s are evenly distributed.
   */
  @Override
  public int hashCode() { 
    return quarterDigest(); 
  }

  private static final char[] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
  
  /** Returns a string representation of this object. */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(DATA_LENGTH*2);
    for (int i = 0; i < DATA_LENGTH; i++) {
      int b = digest[i];
      buf.append(HEX_DIGITS[(b >> 4) & 0xf]);
      buf.append(HEX_DIGITS[b & 0xf]);
    }
    return buf.toString();
  }

  /** Sets the digest value from a hex string. */
  public void setDigest(String hex) {
    if (hex.length() != DATA_LENGTH*2)  throw new IllegalArgumentException("Wrong length: " + hex.length());
    byte[] digest = new byte[DATA_LENGTH];
    for (int i = 0; i < DATA_LENGTH; i++) {
      int j = i << 1;
      digest[i] = (byte)(charToNibble(hex.charAt(j)) << 4 | charToNibble(hex.charAt(j+1)));
    }
    this.digest = digest;
  }

  private static final int charToNibble(char c) {
    if (c >= '0' && c <= '9') {
      return c - '0';
    } else if (c >= 'a' && c <= 'f') {
      return 0xa + (c - 'a');
    } else if (c >= 'A' && c <= 'F') {
      return 0xA + (c - 'A');
    } else {
      throw new RuntimeException("Not a hex character: " + c);
    }
  }

  /** Construct a hash value for a byte array. */
  public static MD5 digest(String text) {
    byte[] digest;
    synchronized (DIGESTER) {
      byte[] data = text.getBytes() ; 
      DIGESTER.update(data, 0, data.length);
      digest = DIGESTER.digest();
    }
    return new MD5(digest);
  }

  /** Construct a hash value for a byte array. */
  public static byte[] hash(String text) {
    byte[] digest;
    synchronized (DIGESTER) {
      byte[] data = text.getBytes() ; 
      DIGESTER.update(data, 0, data.length);
      digest = DIGESTER.digest();
    }
    return digest;
  }


  /** Lexicographic order of binary data. */
  public static int compareBytes(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
    int end1 = s1 + l1;
    int end2 = s2 + l2;
    for (int i = s1, j = s2; i < end1 && j < end2; i++, j++) {
      int a = (b1[i] & 0xff);
      int b = (b2[j] & 0xff);
      if (a != b) {
        return a - b;
      }
    }
    return l1 - l2;
  }
}
