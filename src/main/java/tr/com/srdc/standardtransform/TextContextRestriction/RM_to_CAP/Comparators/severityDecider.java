package tr.com.srdc.standardtransform.TextContextRestriction.RM_to_CAP.Comparators;

import org.w3c.dom.Node;
import tr.com.srdc.standardtransform.TextContextRestriction.TextContextComparatorModes;
import tr.com.srdc.standardtransform.TextContextRestriction.TextContextDecider;

/**
 * Created by arda on 01.08.2016.
 * Comparator class for severity element on Common Alerting Protocol.
 */
public class severityDecider implements TextContextDecider {
    /**
     * @param sourceContext   is the text context of the source node.
     * @param toBeTransformed is the node to be text context changed.
     * @param mode            mode is the working mode of the function
     * @return the value that is to be put in the Node toBeTransformed
     */
    public String decideTextContext( String sourceContext, Node toBeTransformed, TextContextComparatorModes mode ) {
        if ( mode == TextContextComparatorModes.OnDeleteIfNoInfo ) {
            return "Unknown";
        }
        else {
            return "Minor";
        }
    }
}
