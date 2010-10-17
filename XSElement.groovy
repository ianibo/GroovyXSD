
class XSElement {
  groovy.xml.QName name
  groovy.xml.QName type_name
  XSType type

  void describe(schema_info, context) {

    println "element    ${context.parentid} ${name} ${type}";

    if ( type != null ) {
      type.describe(schema_info, context);
    }
    else if ( type_name != null ) {
      def type_info = schema_info.complex_types[type_name]
      type_info.describe(schema_info, context);
    }
    else {
      println "No type information for element with name ${name}"
      System.exit(1)
    }

  }
}
