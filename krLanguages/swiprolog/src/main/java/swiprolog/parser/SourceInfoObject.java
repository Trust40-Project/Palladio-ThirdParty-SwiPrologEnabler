package swiprolog.parser;

import java.io.File;

import krTools.parser.SourceInfo;

public class SourceInfoObject implements SourceInfo {
	
	private File sourceFile = null;
	private int lineNr;
	private int charPos;
	private String msg = new String();
	
	public SourceInfoObject(int lineNr, int charPos) {
		this.lineNr = lineNr;
		this.charPos = charPos;
	}

	@Override
	public File getSource() {
		return sourceFile;
	}
	
	public void setSource(File file) {
		this.sourceFile = file;
	}

	@Override
	public int getLineNumber() {
		return lineNr;
	}

	@Override
	public int getCharacterPosition() {
		return charPos;
	}

	@Override
	public String getMessage() {
		return msg;
	}
	
	public void setMessage(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("line ");
		builder.append(lineNr);
		builder.append(", position ");
		builder.append(charPos);
		if (this.sourceFile != null) {
			builder.append(" in ");
			builder.append(this.sourceFile.getName());
		}
		return builder.toString();
	}

}
