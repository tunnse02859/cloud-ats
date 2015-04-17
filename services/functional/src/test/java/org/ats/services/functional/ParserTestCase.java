/**
 * 
 */
package org.ats.services.functional;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
public class ParserTestCase {

  @Test
  public void test() throws JsonProcessingException, IOException {
    ObjectMapper m = new ObjectMapper();
    JsonNode rootNode = m.readTree(new File("src/test/resources/full_example.json"));
    JsonNode stepsNode = rootNode.get("steps");
    System.out.println(stepsNode.size());
    for (JsonNode json : stepsNode) {
      System.out.println(json);
    }
  }
}
