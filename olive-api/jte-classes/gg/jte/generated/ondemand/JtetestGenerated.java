package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class JtetestGenerated {
	public static final String JTE_NAME = "test.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,1,1,1,4,4,7,7,10,10,10,0,0,0,0};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, com.olive.base.util.Model model) {
		jteOutput.writeContent("package ");
		jteOutput.writeUserContent(model.packageName());
		jteOutput.writeContent("\r\n\r\n@Service\r\npublic class ");
		jteOutput.writeUserContent(model.serviceName());
		jteOutput.writeContent("Service {\r\n    private final JSqlClient sqlClient;\r\n\r\n    public ");
		jteOutput.writeUserContent(model.serviceName());
		jteOutput.writeContent("Service(JSqlClient sqlClient) {\r\n        this.sqlClient = sqlClient;\r\n    }\r\n}");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		com.olive.base.util.Model model = (com.olive.base.util.Model)params.get("model");
		render(jteOutput, jteHtmlInterceptor, model);
	}
}
