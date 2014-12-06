package swiprolog.parser;

import java.io.File;

import krTools.parser.SourceInfo;

public class SourceInfoObject implements SourceInfo {

	private File sourceFile = null;
	private final int lineNr;
	private final int charPos;
	private String msg = new String();

	public SourceInfoObject(int lineNr, int charPos) {
		this.lineNr = lineNr;
		this.charPos = charPos;
	}

	@Override
	public File getSource() {
		return this.sourceFile;
	}

	public void setSource(File file) {
		this.sourceFile = file;
	}

	@Override
	public int getLineNumber() {
		return this.lineNr;
	}

	@Override
	public int getCharacterPosition() {
		return this.charPos;
	}

	@Override
	public String getMessage() {
		return this.msg;
	}

	public void setMessage(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("line ");
		builder.append(this.lineNr);
		builder.append(", position ");
		builder.append(this.charPos);
		if (this.sourceFile != null) {
			builder.append(" in ");
			builder.append(this.sourceFile.getName());
		}
		return builder.toString();
	}

}
