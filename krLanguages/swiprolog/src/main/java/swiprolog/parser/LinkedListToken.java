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

import org.antlr.runtime.ClassicToken;

public class LinkedListToken extends ClassicToken {
	private LinkedListToken prev = null;
	private LinkedListToken next = null;

	public LinkedListToken(int type, String text) {
		super(type, text);
	}

	public LinkedListToken getNext() {
		return this.next;
	}

	public void setNext(LinkedListToken next) {
		if (this == next) {
			throw new IllegalArgumentException("Token stream loop detected ("
					+ toString() + ")");
		}
		this.next = next;
		if (next != null) {
			next.prev = this;
		}
	}

	public LinkedListToken getPrev() {
		return this.prev;
	}

	public void setPrev(LinkedListToken prev) {
		if (this == prev) {
			throw new IllegalArgumentException("Token stream loop detected");
		}
		this.prev = prev;
		if (prev != null) {
			prev.next = this;
		}
	}

	public void afterInsert(LinkedListToken insert) {
		if (insert.getPrev() != null) {
			throw new IllegalArgumentException("afterInsert(" + insert
					+ ") : prev was not null");
		}
		if (insert.getNext() != null) {
			throw new IllegalArgumentException("afterInsert(" + insert
					+ ") : next was not null");
		}
		insert.next = this.next;
		insert.prev = this;
		if (this.next != null) {
			this.next.prev = insert;
		}
		this.next = insert;
	}

	public void beforeInsert(LinkedListToken insert) {
		if (insert.getPrev() != null) {
			throw new IllegalArgumentException("beforeInsert(" + insert
					+ ") : prev was not null");
		}
		if (insert.getNext() != null) {
			throw new IllegalArgumentException("beforeInsert(" + insert
					+ ") : next was not null");
		}
		insert.prev = this.prev;
		insert.next = this;
		if (this.prev != null) {
			this.prev.next = insert;
		}
		this.prev = insert;
	}

	public void delete() {
		if (this.prev != null) {
			this.prev.next = this.next;
		}
		if (this.next != null) {
			this.next.prev = this.prev;
		}
		this.next = this.prev = null;
	}
}
