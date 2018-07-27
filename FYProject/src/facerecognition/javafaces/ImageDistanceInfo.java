package facerecognition.javafaces;

public class ImageDistanceInfo {	
	int index;
	double value;
	public ImageDistanceInfo(double value, int index) {
		this.value = value;
		this.index = index;
	}
	public int getIndex() {
		return index;
	}
	public double getValue() {
		return value;
	}
}
