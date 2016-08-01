package tr.com.srdc.xmltransform.TextContextRestriction;

import org.w3c.dom.Node;

/**
 * Created by arda on 01.08.2016.
 * Generic interface for comparator functions to decide enumerations.
 */

public interface TextContextDecider {
    /**
     * @param sourceContext   is the text context of the source node.
     * @param toBeTransformed is the node to be text context changed.
     * @param mode            is the working mode of the function
     * @return the value that is to be put in the Node toBeTransformed
     */
    String decideTextContext( String sourceContext, Node toBeTransformed, TextContextComparatorModes mode );
}
