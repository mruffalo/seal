/*
 *  ConcatenationRope.java
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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import util.ropes.Rope;

/**
 * A rope that represents the concatenation of two other ropes.
 * 
 * @author Amin Ahmad
 */
public final class ConcatenationRope extends AbstractRope
{
	private final Rope left;
	private final Rope right;
	private final byte depth;
	private final int length;
	
	/**
	 * Create a new concatenation rope from two ropes.
	 * 
	 * @param left
	 *            the first rope.
	 * @param right
	 *            the second rope.
	 */
	public ConcatenationRope(final Rope left, final Rope right)
	{
		this.left = left;
		this.right = right;
		this.depth = (byte) (Math.max(RopeUtilities.INSTANCE.depth(left),
			RopeUtilities.INSTANCE.depth(right)) + 1);
		this.length = left.length() + right.length();
	}
	
	@Override
	public char charAt(final int index)
	{
		if (index >= this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + index);
		
		return (index < this.left.length() ? this.left.charAt(index) : this.right.charAt(index
				- this.left.length()));
	}
	
	@Override
	public byte depth()
	{
		return this.depth;
	}
	
	@Override
	public CharSequence getForSequentialAccess()
	{
		return this.getForSequentialAccess(this);
	}
	
	/*
	 * Returns this object as a char sequence optimized for regular expression
	 * searches. <p> <emph>This method is public only to facilitate unit
	 * testing.</emph>
	 */
	private CharSequence getForSequentialAccess(final Rope rope)
	{
		return new CharSequence()
		{
			
			private final ConcatenationRopeIteratorImpl iterator = (ConcatenationRopeIteratorImpl) rope.iterator(0);
			
			@Override
			public char charAt(final int index)
			{
				if (index > this.iterator.getPos())
				{
					this.iterator.skip(index - this.iterator.getPos() - 1);
					try
					{
						final char c = this.iterator.next();
						return c;
					}
					catch (final IllegalArgumentException e)
					{
						System.out.println("Rope length is: " + rope.length() + " charAt is "
								+ index);
						throw e;
					}
				}
				else
				{ /* if (index <= lastIndex) */
					final int toMoveBack = this.iterator.getPos() - index + 1;
					if (this.iterator.canMoveBackwards(toMoveBack))
					{
						this.iterator.moveBackwards(toMoveBack);
						return this.iterator.next();
					}
					else
					{
						return rope.charAt(index);
					}
				}
			}
			
			@Override
			public int length()
			{
				return rope.length();
			}
			
			@Override
			public CharSequence subSequence(final int start, final int end)
			{
				return rope.subSequence(start, end);
			}
			
		};
	}
	
	/**
	 * Return the left-hand rope.
	 * 
	 * @return the left-hand rope.
	 */
	public Rope getLeft()
	{
		return this.left;
	}
	
	/**
	 * Return the right-hand rope.
	 * 
	 * @return the right-hand rope.
	 */
	public Rope getRight()
	{
		return this.right;
	}
	
	@Override
	public Iterator<Character> iterator(final int start)
	{
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		if (start >= this.left.length())
		{
			return this.right.iterator(start - this.left.length());
		}
		else
		{
			return new ConcatenationRopeIteratorImpl(this, start);
		}
	}
	
	@Override
	public int length()
	{
		return this.length;
	}
	
	@Override
	public Rope rebalance()
	{
		return RopeUtilities.INSTANCE.rebalance(this);
	}
	
	@Override
	public Rope reverse()
	{
		return RopeUtilities.INSTANCE.concatenate(this.getRight().reverse(),
			this.getLeft().reverse());
	}
	
	@Override
	public Iterator<Character> reverseIterator(final int start)
	{
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		if (start >= this.right.length())
		{
			return this.left.reverseIterator(start - this.right.length());
		}
		else
		{
			return new ConcatenationRopeReverseIteratorImpl(this, start);
		}
	}
	
	@Override
	public Rope subSequence(final int start, final int end)
	{
		if (start < 0 || end > this.length())
			throw new IllegalArgumentException("Illegal subsequence (" + start + "," + end + ")");
		if (start == 0 && end == this.length())
			return this;
		final int l = this.left.length();
		if (end <= l)
			return this.left.subSequence(start, end);
		if (start >= l)
			return this.right.subSequence(start - l, end - l);
		return RopeUtilities.INSTANCE.concatenate(this.left.subSequence(start, l),
			this.right.subSequence(0, end - l));
	}
	
	@Override
	public void write(final Writer out) throws IOException
	{
		this.left.write(out);
		this.right.write(out);
	}
	
	@Override
	public void write(final Writer out, final int offset, final int length) throws IOException
	{
		if (offset + length <= this.left.length())
		{
			this.left.write(out, offset, length);
		}
		else if (offset >= this.left.length())
		{
			this.right.write(out, offset - this.left.length(), length);
		}
		else
		{
			final int writeLeft = this.left.length() - offset;
			this.left.write(out, offset, writeLeft);
			this.right.write(out, 0, length - writeLeft);
		}
	}
}
