package kappa.wikiracer.util.amazon;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import kappa.wikiracer.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Client {

  private static final int MAX_IMAGE_BYTES = 1000000;
  private static final int MAX_DIMENSION = 1000;
  private AmazonS3 s3client;
  @Value("${amazonProperties.endpointUrl}")
  private String endpointUrl;
  @Value("${amazonProperties.bucketName}")
  private String bucketName;
  @Value("${amazonProperties.accessKey}")
  private String accessKey;
  @Value("${amazonProperties.secretKey}")
  private String secretKey;
  @Value("${amazonProperties.region}")
  private String region;

  @PostConstruct
  private void init() {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    s3client = AmazonS3ClientBuilder.standard().withRegion(region)
        .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
  }

  public String uploadImage(MultipartFile multipartFile)
      throws IOException, InvalidFileTypeException {
    File file = convertMultiPartToFile(multipartFile);
    try {
      String fileName = generateFileName(multipartFile);
      uploadToBucket(fileName, file);
      return fileName;
    } finally {
      file.delete();
    }
  }

  public byte[] getImage(String fileName) throws IOException {
    S3Object object = s3client.getObject(new GetObjectRequest(bucketName, fileName));
    return IOUtils.toByteArray(object.getObjectContent());
  }

  public void deleteImage(String url) {
    s3client.deleteObject(new DeleteObjectRequest(bucketName, url));
  }

  private void uploadToBucket(String fileName, File file)
      throws InvalidFileTypeException, IOException {
    checkImage(file);
    s3client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(
        CannedAccessControlList.Private));
  }

  private void checkImage(File file) throws IOException, InvalidFileTypeException {
    BufferedImage image = ImageIO.read(file);
    if (image == null) {
      throw new InvalidFileTypeException("Not a valid image type");
    }
    if (image.getHeight() > MAX_DIMENSION && image.getWidth() > MAX_DIMENSION) {
      throw new InvalidFileTypeException("Image exceeds max dimensions");
    }
    if (file.length() > MAX_IMAGE_BYTES) {
      throw new InvalidFileTypeException("Image exceeds max size");
    }
  }

  private File convertMultiPartToFile(MultipartFile file) throws IOException {
    File convFile = new File(file.getOriginalFilename());
    FileOutputStream fos = new FileOutputStream(convFile);
    fos.write(file.getBytes());
    fos.close();
    return convFile;
  }

  private String generateFileName(MultipartFile multiPart) {
    return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
  }
}
