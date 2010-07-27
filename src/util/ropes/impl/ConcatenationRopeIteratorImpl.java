/*
 *  ConcatenationRopeIteratorImpl.java
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

import java.util.ArrayDeque;
import java.util.Iterator;
import util.ropes.Rope;

/**
 * A fast iterator for concatenated ropes. Iterating over a complex rope
 * structure is guaranteed to be O(n) so long as it is reasonably well-balanced.
 * Compare this to O(nlogn) for iteration using <code>charAt</code>.
 * 
 * @author aahmad
 */
public class ConcatenationRopeIteratorImpl implements Iterator<Character>
{
	private final ArrayDeque<Rope> toTraverse;
	private Rope currentRope;
	private int currentRopePos;
	private int skip;
	private int currentAbsolutePos;

	public ConcatenationRopeIteratorImpl(final Rope rope)
	{
		this(rope, 0);
	}

	public ConcatenationRopeIteratorImpl(final Rope rope, final int start)
	{
		this.toTraverse = new ArrayDeque<Rope>();
		this.toTraverse.push(rope);
		this.currentRope = null;
		this.initialize();

		if (start < 0 || start > rope.length())
		{
			throw new IllegalArgumentException("Rope index out of range: " + start);
		}
		this.moveForward(start);
	}

	public boolean canMoveBackwards(final int amount)
	{
		return (-1 <= (this.currentRopePos - amount));
	}

	public int getPos()
	{
		return this.currentAbsolutePos;
	}

	@Override
	public boolean hasNext()
	{
		return this.currentRopePos < this.currentRope.length() - 1 || !this.toTraverse.isEmpty();
	}

	/**
	 * Initialize the currentRope and currentRopePos fields.
	 */
	private void initialize()
	{
		while (!this.toTraverse.isEmpty())
		{
			this.currentRope = this.toTraverse.pop();
			if (this.currentRope instanceof ConcatenationRope)
			{
				this.toTraverse.push(((ConcatenationRope) this.currentRope).getRight());
				this.toTraverse.push(((ConcatenationRope) this.currentRope).getLeft());
			}
			else
			{
				break;
			}
		}
		if (this.currentRope == null)
			throw new IllegalArgumentException("No terminal ropes present.");
		this.currentRopePos = -1;
		this.currentAbsolutePos = -1;
	}

	public void moveBackwards(final int amount)
	{
		if (!this.canMoveBackwards(amount))
			throw new IllegalArgumentException("Unable to move backwards " + amount + ".");
		this.currentRopePos -= amount;
		this.currentAbsolutePos -= amount;
	}

	public void moveForward(final int amount)
	{
		this.currentAbsolutePos += amount;
		int remainingAmt = amount;
		while (remainingAmt != 0)
		{
			final int available = this.currentRope.length() - this.currentRopePos - 1;
			if (remainingAmt <= available)
			{
				this.currentRopePos += remainingAmt;
				return;
			}
			remainingAmt -= available;
			if (this.toTraverse.isEmpty())
			{
				this.currentAbsolutePos -= remainingAmt;
				throw new IllegalArgumentException("Unable to move forward " + amount
						+ ". Reached end of rope.");
			}

			while (!this.toTraverse.isEmpty())
			{
				this.currentRope = this.toTraverse.pop();
				if (this.currentRope instanceof ConcatenationRope)
				{
					this.toTraverse.push(((ConcatenationRope) this.currentRope).getRight());
					this.toTraverse.push(((ConcatenationRope) this.currentRope).getLeft());
				}
				else
				{
					this.currentRopePos = -1;
					break;
				}
			}
		}
	}

	@Override
	public Character next()
	{
		this.moveForward(1 + this.skip);
		this.skip = 0;
		return this.currentRope.charAt(this.currentRopePos);
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("Rope iterator is read-only.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ahmadsoft.ropes.impl.RopeIterators#skip(int)
	 */
	public void skip(final int skip)
	{
		this.skip = skip;
	}
}
