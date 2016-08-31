package tr.com.srdc.standardtransform.core;

/*
 * Created by Arda Guney on 27.7.2016 02:44.
 * Helper class for Standard Transformer
 */

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.*;

public final class XmlUtil {
    private XmlUtil() {
    }

    static List<Node> asList( NodeList n ) {
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

    static boolean isLeafNode( Node node ) {
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

    static boolean isRequiredNode( Node node ) {
        return node.getAttributes().getNamedItem( "required" ) != null && node.getAttributes().getNamedItem( "required" ).getTextContent().equals( "true" );
    }

    static boolean isUnboundedNode( Node node ) {
        return node.getAttributes().getNamedItem( "cardinality" ) != null && node.getAttributes().getNamedItem( "cardinality" ).getTextContent().equals( "unbounded" );
    }

    static boolean isNoTextContextNode( Node node ) {
        return node.getAttributes().getNamedItem( "haveNoTextContext" ) != null && node.getAttributes().getNamedItem( "haveNoTextContext" ).getTextContent().equals( "true" );
    }

    static String generateRandomString( int length ) {
        Random random = new Random();

        return "I" + random.nextInt( 9999 );
        //        return new BigInteger( 130, secureRandom ).toString( 32 ).substring( 0, length );
    }


    private void changeIdIfExist( Element element ) {
        if ( element.getAttribute( "gml:id" ) != null ) {
            element.setAttribute( "gml:id", generateRandomString( 10 ) );
        }

        List<Node> nodeList = XmlUtil.asList( element.getChildNodes() );
        for ( Node currentNode : nodeList ) {
            if ( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
                changeIdIfExist( (Element) currentNode );
            }
        }
    }

    static String getXPath( Node node ) {
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

    private static void traverse( Node node ) {
        String nodeName, parentName;
        nodeName = node.getNodeName().substring( node.getNodeName().indexOf( ":" ) + 1 );
        nodeName = nodeName.substring( 0, 1 ).toUpperCase() + nodeName.substring( 1 );
        //        sitRepStandard[ traverseIndex++ ][ 0 ] = nodeName;
        if ( node.getParentNode() != null ) {
            parentName = node.getParentNode().getNodeName().substring( node.getParentNode().getNodeName().indexOf( ":" ) + 1 );
            parentName = parentName.substring( 0, 1 ).toUpperCase() + parentName.substring( 1 );

            if ( XmlUtil.isLeafNode( node ) ) {
                System.out.println( getXPath( node ).substring( 11, getXPath( node ).length() ).replaceAll( "/", "" ) );
            }
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

    static String prettyPrint( Node parentNode ) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
        StringWriter stringWriter = new StringWriter();

        Source input = new DOMSource( parentNode );
        Result stringOutput = new StreamResult( stringWriter );

        transformer.transform( input, stringOutput );

        return stringWriter.getBuffer().toString();
    }

    static void writeResultToFile( Node parentNode, String fileName ) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );

        Source input = new DOMSource( parentNode );

        Result fileOutput = new StreamResult( new File( "output.xml" ) );

        transformer.transform( input, fileOutput );
    }

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