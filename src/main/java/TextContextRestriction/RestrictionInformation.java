package TextContextRestriction;

import java.util.HashMap;

/**
 * Created by arda on 01.08.2016.
 * Abstract class to hold comparator functions
 */
public abstract class RestrictionInformation {
    protected HashMap<String, TextContextDecider> textContextComparatorHashMap = new HashMap<>();

    public RestrictionInformation() {
        fillMap();
    }

    public abstract void fillMap();

    public TextContextDecider getComparator( String elementName ) {
        return textContextComparatorHashMap.get( elementName );
    }
}
