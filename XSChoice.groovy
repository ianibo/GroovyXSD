
class XSChoice {
  List choice_elements = []

  void describe(schema_info, context, depth) {
    println "choice    ${context.parentid}"
    def ctr = 1
    choice_elements.each {
      assert it != null
      def new_context = context.clone()
      new_context.parentid = "${context.parentid}:${ctr++}"
      println "Processing choice particle ${new_context.parentid} ${it}"
      it.describe(schema_info, new_context, depth++)
    }
  }

}
