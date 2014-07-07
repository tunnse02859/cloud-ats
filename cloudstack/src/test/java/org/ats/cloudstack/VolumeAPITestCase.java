/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.ats.cloudstack.model.Volume;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class VolumeAPITestCase {

  @Test
  public void listVolumes() throws Exception {
    List<Volume> list = VolumeAPI.listVolumes(null, null, "DATADISK", null, null);
    System.out.println(list.size());
    List<Volume> deattached = VolumeAPI.listVolumesNotAttached(null, null, "DATADISK", null);
    System.out.println(deattached.size());
  }
  
  @Test
  public void clearNotAttachedVolumes() throws IOException {
    Assert.assertTrue(VolumeAPI.clearNotAttachedVolumes());
  }
}
