/**
 * 
 */
package org.ats.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 4, 2016
 */
public class ArchiveUtilsTestCase {

  @Test
  public void testZipCompressAndDecomress() throws IOException {
    ArchiveUtils.zipCompress("src/test/resources/data", "target/data.zip");
    ArchiveUtils.zipDecompress(new File("target/data.zip"), new File("target/data-zip-out"));
    
    String fileSource1 = StringUtil.readStream(new FileInputStream("src/test/resources/data/file1.txt"));
    String fileDest1 = StringUtil.readStream(new FileInputStream("target/data-zip-out/data/file1.txt"));
    Assert.assertEquals(fileSource1, fileDest1);
        
    String fileSource2 = StringUtil.readStream(new FileInputStream("src/test/resources/data/file2.txt"));
    String fileDest2 = StringUtil.readStream(new FileInputStream("target/data-zip-out/data/file2.txt"));
    Assert.assertEquals(fileSource2, fileDest2);
    
    String fileSource3 = StringUtil.readStream(new FileInputStream("src/test/resources/data/file3.txt"));
    String fileDest3 = StringUtil.readStream(new FileInputStream("target/data-zip-out/data/file3.txt"));
    Assert.assertEquals(fileSource3, fileDest3);
    
    String subFileSource1 = StringUtil.readStream(new FileInputStream("src/test/resources/data/sub/subfile1.txt"));
    String subFileDest1 = StringUtil.readStream(new FileInputStream("target/data-zip-out/data/sub/subfile1.txt"));
    Assert.assertEquals(subFileSource1, subFileDest1);
        
    String subFileSource2 = StringUtil.readStream(new FileInputStream("src/test/resources/data/sub/subfile2.txt"));
    String subFileDest2 = StringUtil.readStream(new FileInputStream("target/data-zip-out/data/sub/subfile2.txt"));
    Assert.assertEquals(subFileSource2, subFileDest2);
  }
  
  @Test
  public void testGzipCompress()throws IOException {
    ArchiveUtils.gzipCompress("src/test/resources/data", "target/data.tar.gz");
    ArchiveUtils.gzipDecompress(new File("target/data.tar.gz"), new File("target/data-gzip-out"));
    
    String fileSource1 = StringUtil.readStream(new FileInputStream("src/test/resources/data/file1.txt"));
    String fileDest1 = StringUtil.readStream(new FileInputStream("target/data-gzip-out/data/file1.txt"));
    Assert.assertEquals(fileSource1, fileDest1);
        
    String fileSource2 = StringUtil.readStream(new FileInputStream("src/test/resources/data/file2.txt"));
    String fileDest2 = StringUtil.readStream(new FileInputStream("target/data-gzip-out/data/file2.txt"));
    Assert.assertEquals(fileSource2, fileDest2);
    
    String fileSource3 = StringUtil.readStream(new FileInputStream("src/test/resources/data/file3.txt"));
    String fileDest3 = StringUtil.readStream(new FileInputStream("target/data-gzip-out/data/file3.txt"));
    Assert.assertEquals(fileSource3, fileDest3);
    
    String subFileSource1 = StringUtil.readStream(new FileInputStream("src/test/resources/data/sub/subfile1.txt"));
    String subFileDest1 = StringUtil.readStream(new FileInputStream("target/data-gzip-out/data/sub/subfile1.txt"));
    Assert.assertEquals(subFileSource1, subFileDest1);
        
    String subFileSource2 = StringUtil.readStream(new FileInputStream("src/test/resources/data/sub/subfile2.txt"));
    String subFileDest2 = StringUtil.readStream(new FileInputStream("target/data-gzip-out/data/sub/subfile2.txt"));
    Assert.assertEquals(subFileSource2, subFileDest2);
  }
}
