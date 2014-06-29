/*    */ package com.gabezter4.vitals;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Iterator;
/*    */ import java.util.Map;
/*    */ import java.util.Set;
/*    */ import org.bukkit.enchantments.Enchantment;
/*    */ import org.bukkit.inventory.ItemStack;
/*    */ import org.bukkit.material.MaterialData;
/*    */ 
/*    */ class CardboardBox
/*    */ {
/*    */   private final int amount;
/*    */   private final int type;
/*    */   private final short damage;
/*    */   private final byte data;
/*    */   private final String enchants;
/*    */ 
/*    */   public CardboardBox(ItemStack item)
/*    */   {
/* 83 */     amount = item.getAmount(); type = item.getTypeId(); damage = item.getDurability(); data = item.getData().getData(); String es = "";
/* 84 */     Map ee = item.getEnchantments();
/*    */     Enchantment e;
/* 84 */     for (Iterator localIterator = ee.keySet().iterator(); localIterator.hasNext(); es = es + "-" + e.getId() + "-" + ee.get(e)) e = (Enchantment)localIterator.next(); enchants = (es.length() > 0 ? es.substring(1) : ""); } 
/* 85 */   public CardboardBox(String s) { String[] ss = s.split("_"); amount = Integer.parseInt(ss[0]); type = Integer.parseInt(ss[1]); damage = ((short)Integer.parseInt(ss[2])); data = ((byte)Integer.parseInt(ss[3])); enchants = (ss.length >= 5 ? ss[4] : ""); } 
/* 86 */   public String toString() { return amount + "_" + type + "_" + damage + "_" + data + (enchants.length() > 0 ? "_" + enchants : ""); } 
/* 87 */   public ItemStack unbox() { ItemStack is = new ItemStack(type, amount, damage, Byte.valueOf(data)); String[] es = enchants.split("-"); HashMap ee = new HashMap();
/* 88 */     if (enchants.length() > 0) for (int i = 0; i < es.length; i += 2) ee.put(Enchantment.getById(Integer.parseInt(es[i])), Integer.valueOf(Integer.parseInt(es[(i + 1)]))); 
/* 88 */     is.addUnsafeEnchantments(ee); return is;
/*    */   }
/*    */ }