package com.gabezter4.Vitals;

import java.util.Comparator;
import java.util.Map;

class ValueComparator
  implements Comparator
{
  Map base;

  public ValueComparator(Map base)
  {
    this.base = base; } 
  public int compare(Object a, Object b) { if (((Double)base.get(a)).doubleValue() < ((Double)base.get(b)).doubleValue()) return 1; if (((Double)base.get(a)).equals((Double)base.get(b))) return ((String)a).compareTo((String)b); return -1;
  }
}

/* Location:           C:\Users\gabez_000\Downloads\Vitals.jar
 * Qualified Name:     com.pzxc.Vitals.ValueComparator
 * JD-Core Version:    0.6.2
 */