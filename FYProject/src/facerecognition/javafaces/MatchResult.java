package facerecognition.javafaces;

public class MatchResult {
		private String matchFileName;
		private double matchDistance;
		private String matchMessage;
		private boolean matchsuccess;
		public MatchResult(boolean matchsuccess,String matchFileName,double matchDistance,String matchMessage){
			this.matchFileName=matchFileName;
			this.matchDistance=matchDistance;
			this.matchMessage=matchMessage;
			this.matchsuccess=matchsuccess;
		}
		public boolean getMatchSuccess() {
			return matchsuccess;
		}
		public void setMatchSuccess(boolean matchsuccess) {
			this.matchsuccess = matchsuccess;
		}
		public String getMatchFileName() {
			return matchFileName;
		}
		public void setMatchFileName(String matchFileName) {
			this.matchFileName = matchFileName;
		}
		public double getMatchDistance() {
			return matchDistance;
		}
		public void setMatchDistance(double matchDistance) {
			this.matchDistance = matchDistance;
		}
		public String getMatchMessage() {
			return matchMessage;
		}
		
	}

