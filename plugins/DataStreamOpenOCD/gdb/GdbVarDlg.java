/*     */ package com.st.stmstudio.gdb;
/*     */ 
/*     */ import com.st.stmstudio.main.AbstractVariable;
/*     */ import com.st.stmstudio.main.AcqVariablesTable;
/*     */ import com.st.stmstudio.main.ColorManager;
/*     */ import com.st.stmstudio.main.ExprVariablesTable;
/*     */ import com.st.stmstudio.main.MainDlg;
/*     */ import com.st.stmstudio.main.MainDlg.vartables;
/*     */ import com.st.stmstudio.main.RelativePath;
/*     */ import com.st.stmstudio.main.TableDynamicModel;
/*     */ import com.st.stmstudio.main.WriteVariablesTable;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Image;
/*     */ import java.awt.Point;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.KeyEvent;
/*     */ import java.awt.event.KeyListener;
/*     */ import java.io.File;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.Box;
/*     */ import javax.swing.BoxLayout;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JRadioButton;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTable;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ import javax.swing.filechooser.FileFilter;
/*     */ import javax.swing.table.TableColumn;
/*     */ import javax.swing.table.TableColumnModel;
/*     */ import javax.swing.table.TableModel;
/*     */ 
/*     */ public class GdbVarDlg extends JDialog
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  42 */   private static int USER_SETTINGS_GDBVAR_VER = 1;
/*  43 */   private static String m_lastExecutable = "";
/*  44 */   private static String m_execFilenameFromSettings = "";
/*  45 */   private static boolean m_bStoreRelative = true;
/*  46 */   private static boolean m_bStoreRelativeFromSettings = true;
/*     */   private static MainDlg m_parent;
/*  50 */   private static ArrayList<String> m_symbolList = new ArrayList();
/*     */ 
/*  52 */   private ArrayList<GdbVar> m_gdbVarList = new ArrayList();
/*     */ 
/*  54 */   private JButton m_browseFile = null;
/*  55 */   private JTextField m_execFilename = null;
/*  56 */   private JCheckBox m_ExpandTableElements = null;
/*  57 */   private JCheckBox m_StoreRelativePath = null;
/*     */   private JTable m_varTable;
/*     */   private TableDynamicModel m_variableTableModel;
/*     */   private JPanel m_varHeaderPanel;
/*     */   private JTextField m_filterString;
/*     */   private JCheckBox m_filterMatchCase;
/*  64 */   private int m_BtnHeight = 20;
/*  65 */   private int m_BtnWidth = 20;
/*  66 */   private int m_DlgWidth = 620;
/*  67 */   private int m_DlgHeigth = 450;
/*  68 */   private JButton m_selectAll = null;
/*  69 */   private JButton m_unselectAll = null;
/*  70 */   private JButton m_importVar = null;
/*  71 */   private JButton m_cancel = null;
/*  72 */   private JButton m_importScaledVar = null;
/*     */   private JLabel LinearLabel;
/*     */   private JLabel ConstValALabel;
/*     */   private JLabel ConstValBLabel;
/*     */   private JLabel ConstExprALabel;
/*     */   private JLabel ConstExprBLabel;
/*     */   private JComboBox m_constExprA;
/*     */   private JComboBox m_constExprB;
/*  81 */   private String[] variableColumns = { "File", "Name", "Address", "Type" };
/*     */   private JComboBox m_ImportTo;
/*     */   private JTextField ConstValA;
/*     */   private JTextField ConstValB;
/*     */   private JRadioButton AandB_Const_Button;
/*     */   private JRadioButton AandB_Expr_Button;
/*  89 */   private MainDlg.vartables defaultDestinationTable = MainDlg.vartables.UNDEF_VAR_TABLE;
/*     */   private boolean m_bBuildSymbolListOnly;
/*     */   private boolean m_bUserConfigDirtyOpening;
/*     */ 
/*     */   public GdbVarDlg(MainDlg paramMainDlg)
/*     */   {
/*  97 */     this(paramMainDlg, false);
/*     */   }
/*     */ 
/*     */   public GdbVarDlg(MainDlg paramMainDlg, boolean paramBoolean) {
/* 101 */     super(paramMainDlg, true);
/*     */ 
/* 103 */     m_parent = paramMainDlg;
/* 104 */     this.m_bUserConfigDirtyOpening = m_parent.isDirty();
/*     */ 
/* 106 */     this.m_bBuildSymbolListOnly = paramBoolean;
/*     */ 
/* 108 */     m_symbolList.clear();
/*     */ 
/* 110 */     this.m_filterString = new JTextField("");
/*     */ 
/* 112 */     setTitle("Import variables from executable");
/* 113 */     Image localImage = new ImageIcon("icons/application_get.png").getImage();
/* 114 */     setIconImage(localImage);
/* 115 */     setLocation(paramMainDlg.getLocation().x + 100, paramMainDlg.getLocation().y + 50);
/* 116 */     setMinimumSize(new Dimension(this.m_DlgWidth, this.m_DlgHeigth));
/*     */ 
/* 118 */     setLayout(new BorderLayout());
/* 119 */     setResizable(true);
/*     */ 
/* 122 */     JPanel localJPanel1 = new JPanel();
/*     */ 
/* 124 */     localJPanel1.setLayout(new FlowLayout());
/*     */ 
/* 127 */     JPanel localJPanel2 = new JPanel();
/* 128 */     localJPanel2.setBorder(BorderFactory.createTitledBorder("File selection"));
/* 129 */     localJPanel2.setLayout(new BorderLayout());
/*     */ 
/* 131 */     this.m_execFilename = new JTextField(m_lastExecutable);
/* 132 */     this.m_execFilename.setPreferredSize(new Dimension(this.m_DlgWidth - this.m_BtnWidth - 30, this.m_BtnHeight));
/*     */ 
/* 134 */     this.m_execFilename.setEditable(false);
/*     */ 
/* 136 */     this.m_browseFile = new JButton();
/* 137 */     this.m_browseFile.setText("...");
/* 138 */     this.m_browseFile.setPreferredSize(new Dimension(this.m_BtnWidth, this.m_BtnHeight));
/* 139 */     this.m_browseFile.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 142 */         JFileChooser localJFileChooser = new JFileChooser();
/* 143 */         GdbVarDlg.ElfExtension localElfExtension = new GdbVarDlg.ElfExtension(GdbVarDlg.this);
/*     */         File localFile1;
/* 145 */         if (GdbVarDlg.this.m_execFilename.getText().isEmpty())
/*     */         {
/* 147 */           localFile1 = new File(GdbVarDlg.m_parent.getUserDataPath());
/*     */         }
/* 149 */         else localFile1 = new File(GdbVarDlg.this.m_execFilename.getText());
/*     */ 
/* 151 */         localJFileChooser.setSelectedFile(localFile1);
/* 152 */         localJFileChooser.addChoosableFileFilter(localElfExtension);
/* 153 */         localJFileChooser.setFileFilter(localElfExtension);
/* 154 */         int i = localJFileChooser.showDialog(GdbVarDlg.this, "Select executable file");
/* 155 */         if (i == 0) {
/* 156 */           File localFile2 = localJFileChooser.getSelectedFile();
/* 157 */           GdbVarDlg.this.m_execFilename.setText(localFile2.getPath());
/* 158 */           GdbVarDlg.access$202(GdbVarDlg.this.m_execFilename.getText());
/* 159 */           GdbVarDlg.this.setUserConfigDirty();
/* 160 */           GdbVarDlg.this.ExtractVariables(GdbVarDlg.this.m_ExpandTableElements.isSelected());
/*     */         }
/*     */       }
/*     */     });
/* 165 */     this.m_ExpandTableElements = new JCheckBox("Expand table elements (this may take several seconds more)");
/* 166 */     this.m_ExpandTableElements.setSelected(false);
/* 167 */     this.m_ExpandTableElements.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent)
/*     */       {
/* 171 */         GdbVarDlg.this.ExtractVariables(GdbVarDlg.this.m_ExpandTableElements.isSelected());
/*     */       }
/*     */     });
/* 174 */     this.m_StoreRelativePath = new JCheckBox("Store executable path relatively to the user settings file");
/* 175 */     this.m_StoreRelativePath.setSelected(m_bStoreRelative);
/* 176 */     this.m_StoreRelativePath.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 179 */         GdbVarDlg.access$602(GdbVarDlg.this.m_StoreRelativePath.isSelected());
/* 180 */         GdbVarDlg.this.setUserConfigDirty();
/*     */       }
/*     */     });
/* 183 */     JPanel localJPanel3 = new JPanel();
/* 184 */     localJPanel3.setLayout(new BoxLayout(localJPanel3, 1));
/* 185 */     localJPanel3.add(this.m_StoreRelativePath);
/* 186 */     localJPanel3.add(this.m_ExpandTableElements);
/*     */ 
/* 188 */     localJPanel2.add(new JLabel("Executable file"), "North");
/* 189 */     localJPanel2.add(this.m_execFilename, "Center");
/* 190 */     localJPanel2.add(this.m_browseFile, "East");
/* 191 */     localJPanel2.add(localJPanel3, "South");
/* 192 */     localJPanel1.add(localJPanel2);
/*     */ 
/* 196 */     JPanel localJPanel4 = new JPanel();
/* 197 */     if (!this.m_bBuildSymbolListOnly) {
/* 198 */       localJPanel4.setBorder(BorderFactory.createTitledBorder("Selection"));
/*     */     }
/* 200 */     localJPanel4.setLayout(new BoxLayout(localJPanel4, 1));
/* 201 */     this.m_selectAll = new JButton();
/* 202 */     this.m_selectAll.setText("Select all");
/* 203 */     this.m_selectAll.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 206 */         GdbVarDlg.this.m_varTable.selectAll();
/*     */       }
/*     */     });
/* 211 */     this.m_selectAll.setEnabled(false);
/*     */ 
/* 213 */     this.m_unselectAll = new JButton();
/* 214 */     this.m_unselectAll.setText("Unselect all");
/* 215 */     this.m_unselectAll.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 218 */         GdbVarDlg.this.m_varTable.clearSelection();
/*     */       }
/*     */     });
/* 223 */     this.m_unselectAll.setEnabled(false);
/*     */ 
/* 225 */     this.m_importVar = new JButton();
/* 226 */     this.m_importVar.setText("Import");
/* 227 */     this.m_importVar.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 230 */         int[] arrayOfInt = GdbVarDlg.this.m_varTable.getSelectedRows();
/* 231 */         for (int i = 0; i < arrayOfInt.length; i++) {
/* 232 */           int j = GdbVarDlg.this.m_varTable.convertRowIndexToModel(arrayOfInt[i]);
/* 233 */           String str1 = (String)GdbVarDlg.this.m_variableTableModel.getValueAt(j, 1);
/* 234 */           String str2 = (String)GdbVarDlg.this.m_variableTableModel.getValueAt(j, 2);
/* 235 */           String str3 = (String)GdbVarDlg.this.m_variableTableModel.getValueAt(j, 3);
/* 236 */           long l = Long.decode(str2).longValue();
/* 237 */           if (GdbVarDlg.this.m_ImportTo.getSelectedIndex() == 0)
/*     */           {
/* 239 */             GdbVarDlg.m_parent.getAcqVarTable().addVariable(str1, l, str3, false);
/*     */           }
/* 241 */           else if (GdbVarDlg.this.m_ImportTo.getSelectedIndex() == 1)
/*     */           {
/* 243 */             GdbVarDlg.m_parent.getWriteVarTable().addVariable(str1, l, str3, null);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 250 */         GdbVarDlg.this.m_varTable.clearSelection();
/*     */       }
/*     */     });
/* 255 */     this.m_importVar.setEnabled(false);
/*     */ 
/* 257 */     this.m_cancel = new JButton("Close");
/* 258 */     this.m_cancel.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 261 */         GdbVarDlg.this.dispose();
/*     */       }
/*     */     });
/* 264 */     this.m_importScaledVar = new JButton();
/* 265 */     this.m_importScaledVar.setText("Import scaled variable in expression");
/* 266 */     this.m_importScaledVar.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 269 */         int i = 1;
/* 270 */         double d1 = 0.0D;
/* 271 */         double d2 = 0.0D;
/*     */ 
/* 273 */         if (GdbVarDlg.this.AandB_Const_Button.isSelected()) {
/*     */           try {
/* 275 */             d1 = Double.parseDouble(GdbVarDlg.this.ConstValA.getText());
/*     */           } catch (NumberFormatException localNumberFormatException1) {
/* 277 */             JOptionPane.showMessageDialog(null, "A=" + GdbVarDlg.this.ConstValA.getText() + ": Not a constant (need double)");
/* 278 */             i = 0;
/*     */           }
/* 280 */           if ((i != 0) && 
/* 281 */             (!GdbVarDlg.this.ConstValB.getText().isEmpty())) {
/*     */             try {
/* 283 */               d2 = Double.parseDouble(GdbVarDlg.this.ConstValB.getText());
/*     */             } catch (NumberFormatException localNumberFormatException2) {
/* 285 */               JOptionPane.showMessageDialog(null, "B=" + GdbVarDlg.this.ConstValB.getText() + ": Not a constant (need double)");
/* 286 */               i = 0;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 291 */         if (i != 0) {
/* 292 */           int[] arrayOfInt = GdbVarDlg.this.m_varTable.getSelectedRows();
/* 293 */           for (int j = 0; j < arrayOfInt.length; j++) { int k = GdbVarDlg.this.m_varTable.convertRowIndexToModel(arrayOfInt[j]);
/* 295 */             String str1 = (String)GdbVarDlg.this.m_variableTableModel.getValueAt(k, 1);
/* 296 */             String str2 = (String)GdbVarDlg.this.m_variableTableModel.getValueAt(k, 2);
/* 297 */             String str3 = (String)GdbVarDlg.this.m_variableTableModel.getValueAt(k, 3);
/* 298 */             short s = AbstractVariable.getType((String)GdbVarDlg.this.m_variableTableModel.getValueAt(k, 3));
/* 299 */             long l = Long.decode(str2).longValue();
/*     */ 
/* 301 */             GdbVarDlg.m_parent.getAcqVarTable().addVariable(str1, l, str3, false);
/*     */ 
/* 305 */             int m = 0;
/*     */             String str4;
/*     */             do { str4 = "newExpr_" + m;
/* 308 */               m++; }
/* 309 */             while (GdbVarDlg.m_parent.getVarByName(str4) != null);
/*     */             String str5;
/* 311 */             if (GdbVarDlg.this.AandB_Const_Button.isSelected()) {
/* 312 */               if (d2 != 0.0D) {
/* 313 */                 if (d2 < 0.0D) {
/* 314 */                   str5 = d1 + "*" + str1 + d2;
/*     */                 }
/*     */                 else {
/* 317 */                   str5 = d1 + "*" + str1 + "+" + d2;
/*     */                 }
/*     */               }
/*     */               else
/*     */               {
/* 322 */                 str5 = d1 + "*" + str1;
/*     */               }
/* 324 */               GdbVarDlg.m_parent.getExprVarTable().addVariable(str4, str5, s, ColorManager.getDefaultColor(), false);
/*     */             }
/* 327 */             else if (GdbVarDlg.this.m_constExprA.getSelectedItem() != null) {
/* 328 */               m = 0;
/*     */               do {
/* 330 */                 str4 = "newExpr_" + m;
/* 331 */                 m++;
/* 332 */               }while (GdbVarDlg.m_parent.getVarByName(str4) != null);
/* 333 */               if (GdbVarDlg.this.m_constExprB.getSelectedItem() != "") {
/* 334 */                 str5 = GdbVarDlg.this.m_constExprA.getSelectedItem() + "*" + str1 + "+" + GdbVarDlg.this.m_constExprB.getSelectedItem();
/*     */               }
/*     */               else
/*     */               {
/* 338 */                 str5 = GdbVarDlg.this.m_constExprA.getSelectedItem() + "*" + str1;
/*     */               }
/* 340 */               GdbVarDlg.m_parent.getExprVarTable().addVariable(str4, str5, s, ColorManager.getDefaultColor(), false);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 348 */           GdbVarDlg.this.m_varTable.clearSelection();
/*     */         }
/*     */       }
/*     */     });
/* 354 */     this.m_unselectAll.setMaximumSize(this.m_importScaledVar.getMaximumSize());
/* 355 */     this.m_selectAll.setMaximumSize(this.m_importScaledVar.getMaximumSize());
/* 356 */     this.m_importVar.setMaximumSize(this.m_importScaledVar.getMaximumSize());
/* 357 */     this.m_cancel.setMaximumSize(this.m_importScaledVar.getMaximumSize());
/*     */ 
/* 361 */     this.m_importScaledVar.setEnabled(false);
/* 362 */     this.m_importScaledVar.setMaximumSize(this.m_importScaledVar.getMaximumSize());
/* 363 */     this.LinearLabel = new JLabel("Linear expression A*variable + B:");
/* 364 */     this.LinearLabel.setEnabled(false);
/*     */ 
/* 366 */     this.AandB_Const_Button = new JRadioButton("Import with A and B as constants");
/* 367 */     this.AandB_Const_Button.setSelected(true);
/* 368 */     this.AandB_Const_Button.setEnabled(false);
/* 369 */     this.AandB_Expr_Button = new JRadioButton("Import with A and B as expressions");
/* 370 */     this.AandB_Expr_Button.setSelected(false);
/* 371 */     this.AandB_Expr_Button.setEnabled(false);
/* 372 */     ButtonGroup localButtonGroup = new ButtonGroup();
/* 373 */     localButtonGroup.add(this.AandB_Const_Button);
/* 374 */     localButtonGroup.add(this.AandB_Expr_Button);
/*     */ 
/* 376 */     this.ConstValALabel = new JLabel("Enter A (double): ");
/* 377 */     this.ConstValBLabel = new JLabel("Enter B (double): ");
/* 378 */     this.ConstValALabel.setEnabled(false);
/* 379 */     this.ConstValBLabel.setEnabled(false);
/* 380 */     this.ConstValA = new JTextField("");
/* 381 */     this.ConstValB = new JTextField("");
/* 382 */     this.ConstValA.setMaximumSize(new Dimension(100, this.m_BtnHeight));
/* 383 */     this.ConstValB.setMaximumSize(new Dimension(100, this.m_BtnHeight));
/* 384 */     this.ConstValA.setEnabled(false);
/* 385 */     this.ConstValB.setEnabled(false);
/* 386 */     this.ConstExprALabel = new JLabel("Choose A from constant expressions:");
/* 387 */     this.ConstExprALabel.setEnabled(false);
/* 388 */     String[] arrayOfString1 = { "" };
/* 389 */     this.m_constExprA = new JComboBox(arrayOfString1);
/* 390 */     int i = m_parent.getExprVarTable().getListOfConstantExpressions().size();
/*     */ 
/* 392 */     List localList = m_parent.getExprVarTable().getListOfConstantExpressions();
/* 393 */     this.m_constExprA.removeAllItems();
/*     */ 
/* 395 */     for (int j = 0; j < i; j++) {
/* 396 */       this.m_constExprA.addItem(localList.get(j));
/*     */     }
/* 398 */     this.m_constExprA.setMaximumSize(this.m_importScaledVar.getMaximumSize());
/* 399 */     this.m_constExprA.setAlignmentX(0.0F);
/* 400 */     this.m_constExprA.setEnabled(false);
/* 401 */     this.ConstExprBLabel = new JLabel("Choose B from constant Expressions:");
/* 402 */     this.ConstExprBLabel.setEnabled(false);
/* 403 */     String[] arrayOfString2 = { "" };
/* 404 */     this.m_constExprB = new JComboBox(arrayOfString2);
/*     */ 
/* 407 */     for (int k = 0; k < i; k++) {
/* 408 */       this.m_constExprB.addItem(localList.get(k));
/*     */     }
/* 410 */     this.m_constExprB.setMaximumSize(new Dimension(250, this.m_BtnHeight));
/* 411 */     this.m_constExprB.setAlignmentX(0.0F);
/* 412 */     this.m_constExprB.setEnabled(false);
/*     */ 
/* 414 */     if (!this.m_bBuildSymbolListOnly) {
/* 415 */       localJPanel4.add(this.m_selectAll);
/* 416 */       localJPanel4.add(this.m_unselectAll);
/* 417 */       localJPanel4.add(Box.createRigidArea(new Dimension(0, 10)));
/* 418 */       localJPanel4.add(this.m_importVar);
/* 419 */       localJPanel4.add(Box.createRigidArea(new Dimension(0, 30)));
/*     */ 
/* 422 */       localJPanel5 = new JPanel();
/* 423 */       localJPanel5.setLayout(new BoxLayout(localJPanel5, 1));
/* 424 */       localJPanel5.setBorder(BorderFactory.createTitledBorder(""));
/* 425 */       localJPanel5.add(this.m_importScaledVar);
/* 426 */       localJPanel5.add(Box.createRigidArea(new Dimension(0, 10)));
/* 427 */       localJPanel5.add(this.LinearLabel);
/* 428 */       localJPanel5.add(this.AandB_Const_Button);
/* 429 */       localJPanel5.add(this.AandB_Expr_Button);
/*     */ 
/* 431 */       localObject = new JPanel();
/* 432 */       ((JPanel)localObject).setLayout(new BoxLayout((Container)localObject, 0));
/* 433 */       ((JPanel)localObject).setAlignmentX(0.0F);
/* 434 */       ((JPanel)localObject).add(this.ConstValALabel);
/* 435 */       ((JPanel)localObject).add(this.ConstValA);
/*     */ 
/* 437 */       JPanel localJPanel6 = new JPanel();
/* 438 */       localJPanel6.setLayout(new BoxLayout(localJPanel6, 0));
/* 439 */       localJPanel6.setAlignmentX(0.0F);
/* 440 */       localJPanel6.add(this.ConstValBLabel);
/* 441 */       localJPanel6.add(this.ConstValB);
/*     */ 
/* 443 */       localJPanel5.add(Box.createRigidArea(new Dimension(0, 5)));
/* 444 */       localJPanel5.add((Component)localObject);
/* 445 */       localJPanel5.add(Box.createRigidArea(new Dimension(0, 5)));
/* 446 */       localJPanel5.add(localJPanel6);
/*     */ 
/* 448 */       localJPanel5.add(Box.createRigidArea(new Dimension(0, 5)));
/* 449 */       localJPanel5.add(this.ConstExprALabel);
/* 450 */       localJPanel5.add(this.m_constExprA);
/* 451 */       localJPanel5.add(Box.createRigidArea(new Dimension(0, 5)));
/* 452 */       localJPanel5.add(this.ConstExprBLabel);
/* 453 */       localJPanel5.add(this.m_constExprB);
/* 454 */       localJPanel5.add(Box.createRigidArea(new Dimension(0, 5)));
/*     */ 
/* 456 */       localJPanel4.add(localJPanel5);
/*     */     }
/* 458 */     localJPanel4.add(Box.createRigidArea(new Dimension(0, 20)));
/*     */ 
/* 460 */     localJPanel4.add(this.m_cancel);
/* 461 */     localJPanel4.add(Box.createRigidArea(new Dimension(0, 5)));
/*     */ 
/* 464 */     JPanel localJPanel5 = new JPanel();
/* 465 */     localJPanel5.setLayout(new BorderLayout());
/* 466 */     localJPanel5.setBorder(BorderFactory.createTitledBorder("Variables"));
/* 467 */     this.m_variableTableModel = new TableDynamicModel(this.variableColumns);
/* 468 */     this.m_varTable = new JTable(this.m_variableTableModel)
/*     */     {
/*     */       private static final long serialVersionUID = 1L;
/*     */ 
/*     */       public boolean isCellEditable(int paramAnonymousInt1, int paramAnonymousInt2)
/*     */       {
/* 474 */         return false;
/*     */       }
/*     */     };
/* 477 */     this.m_varTable.setAutoCreateRowSorter(true);
/*     */ 
/* 479 */     if (this.m_bBuildSymbolListOnly == true)
/*     */     {
/* 481 */       this.m_varTable.setEnabled(false);
/*     */     }
/*     */ 
/* 484 */     Object localObject = this.m_varTable.getSelectionModel();
/* 485 */     ((ListSelectionModel)localObject).addListSelectionListener(new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent paramAnonymousListSelectionEvent)
/*     */       {
/* 490 */         if (GdbVarDlg.this.m_varTable.getSelectedRowCount() > 0) {
/* 491 */           GdbVarDlg.this.m_importVar.setEnabled(true);
/* 492 */           GdbVarDlg.this.m_unselectAll.setEnabled(true);
/* 493 */           GdbVarDlg.this.m_importScaledVar.setEnabled(true);
/* 494 */           GdbVarDlg.this.AandB_Const_Button.setEnabled(true);
/* 495 */           GdbVarDlg.this.AandB_Expr_Button.setEnabled(true);
/* 496 */           GdbVarDlg.this.LinearLabel.setEnabled(true);
/* 497 */           GdbVarDlg.this.ConstValALabel.setEnabled(true);
/* 498 */           GdbVarDlg.this.ConstValBLabel.setEnabled(true);
/* 499 */           GdbVarDlg.this.ConstExprALabel.setEnabled(true);
/* 500 */           GdbVarDlg.this.ConstExprBLabel.setEnabled(true);
/* 501 */           if (GdbVarDlg.this.AandB_Const_Button.isSelected()) {
/* 502 */             GdbVarDlg.this.ConstValA.setEnabled(true);
/* 503 */             GdbVarDlg.this.ConstValB.setEnabled(true);
/* 504 */             GdbVarDlg.this.m_constExprA.setEnabled(false);
/* 505 */             GdbVarDlg.this.m_constExprB.setEnabled(false);
/*     */           }
/*     */           else {
/* 508 */             GdbVarDlg.this.ConstValA.setEnabled(false);
/* 509 */             GdbVarDlg.this.ConstValB.setEnabled(false);
/* 510 */             GdbVarDlg.this.m_constExprA.setEnabled(true);
/* 511 */             GdbVarDlg.this.m_constExprB.setEnabled(true);
/*     */           }
/*     */         }
/*     */         else {
/* 515 */           GdbVarDlg.this.m_importVar.setEnabled(false);
/* 516 */           GdbVarDlg.this.m_unselectAll.setEnabled(false);
/* 517 */           GdbVarDlg.this.m_importScaledVar.setEnabled(false);
/* 518 */           GdbVarDlg.this.LinearLabel.setEnabled(false);
/* 519 */           GdbVarDlg.this.ConstValALabel.setEnabled(false);
/* 520 */           GdbVarDlg.this.ConstValBLabel.setEnabled(false);
/* 521 */           GdbVarDlg.this.ConstExprALabel.setEnabled(false);
/* 522 */           GdbVarDlg.this.ConstExprBLabel.setEnabled(false);
/* 523 */           GdbVarDlg.this.AandB_Const_Button.setEnabled(false);
/* 524 */           GdbVarDlg.this.AandB_Expr_Button.setEnabled(false);
/* 525 */           GdbVarDlg.this.ConstValA.setEnabled(false);
/* 526 */           GdbVarDlg.this.ConstValB.setEnabled(false);
/* 527 */           GdbVarDlg.this.m_constExprA.setEnabled(false);
/* 528 */           GdbVarDlg.this.m_constExprB.setEnabled(false);
/*     */         }
/* 530 */         if ((GdbVarDlg.this.m_varTable.getRowCount() > 0) && (GdbVarDlg.this.m_varTable.getSelectedRowCount() < GdbVarDlg.this.m_varTable.getRowCount()))
/*     */         {
/* 532 */           GdbVarDlg.this.m_selectAll.setEnabled(true);
/*     */         }
/*     */         else
/* 535 */           GdbVarDlg.this.m_selectAll.setEnabled(false);
/*     */       }
/*     */     });
/* 541 */     this.AandB_Const_Button.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 544 */         if (GdbVarDlg.this.m_varTable.getSelectedRowCount() > 0)
/* 545 */           if (GdbVarDlg.this.AandB_Const_Button.isSelected()) {
/* 546 */             GdbVarDlg.this.ConstValA.setEnabled(true);
/* 547 */             GdbVarDlg.this.ConstValB.setEnabled(true);
/* 548 */             GdbVarDlg.this.m_constExprA.setEnabled(false);
/* 549 */             GdbVarDlg.this.m_constExprB.setEnabled(false);
/*     */           }
/*     */           else {
/* 552 */             GdbVarDlg.this.ConstValA.setEnabled(false);
/* 553 */             GdbVarDlg.this.ConstValB.setEnabled(false);
/* 554 */             GdbVarDlg.this.m_constExprA.setEnabled(true);
/* 555 */             GdbVarDlg.this.m_constExprB.setEnabled(true);
/*     */           }
/*     */       }
/*     */     });
/* 561 */     this.AandB_Expr_Button.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 564 */         if (GdbVarDlg.this.m_varTable.getSelectedRowCount() > 0)
/* 565 */           if (GdbVarDlg.this.AandB_Const_Button.isSelected()) {
/* 566 */             GdbVarDlg.this.ConstValA.setEnabled(true);
/* 567 */             GdbVarDlg.this.ConstValB.setEnabled(true);
/* 568 */             GdbVarDlg.this.m_constExprA.setEnabled(false);
/* 569 */             GdbVarDlg.this.m_constExprB.setEnabled(false);
/*     */           }
/*     */           else {
/* 572 */             GdbVarDlg.this.ConstValA.setEnabled(false);
/* 573 */             GdbVarDlg.this.ConstValB.setEnabled(false);
/* 574 */             GdbVarDlg.this.m_constExprA.setEnabled(true);
/* 575 */             GdbVarDlg.this.m_constExprB.setEnabled(true);
/*     */           }
/*     */       }
/*     */     });
/* 583 */     int m = this.m_DlgHeigth - 100;
/* 584 */     this.m_varTable.setMinimumSize(new Dimension(this.m_DlgWidth - this.m_unselectAll.getMaximumSize().width - 30, m));
/* 585 */     this.m_varTable.setPreferredScrollableViewportSize(new Dimension(this.m_DlgWidth - this.m_unselectAll.getMaximumSize().width - 30, m));
/*     */ 
/* 587 */     JScrollPane localJScrollPane = new JScrollPane(this.m_varTable);
/*     */ 
/* 589 */     DrawVarHeaderPanel();
/*     */ 
/* 591 */     localJPanel5.add(this.m_varHeaderPanel, "North");
/* 592 */     localJPanel5.add(localJScrollPane, "Center");
/*     */ 
/* 594 */     this.m_varTable.getColumnModel().getColumn(2).setMaxWidth(70);
/* 595 */     this.m_varTable.getColumnModel().getColumn(3).setMaxWidth(90);
/* 596 */     this.m_varTable.getColumnModel().getColumn(3).setPreferredWidth(90);
/*     */ 
/* 598 */     getContentPane().add(localJPanel1, "North");
/* 599 */     getContentPane().add(localJPanel5, "Center");
/* 600 */     getContentPane().add(localJPanel4, "East");
/* 601 */     pack();
/*     */ 
/* 605 */     if (!m_lastExecutable.isEmpty()) {
/* 606 */       ExtractVariables(this.m_ExpandTableElements.isSelected());
/*     */     }
/*     */ 
/* 609 */     setLocationRelativeTo(m_parent);
/*     */   }
/*     */ 
/*     */   private void DrawVarHeaderPanel() {
/* 613 */     this.m_varHeaderPanel = new JPanel();
/*     */ 
/* 615 */     JPanel localJPanel = new JPanel();
/* 616 */     JLabel localJLabel1 = new JLabel("Show symbols containing ...");
/* 617 */     this.m_filterString.setPreferredSize(new Dimension(100, this.m_BtnHeight));
/* 618 */     this.m_filterString.addKeyListener(new KeyListener()
/*     */     {
/*     */       public void keyTyped(KeyEvent paramAnonymousKeyEvent)
/*     */       {
/*     */       }
/*     */ 
/*     */       public void keyPressed(KeyEvent paramAnonymousKeyEvent)
/*     */       {
/*     */       }
/*     */ 
/*     */       public void keyReleased(KeyEvent paramAnonymousKeyEvent)
/*     */       {
/* 630 */         GdbVarDlg.this.RefreshTable();
/*     */       }
/*     */     });
/* 634 */     JLabel localJLabel2 = new JLabel("  Match case");
/* 635 */     this.m_filterMatchCase = new JCheckBox("");
/* 636 */     this.m_filterMatchCase.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
/* 639 */         if (!GdbVarDlg.this.m_filterString.getText().isEmpty())
/*     */         {
/* 641 */           GdbVarDlg.this.RefreshTable();
/*     */         }
/*     */       }
/*     */     });
/* 646 */     localJPanel.add(localJLabel1);
/* 647 */     localJPanel.add(this.m_filterString);
/* 648 */     localJPanel.add(localJLabel2);
/* 649 */     localJPanel.add(this.m_filterMatchCase);
/*     */ 
/* 651 */     this.m_varHeaderPanel.setLayout(new BorderLayout());
/*     */ 
/* 653 */     String[] arrayOfString = { "Add variables to the display variables table", "Add variables to the write variables table" };
/* 654 */     if (!this.m_bBuildSymbolListOnly) {
/* 655 */       this.m_ImportTo = new JComboBox(arrayOfString);
/*     */ 
/* 657 */       this.m_varHeaderPanel.add(this.m_ImportTo, "Center");
/*     */     }
/* 659 */     this.m_varHeaderPanel.add(localJPanel, "South");
/*     */   }
/*     */ 
/*     */   public static String getExecutableFile()
/*     */   {
/* 664 */     return m_lastExecutable;
/*     */   }
/*     */ 
/*     */   public static void clear() {
/* 668 */     m_lastExecutable = "";
/* 669 */     m_symbolList.clear();
/*     */   }
/*     */ 
/*     */   public static ArrayList<String> getListOfSymbols() {
/* 673 */     return m_symbolList;
/*     */   }
/*     */ 
/*     */   private void setUserConfigDirty() {
/* 677 */     if (!this.m_bUserConfigDirtyOpening)
/*     */     {
/* 680 */       if ((m_lastExecutable.compareTo(m_execFilenameFromSettings) == 0) && (m_bStoreRelative == m_bStoreRelativeFromSettings))
/*     */       {
/* 683 */         m_parent.setDirty(false);
/*     */       }
/*     */       else
/* 686 */         m_parent.setDirty(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void saveSettings(Properties paramProperties, String paramString)
/*     */   {
/* 701 */     paramProperties.setProperty("GdbVar.Version", USER_SETTINGS_GDBVAR_VER + "");
/*     */ 
/* 704 */     String str = null;
/* 705 */     if (!m_lastExecutable.isEmpty()) {
/* 706 */       str = RelativePath.getRelativeFile(m_lastExecutable, paramString);
/*     */     }
/* 708 */     if ((str == null) || (!m_bStoreRelative))
/*     */     {
/* 710 */       paramProperties.setProperty("GdbVar.LastExec", m_lastExecutable);
/* 711 */       m_bStoreRelativeFromSettings = false;
/*     */     } else {
/* 713 */       paramProperties.setProperty("GdbVar.LastExecRelative", str);
/* 714 */       m_bStoreRelativeFromSettings = true;
/*     */     }
/* 716 */     m_execFilenameFromSettings = m_lastExecutable;
/*     */ 
/* 718 */     if (!m_lastExecutable.isEmpty())
/* 719 */       paramProperties.setProperty("GdbVar.ExecDate", Long.toString(getFileTime(m_lastExecutable)));
/*     */   }
/*     */ 
/*     */   public static void loadSettings(Properties paramProperties, String paramString)
/*     */   {
/* 728 */     String str1 = paramProperties.getProperty("GdbVar.Version", "0");
/* 729 */     int i = Integer.parseInt(str1);
/* 730 */     if (i > USER_SETTINGS_GDBVAR_VER)
/*     */     {
/* 732 */       JOptionPane.showMessageDialog(null, "Settings file was made with a more recent version of the software: the loading may fail");
/*     */     }
/* 734 */     if ((i > 0) && (i <= USER_SETTINGS_GDBVAR_VER))
/*     */     {
/* 736 */       Object localObject = paramProperties.getProperty("GdbVar.LastExecRelative", "null");
/* 737 */       if (((String)localObject).equals("null")) {
/* 738 */         localObject = paramProperties.getProperty("GdbVar.LastExec", "");
/* 739 */         if (!((String)localObject).isEmpty()) {
/* 740 */           m_bStoreRelativeFromSettings = false;
/*     */         }
/*     */         else
/* 743 */           m_bStoreRelativeFromSettings = true;
/*     */       }
/*     */       else
/*     */       {
/* 747 */         String str2 = RelativePath.getAbsolutePath((String)localObject, paramString);
/* 748 */         if (str2 == null) {
/* 749 */           str2 = "";
/*     */         }
/* 751 */         localObject = str2;
/* 752 */         m_bStoreRelativeFromSettings = true;
/*     */       }
/* 754 */       m_execFilenameFromSettings = (String)localObject;
/* 755 */       m_lastExecutable = (String)localObject;
/* 756 */       m_bStoreRelative = m_bStoreRelativeFromSettings;
/*     */     }
/*     */ 
/* 759 */     if (isExecutableNoLongerExist())
/* 760 */       JOptionPane.showMessageDialog(null, "Executable file: \"" + getExecutableFile() + "\" is not found");
/*     */   }
/*     */ 
/*     */   public static boolean isExecutableNoLongerExist()
/*     */   {
/* 772 */     boolean bool = false;
/* 773 */     if (!m_lastExecutable.isEmpty())
/*     */     {
/* 775 */       File localFile = new File(m_lastExecutable);
/* 776 */       bool = !localFile.exists();
/*     */     }
/* 778 */     return bool;
/*     */   }
/*     */ 
/*     */   public void setDestination(MainDlg.vartables paramvartables) {
/* 782 */     this.defaultDestinationTable = paramvartables;
/* 783 */     switch (15.$SwitchMap$com$st$stmstudio$main$MainDlg$vartables[this.defaultDestinationTable.ordinal()]) {
/*     */     case 1:
/* 785 */       this.m_ImportTo.setSelectedIndex(0);
/* 786 */       this.m_ImportTo.setEnabled(false);
/* 787 */       break;
/*     */     case 2:
/* 789 */       this.m_ImportTo.setSelectedIndex(1);
/* 790 */       this.m_ImportTo.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Date getExecutableFileDate()
/*     */   {
/* 796 */     Date localDate = null;
/* 797 */     if (!m_lastExecutable.isEmpty())
/*     */     {
/* 799 */       File localFile = new File(m_lastExecutable);
/* 800 */       if (localFile.exists())
/*     */       {
/* 802 */         Long localLong = Long.valueOf(localFile.lastModified());
/*     */ 
/* 805 */         localDate = new Date(localLong.longValue());
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 810 */     return localDate;
/*     */   }
/*     */ 
/*     */   public static GdbExec getGdbAccessor(MainDlg paramMainDlg) {
/* 814 */     GdbExec localGdbExec = null;
/* 815 */     if ((!isExecutableNoLongerExist()) && (!m_lastExecutable.isEmpty())) {
/* 816 */       m_parent = paramMainDlg;
/* 817 */       localGdbExec = new GdbExec(m_lastExecutable, getAddressOffset());
/* 818 */       if (!localGdbExec.isRunning()) {
/* 819 */         localGdbExec = null;
/*     */       }
/*     */     }
/* 822 */     return localGdbExec;
/*     */   }
/*     */ 
/*     */   private static long getFileTime(String paramString) {
/* 826 */     long l = 0L;
/* 827 */     if (!paramString.isEmpty())
/*     */     {
/* 829 */       File localFile = new File(paramString);
/* 830 */       if (localFile.exists())
/*     */       {
/* 832 */         l = localFile.lastModified();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 837 */     return l;
/*     */   }
/*     */ 
/*     */   private static long getAddressOffset() {
/* 841 */     long l = 0L;
/*     */ 
/* 848 */     return l;
/*     */   }
/*     */ 
/*     */   private void ExtractVariables(boolean paramBoolean)
/*     */   {
/* 853 */     Date localDate = new Date();
/*     */ 
/* 855 */     GdbExec localGdbExec = new GdbExec(getAddressOffset());
/*     */ 
/* 858 */     this.m_gdbVarList.clear();
/*     */ 
/* 860 */     if (localGdbExec.buildVarList(m_lastExecutable, paramBoolean) == true) {
/* 861 */       for (localObject = localGdbExec.getVarIterator(); ((Iterator)localObject).hasNext(); ) {
/* 862 */         GdbVar localGdbVar = (GdbVar)((Iterator)localObject).next();
/* 863 */         this.m_gdbVarList.add(localGdbVar);
/* 864 */         m_symbolList.add(localGdbVar.get_name());
/*     */       }
/*     */     }
/*     */ 
/* 868 */     RefreshTable();
/*     */ 
/* 870 */     Object localObject = new Date();
/* 871 */     System.out.println("\nExtractVariables Elapsed time: " + (((Date)localObject).getTime() - localDate.getTime()) + "ms\n");
/*     */   }
/*     */ 
/*     */   private void RefreshTable() {
/* 875 */     int i = this.m_variableTableModel.getRowCount();
/*     */ 
/* 877 */     for (int k = 0; k < i; k++) {
/* 878 */       this.m_variableTableModel.removeRow(0);
/*     */     }
/*     */ 
/* 881 */     for (Iterator localIterator = this.m_gdbVarList.iterator(); localIterator.hasNext(); ) {
/* 882 */       GdbVar localGdbVar = (GdbVar)localIterator.next();
/*     */ 
/* 884 */       int j = 1;
/* 885 */       if (!this.m_filterString.getText().isEmpty())
/*     */       {
/* 887 */         if (this.m_filterMatchCase.isSelected())
/*     */         {
/* 889 */           if (!localGdbVar.get_name().contains(this.m_filterString.getText().trim()))
/*     */           {
/* 891 */             j = 0;
/*     */           }
/*     */ 
/*     */         }
/* 895 */         else if (!localGdbVar.get_name().toUpperCase().contains(this.m_filterString.getText().toUpperCase().trim()))
/*     */         {
/* 897 */           j = 0;
/*     */         }
/*     */       }
/*     */ 
/* 901 */       if (j == 1) {
/* 902 */         ArrayList localArrayList = this.m_variableTableModel.createRow();
/* 903 */         localArrayList.set(0, localGdbVar.get_filename());
/* 904 */         localArrayList.set(1, localGdbVar.get_name());
/* 905 */         localArrayList.set(2, "0x" + Long.toHexString(localGdbVar.get_address()));
/* 906 */         localArrayList.set(3, AbstractVariable.getType(localGdbVar.get_type()));
/* 907 */         this.m_variableTableModel.addRow(localArrayList);
/*     */       }
/*     */     }
/* 910 */     if ((this.m_varTable.getRowCount() > 0) && (this.m_varTable.getSelectedRowCount() < this.m_varTable.getRowCount()))
/*     */     {
/* 912 */       this.m_selectAll.setEnabled(true);
/*     */     }
/* 914 */     else this.m_selectAll.setEnabled(false);
/*     */   }
/*     */ 
/*     */   class ElfExtension extends FileFilter
/*     */   {
/* 919 */     private final String[] elfFileExtensions = { "elf", "out", "axf" };
/*     */ 
/*     */     ElfExtension() {
/*     */     }
/*     */     public boolean accept(File paramFile) {
/* 924 */       if (paramFile.isDirectory()) {
/* 925 */         return true;
/*     */       }
/* 927 */       for (String str : this.elfFileExtensions) {
/* 928 */         if (paramFile.getName().toLowerCase().endsWith("." + str)) {
/* 929 */           return true;
/*     */         }
/*     */       }
/* 932 */       return false;
/*     */     }
/*     */ 
/*     */     public String getDescription()
/*     */     {
/* 937 */       String str1 = "Elf Files (.";
/* 938 */       int i = 0;
/* 939 */       for (String str2 : this.elfFileExtensions) {
/* 940 */         if (i == 0)
/* 941 */           str1 = str1 + str2;
/*     */         else {
/* 943 */           str1 = str1 + ", ." + str2;
/*     */         }
/* 945 */         i++;
/*     */       }
/* 947 */       str1 = str1 + ")";
/* 948 */       return str1;
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/mm/STMStudio.jar
 * Qualified Name:     com.st.stmstudio.gdb.GdbVarDlg
 * JD-Core Version:    0.6.2
 */