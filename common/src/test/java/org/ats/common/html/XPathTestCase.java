/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ats.common.html;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class XPathTestCase extends Assert {
   @Test
   public void testXpath() throws Exception  {
      String html = 
            "<html>" +
      		"   <head>" +
      		"      <title>Hello world</title>" +
      		"   </head>" +
      		"   <body>" +
      		"      <div><p>test</p></div>" +
      		"      <div><p>Hello world</p></div>" +
      		"   </body>" +
      		"</html>" ;
      HtmlParser parser = new HtmlParser();
      Document doc = parser.parseWellForm(html);
      assertTrue(doc.hasChildNodes());
      assertEquals(1, doc.getChildNodes().getLength());
      NodeList nodeList = (NodeList)XPathUtil.read(doc, "html/body[1]/div[1]", XPathConstants.NODESET);
      Node node = nodeList.item(0);
      assertEquals("test", node.getTextContent());
      assertEquals("html/body[1]/div[1]", XPathUtil.getXPath(node));
      
      List<XPathNode> list = XPathUtil.getXPathContentNodes(node);
      assertEquals(1, list.size());
      assertEquals("html/body[1]/div[1]/p[1]", list.get(0).getXpath());
      assertEquals("test", list.get(0).getText());
      
      List<String> holder = new ArrayList<String>();
      XPathUtil.collectXpathContainText(doc, "test", holder);
      assertEquals(2, holder.size());
      assertEquals("html/body[1]/div[1]", holder.get(0));
      assertEquals("html/body[1]/div[1]/p[1]", holder.get(1));
      
      holder.clear();
      XPathUtil.collectXpathContainText(doc, "Hello world", holder);
      assertEquals(3, holder.size());
      assertEquals("html/head[1]/title[1]", holder.get(0));
      assertEquals("html/body[1]/div[2]", holder.get(1));
      assertEquals("html/body[1]/div[2]/p[1]", holder.get(2));
   }
   
}
