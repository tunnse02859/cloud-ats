/**
 * 
 */
package org.ats.services.keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import org.ats.common.StringUtil;
import org.ats.services.datadriven.DataDriven;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.keyword.action.AbstractAction;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Case extends AbstractTemplate {
  
  private List<JsonNode> actions = new ArrayList<JsonNode>();
  
  @Inject
  private ActionFactory actionFactory;
  
  @Inject
  private ReferenceFactory<DataDrivenReference> drivenRefFactory;
  
  @Inject
  private Case(@Assisted("projectId") String projectId, @Assisted("name") String name, @Nullable @Assisted("dataDriven") DataDrivenReference ref) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("project_id", projectId);
    this.put("name", name);
    this.put("data_driven", ref != null ? ref.toJSon() : null);
    this.put("created_date", new Date());
    this.put("steps",  new BasicDBList());
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getProjectId() {
    return this.getString("project_id");
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public Date getCreatedDate() {
    return this.getDate("created_date");
  }
  
  public void setDataDriven(DataDrivenReference driven) {
    this.put("data_driven", driven != null ? driven.toJSon() : null);
  }
  
  public DataDrivenReference getDataDriven() {
    Object obj = this.get("data_driven");
    if (obj == null) return null;
    return drivenRefFactory.create(((BasicDBObject) obj).getString("_id"));
  }
  
  public Case addAction(JsonNode... actions) {
    for (JsonNode action : actions) {
      if (action == null) continue;
      this.actions.add(action);
    }
    
    BasicDBList list = new BasicDBList();
    for (JsonNode action : this.actions) {
      list.add(JSON.parse(action.toString()));
    }
    this.put("steps",  list);
    return this;
  }
  
  public void clearActions() {
    this.remove("steps");
    this.actions.clear();
  }
  
  public List<JsonNode> getActions() {
    return Collections.unmodifiableList(actions);
  }
   
  public String transform() throws IOException {
    return transform(0,false,0);
  }
  
  public String transform(int valueDelay, boolean sequenceMode, int order) throws IOException {
	String str = "";
	List<String> listParams = new ArrayList<String>();
	StringBuilder sb = new StringBuilder();
    boolean isUseDataProvider = getDataDriven() != null;
    int valueDelayTransform = valueDelay*1000;
    
    String delayTime = "";
    ObjectMapper mapper = new ObjectMapper(); 
    JsonNode nodePause = mapper.readTree("{\"type\":\"pause\",\"waitTime\":\""+valueDelayTransform+"\"}");
    AbstractAction actionPause = actionFactory.createAction(nodePause);
    delayTime = "[INFO] Waiting "+ valueDelay + "(s) ";
    
    if(isUseDataProvider) {
      
      DataDriven dataDriven = getDataDriven().get();
      
      ObjectMapper obj = new ObjectMapper();
      JsonNode rootNode;
      rootNode = obj.readTree(dataDriven.getDataSource());

      JsonNode data = rootNode.get(0);
      Iterator<Entry<String, JsonNode>> nodeIterator = data.fields();
      
      sb.append(getDataDriven().get().transform(getId()));
      sb.append("\n");
      if(sequenceMode) {
        sb.append("  @Test (dataProvider = \"").append(StringUtil.normalizeName(dataDriven.getName())).append(getId().substring(0, 8)).append("\", priority = ").append(order).append(")\n");
      } else {
        sb.append("  @Test (dataProvider = \"").append(StringUtil.normalizeName(dataDriven.getName())).append(getId().substring(0, 8)).append("\")\n");
      }
      sb.append("  public void ").append(StringUtil.normalizeName(getName())).append(getId().substring(0, 8)).append("(JsonNode data) throws Exception {\n");
      sb.append("    System.out.println(\"[Start][Case]{\\\"name\\\": \\\""+this.getName()+"\\\", \\\"id\\\": \\\""+this.getId()+"\\\", \\\"timestamp\\\": \\\"\"+System.currentTimeMillis()+\"\\\"} \"); \n");
      sb.append("    System.out.println(\"[Start][Data]\"+data.toString()); \n");
      
      while (nodeIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
        String key = entry.getKey();
        sb.append("    Object data_").append(key).append(" = data.get(\"").append(key).append("\");\n");
        sb.append("    String ").append(key).append(" = null;\n");
        sb.append("    if (data_").append(key).append(" != null) {\n");
        sb.append("        ").append(key).append(" = ").append("data_").append(key).append(".toString();\n");
        sb.append("        ").append(key).append(" = ").append(key).append(".substring(1, ").append(key).append(".length() - 1).replace(\"\\\\\\\"\",\"\\\"\");\n");
        sb.append("    }\n");
        sb.append("\n");
     }
      
    } else {
      sb.append("\n");
      if(sequenceMode) {
        sb.append("  @Test(priority = ").append(order).append(")\n");
      } else {
        sb.append("  @Test\n");
      }
      sb.append("  public void ").append(StringUtil.normalizeName(getName())).append(getId().substring(0, 8)).append("() throws Exception {\n");
      sb.append("    System.out.println(\"[Start][Case]{\\\"name\\\": \\\""+this.getName()+"\\\", \\\"id\\\": \\\""+this.getId()+"\\\", \\\"timestamp\\\": \\\"\"+System.currentTimeMillis()+\"\\\"} \"); \n");
    }
    
    for (JsonNode json : actions) {
      AbstractAction action = actionFactory.createAction(json);
      
      if (action == null) continue;
      
      //If show action is true, show action in log
        
        String type = "\\\"keyword_type\\\":\\\""+json.get("type").asText()+" \\\",";
        String locator = "";
        String text = "";
        String url = "";
        String targetLocator = "";
        String variable = "";
        String source = "";
        String title = "";
        String attributeName = "";
        String propertyName = "";
        String name = "";
        String options = "";
        String file = "";
        String identifier = "";
        String index = "";
        String script = "";
        String value = "";
        String temp = "";
        String waitTime = "";
        String locationType = "";
        JsonNode locatorNode = null;
        
        if (json.get("locator") != null) {
          locatorNode = json.get("locator");
          locationType = locatorNode.get("type").toString().split("\"")[1];
          temp = locatorNode.get("value").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          locator = "\\\"locator\\\":{\\\"type\\\":\\\""+locationType+"\\\",\\\"value\\\":\\\"" +temp.replace("\"", "\\\\\"")+"\\\"},";
          listParams.add("\\\"locator\\\"");
        }
        
        if (json.get("text") != null) {
          temp =  json.get("text").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          text = "\\\"text\\\":"+ "\\\""+temp+"\\\",";
          listParams.add("\\\"text\\\"");
        }
        
        if (json.get("waitTime") != null) {
        	waitTime = "\\\"waittime\\\":"+"\\\""+json.get("waitTime").asLong()+"ms\\\",";
        	listParams.add("\\\"waittime\\\"");
        }
        
        if (json.get("url") != null) {
          temp = json.get("url").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          url = "\\\"url\\\":"+ "\\\""+temp+"\\\",";
          listParams.add("\\\"url\\\"");
        }
        
        if (json.get("targetLocator") != null) {
          locatorNode = json.get("targetLocator");
          locationType = json.get("type").toString().split("\"")[1];
          temp = locatorNode.get("value").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          targetLocator = "\\\"targetLocator\\\":{\\\"type\\\":\\\""+locationType+"\\\",\\\"value\\\":\\\"" +temp.replace("\"", "\\\\\"")+"\\\"},";
          listParams.add("\\\"targetLocator\\\"");
        }
        
        if (json.get("variable") != null) {
          temp = json.get("variable").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          variable = "\\\"variable\\\":"+ "\\\""+temp+"\\\",";
          listParams.add("\\\"variable\\\"");
        }
        
        if (json.get("source") != null) {
          temp = json.get("source").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          source = "\\\"source\\\":"+"\\\""+temp+"\\\",";
          listParams.add("\\\"source\\\"");
        }
        
        if (json.get("title") != null) {
          temp = json.get("title").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          title = "\\\"title\\\":"+"\\\""+temp+"\\\",";
          listParams.add("\\\"title\\\"");
        }
        
        if (json.get("attributeName") != null) {
          temp = json.get("attributeName").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          attributeName = "\\\"attribute_name\\\":"+ "\\\""+temp+"\\\",";
          listParams.add("\\\"attribute_name\\\"");
        }
        
        if (json.get("propertyName") != null) {
          temp = json.get("propertyName").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          propertyName = "\\\"property_name\\\":" + "\\\""+temp+"\\\",";
          listParams.add("\\\"property_name\\\"");
        }
        
        if (json.get("options") != null) {
          temp = json.get("options").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          options = "\\\"options\\\":"+ "\\\""+temp+"\\\",";
          listParams.add("\\\"options\\\"");
        }
        
        if (json.get("name") != null) {
          temp = json.get("name").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          name = "\\\"name\\\":"+"\\\""+temp+"\\\",";
          listParams.add("\\\"name\\\"");
        }
        
        if (json.get("file") != null) {
          temp = json.get("file").asText();
          file = "\\\"file\\\":"+"\\\""+temp+"\\\",";
          listParams.add("\\\"file\\\"");
        }
        
        if (json.get("identifier") != null) {
          temp = json.get("identifier").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          identifier = "\\\"identifier\\\":"+"\\\""+temp+"\\\",";
          listParams.add("\\\"identifier\\\"");
        }
        
        if (json.get("index") != null) {
        	index = "\\\"index\\\":"+"\\\""+json.get("index").asInt()+"\\\",";
        	listParams.add("\\\"index\\\"");
        }
        
        if (json.get("script") != null) {
          temp = json.get("script").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          script = "\\\"script\\\":"+"\\\""+temp+"\\\",";
          listParams.add("\\\"script\\\"");
        }
        
        if(json.get("value") != null) {
          temp = json.get("value").asText();
          if (temp.indexOf("\"") != -1) temp = temp.replace("\"", "\\\"");
          value = "\\\"value\\\":"+"\\\""+temp+"\\\",";
          listParams.add("\\\"value\\\"");
        }
        
        str = new StringBuilder() 
        .append(type)
        .append(locator)
        .append(targetLocator)
        .append(url)
        .append(text)
        .append(script)
        .append(name)
        .append(source)
        .append(title)
        .append(options)
        .append(propertyName)
        .append(attributeName)
        .append(variable)
        .append(value)
        .append(file)
        .append(waitTime)
        .append(identifier)
        .append(index).toString() ;
        sb.append("\n");
        sb.append("    System.out.println(\"[Start][Step]{");
        sb.append(str);
        sb.append("\\\"timestamp\\\": \\\"\"+System.currentTimeMillis()+\"\\\",");
        sb.append("\\\"params\\\":"+listParams.toString()+"} \"); \n");
        
        sb.append("    ");
        sb.append(action.transform());
        sb.append("\n");
        if(valueDelayTransform != 0) {
          sb.append("    System.out.println(\"");
          sb.append(delayTime);
          sb.append("\");\n");
          sb.append(actionPause.transform());
        }
        
        listParams.clear();
      
    }
    if(isUseDataProvider) {
    	sb.append("System.out.println(\"[End][Data]\"); \n");
    }
    sb.append("    System.out.println(\"[End][Case]\"); \n");
    sb.append("  }");
    return sb.toString();
  }
}
