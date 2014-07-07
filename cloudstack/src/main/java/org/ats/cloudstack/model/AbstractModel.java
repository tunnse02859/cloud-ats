/**
 * 
 */
package org.ats.cloudstack.model;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public abstract class AbstractModel {

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, this);
      return writer.toString();
    } catch (IOException e) {
      e.printStackTrace();
      return super.toString();
    }
  }
}
