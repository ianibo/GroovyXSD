
class XSElement {
  groovy.xml.QName name
  groovy.xml.QName type

  void describe(schema_info, context) {

    println "element    ${context.parentid} ${name} ${type}";

    def type_info = schema_info.complex_types[type]

    if ( type_info != null )
      type_info.describe(schema_info, context);
    else
      println "No definition for ${name} ${type}"
  }
}
