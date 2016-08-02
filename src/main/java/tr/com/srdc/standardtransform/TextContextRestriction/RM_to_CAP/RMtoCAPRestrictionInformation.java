package tr.com.srdc.standardtransform.TextContextRestriction.RM_to_CAP;

import tr.com.srdc.standardtransform.TextContextRestriction.RM_to_CAP.Comparators.*;
import tr.com.srdc.standardtransform.TextContextRestriction.RestrictionInformation;

/**
 * Created by arda on 01.08.2016.
 * Comparator functions for Resource Messaging to Common Alerting Protocol Standard
 */

public class RMtoCAPRestrictionInformation extends RestrictionInformation {

    public void fillMap() {
        textContextComparatorHashMap.put( "category", new categoryDecider() );
        textContextComparatorHashMap.put( "certainty", new certaintyDecider() );
        textContextComparatorHashMap.put( "msgType", new msgTypeDecider() );
        textContextComparatorHashMap.put( "scope", new scopeDecider() );
        textContextComparatorHashMap.put( "severity", new severityDecider() );
        textContextComparatorHashMap.put( "status", new statusDecider() );
        textContextComparatorHashMap.put( "urgency", new urgencyDecider() );
    }
}
