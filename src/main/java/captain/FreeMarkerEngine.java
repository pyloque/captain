package captain;

import java.io.IOException;
import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import spark.ModelAndView;
import spark.TemplateEngine;

public class FreeMarkerEngine extends TemplateEngine {

	private Configuration configuration;

	protected FreeMarkerEngine() {
		this.configuration = createFreemarkerConfiguration();
	}

	@Override
	public String render(ModelAndView modelAndView) {
		try {
			StringWriter stringWriter = new StringWriter();
			Template template = configuration.getTemplate(modelAndView.getViewName());
			template.process(modelAndView.getModel(), stringWriter);
			return stringWriter.toString();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (TemplateException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Configuration createFreemarkerConfiguration() {
		Configuration config = new Configuration(Configuration.VERSION_2_3_23);
		config.setClassForTemplateLoading(this.getClass(), "");
		return config;
	}
}
