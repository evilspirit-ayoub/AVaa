package DSBot.utils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
		File tmpFolder = LoadLibs.extractTessResources("linux-x86-64");
		System.setProperty("java.library.path", tmpFolder.getPath());
		BufferedImage image = ImageIO.read(file);
		if(!verification(image, startX, startY, width, height)) return new ArrayList<>();
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
        		rect2 = getEnlargeRect(image, word.getBoundingBox());
        		words2 = doOCR(instance, file, new Rect(rect1.x + rect2.x , rect1.y + rect2.y, rect2.width, rect2.height), true, type, TessPageIteratorLevel.RIL_WORD);
        		for(Word w : words2) str += w.getText() + " ";
        	}
        	res.add(str);
        }
		return res;
	}
	
	private static boolean verification(BufferedImage image, int startX, int startY, int width, int height) {
		if(startX < 0
				|| startX > image.getWidth() - 1
				|| startY < 0
				|| startY > image.getHeight() - 1
				|| width < 0
				|| width > image.getWidth() - 1 - startX
				|| height < 0
				|| height > image.getHeight() - 1 - startY) return false;
		return true;
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
		Mat subimage = new Mat(Imgcodecs.imread(file.getPath()), subimageDimension);
		if(resize) subimage = ImageUtils.resizeMat(subimage, subimageDimension.width * 8, subimageDimension.height * 8);
		Mat gray = new Mat();
        Imgproc.cvtColor(subimage, gray, Imgproc.COLOR_BGR2GRAY);
		Mat threshold = new Mat();
		Imgproc.threshold(gray, threshold, 127, 255, type);
		//Imgproc.adaptiveThreshold(gray, threshold, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 3, 5);
		BufferedImage imgBuff = ImageUtils.matToBufferedImage(threshold);
		//return ImageUtils.invertBlackAndWhite(imgBuff);
		return imgBuff;
	}
}
