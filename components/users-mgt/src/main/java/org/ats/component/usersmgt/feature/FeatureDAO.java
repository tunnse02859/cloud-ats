/**
 * 
 */
package org.ats.component.usersmgt.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class FeatureDAO extends ManagementDAO<Feature> {

  private FeatureDAO(String dbName) {
    super(dbName, "feature");
  }

  public static FeatureDAO getInstance(String dbName) {
    return new FeatureDAO(dbName);
  }

  public List<Operation> getOperations(Feature feature) {

    if (feature.get("operation_ids") == null) {
      return Collections.emptyList();
    }

    String[] op_ids = feature.getString("operation_ids").split("::");
    Set<Operation> operations = new HashSet<Operation>();

    try {
      for (String op_id : op_ids) {
        Operation operation = OperationDAO.getInstance(feature.getDbName()).findOne(op_id);
        operations.add(operation);
      }
    } catch (UserManagementException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    List<Operation> list = new ArrayList<Operation>(operations);
    Collections.sort(list, new Comparator<Operation>() {
      public int compare(Operation o1, Operation o2) {
        return o1.getString("name").compareTo(o2.getString("name"));
      }
    });
    return list;
  }

  @Override
  public Feature transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Feature().from(obj);
  }

}
