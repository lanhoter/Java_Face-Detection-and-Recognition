package facerecognition.utils;

public class ValueIndexPair implements Comparable<ValueIndexPair>{
	double vectorElement;
	int matrixRowIndex;
	
	public ValueIndexPair() {
		super();
	}
	public ValueIndexPair(double vectorElement, int matrixRowIndex) {
		super();
		this.vectorElement = vectorElement;
		this.matrixRowIndex = matrixRowIndex;
	}
	public double getVectorElement() {
		return vectorElement;
	}
	public void setVectorElement(double vectorElement) {
		this.vectorElement = vectorElement;
	}
	public int getMatrixRowIndex() {
		return matrixRowIndex;
	}
	public void setMatrixRowIndex(int matrixRowIndex) {
		this.matrixRowIndex = matrixRowIndex;
	}
	
	@Override
	public int compareTo(ValueIndexPair other) {
		int ret = 0;
		//will be sorted in descending order in a sorted collection
		if (vectorElement > other.getVectorElement()){
			ret = -1;
		}else if (vectorElement < other.getVectorElement()){
			ret = 1;
		}
		return ret;
	}
	
}