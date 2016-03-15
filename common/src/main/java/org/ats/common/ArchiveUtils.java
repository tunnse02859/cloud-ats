package org.ats.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class ArchiveUtils {

  private static void extractFile(InputStream in, File outdir, String name) throws IOException {
    byte[] buffer = new byte[4096];
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
        new File(outdir, name)));
    int count = -1;
    while ((count = in.read(buffer)) != -1)
      out.write(buffer, 0, count);
    out.close();
  }

  private static void mkdirs(File outdir, String path) {
    File d = new File(outdir, path);
    if (!d.exists())
      d.mkdirs();
  }

  private static String dirpart(String name) {
    int s = name.lastIndexOf(File.separatorChar);
    return s == -1 ? null : name.substring(0, s);
  }

  /***
   * Extract zipfile to outdir with complete directory structure
   * 
   * @param zipfile
   *          Input .zip file
   * @param outdir
   *          Output directory
   */
  public static void zipDecompress(File zipfile, File outdir) {
    try {
      ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
      ZipEntry entry;
      String name, dir;
      while ((entry = zin.getNextEntry()) != null) {
        name = entry.getName();
        if (entry.isDirectory()) {
          mkdirs(outdir, name);
          continue;
        }
        /*
         * this part is necessary because file entry can come before directory
         * entry where is file located i.e.: /foo/foo.txt /foo/
         */
        dir = dirpart(name);
        if (dir != null)
          mkdirs(outdir, dir);

        extractFile(zin, outdir, name);
      }
      zin.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void gzipDecompress(File gzipFile, File outDir) {
    try {
      GzipCompressorInputStream gzIn = new GzipCompressorInputStream(new FileInputStream(gzipFile));
      TarArchiveInputStream zin = new TarArchiveInputStream(gzIn);
      TarArchiveEntry entry;
      String name, dir;
      while ((entry = (TarArchiveEntry) zin.getNextEntry()) != null) {
        name = entry.getName();
        if (entry.isDirectory()) {
          mkdirs(outDir, name);
          continue;
        }
        /*
         * this part is necessary because file entry can come before directory
         * entry where is file located i.e.: /foo/foo.txt /foo/
         */
        dir = dirpart(name);
        if (dir != null)
          mkdirs(outDir, dir);

        extractFile(zin, outDir, name);
      }
      zin.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void gzipCompress(String sourceFolder, String destOutput) throws IOException {
    gzipCompress(sourceFolder, new FileOutputStream(destOutput));
  }
  
  public static void gzipCompress(String sourceFolder, OutputStream destOutput) throws IOException {
    File fromDir = new File(sourceFolder);
    String lastFolder = sourceFolder.substring(sourceFolder.lastIndexOf('/') + 1);
    
    TarArchiveOutputStream outDir = new TarArchiveOutputStream(new GZIPOutputStream(destOutput));
    outDir.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to get past the 8 gig limit
    outDir.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    
    write(lastFolder, fromDir, outDir);
    outDir.close();
    destOutput.close();
  }
  
  
  private static void write(String lastFolder, File file, TarArchiveOutputStream outDir) throws IOException {
    byte[] buffer = new byte[4096]; // Create a buffer for copying
    int bytes_read;
    
    for (String entry : file.list()) {
      File f = new File(file, entry);
      if (f.isDirectory()) {
        write(lastFolder, f, outDir);
        continue;
      }
      
      FileInputStream fis = new FileInputStream(f);
      
      String path = f.getPath().substring(f.getPath().indexOf(lastFolder));
      TarArchiveEntry zip = new TarArchiveEntry(f, path);
      outDir.putArchiveEntry(zip);
      while ((bytes_read = fis.read(buffer)) != -1) {
        outDir.write(buffer, 0, bytes_read);
      }
      fis.close();
      outDir.closeArchiveEntry();
    }
  }
  
  public static void zipCompress(String sourceFolder, String destOutput) throws IOException {
    zipCompress(sourceFolder, new FileOutputStream(destOutput));
  }
  
  public static void zipCompress(String sourceFolder, OutputStream destOutput) throws IOException {
    File fromDir = new File(sourceFolder);
    String lastFolder = sourceFolder.substring(sourceFolder.lastIndexOf('/') + 1);
    ZipOutputStream outDir = new ZipOutputStream(destOutput);
    write(lastFolder, fromDir, outDir);
    outDir.close();
    destOutput.close();
  }
  
  private static void write(String lastFolder, File file, ZipOutputStream outDir) throws IOException {
    byte[] buffer = new byte[4096]; // Create a buffer for copying
    int bytes_read;
    
    for (String entry : file.list()) {
      File f = new File(file, entry);
      if (f.isDirectory()) {
        write(lastFolder, f, outDir);
        continue;
      }
      FileInputStream fis = new FileInputStream(f);
      
      String path = f.getPath().substring(f.getPath().indexOf(lastFolder));
      ZipEntry zip = new ZipEntry(path);
      outDir.putNextEntry(zip);
      while ((bytes_read = fis.read(buffer)) != -1) {
        outDir.write(buffer, 0, bytes_read);
      }
      fis.close();
    }
  }
}