/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 *         Apr 10, 2015
 */
@SuppressWarnings("serial")
public class DoubleClickElement extends AbstractAction {

	private AbstractLocator locator;

	public DoubleClickElement(AbstractLocator locator) {
		this.locator = locator;
	}

	public String transform() throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append("try { \n");
		sb.append("     ew Actions(wd).doubleClick(wd.findElement(@locator)).build().perform();\n");
		sb.append("     System.out.println(\"[End][Step]\"); \n");
		sb.append("   } catch (Exception e) { \n");
		sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
		sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_doubleClickElement.png\"));\n");
		sb.append("     throw e ; \n");
		sb.append("   }\n");

		return Rythm.render(sb.toString(), locator.transform());
	}

	public String getAction() {
		return "doubleClickElement";
	}

}
