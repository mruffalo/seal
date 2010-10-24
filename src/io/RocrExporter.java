package io;

import external.AlignmentResults;

public class RocrExporter
{
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = 0;

	public static void printPredictionsAndLabels(AlignmentResults r)
	{
		System.out.println("predictions,labels");
		for (int p : r.positives)
		{
			System.out.printf("%d,%d%n", p, POSITIVE);
		}
		for (int n : r.negatives)
		{
			System.out.printf("%d,%d%n", n, NEGATIVE);
		}
	}
}
