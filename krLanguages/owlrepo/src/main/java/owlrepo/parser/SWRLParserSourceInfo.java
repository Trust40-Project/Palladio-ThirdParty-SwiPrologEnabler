package owlrepo.parser;

import java.io.File;

import krTools.parser.SourceInfo;

public class SWRLParserSourceInfo implements SourceInfo {
	private File sourceFile;
	private int lineNr;
	private int charPos;
	private String msg;

	public SWRLParserSourceInfo(File f) {
		this(f, 0, 0, "");
	}

	public SWRLParserSourceInfo(File f, int line, int charpos, String msg) {
		this.sourceFile = f;
		this.lineNr = line;
		this.charPos = charpos;
		this.msg = msg;
	}

	@Override
	public File getSource() {
		return this.sourceFile;
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

	@Override
	public int hashCode() {
		int hash = (31 * this.lineNr) << 16 + this.charPos;
		if (this.sourceFile != null) {
			hash += this.sourceFile.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (!(other instanceof SWRLParserSourceInfo)) {
			return false;
		}
		SWRLParserSourceInfo that = (SWRLParserSourceInfo) other;
		if (this.lineNr != that.lineNr) {
			return false;
		} else if (this.charPos != that.charPos) {
			return false;
		}
		if (this.sourceFile == null) {
			return that.sourceFile == null;
		} else {
			return this.sourceFile.getAbsoluteFile().equals(that.sourceFile.getAbsoluteFile());
		}
	}

	@Override
	public int getStartIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStopIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(SourceInfo o) {
		// ASSUMES the two sources being compared are in the same file.
		if (this.lineNr < o.getLineNumber()) {
			return -1;
		} else if (this.lineNr > o.getLineNumber()) {
			return 1;
		} else if (this.charPos < o.getCharacterPosition()) {
			return -1;
		} else if (this.charPos > o.getCharacterPosition()) {
			return 1;
		} else {
			return 0;
		}
	}
}
