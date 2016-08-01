/*
 * Created by arda on 15.07.2016.
 * Class to transform template xmlString's
 */

import TextContextRestriction.RM_to_CAP.RM_to_CAP_RestrictionInformation;
import TextContextRestriction.RestrictionInformation;
import TextContextRestriction.TextContextComparatorModes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

public class XmlTransform {
    private XPath xPath;
    private RestrictionInformation restrictionInformation;

    public void setRestrictionInformation( RestrictionInformation restrictionInformation ) {
        this.restrictionInformation = restrictionInformation;
    }

    public XmlTransform() throws ParserConfigurationException, IOException, SAXException {
        xPath = XPathFactory.newInstance().newXPath();
    }

    private void deleteUntouched( Node node ) {

        if ( node.getTextContent().replaceAll( "££££", "" ).replaceAll( "NO_INFO", "" ).trim().equals( "" ) ) {
            if ( node.getAttributes().getNamedItem( "required" ) == null ) {
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
            }
        }

        if ( node.getAttributes().getNamedItem( "cardinality" ) != null ) {
            ( (Element) node ).removeAttribute( "cardinality" );
        }

        if ( node.getAttributes().getNamedItem( "required" ) != null ) {
            ( (Element) node ).removeAttribute( "required" );
        }

        List<Node> nodeList = XmlUtil.asList( node.getChildNodes() );
        int prevSize = nodeList.size();
        for ( int m = 0; m < nodeList.size(); m++ ) {

            if ( prevSize != nodeList.size() )
                m--;
            prevSize = nodeList.size();

            Node currentNode = nodeList.get( m );
            if ( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
                //calls this method for all the children which is Element
                deleteUntouched( currentNode );
            }
            else if ( currentNode.getNodeType() == Node.TEXT_NODE && currentNode.getTextContent().trim().equals( "" ) ) {
                currentNode.getParentNode().removeChild( currentNode );
            }
        }
    }

    private void makeUntouched( Node node ) {
        if ( node.getTextContent().replaceAll( "££££", "" ).trim().equals( "" ) == false ) {
            if ( node.hasChildNodes() == false || ( ( node.getChildNodes().getLength() == 1 ) && ( node.getChildNodes().item( 0 ).getNodeType() == Node.TEXT_NODE ) ) ) {
                if ( node.getAttributes().getNamedItem( "required" ) == null ) {
                    node.setTextContent( "££££" );
                }
                else {
                    node.setTextContent( "NO_INFO" );
                }
            }
        }
        NodeList nodeList = node.getChildNodes();
        for ( int m = 0; m < nodeList.getLength(); m++ ) {
            Node currentNode = nodeList.item( m );
            if ( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
                //calls this method for all the children which is Element
                makeUntouched( currentNode );
            }
        }
    }

    private void createNewNodes( List<Node> sourceList, List<Node> targetList, CreateNewNodesModes mode ) {
        Node target = targetList.get( 0 );
        String targetName = target.getNodeName();

        Node dummyTarget = target;

        for ( int i = 0; i < sourceList.size(); i++ ) {
            Node source = sourceList.get( i );
            Node targetNodeParentNode = dummyTarget.getParentNode();

            //unbounded'sa buraya gir
            if ( target.getAttributes().getNamedItem( "cardinality" ) != null && target.getAttributes().getNamedItem( "cardinality" ).getTextContent().equals( "unbounded" ) ) {
                Node newcomerNode = dummyTarget.cloneNode( true );

                transformNode( newcomerNode.getNodeName(), source.getTextContent(), newcomerNode );

                targetNodeParentNode.insertBefore( newcomerNode, dummyTarget );
            }
            //unbounded degilse buraya gir
            else {
                // A new node is creating in here
                if ( sourceList.size() + 1 > targetList.size() || mode == CreateNewNodesModes.OnlyCreate ) {

                    while ( dummyTarget.getParentNode() != null ) {
                        if ( dummyTarget.getAttributes().getNamedItem( "cardinality" ) != null && dummyTarget.getAttributes().getNamedItem( "cardinality" ).getTextContent().equals( "unbounded" ) == true ) {
                            break;
                        }
                        dummyTarget = dummyTarget.getParentNode();
                    }

                    if ( dummyTarget.getParentNode() == null || dummyTarget.equals( target.getOwnerDocument().getDocumentElement() ) ) {
                        dummyTarget = target;
                        continue;
                    }

                    Node newcomer = dummyTarget.cloneNode( true );
                    makeUntouched( newcomer );
                    transformNode( targetName, source.getTextContent(), newcomer );
                    dummyTarget.getParentNode().insertBefore( newcomer, dummyTarget );

                }
                // No need to create new node in  here
                else {
                    int sourceNodeIndex = 0;
                    for ( int j = 1; j < sourceList.size(); j++ ) {
                        if ( sourceList.get( j ).isEqualNode( source ) ) {
                            sourceNodeIndex = j;
                            break;
                        }
                    }
                    /*target = targetList.get( sourceNodeIndex );
                    if ( sourceList.size() != 0 && target.getNodeType() == Node.ELEMENT_NODE && target.getChildNodes().getLength() == 1 ) {
                        target.setTextContent( sourceList.get( sourceNodeIndex ).getTextContent() );
                    }*/
                    transformNode( targetList.get( sourceNodeIndex ).getNodeName(), sourceList.get( sourceNodeIndex ).getTextContent(), targetList.get( sourceNodeIndex + 1 ) );
                }
            }
        }
    }

    private void transformNode( String targetNodeName, String sourceNodeTextContext, Node toBeTransformedNode ) {
        Stack<Node> stack = new Stack<>();
        stack.push( toBeTransformedNode );
        while ( !stack.isEmpty() ) {
            Node poppedNode = stack.pop();
            if ( poppedNode.getNodeType() == Node.ELEMENT_NODE && poppedNode.getNodeName().equals( targetNodeName ) ) {

                if ( restrictionInformation.getComparator( poppedNode.getNodeName() ) != null ) {
                    poppedNode.setTextContent( restrictionInformation.getComparator( poppedNode.getNodeName() ).decideTextContext( sourceNodeTextContext, poppedNode, TextContextComparatorModes.CompareValuesAndDecide ) );
                }
                else {
                    poppedNode.setTextContent( sourceNodeTextContext );
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

            // use comma as separator
            String[] mapping = line.split( ";" );


            if ( mapping[ 2 ].equals( "." ) == false ) {

                List<Node> sourceNodeList = XmlUtil.asList( (NodeList) xPath.compile( mapping[ 1 ] ).evaluate( sourceDocumentParam, XPathConstants.NODESET ) );

                if ( sourceNodeList.size() == 0 ) {
                    continue;
                }

                //target
                List<Node> targetNodeList = XmlUtil.asList( (NodeList) xPath.compile( mapping[ 2 ] ).evaluate( templateDocumentParam, XPathConstants.NODESET ) );
                Node targetNode = (Node) xPath.compile( mapping[ 2 ] ).evaluate( templateDocumentParam, XPathConstants.NODE );

                if ( targetNode == null ) {
                    System.out.println( "Target: " + mapping[ 2 ] + " is null." );
                    continue;
                }

                if ( mapping[ 0 ].equals( "circle" ) ) {
                    System.out.println( "" );
                }

                // Mapping have been previously done, so just create new nodes
                if ( targetNode.getTextContent().replaceAll( "££££", "" ).trim().equals( "" ) == false ) {
                    createNewNodes( sourceNodeList, targetNodeList, CreateNewNodesModes.OnlyCreate );
                }
                else {
                    // ilk eslesmeyi yapıp direkt geciriyor
                    this.transformNode( targetNode.getNodeName(), sourceNodeList.get( 0 ).getTextContent(), targetNode );

                    if ( sourceNodeList.size() > 1 ) {
                        this.createNewNodes( sourceNodeList.subList( 1, sourceNodeList.size() ), targetNodeList, CreateNewNodesModes.CanReplace );
                    }
                }

            }
        }

        deleteUntouched( templateDocumentParam.getDocumentElement() );

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
/*        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/CAP/CAP1_2SevereThunderstormWarning.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/SensorMlTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--SensorML.csv" ).getFile() );*/

        //output2
/*        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Fake/CAP/edxl-cap1.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/SensorMlTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--SensorML.csv" ).getFile() );*/

        //output3
        /*Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Fake/CAP/edxl-cap1.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/RMTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--rm.csv" ).getFile() );
*/
        //output4
/*        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/RM/RMRequestResource_OASIS_Example.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/CAPTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "TestFiles/CSV/test--complextype--rm--cap.csv" ).getFile() );*/

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
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--SensorML.csv" ).getFile() );
*/

        //output7
/*
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/CAP/CAP1_2HomelandSecurityAdvisory.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/RMTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--cap--rm.csv" ).getFile() );
*/

        //output8
        Document sourceDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/RealWorld/RM/RMRequestResource_OASIS_Example.xml" ).getFile() ) );
        Document targetDocument = builder.parse( new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Templates/CAPTemplate.xml" ).getFile() ) );
        File csvFile = new File( XmlTransform.class.getClassLoader().getResource( "SampleXmlFiles/Mappings/mapping--rm--cap.csv" ).getFile() );

        XmlTransform xmlTransform = new XmlTransform();
        xmlTransform.setRestrictionInformation( new RM_to_CAP_RestrictionInformation() );

        System.out.println( xmlTransform.transformDocument( sourceDocument, targetDocument, csvFile ) );
    }
}