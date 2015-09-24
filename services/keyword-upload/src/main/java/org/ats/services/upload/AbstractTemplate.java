/**
 * 
 */
package org.ats.services.upload;

import java.io.IOException;

import com.mongodb.BasicDBObject;

/**
 * @author NamBV2
 *
 * Sep 18, 2015
 */
@SuppressWarnings("serial")
public abstract class AbstractTemplate extends BasicDBObject {

  public abstract String transform() throws IOException;
}
