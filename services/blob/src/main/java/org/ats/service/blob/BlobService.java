/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.service.blob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * @author TrinhTV3
 *
 */

@Singleton
public class BlobService {
  
  /** .*/
  private final GridFS fs;
  
  @Inject
  BlobService(MongoDBService mongo, Logger logger) {
    this.fs = new GridFS(mongo.getDatabase());
  }
  
  public GridFSInputFile create(File file) throws IOException {
    return fs.createFile(file);
  }
  
  public void save(GridFSInputFile fileInput) {
    fileInput.save();
  }
  
  public List<GridFSDBFile> find(BasicDBObject obj) {
    return fs.find(obj);
  }
  
  public void delete(String fileName) {
    fs.remove(fileName);
  }
  
  public void delete(DBObject obj) {
    fs.remove(obj);
  }
  
  public GridFSDBFile findOne(BasicDBObject obj) {
    return fs.findOne(obj);
  }

  public void deleteById(String csvId) {
    DBObject obj = new BasicDBObject("_id", csvId);
    fs.remove(obj);
  }
  
  public List<GridFSDBFile> query(DBObject obj) {
    return fs.find(obj);
  }
  
  public GridFSInputFile create(InputStream in) {
    return fs.createFile(in);
  }
  
  public GridFSInputFile create(byte[] binary) {
    return fs.createFile(binary);
  }
}
