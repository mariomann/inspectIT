package info.novatec.inspectit.cmr.service.rest.config;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.ReflectiveJaxrsScanner;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;

/**
 * @author Mario Mann
 *
 */
@Component
public class SwaggerConfiguration {

	private String resourcePackage;

	private String basePath;

	private String apiVersion;

	@PostConstruct
	public void init() {
		final ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
		scanner.setResourcePackage("info.novatec.inspectit.cmr.service.rest");

		ScannerFactory.setScanner(scanner);
		ClassReaders.setReader(new DefaultJaxrsApiReader());

		final SwaggerConfig config = ConfigFactory.config();
		config.setApiVersion("1.0");
		config.setBasePath("http://localhost:8182/sw/rest");
	}

	public String getResourcePackage() {
		return resourcePackage;
	}

	public void setResourcePackage(String resourcePackage) {
		this.resourcePackage = resourcePackage;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
}