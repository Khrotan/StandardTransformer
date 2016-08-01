package tr.com.srdc.xmltransform.TextContextRestriction;

import java.util.HashMap;

/**
 * Created by arda on 01.08.2016.
 * Abstract class to hold comparator functions
 */
public abstract class RestrictionInformation {
    protected HashMap<String, TextContextDecider> textContextComparatorHashMap = new HashMap<>();

    public abstract void fillMap();

    public RestrictionInformation() {
        fillMap();
    }

    public TextContextDecider getComparator( String elementName ) {
        return textContextComparatorHashMap.get( elementName );
    }
}