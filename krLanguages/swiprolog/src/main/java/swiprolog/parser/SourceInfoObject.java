package swiprolog.parser;

import java.io.File;

import krTools.parser.SourceInfo;

public class SourceInfoObject implements SourceInfo {

	private final File sourceFile;
	private final int lineNr;
	private final int charPos;
	private final int start;
	private final int end;
	private final String msg = new String();

	public SourceInfoObject(File file, int lineNr, int charPos, int start, int end) {
		sourceFile = file;
		this.lineNr = lineNr;
		this.charPos = charPos;
		this.start = start;
		this.end = end;
	}

	@Override
	public File getSource() {
		return sourceFile;
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
	public int getStartIndex() {
		return start;
	}

	@Override
	public int getStopIndex() {
		return end;
	}

	@Override
	public String getMessage() {
		return msg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("line ");
		builder.append(lineNr);
		builder.append(", position ");
		builder.append(charPos);
		if (sourceFile != null) {
			builder.append(" in ");
			builder.append(sourceFile.getName());
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		int hash = (31 * lineNr) << 16 + charPos;
		if (sourceFile != null) {
			hash += sourceFile.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (!(other instanceof SourceInfoObject)) {
			return false;
		}
		SourceInfoObject that = (SourceInfoObject) other;
		if (lineNr != that.lineNr) {
			return false;
		} else if (charPos != that.charPos) {
			return false;
		}
		if (sourceFile == null) {
			return that.sourceFile == null;
		} else {
			return sourceFile.getAbsoluteFile().equals(that.sourceFile.getAbsoluteFile());
		}
	}
}
