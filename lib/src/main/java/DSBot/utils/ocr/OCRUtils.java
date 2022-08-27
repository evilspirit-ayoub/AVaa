package DSBot.utils.ocr;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import DSBot.utils.image.ImageUtils;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.LoadLibs;

public class OCRUtils {
	
	public static final int SIZE_ENLARGEMENT = 4;
	
	static {
		// nu.pattern.OpenCV.loadShared()
		nu.pattern.OpenCV.loadLocally();
	}
	
	public static List<String> OCR(File file, int startX, int startY, int width, int height, int type) throws TesseractException, Exception {
		if(!file.exists()) return new ArrayList<>();
		ITesseract instance = new Tesseract();
		File tessDataFolder = LoadLibs.extractTessResources("tessdata");
		instance.setDatapath(tessDataFolder.getAbsolutePath());
		/*File tmpFolder = LoadLibs.extractTessResources("linux-x86-64");
		System.setProperty("java.library.path", tmpFolder.getPath());*/
		BufferedImage image = ImageIO.read(file).getSubimage(startX, startY, width, height);
		//if(!verification(image, startX, startY, width, height)) return new ArrayList<>();
		List<String> res = new ArrayList<>();
        List<Word> lines = doOCR(instance, file, new Rect(startX, startY, width, height), false, type, TessPageIteratorLevel.RIL_TEXTLINE);
        List<Word> words1, words2;
        Rect rect1, rect2;
        String str;
        for(Word line : lines) {
        	rect1 = getEnlargeRect(image, line.getBoundingBox());
        	words1 = doOCR(instance, file, rect1, false, type, TessPageIteratorLevel.RIL_WORD);
        	str = "";
        	for(Word word : words1) {
        		Rectangle bb = word.getBoundingBox();
        		rect2 = getEnlargeRect(image, new Rectangle(rect1.x + bb.x, rect1.y + bb.y, bb.width, bb.height));
        		words2 = doOCR(instance, file, rect2, true, type, TessPageIteratorLevel.RIL_WORD);
        		for(Word w : words2) str += w.getText() + " ";
        	}
        	res.add(str);
        }
		return res;
	}
	
	private static List<Word> doOCR(ITesseract instance, File file, Rect ocrLocation, boolean resize, int imgProcType, int iteratorLevel) throws Exception {
		BufferedImage preprocessImg =  preprocess(file, ocrLocation, resize, imgProcType);
		return instance.getWords(preprocessImg, iteratorLevel);
	}
	
	private static Rect getEnlargeRect(BufferedImage image, Rectangle actualBox) {
		for(int i = 0; i < SIZE_ENLARGEMENT; i++) {
			if(actualBox.x - 1 > -1) {
				actualBox.x--;
				actualBox.width++;
			}
			if(actualBox.y - 1 > -1) {
				actualBox.y--;
				actualBox.height++;
			}
			actualBox.width = (actualBox.width + 1 + actualBox.x) >= image.getWidth() ? actualBox.width : actualBox.width + 1;
			actualBox.height = (actualBox.height + 1 + actualBox.y) >= image.getHeight() ? actualBox.height : actualBox.height + 1;
		}
		return new Rect(actualBox.x, actualBox.y, actualBox.width, actualBox.height);
	}
	
	public static BufferedImage preprocess(File file, Rect subimageDimension, boolean resize, int type) throws Exception {
		Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(1, 1));
		Mat img = new Mat();
        Mat imgGray = new Mat();
        Mat imgGaussianBlur = new Mat();  
        Mat imgThreshold = new Mat();
        Mat dilate = new Mat();
        
        img = fileToMat(file, subimageDimension);
        if(resize) img = ImageUtils.resizeMat(img, subimageDimension.width * 3, subimageDimension.height * 3);
        Imgproc.GaussianBlur(img, imgGaussianBlur, new Size(0, 0), 10);
        Core.addWeighted(img, 1.5, imgGaussianBlur, -0.5, 0, imgGaussianBlur);
        Imgproc.cvtColor(imgGaussianBlur, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(imgGray, imgThreshold, 127, 255, type);
        Imgproc.dilate(imgThreshold, dilate, kernel);
        //Imgcodecs.imwrite("C:\\Users\\okutucu\\Desktop\\block\\" + i + file.getName(), imgThreshold);
        //i++;
		//return ImageUtils.matToBufferedImage(erode);
        return (BufferedImage) HighGui.toBufferedImage(dilate);
	}
	
	
	public static Mat fileToMat(File file, Rect subimageDimension) throws IOException {
		BufferedImage image = ImageIO.read(file).getSubimage(subimageDimension.x, subimageDimension.y, subimageDimension.width, subimageDimension.height);
		// Here we convert into *supported* format
		BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		imageCopy.getGraphics().drawImage(image, 0, 0, null);
		byte[] data = ((DataBufferByte) imageCopy.getRaster().getDataBuffer()).getData();  
		Mat img = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
		img.put(0, 0, data);
		return img;
	}
}
