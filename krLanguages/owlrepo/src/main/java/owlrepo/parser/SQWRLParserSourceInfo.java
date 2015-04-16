package owlrepo.parser;

import java.io.File;

import krTools.parser.SourceInfo;

public class SQWRLParserSourceInfo implements SourceInfo {
	
	private File file;
	private int line;
	private int charpos;
	private String msg;
	
	public SQWRLParserSourceInfo(){
		
	}
	public SQWRLParserSourceInfo(File f){
		this(f, 0, 0, "");
	}
	
	public SQWRLParserSourceInfo(File f, int line, int charpos, String msg){
		this.file = f;
		this.line = line;
		this.charpos = charpos;
		this.msg = msg;
	}

	public File getSource() {
		return this.file;
	}

	public int getLineNumber() {
		return this.line;
	}

	public int getCharacterPosition() {
		return this.charpos;
	}

	public String getMessage() {
		return this.msg;
	}

}
