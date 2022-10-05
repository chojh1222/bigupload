package cho.big.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import me.desair.tus.server.TusFileUploadService;

@EnableAdminServer
@SpringBootApplication
public class BigUploadApplication {

	@Value("${app.tus-upload-directory}")
    protected String tusUploadDir;
	
	@Value("#{servletContext.contextPath}")
    private String servletContextPath;
	
	@Bean
    public TusFileUploadService tusFileUploadService() {
        return new TusFileUploadService()
                .withStoragePath(tusUploadDir)
                .withDownloadFeature()
                .withUploadURI("/upload")
                .withThreadLocalCache(true);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(BigUploadApplication.class, args);
	}

}
