/**
 * 
 */
package org.ats.cloudstack;

import java.util.List;

import junit.framework.Assert;

import org.ats.cloudstack.model.Zone;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class ZoneAPITestCase {

  @Test
  public void listAvailableZone() throws Exception {
    List<Zone> zones = ZoneAPI.listAvailableZones();
    Assert.assertEquals(1, zones.size());
    Zone zone = zones.get(0);
    Assert.assertEquals("myzone", zone.name);
  }
}
