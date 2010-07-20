/*
 *  SerializedRope.java
 *  Copyright (C) 2007 Amin Ahmad.
 *
 *  This file is part of Java Ropes.
 *
 *  Java Ropes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Java Ropes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Java Ropes.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Amin Ahmad can be contacted at amin.ahmad@gmail.com or on the web at
 *  www.ahmadsoft.org.
 */
package util.ropes.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import util.ropes.Rope;

/**
 * An instance of this class replaces ropes during the serialization process.
 * This class serializes into string form, and deserializes into a
 * <code>FlatRope</code>. <code>readResolve</code> returns the flat rope.
 * <p>
 * The purpose of this class is to provide a performant serialization mechanism
 * for Ropes. The ideal serial form of a rope is as a String, regardless of the
 * particular in-memory representation.
 * 
 * @author Amin Ahmad
 */
final class SerializedRope implements Externalizable
{
	/**
	 * The rope.
	 */
	private Rope rope;
	
	/**
	 * Public no-arg constructor for use during serialization.
	 */
	public SerializedRope()
	{
	}
	
	/**
	 * Create a new concatenation rope from two ropes.
	 * 
	 * @param left
	 *            the first rope.
	 * @param right
	 *            the second rope.
	 */
	public SerializedRope(final Rope rope)
	{
		this.rope = rope;
	}
	
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		// Read the UTF string and build a rope from it. This should
		// result in a FlatRope.
		this.rope = Rope.BUILDER.build(in.readUTF());
	}
	
	private Object readResolve() throws ObjectStreamException
	{
		// Substitute an instance of this class with the deserialized
		// rope.
		return this.rope;
	}
	
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException
	{
		// Evaluate the rope (toString()) and write as UTF. Unfortunately,
		// this requires O(n) temporarily-allocated heap space.
		out.writeUTF(this.rope.toString());
	}
}
