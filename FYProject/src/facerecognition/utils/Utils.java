package facerecognition.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class Utils {
	public static boolean isImageFile(String filename) throws IOException{
		boolean isImage=false;
		FileInputStream is ;
		ImageInputStream imginstream =null;
		is = new FileInputStream(filename); 
		imginstream =ImageIO.createImageInputStream(is);
		// get image readers who can read the image format 
		Iterator<ImageReader> iter = ImageIO.getImageReaders(imginstream); 
		if (iter.hasNext()) { 
			isImage=true;
		}
		return isImage;
	}
}
