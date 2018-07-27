package facerecognition.javafaces;

import java.io.Serializable;
import java.util.List;
public class FaceBundle implements Serializable{
	private double[][] adjFaces;
	private List<String> imageFileNamesList;	
	private double[] averageFace;
	private double[][] eigenFaces;
	private double[] eigenValues;
	private int imageWidth;
	private int imageHeight;
	
	public FaceBundle(List<String> imglist,double[][] adjFaces,double[] avgFace,double[][] eigenFaces,double[] evals,int w,int h){
		this.imageFileNamesList=imglist;
		this.adjFaces=adjFaces;		
		this.averageFace=avgFace;
		this.eigenFaces=eigenFaces;
		this.eigenValues=evals;
		this.imageWidth=w;
		this.imageHeight=h;
	}
	
	public double[][]getAdjustedFaces(){
		return adjFaces;
	}
	public double[][]getEigenFaces(){
		return eigenFaces;
	}
	public double[] getAvgFace(){
		return averageFace;
	}
	public double[] getEigenValues(){
		return eigenValues;
	}
	public List<String> getImageFileNamesList(){
		return imageFileNamesList;
	}
	public int getImageWidth(){
		return imageWidth;
	}
	public int getImageHeight(){
		return imageHeight;
	}
}
