package swiprolog.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import krTools.errors.exceptions.ParserException;
import krTools.language.Update;
import krTools.parser.Parser;

import org.antlr.runtime.ANTLRReaderStream;
import org.junit.Test;

public class TestKRInterfaceParser {

	@Test
	public void testParseUpdate() throws IOException, ParserException {
		StringReader reader = new StringReader("on(a,b), on(b,c), on(c,table)");
		ANTLRReaderStream stream = new ANTLRReaderStream(reader);
		Parser parser = new KRInterfaceParser(stream);
		Update update = parser.parseUpdate(new SourceInfoObject(-1, -1));
		
		assertEquals(",/2", update.getSignature());
		
		assertEquals("on(a,b) , on(b,c) , on(c,table)", update.toString());
	}
	
	@Test
	public void testParseUpdate_2() throws IOException, ParserException {
		StringReader reader = new StringReader("zone(ID, Name, X, Y, Neighbours)");
		ANTLRReaderStream stream = new ANTLRReaderStream(reader);
		KRInterfaceParser parser = new KRInterfaceParser(stream);
		Update update = parser.parseUpdate(new SourceInfoObject(-1, -1));
		
		assertEquals("zone/5", update.getSignature());
		
		assertEquals("zone(ID,Name,X,Y,Neighbours)", update.toString());
	}

//	@Test
//	public void testParseDBFs() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testParseQuery() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testParseTerms() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testParseTerm() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testParseVar() {
//		fail("Not yet implemented");
//	}

}
