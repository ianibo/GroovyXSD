import grails.converters.*
import groovy.util.XmlSlurper

XSChoice processChoice(choice_node, context) {
  // println "Choice...";
  def result = new XSChoice();
  choice_node.children().each {
    // result.add( processParticle(it) );
    result.choice_elements.add(processParticle(it, context))
  }
  result
}

Object processParticle(it, context) {
  // println "processParticle ${it.name()}"
  def result

  switch ( it.name() ) {
    case "choice":
      result = processChoice(it, context);
      break;
    case "attributeGroup":
      // println "attributeGroup...";
      result = new XSAttrGroup();
      break;
    case "group":
      // println "group...";
      if ( it.@ref != '' ) {
        println "Process group reference ${it.@ref}"
        result = new XSGroup(group_contents: new XSRef(ref:interpretTypeName(it.@ref.text(), context)));
      }
      else {
        // println "Don't handle inline groups yet";
        System.exit(1);
      }
      break;
    case "simpleContent":
      result = new XSSimpleContent();
      break;
    case "complexContent":
      result = new XSComplexContent();
      break;
    case "sequence":
      result = new XSSequence();
      break;
    default:
      println "Unhandled particle ${it.name()}"
      break;
  }

  result
}

XSElement processElementDefinition(element_definition_node, context) {
  println "processElementDefinition"
  new XSElement(name: interpretTypeName(element_definition_node.@name.text(), context), type: interpretTypeName(element_definition_node.@type.text(), context))
}

XSComplexType processComplexTypeDefinition(complex_type_definition_node, context) {

  println "processComplexTypeDefinition ${complex_type_definition_node.@name}"

  // Process each element in this comlex definition
  def result = new XSComplexType(name: complex_type_definition_node.@name.text());

  complex_type_definition_node.children().each {
    def particle_def = processParticle(it, context);
    if ( particle_def != null )
      result.particles.add(particle_def);
    else {
      println "processParticle returned null"
      System.exit(1)
    }
     
  }

  result;
}

XSSimpleType processSimpleTypeDefinition(simple_type_definition_node) {
  println "processSimpleTypeDefinition"
  new XSSimpleType(name: simple_type_definition_node.@name)
}

XSGroup processGroupDefinition(group_definition_node) {
  println "processGroupDefinition ${group_definition_node.@name}"
  new XSGroup()
}


void loadSchema(level, schema_url, schema_info) {

  // println "Loading ${schema_url}, depth=${level}"

  // def schema_text = schema_url.toURL().text
  def schema_xml = new XmlSlurper().parse(schema_url.openStream()).declareNamespace(xs:"http://www.w3.org/2001/XMLSchema")

  println "[${level}]Parsed ${schema_url} result is of class. Target namespace: ${schema_xml.@targetNamespace}"

  def context = [:]

  if ( schema_xml.@targetNamespace != '' ) {
    // println "Setting target namespace to ${schema_xml.@targetNamespace}"
    context["targetNamespace"] = schema_xml.@targetNamespace.text();
  }
  context["doc"] = schema_xml

  // println "Lookup namespace ${schema_xml.lookupNamespace('xs')}"

  // println ("namespace of root node is : ${schema_xml.namespaceURI()}");

  // Iterate through each element in the schema.. processing...
  schema_xml.children().each {

    // println "${it.name()} ${it.name().getClass().getName()} prefix=${it.namespaceURI()}"

    // println "Processing ${it.name().getQualifiedName()} ${it.name().getNamespaceURI()} ${it.name().getLocalPart()}"
    if ( it.name() == "annotation" ) {
      // println "Process annotation"
    }
    else if ( it.name() == "include" ) {
      // println "Process include ${it.@schemaLocation}"
      if ( it.@schemaLocation != '' ) {
        loadSchema(level+1,new URL(schema_url, it.@schemaLocation.text()), schema_info)
      }
    }
    else if ( it.name() == "import" ) {
      // println "Process import ${it.@schemaLocation}"
      if ( it.@schemaLocation != '' ) {
        loadSchema(level+1,new URL(schema_url,it.@schemaLocation.text()), schema_info)
      }
    }
    else if ( it.name() == "group" ) {
      // // println "Process group"
      schema_info["groups"][interpretTypeName(it.@name.text(),context)] = processGroupDefinition(it)
    }
    else if ( it.name() == "attributeGroup" ) {
      // println "Process attributeGroup"
      // schema_info["attr_groups"][
    }
    else if ( it.name() == "simpleType" ) {
      // println "Process simpleType ${it.@name}"
      schema_info["simple_types"][interpretTypeName(it.@name.text(),context)] = processSimpleTypeDefinition(it)
    }
    else if ( it.name() == "complexType" ) {
      def type_id = interpretTypeName(it.@name.text(),context);
      println "Process complexType ${type_id} ${it.@name}"
      schema_info["complex_types"][type_id] = processComplexTypeDefinition(it, context)
    }
    else if ( it.name() == "element" ) {
      println "\n\n\n ** Process element ${it.@name} ** \n\n\n"
      schema_info["elements"][interpretTypeName(it.@name.text(),context)] = processElementDefinition(it, context)
    }
    else {
      // println "Unhandled xml schema construct ${it.name()} ${it.name().getClass().getName()}"
    }
  }

  // println "Completed processing of ${schema_url}"
}

groovy.xml.QName interpretTypeName(String type_name, context) {
  def result = null;

  if ( type_name.indexOf(':') > -1 ) {
    def prefix = type_name.substring(0,type_name.indexOf(':'))
    def elem = type_name.substring(type_name.indexOf(':')+1)
    def namespace_uri = context.doc.lookupNamespace(prefix)
    // println "\n\nitn: Processing a QName - ${namespace_uri} ${prefix} ${type_name}\n\n"

    result = new groovy.xml.QName(namespace_uri,elem)
  }
  else {
    // type name is an NCName so we add the target namespace of the current schema document
    result = new groovy.xml.QName(context.targetNamespace,type_name)
  }

  // println "interpretTypeName ${type_name} = ${result}"

  result;
}



void describe(root_element, schema_info) {

  // Look up root element
  XSElement e = schema_info.elements[root_element];

  // println "Located element ${e}"
  def context = [:]
  context.parentid = "";

  e.describe(schema_info, context);

  // Now describe the type for that element
}

// test 1 - load the loms schema

// Declare an empty map
def schema_info = [complex_types:[:], simple_types:[:], elements:[:], groups:[:], attr_groups:[:], addinfo:[:]]

loadSchema(0,new URL("http://ltsc.ieee.org/xsd/lomv1.0/lom.xsd"),schema_info)

println "Schema contains ${schema_info.complex_types.size()} complex elements. "

describe(new groovy.xml.QName("http://ltsc.ieee.org/xsd/LOM","lom"),schema_info);



