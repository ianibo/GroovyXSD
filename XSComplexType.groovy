
class XSComplexType extends XSType {

  String name
  List particles = [];

  void describe(schema_info, context, depth) {
    println "Describe complex type"
    def ctr = 1;
    particles.each {
      def new_context = context.clone()
      new_context.parentid = "${context.parentid}:${ctr++}"
      println "Processing particle ${new_context.parentid} ${it}"
      it.describe(schema_info, new_context,depth++)
    }
  }
}
