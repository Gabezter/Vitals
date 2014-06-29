/*    */ package com.gabezter4.vitals;
/*    */ 
/*    */ import java.util.Comparator;
import java.util.Map;
/*    */ 
/*    */ 
/*    */ class ValueComparator
/*    */   implements Comparator
/*    */ {
/*    */   Map base;
/*    */ 
/*    */   public ValueComparator(Map base)
/*    */   {
/* 78 */     this.base = base; } 
/* 79 */   public int compare(Object a, Object b) { if (((Double)base.get(a)).doubleValue() < ((Double)base.get(b)).doubleValue()) return 1; if (((Double)base.get(a)).equals((Double)base.get(b))) return ((String)a).compareTo((String)b); return -1;
/*    */   }
/*    */ }