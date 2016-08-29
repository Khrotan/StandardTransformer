/*
 * Created by arda on 15.07.2016.
 * Class to transform template xmlString's
 */

package tr.com.srdc.standardtransform.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tr.com.srdc.standardtransform.TextContextRestriction.RM_to_CAP.RMtoCAPRestrictionInformation;
import tr.com.srdc.standardtransform.TextContextRestriction.RestrictionInformation;
import tr.com.srdc.standardtransform.TextContextRestriction.TextContextComparatorModes;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.List;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class XmlTransform {
    private static final Logger logger = Logger.getLogger( XmlTransform.class.getName() );

    private XPath xPath;
    private RestrictionInformation restrictionInformation;

    public void setRestrictionInformation( RestrictionInformation restrictionInformation ) {
        this.restrictionInformation = restrictionInformation;
    }

    public XmlTransform() throws ParserConfigurationException, IOException, SAXException {
        xPath = XPathFactory.newInstance().newXPath();

        FileHandler fileHandler = new FileHandler( "XmlTransform.log", false );
        fileHandler.setFormatter( new SimpleFormatter() );
        logger.addHandler( fileHandler );
        logger.setLevel( Level.ALL );
    }

    /**
     * @param node Base node to delete appropriate elements
     */
    private void deleteNonmappedNodes( Node node ) {
        if ( node.getTextContent().replaceAll( "££££", "" ).replaceAll( "NO_INFO", "" ).trim().equals( "" ) ) {
            if ( XmlUtil.isRequiredNode( node ) == false ) {
                node.getParentNode().removeChild( node );
                return;
            }
            else if ( XmlUtil.isLeafNode( node ) ) {
                if ( restrictionInformation.getComparator( node.getNodeName() ) == null ) {
                    node.setTextContent( "NO_INFO" );
                }
                else {
                    node.setTextContent( restrictionInformation.getComparator( node.getNodeName() ).decideTextContext( null, node, TextContextComparatorModes.OnDeleteIfNoInfo ) );
                }

                if ( XmlUtil.isNoTextContextNode( node ) ) {
                    if ( node.getTextContent().replaceAll( "££££", "" ).replaceAll( "NO_INFO", "" ).equals( "" ) == false ) {
                        logger.log( Level.WARNING, XmlUtil.getXPath( node ) + " have been mapped wrongly, is a no text context node." );
                    }
                    node.setTextContent( "" );
                }
            }
        }

        if ( node.getAttributes().getNamedItem( "cardinality" ) != null ) {
            ( (Element) node ).removeAttribute( "cardinality" );
        }

        if ( node.getAttributes().getNamedItem( "required" ) != null ) {
            ( (Element) node ).removeAttribute( "required" );
        }

        if ( node.getAttributes().getNamedItem( "haveNoTextContext" ) != null ) {
            ( (Element) node ).removeAttribute( "haveNoTextContext" );
        }

        List<Node> nodeList = XmlUtil.asList( node.getChildNodes() );
        int prevSize = nodeList.size();
        for ( int m = 0 ; m < nodeList.size() ; m++ ) {

            if ( prevSize != nodeList.size() ) {
                m--;
            }

            prevSize = nodeList.size();

            Node currentNode = nodeList.get( m );
            if ( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
                //calls this method for all the children which is Element
                deleteNonmappedNodes( currentNode );
            }
            else if ( currentNode.getNodeType() == Node.TEXT_NODE && currentNode.getTextContent().trim().equals( "" ) ) {
                currentNode.getParentNode().removeChild( currentNode );
            }
        }
    }

    /**
     * <p>
     * This method replaces every descendant node of given parameter's text context with either ££££ or NO_INFO.
     * </p>
     *
     * @param node Base node that is going to be reverted back to it's template form.
     */
    private void revertNodeToTemplateForm( Node node ) {
        if ( node.getTextContent().replaceAll( "££££", "" ).trim().equals( "" ) == false ) {
            if ( XmlUtil.isLeafNode( node ) ) {
                if ( XmlUtil.isRequiredNode( node ) == false ) {
                    node.setTextContent( "££££" );
                }
                else {
                    node.setTextContent( "NO_INFO" );
                }
            }
        }

        if ( ( (Element) node ).getAttribute( "gml:id" ) != null && ( (Element) node ).getAttribute( "gml:id" ).equals( "" ) == false ) {
            //System.out.println( ( (Element) node ).getAttribute( "gml:id" ) );
            ( (Element) node ).setAttribute( "gml:id", XmlUtil.generateRandomString( 5 ) );
        }

        NodeList nodeList = node.getChildNodes();
        for ( int m = 0 ; m < nodeList.getLength() ; m++ ) {
            Node currentNode = nodeList.item( m );
            if ( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
                //calls this method for all the children which is Element, recursive call
                revertNodeToTemplateForm( currentNode );
            }
        }
    }

    public Node findUnboundedAncestorNode( Node unboundedAncestor ) {
        while ( unboundedAncestor.getParentNode() != null ) {
            if ( XmlUtil.isUnboundedNode( unboundedAncestor ) ) {
                break;
            } else {
                unboundedAncestor = unboundedAncestor.getParentNode();
            }
        }

        if ( unboundedAncestor.getParentNode() == null || unboundedAncestor.equals( unboundedAncestor.getOwnerDocument().getDocumentElement() ) ) {
            return null;
        }

        return unboundedAncestor;
    }

    /*public void createUnboundedNodes( List<Node> sourceList, Node target ) {
        for ( Node sourceNode : sourceList ) {
            Node newcomerNode = target.cloneNode( true );
            changeTextContext( newcomerNode.getNodeName(), sourceNode.getTextContent(), newcomerNode );
            target.getParentNode().insertBefore( newcomerNode, target );
        }
    }*/

    public void createBoundedNodes( List<Node> sourceList, Node target ) {
        Node unboundedAncestor = findUnboundedAncestorNode( target );

        if ( unboundedAncestor == null ) {
            logger.log( Level.SEVERE, "Mapping from " + XmlUtil.getXPath( sourceList.get( 0 ) ) + " to " + XmlUtil.getXPath( target ) + " has been done incorrectly. There is no unbounded ancestor node." );
            return;
        }

        for ( Node sourceNode : sourceList ) {
            Node newcomer = unboundedAncestor.cloneNode( true );
            revertNodeToTemplateForm( newcomer );
            changeTextContext( target.getNodeName(), sourceNode.getTextContent(), newcomer );
            unboundedAncestor.getParentNode().insertBefore( newcomer, unboundedAncestor );
        }
    }

    public void replaceNodes( List<Node> sourceList, List<Node> targetList ) {
        for ( int i = 0 ; i < sourceList.size() ; i++ ) {
            changeTextContext( targetList.get( i ).getNodeName(), sourceList.get( i ).getTextContent(), targetList.get( i ) );
        }
    }

    /**
     * @param sourceList is the list of source xpath nodes
     * @param targetList is the list of 4
     * @param mode
     */
    private void createNewNodes( List<Node> sourceList, List<Node> targetList, CreateNewNodesModes mode ) {
        Node target = targetList.get( 0 );
        String targetName = target.getNodeName();

        Node dummyTarget = target;

        for ( int i = 0 ; i < sourceList.size() ; i++ ) {
            Node source = sourceList.get( i );
            Node targetNodeParentNode = dummyTarget.getParentNode();

            if ( XmlUtil.isUnboundedNode( target ) ) {
                Node newcomerNode = dummyTarget.cloneNode( true );
                changeTextContext( newcomerNode.getNodeName(), source.getTextContent(), newcomerNode );
                targetNodeParentNode.insertBefore( newcomerNode, dummyTarget );
            }
            else {
                // A new node is creating in here
                if ( sourceList.size() + 1 > targetList.size() || mode == CreateNewNodesModes.OnlyCreate ) {

                    while ( dummyTarget.getParentNode() != null ) {
                        if ( XmlUtil.isUnboundedNode( dummyTarget ) ) {
                            break;
                        }
                        dummyTarget = dummyTarget.getParentNode();
                    }

                    if ( dummyTarget.getParentNode() == null || dummyTarget.equals( target.getOwnerDocument().getDocumentElement() ) ) {
                        dummyTarget = target;
                        continue;
                    }

                    Node newcomer = dummyTarget.cloneNode( true );
                    revertNodeToTemplateForm( newcomer );
                    changeTextContext( targetName, source.getTextContent(), newcomer );
                    dummyTarget.getParentNode().insertBefore( newcomer, dummyTarget );
                }
                // No need to create new node in  here
                else {
                    int sourceNodeIndex = 0;
                    for ( int j = 1 ; j < sourceList.size() ; j++ ) {
                        if ( sourceList.get( j ).isEqualNode( source ) ) {
                            sourceNodeIndex = j;
                            break;
                        }
                    }
                    /*target = targetList.get( sourceNodeIndex );
                    if ( sourceList.size() != 0 && target.getNodeType() == Node.ELEMENT_NODE && target.getChildNodes().getLength() == 1 ) {
                        target.setTextContent( sourceList.get( sourceNodeIndex ).getTextContent() );
                    }*/
                    changeTextContext( targetList.get( sourceNodeIndex ).getNodeName(), sourceList.get( sourceNodeIndex ).getTextContent(), targetList.get( sourceNodeIndex + 1 ) );
                }
            }
        }
    }

    /**
     * Iterative Depth First Search implementation to find node with targetName in the children of toBeSearchedNode.
     * If it has found, replace it's text context with appropriate value.
     *
     * @param targetName        Target node's name that is going to searched in toBeSearchedNode
     * @param sourceTextContext Source node's text context that is going to be replaced
     * @param toBeSearchedNode  Base node that is going to be searched in, for element with targetName
     */
    private void changeTextContext( String targetName, String sourceTextContext, Node toBeSearchedNode ) {
        Stack<Node> stack = new Stack<>();
        stack.push( toBeSearchedNode );
        while ( !stack.isEmpty() ) {
            Node poppedNode = stack.pop();
            if ( poppedNode.getNodeType() == Node.ELEMENT_NODE
                    && poppedNode.getNodeName().equals( targetName ) ) {

                if ( restrictionInformation.getComparator( poppedNode.getNodeName() ) != null ) {
                    poppedNode.setTextContent( restrictionInformation.getComparator( poppedNode.getNodeName() ).decideTextContext( sourceTextContext, poppedNode, TextContextComparatorModes.CompareValuesAndDecide ) );
                }
                else {
                    poppedNode.setTextContent( sourceTextContext );
                }

                break;
            }
            stack.addAll( XmlUtil.asList( poppedNode.getChildNodes() ) );
        }
    }

    public String transformDocument( Document sourceDocumentParam, Document templateDocumentParam, File csvFile ) throws IOException, XPathExpressionException, TransformerException {
        BufferedReader bufferedReader = new BufferedReader( new FileReader( csvFile ) );

        String line;

        while ( ( line = bufferedReader.readLine() ) != null ) {

            String[] mapping = line.split( ";" );

            if ( mapping[ 2 ].equals( "." ) == false ) {

                List<Node> sourceNodeList = XmlUtil.asList( (NodeList) xPath.compile( mapping[ 1 ] ).evaluate( sourceDocumentParam, XPathConstants.NODESET ) );

                if ( sourceNodeList.size() == 0 ) {
                    logger.log( Level.INFO, "Source: " + mapping[ 1 ] + " doesn't exist in incoming message." );
                    continue;
                }

                // Target
                List<Node> targetNodeList = XmlUtil.asList( (NodeList) xPath.compile( mapping[ 2 ] ).evaluate( templateDocumentParam, XPathConstants.NODESET ) );

                if ( targetNodeList.size() == 0 ) {
                    logger.log( Level.SEVERE, "Target: " + mapping[ 2 ] + " doesn't exist in target template." );
                    continue;
                }

                if ( mapping[0].equals( "resourceDesc" ) ) {
                    System.out.print("");
                }

                // Mapping have been previously done, so just create new nodes
                if ( targetNodeList.get( 0 ).getTextContent().replaceAll( "££££", "" ).trim().equals( "" ) == false ) {
                    createBoundedNodes( sourceNodeList, targetNodeList.get( 0 ) );
                }
                else {
                    // The first mapping on the the template document's element with ££££ value.
                    replaceNodes( sourceNodeList.subList( 0, 1 ), targetNodeList.subList( 0, 1 ) );

                    if ( sourceNodeList.size() > 1 ) {
//                        this.createNewNodes( sourceNodeList.subList( 1, sourceNodeList.size() ), targetNodeList, CreateNewNodesModes.CanReplace );

                        if ( sourceNodeList.size() > targetNodeList.size() ) {
                            createBoundedNodes( sourceNodeList.subList( 1, sourceNodeList.size() ), targetNodeList.get( 0 ) );
                        } else {
                            replaceNodes( sourceNodeList.subList( 1, sourceNodeList.size() ),
                                    targetNodeList.subList( 1, targetNodeList.size() ));
                        }
                    }
                }

            }
        }

        deleteNonmappedNodes( templateDocumentParam.getDocumentElement() );

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
        StringWriter stringWriter = new StringWriter();
        Result output = new StreamResult( new File( "output.xml" ) );

        Result stringOutput = new StreamResult( stringWriter );

        Source input = new DOMSource( templateDocumentParam );

        transformer.transform( input, output );

        transformer.transform( input, stringOutput );

        return stringWriter.getBuffer().toString();
    }

    public static void main( String[] args ) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        //output1
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/CAP/CAP1_2SevereThunderstormWarning.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/SensorMlTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--sensorml.csv" ).getFile() );
*/

        //output2
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Fake/CAP/edxl-cap1.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/SensorMlTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--sensorml.csv" ).getFile() );

        //output3
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Fake/CAP/edxl-cap1.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/RM/RMRequestResourceTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--rm.csv" ).getFile() );
*/

        //output4
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/RM/RMRequestResource_OASIS_Example.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/CAPTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "TestFiles/CSV/test--complextype--rm--cap.csv" ).getFile() );
*/

        //output5
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/RM/RMRequestResource_OASIS_Example.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/CAPTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "TestFiles/CSV/test--createnewnodes--samemapping--rm--cap.csv" ).getFile() );
*/

        //output6
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/CAP/CAP1_2HomelandSecurityAdvisory.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/SensorMlTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--sensorml.csv" ).getFile() );
*/

        //output7
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/CAP/CAP1_2HomelandSecurityAdvisory.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/RM/RMRequestResourceTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--rm.csv" ).getFile() );
*/

        //output8
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/RM/RMRequestResource_OASIS_Example.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/CAPTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--rm--cap.csv" ).getFile() );
*/

        //XmlUtil.traverse( targetDocument.getDocumentElement() );

        XmlTransform xmlTransform = new XmlTransform();
        xmlTransform.setRestrictionInformation( new RMtoCAPRestrictionInformation() );

        xmlTransform.transformDocument( sourceDocument, targetDocument, csvFile );
    }
}