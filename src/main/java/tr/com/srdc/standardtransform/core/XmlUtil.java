package tr.com.srdc.standardtransform.core;

/*
 * Created by Arda Guney on 27.7.2016 02:44.
 * Helper class for Xml Transform
 */

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public final class XmlUtil {
    private XmlUtil() {
    }

    public static List<Node> asList( NodeList n ) {
        return n.getLength() == 0 ? Collections.emptyList() : new NodeListWrapper( n );
    }

    public static Node[] convertToArray( NodeList list ) {
        int length = list.getLength();
        Node[] copy = new Node[ length ];

        for ( int n = 0 ; n < length ; ++n ) {
            copy[ n ] = list.item( n );
        }

        return copy;
    }

    protected static boolean isLeafNode( Node node ) {
        if ( node.hasChildNodes() == false ) {
            return true;
        }
        else {
            for ( Node forNode : XmlUtil.asList( node.getChildNodes() ) ) {
                if ( forNode.getNodeType() == Node.ELEMENT_NODE ) {
                    return false;
                }
            }
        }
        return true;
    }

    protected static boolean isRequiredNode( Node node ) {
        if ( node.getAttributes().getNamedItem( "required" ) != null && node.getAttributes().getNamedItem( "required" ).getTextContent().equals( "true" ) ) {
            return true;
        }
        else {
            return false;
        }
    }

    protected static boolean isUnboundedNode( Node node ) {
        if ( node.getAttributes().getNamedItem( "cardinality" ) != null && node.getAttributes().getNamedItem( "cardinality" ).getTextContent().equals( "unbounded" ) ) {
            return true;
        }
        else {
            return false;
        }
    }

    protected static boolean isNoTextContextNode( Node node ) {
        if ( node.getAttributes().getNamedItem( "haveNoTextContext" ) != null && node.getAttributes().getNamedItem( "haveNoTextContext" ).getTextContent().equals( "true" ) ) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String generateRandomString( int length ) {
        SecureRandom secureRandom = new SecureRandom();
        Random random = new Random();

        return "I" + random.nextInt(9999);
//        return new BigInteger( 130, secureRandom ).toString( 32 ).substring( 0, length );
    }


    public void changeIdIfExist( Element element ) {
        if ( element.getAttribute( "gml:id" ) != null ) {
            element.setAttribute( "gml:id", generateRandomString( 10 ) );
        }

        List<Node> nodeList = XmlUtil.asList( element.getChildNodes() );
        for ( int m = 0 ; m < nodeList.size() ; m++ ) {
            Node currentNode = nodeList.get( m );
            if ( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
                changeIdIfExist( (Element) currentNode );
            }
        }
    }

    protected static String getXPath( Node node ) {
        Node parent = node.getParentNode();
        if ( parent == null ) {
            if ( node.getNodeName().split( ":" ).length == 2 ) {
                return "/" + node.getNodeName().split( ":" )[ 1 ];
            }
            else {
                return "/" + node.getNodeName();
            }
        }

        if ( node.getNodeName().split( ":" ).length == 2 ) {
            return getXPath( parent ) + "/" + node.getNodeName().split( ":" )[ 1 ];
        }
        else {
            return getXPath( parent ) + "/" + node.getNodeName();
        }
    }

    protected static void traverse( Node node ) {
        String nodeName, parentName;
        nodeName = node.getNodeName().substring( node.getNodeName().indexOf( ":" ) + 1 );
        nodeName = nodeName.substring( 0, 1 ).toUpperCase() + nodeName.substring( 1 );
        //        sitRepStandard[ traverseIndex++ ][ 0 ] = nodeName;
        if ( node.getParentNode() != null ) {
            parentName = node.getParentNode().getNodeName().substring( node.getParentNode().getNodeName().indexOf( ":" ) + 1 );
            parentName = parentName.substring( 0, 1 ).toUpperCase() + parentName.substring( 1 );

            if ( XmlUtil.isLeafNode( node ) )
                System.out.println( getXPath( node ).substring( 11, getXPath( node ).length() ).replaceAll( "/", "" ) );
                //System.out.println( nodeName + ";" + getXPath( node ) + ";." );
        }
        else {
            //            sitRepStandard[ traverseIndex - 1 ][ 1 ] = "parent";
        }
        //        sitRepStandard[ traverseIndex - 1 ][ 2 ] = getXPath( node ).substring( 10 );

        NodeList nodeList = node.getChildNodes();
        for ( int m = 0 ; m < nodeList.getLength() ; m++ ) {
            Node currentNode = nodeList.item( m );
            if ( currentNode.getNodeType() == Node.ELEMENT_NODE || ( currentNode.getNodeType() == Node.ATTRIBUTE_NODE && currentNode.getNodeName().equals( "href" ) ) ) {
                traverse( currentNode );
            }
        }
    }

    /*protected int findIndexInExcelFile( String parentElement, String childElement ) throws IOException {
        FileInputStream file = new FileInputStream( new File( getClass().getClassLoader().getResource( "c2SenseMapping.xls" ).getFile() ) );

        //Get the workbook instance for XLS file
        HSSFWorkbook workbook = new HSSFWorkbook( file );

        //Get first sheet from the workbook
        HSSFSheet sheet = workbook.getSheetAt( 0 );

        //Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = sheet.iterator();

        Row row = rowIterator.next();

        while ( rowIterator.hasNext() ) {
            String BColumn = row.getCell( 1 ).getStringCellValue();
            String DColumn = row.getCell( 3 ).getStringCellValue();

            if ( parentElement.equals( BColumn ) && childElement.equals( DColumn ) ) {
                return row.getRowNum();
            }

            row = rowIterator.next();
        }

        return -1;
    }*/

    static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
        private final NodeList list;

        NodeListWrapper( NodeList l ) {
            list = l;
        }

        public Node get( int index ) {
            return list.item( index );
        }

        public int size() {
            return list.getLength();
        }
    }
}