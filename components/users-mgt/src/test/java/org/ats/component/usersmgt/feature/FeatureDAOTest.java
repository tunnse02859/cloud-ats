/**
 * 
 */
package org.ats.component.usersmgt.feature;

import junit.framework.Assert;

import org.ats.component.usersmgt.DataFactory;
import org.ats.component.usersmgt.EventExecutor;
import org.ats.component.usersmgt.UserManagementException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 16, 2014
 */
public class FeatureDAOTest {

  @Before
  public void setUp() {
    DataFactory.getDatabase("cloud-ats");
  }
  
  @Test
  public void testCreateOperation() throws UserManagementException {
    Operation op1 = new Operation("create");
    OperationDAO.INSANCE.create(op1);
    
    Operation op2 = OperationDAO.INSANCE.findOne(op1.getId());
    Assert.assertEquals(op1, op2);
  }
  
  @Test
  public void testUpdateOperation() throws UserManagementException {
    Operation op1 = new Operation("create");
    OperationDAO.INSANCE.create(op1);
    
    op1.setName("create-update");
    OperationDAO.INSANCE.update(op1);
    
    Operation op2 = OperationDAO.INSANCE.findOne(op1.getId());
    Assert.assertEquals(op1, op2);
  }
  
  @Test
  public void testFeature() throws UserManagementException {
    Operation op1 = new Operation("create");
    OperationDAO.INSANCE.create(op1);
    
    Operation op2 = new Operation("delete");
    OperationDAO.INSANCE.create(op2);
    
    Feature f1 = new Feature("f1");
    f1.addOperation(op1);
    FeatureDAO.INSTANCE.create(f1);
    
    Feature f2 = FeatureDAO.INSTANCE.findOne(f1.getId());
    Assert.assertEquals(f1, f2);
    Assert.assertEquals(f1.getOperations().iterator().next(), op1);
    
    f1.addOperation(op2);
    FeatureDAO.INSTANCE.update(f1);
    
    f2 = FeatureDAO.INSTANCE.findOne(f1.getId());
    Assert.assertEquals(f1, f2);
    Assert.assertEquals(2, f1.getOperations().size());
    Assert.assertTrue(f1.getOperations().contains(op1));
    Assert.assertTrue(f1.getOperations().contains(op2));
  }
  
  @Test
  public void testDropOperation() throws UserManagementException, InterruptedException{
    EventExecutor.INSTANCE.start();
    
    Operation op1 = new Operation("create");
    OperationDAO.INSANCE.create(op1);
    
    Operation op2 = new Operation("delete");
    OperationDAO.INSANCE.create(op2);
    
    Feature f1 = new Feature("f1");
    f1.addOperation(op1);
    f1.addOperation(op2);
    FeatureDAO.INSTANCE.create(f1);
    
    Feature f2 = FeatureDAO.INSTANCE.findOne(f1.getId());
    Assert.assertEquals(f1, f2);
    Assert.assertEquals(2, f2.getOperations().size());
    
    OperationDAO.INSANCE.delete(op2);
    
    //wait until process whole events
    while (EventExecutor.INSTANCE.isInProgress()) {
    }
    
    f2 = FeatureDAO.INSTANCE.findOne(f1.getId());
    
    Assert.assertEquals(1, f2.getOperations().size());
    Assert.assertEquals(op1, f2.getOperations().iterator().next());
    
    OperationDAO.INSANCE.delete(op1);
    
    //wait until process whole events
    while (EventExecutor.INSTANCE.isInProgress()) {
    }
    
    f2 = FeatureDAO.INSTANCE.findOne(f1.getId());
    
    Assert.assertTrue(f2.getOperations().isEmpty());
    
    Assert.assertNull(OperationDAO.INSANCE.findOne(op1.getId()));
    Assert.assertNull(OperationDAO.INSANCE.findOne(op2.getId()));
    
    long eventCount = DataFactory.getDatabase("cloud-ats").getCollection("event").count();
    Assert.assertEquals(0, eventCount);
    
    EventExecutor.INSTANCE.stop();
  }
  
  @After
  public void tearDown() {
    DataFactory.dropDatabase("cloud-ats");
  }
}
