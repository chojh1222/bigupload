package cho.big.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;

@RestController
public class UploadController {
	
	@Value("${app.upload-directory}")
    protected String uploadDir;
	
	@Autowired
    private TusFileUploadService tusFileUploadService;
	
	@RequestMapping(value = { "/upload", "/upload/**" }, method = { RequestMethod.POST,
		      RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.DELETE, RequestMethod.GET })
    public String processUpload(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws IOException, TusException {
		System.out.printf("=============processing upload [%s] [%s]\n", servletRequest.getMethod(), servletRequest.getRequestURI());
		
		tusFileUploadService.process(servletRequest, servletResponse);
		
		System.out.println("============processing done!");
        
		String requestUri = servletRequest.getRequestURI();
        UploadInfo uploadInfo = tusFileUploadService.getUploadInfo(requestUri);
        if(uploadInfo != null && !uploadInfo.isUploadInProgress()) {
        	try(InputStream inputStream = tusFileUploadService.getUploadedBytes(requestUri)) {
        		Path path = Paths.get(uploadDir).resolve(uploadInfo.getFileName());
            	Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            	tusFileUploadService.deleteUpload(requestUri);
        	}
        	catch(IOException ioe) {
    			throw ioe;
    		}
        }
		
        return "saved file!";
    }
	
	@PostMapping("/upload0")
	public String upload(final HttpServletRequest request) throws Exception {
		if(!ServletFileUpload.isMultipartContent(request)) {
            throw new Exception("Multipart request expected");
        }
		
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator fileIterator = upload.getItemIterator(request);
        
        while (fileIterator.hasNext()) {
            FileItemStream item = fileIterator.next();
            
            if (item.isFormField()) {
            	continue;
            }
            
            String fileName = item.getName();
            String type = item.getContentType();
            
            InputStream ins = item.openStream();
            File destination = new File(uploadDir, fileName);
//            File destination = new File(uploadDir, UUID.randomUUID() + "");
            OutputStream outs = new FileOutputStream(destination);
            IOUtils.copy(ins, outs);
            IOUtils.closeQuietly(ins);
            IOUtils.closeQuietly(outs);
        }

		return "saved file!";
	}
	
//	@PostMapping("/upload0")
//	public String upload(MultipartFile file, String threadGroup) throws Exception {
//		System.out.printf("upload0! controller! threadGroup[%s] [%s]\n", threadGroup, file.getOriginalFilename());
//		Path path = Paths.get(uploadDir).resolve(file.getOriginalFilename());
//		file.transferTo(path);
//
//		return "saved file !";
//	}
	
	@GetMapping("/test")
	public String test() throws InterruptedException {
		Thread.sleep(1000);
		return "test!";
	}
}
