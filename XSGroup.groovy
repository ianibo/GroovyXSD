
class XSGroup {

  Object group_contents

  void describe(schema_info, context) {
    if ( group_contents instanceof XSRef ) {
      println "Referenced group: ${group_contents.ref} - Describing"
      def ref_defn = schema_info.groups[group_contents.ref]
      if ( ref_defn != null )
        ref_defn.describe(schema_info, context);
      else
        println "No definition found for referenced group ${group_contents.ref}"
    }
    else {
      println "group def'n"
    }
  }

}
