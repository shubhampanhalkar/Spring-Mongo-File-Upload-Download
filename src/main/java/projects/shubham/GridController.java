package projects.shubham;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;

@RestController
public class GridController {

	@Autowired
	private GridFsTemplate gridFsTemplate;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	
	@PostMapping("/Upload")
	public String fileUpload(@RequestParam("file") MultipartFile file) throws Exception {
		
		//File file = new File(InputFile.());
		//InputStream content = new FileInputStream(file);
		DBObject metaData = new BasicDBObject();
		metaData.put("type",file.getContentType());
		metaData.put("title", file.getOriginalFilename());
		metaData.put("userid", "1");
		ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(),file.getContentType(),metaData);
		return "File Uploaded Successfully"+fileId.toString();
	}
	
	@GetMapping("/Download/{title}")
	public String fileDownload(@PathVariable("title")String title,HttpServletResponse response) 
	{
		try 
		{
			GridFSFile gridfs = gridFsTemplate.findOne(Query.query(Criteria.where("metadata.title").is(title)));
	         response.setHeader("Content-Disposition","attachment;filename=\""+gridfs.getFilename()+"\"");
			GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
			GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridfs.getObjectId());
			GridFsResource resource = new GridFsResource(gridfs,gridFSDownloadStream);
			InputStream inp = resource.getInputStream();
			OutputStream out = response.getOutputStream();
			IOUtils.copy(inp, out);
		}
		catch(Exception e)
		{
			System.out.println("Something went wrong"+e.getMessage());
		}
		return "File downloaded successfully";
	}
	
	
}
