
//
//  Created by Alexandru Antonica on 11/24/15.
//  Copyright © 2015 Alexandru Antonica. All rights reserved.
//

public class PartialSolution {

	
	private String fileName;
	private long startOffSet;
	private int toRead;
	private long endOffSet;

	public PartialSolution(String fileName,long startOffSet , int toRead){
		this.fileName = fileName;
		this.startOffSet = startOffSet;
		this.toRead = toRead;
		this.endOffSet = startOffSet + toRead;
	}

	public String getFileName() {
		return fileName;
	}

	public long getStartOffSet() {
		return startOffSet;
	}

	public int getToRead() {
		return toRead;
	}

	public long getEndOffSet() {
		return endOffSet;
	}

	@Override
	public String toString() {
		return "PartialSolution [fileName=" + fileName + ", startOffSet=" + startOffSet + ", toRead=" + toRead
				+ ", endOffSet=" + endOffSet + "]";
	}
	
	
}