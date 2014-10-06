/**
 * 
 */
package org.ats.gitlab;

import org.codehaus.jackson.annotate.JsonProperty;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 6, 2014
 */
public class GitlabFile {

  /** .*/
  public static String URL = "/repository/files";
  
  @JsonProperty("file_name")
  private String fileName;
  
  @JsonProperty("file_path")
  private String filePath;
  
  private int size;
  
  private String encoding;
  
  private String content;
  
  private String ref;

  @JsonProperty("blob_id")
  private String blobId;
  
  @JsonProperty("commit_id")
  private String commitId;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public String getBlobId() {
    return blobId;
  }

  public void setBlobId(String blobId) {
    this.blobId = blobId;
  }

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }
  
}
