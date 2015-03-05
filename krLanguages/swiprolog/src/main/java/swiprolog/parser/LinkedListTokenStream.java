/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package swiprolog.parser;

/*
 * Copyright (c) 2006 David Holroyd
 */

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;

public class LinkedListTokenStream implements TokenStream {

	private TokenSource tokenSource;
	private LinkedListToken head;
	private LinkedListToken tail;

	/**
	 * Skip tokens on any channel but this one; this is how we skip
	 * whitespace...
	 */
	private int channel = Token.DEFAULT_CHANNEL;

	/** By default, track all incoming tokens */
	private boolean discardOffChannelTokens = false;

	/** Track the last mark() call result value for use in rewind(). */
	private LinkedListToken lastMarker;

	/**
	 * The current element in the tokens list (next token to consume). p==null
	 * indicates that the tokens list is empty
	 */
	private LinkedListToken p = null;

	public LinkedListTokenStream() {
	}

	public TokenSource getSource() {
		return this.tokenSource;
	}

	/**
	 * Reverses the stream 'count' tokens back, causing the tokens to be removed
	 * from the stream. Can be used to erase tokens which parser lookahead has
	 * summoned, but which represent input to be handled by an 'island grammar'.
	 */
	public void scrub(int count) {
		if (this.p == null) {
			this.p = this.tail;
		}
		for (; count > 0; count--) {
			this.p = this.p.getPrev();
		}
		this.p.setNext(null);
		this.tail = this.p;
		this.p = null;
	}

	/**
	 * The given TokenSource must produce tokens of type LinkedListToken
	 */
	public LinkedListTokenStream(TokenSource tokenSource) {
		this.tokenSource = tokenSource;
	}

	public LinkedListTokenStream(TokenSource tokenSource, int channel) {
		this(tokenSource);
		this.channel = channel;
	}

	/** Reset this token stream by setting its token source. */
	public void setTokenSource(TokenSource tokenSource) {
		this.tokenSource = tokenSource;
		this.p = null;
		this.channel = Token.DEFAULT_CHANNEL;
	}

	private LinkedListToken readNextToken() {
		LinkedListToken t = (LinkedListToken) this.tokenSource.nextToken();
		while (t != null && t.getType() != CharStream.EOF) {
			boolean discard = false;
			if (this.discardOffChannelTokens && t.getChannel() != this.channel) {
				discard = true;
			}
			if (!discard) {
				if (this.head == null && this.tail == null) {
					this.head = this.tail = t;
				} else {
					this.tail.setNext(t);
					t.setPrev(this.tail);
					this.tail = t;
				}
				break;
			}
			t = (LinkedListToken) this.tokenSource.nextToken();
		}
		if (t.getType() == CharStream.EOF) {
			// prevent ourselves from producing lots of EOF tokens
			// if the parser is 'pushy'; also, do the head/tail dance
			if (this.tail != null && this.tail.getType() == CharStream.EOF) {
				return this.tail;
			} else {
				if (this.head == null && this.tail == null) {
					this.head = this.tail = t;
				} else {
					this.tail.setNext(t);
					t.setPrev(this.tail);
					this.tail = t;
				}
			}
		}
		return skipOffTokenChannels(t);
	}

	/**
	 * Returns the token that follows the given token in the stream, or null if
	 * there's no token following
	 */
	private LinkedListToken succ(LinkedListToken tok) {
		LinkedListToken next = tok.getNext();
		if (next == null) {
			next = readNextToken();
		}
		return next;
	}

	/**
	 * Return absolute token i; ignore which channel the tokens are on; that is,
	 * count all tokens not just on-channel tokens.
	 */
	@Override
	public Token get(int i) {
		LinkedListToken tok = this.head;
		for (int c = 0; c < i; c++) {
			tok = succ(tok);
		}
		return tok;
	}

	@Override
	public TokenSource getTokenSource() {
		return this.tokenSource;
	}

	@Override
	public Token LT(int k) {
		if (this.p == null) {
			this.p = readNextToken();
		}
		if (k == 0) {
			return null;
		}
		if (k < 0) {
			return LB(-k);
		}
		LinkedListToken i = this.p;
		int n = 1;
		// find k good tokens
		while (n < k) {
			LinkedListToken next = succ(i);
			if (i == null) {
				return Token.EOF_TOKEN;
			}
			// skip off-channel tokens
			i = skipOffTokenChannels(next); // leave p on valid token
			n++;
		}
		if (i == null) {
			return Token.EOF_TOKEN;
		}
		return i;
	}

	/** Look backwards k tokens on-channel tokens */
	protected Token LB(int k) {
		if (this.p == null) {
			this.p = readNextToken();
		}
		if (k == 0) {
			return null;
		}

		LinkedListToken i = this.p;
		int n = 1;
		// find k good tokens looking backwards
		while (n <= k) {
			LinkedListToken next = i.getPrev();
			if (next == null) {
				return null;
			}
			// skip off-channel tokens
			i = skipOffTokenChannelsReverse(next); // leave p on valid token
			n++;
		}
		return i;
	}

	@Override
	public String toString(int start, int stop) {
		LinkedListToken tok = this.head;
		int i = 0;
		for (; i < start && tok != null; i++) {
			tok = succ(tok);
		}
		StringBuffer buf = new StringBuffer();
		for (; i <= stop && tok != null; i++) {
			buf.append(tok.getText());
			tok = succ(tok);
		}
		return buf.toString();
	}

	@Override
	public String toString(Token start, Token stop) {
		LinkedListToken tok = (LinkedListToken) start;
		StringBuffer buf = new StringBuffer();
		do {
			buf.append(tok.getText());
			tok = succ(tok);
		} while (tok != null && tok != stop);
		return buf.toString();
	}

	@Override
	public void consume() {
		do {
			this.p = this.p.getNext();
		} while (this.p != null && this.p.getChannel() != this.channel);
	}

	@Override
	public int index() {
		int i = 0;
		for (LinkedListToken tok = this.head; tok != this.p && tok != null; tok = tok
				.getNext()) {
			i++;
		}
		return i;
	}

	@Override
	public int LA(int i) {
		return LT(i).getType();
	}

	@Override
	public int mark() {
		// TODO: could store marks in a hash; does it make any difference?
		this.lastMarker = this.p;
		return index();
	}

	@Override
	public void release(int marker) {
		// no resources to release
	}

	@Override
	public void rewind() {
		this.p = this.lastMarker;
	}

	@Override
	public void rewind(int marker) {
		seek(marker);
	}

	@Override
	public void seek(int index) {
		this.p = this.head;
		for (int i = 0; i < index; i++) {
			this.p = succ(this.p);
		}
	}

	@Override
	public int size() {
		int s = 0;
		for (LinkedListToken tok = this.head; tok != null; tok = tok.getNext()) {
			s++;
		}
		return s;
	}

	public void discardOffChannelTokens(boolean discardOffChannelTokens) {
		this.discardOffChannelTokens = discardOffChannelTokens;
	}

	/**
	 * Given a starting token, return the first on-channel token.
	 */
	protected LinkedListToken skipOffTokenChannels(LinkedListToken i) {
		while (i != null && i.getChannel() != this.channel) {
			i = succ(i);
		}
		return i;
	}

	protected LinkedListToken skipOffTokenChannelsReverse(LinkedListToken i) {
		while (i != null && i.getChannel() != this.channel) {
			i = i.getPrev();
		}
		return i;
	}

	@Override
	public String getSourceName() {
		return this.tokenSource.getSourceName();
	}
}