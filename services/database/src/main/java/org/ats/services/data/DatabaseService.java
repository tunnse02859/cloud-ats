/**
 * 
 */
package org.ats.services.data;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public interface DatabaseService<D> {

  public D getDatabase();
  
  public String getDatabaseName();
  
  public void dropDatabase();
  
  public String getHost();
  
  public int getPort();
  
  public String getEngine();
}
