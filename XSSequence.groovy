
class XSSequence {

  List elements = []

  void describe(schema_info, context) {
    println "Sequence"
    def ctr = 1;
    elements.each {
      def new_context = context.clone()
      new_context.parentid = "${context.parentid}:${ctr++}"
      println "Processing particle ${new_context.parentid} ${it}"
      it.describe(schema_info, new_context)
    }

  }

}
