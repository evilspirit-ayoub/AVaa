package DSBot.utils;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageUtils {
	
	public static BufferedImage toBlackAndWhite(BufferedImage image) {
		BufferedImage blackWhite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = blackWhite.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return blackWhite;
	}
	
	public static BufferedImage toGrayScale(BufferedImage image) {
		BufferedImage blackWhite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = blackWhite.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return blackWhite;
	}
	
	public static BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {
	    int imageWidth  = image.getWidth();
	    int imageHeight = image.getHeight();

	    double scaleX = (double)width/imageWidth;
	    double scaleY = (double)height/imageHeight;
	    AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
	    AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

	    return bilinearScaleOp.filter(
	        image,
	        new BufferedImage(width, height, image.getType()));
	}
	
	public static BufferedImage getScreenCapture(int x, int y, int width, int height) throws AWTException {
		Rectangle screenRect = new Rectangle(x, y, width, height);
		return new Robot().createScreenCapture(screenRect);
	}
	
	public static Dimension getSizeOf(File file) throws IOException {
		BufferedImage bimg = ImageIO.read(file);
		int width = bimg.getWidth();
		int height = bimg.getHeight();
		return new Dimension(width, height);
	}
	
	public static Mat bufferedImageToMat(BufferedImage image) {
        image = convertTo3ByteBGRType(image);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }
	
	private static BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }
	
	public static BufferedImage matToBufferedImage(Mat mat) throws Exception {
	    MatOfByte mob = new MatOfByte();
	    Imgcodecs.imencode(".png", mat, mob);
	    byte ba[] = mob.toArray();
	    BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
	    return bi;
	}
	
	public static Mat resizeMat(Mat mat, int width, int height) {
		Mat resizeimage = new Mat();
		Size sz = new Size(width,height);
		Imgproc.resize(mat, resizeimage, sz );
		return resizeimage;
	}
	
	public static BufferedImage invertBlackAndWhite(BufferedImage image) {
        BufferedImage imageOut = getBlackImage(image.getWidth(), image.getHeight());
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j));
                if (c.equals(Color.white)) imageOut.setRGB(i, j, Color.black.getRGB());
                else if (c.equals(Color.black)) imageOut.setRGB(i, j, Color.white.getRGB());
                else imageOut.setRGB(i, j, image.getRGB(i, j));

            }
        }
        return imageOut;
    }
	
	public static BufferedImage getBlackImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < width - 1; i++)
            for (int j = 0; j < height - 1; j++) image.setRGB(i, j, Color.black.getRGB());
        return image;
    }
}
