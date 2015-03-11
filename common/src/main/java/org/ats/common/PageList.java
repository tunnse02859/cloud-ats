/**
 * 
 */
package org.ats.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public abstract class PageList<T> implements Serializable {

  /** .*/
  private static final long serialVersionUID = 1L;

 /** .*/
  protected int pageSize = 10;
  
  /** .*/
  protected int currentPage;
  
  /** .*/
  protected Map<String, Boolean>  sortableKeys;
  
  public PageList(int pageSize) {
    if (pageSize > 0) this.pageSize = pageSize;
    else throw new IllegalArgumentException("The page size have to be greater than zero.");
    this.currentPage = 0;
  }
  
  public boolean hasNext() {
    return getCurrentPage() < totalPage();
  }
  
  public void setPageSize(int pageSize) {
    if (pageSize < 1) throw new IllegalArgumentException("The page size could not less than one.");
    this.currentPage = 0;
    this.pageSize = pageSize;
  }
  
  public int getPageSize() {
    return this.pageSize;
  }
  
  public int getCurrentPage() {
    return this.currentPage;
  }
  
  public int totalPage() {
    return (int) Math.ceil(count() / (double) pageSize);
  }
  
  public List<T> next() {
    currentPage++;
    int from = (currentPage - 1) * pageSize;
    return get(from);
  }
  
  public List<T> previous() {
    if (currentPage == 0) return null;
    currentPage--;
    int from = (currentPage - 1) * pageSize;
    return get(from);
  }
  
  public List<T> getPage(int pageNumber) {
    if (pageNumber < 1 || pageNumber > totalPage()) return null;
    currentPage = pageNumber;
    int from = (currentPage - 1) * pageSize;
    return get(from);
  }
  
  protected abstract List<T> get(int from);
  
  public abstract long count();

  /**
   * Set up to sort
   * @param keys is set of sortable_field/ascending
   */
  public void setSortable(Map<String, Boolean> keys) {
    this.sortableKeys = keys;
  }
  
}