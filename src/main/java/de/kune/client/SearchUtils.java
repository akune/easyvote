package de.kune.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

/**
 * 
 * @author rhanson
 */
public class SearchUtils
{

    /**
     * Returns a List of Element objects that have the specified CSS class name.
     * 
     * @param element Element to start search from
     * @param className name of class to find
     * @return
     */
    public static List<Element> findElementsForClass (Element element, String className)
    {
        ArrayList<Element> result = new ArrayList<Element>();
        recElementsForClass(result, element, className);
        return result;
    }

    private static void recElementsForClass (ArrayList<Element> res, Element element, String className)
    {
        String c;
        
        if (element == null) {
            return;
        }

        c = element.getPropertyString("className");
        
        if (c != null) {
            String[] p = c.split(" ");
            
            for (int x = 0; x < p.length; x++) {
                if (p[x].equals(className)) {
                    res.add(element);
                }
            }
        }
        
        for (int i = 0; i < element.getChildCount(); i++) {
            Element child = (Element)element.getChild(i);
            recElementsForClass(res, child, className);
        }
    }
    
}
