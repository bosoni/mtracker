/*
 * Tracker.java
 * by mjt, 2007
 * mixut@hotmail.com
 *
 */
package mtracker;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.JFileChooser;


class Instru
{
    String name=""; // samplen tiedostonimi
    boolean loop; // looppaako
}

public class SetInstrument extends javax.swing.JFrame
{
    static Instru instr[] = new Instru [SoundPlayer.BUFFERS];
    static String curDir=null;
    static String name=""; // track name
    
    static String saveSamples(String dta)
    {
	for(int q=0; q<SoundPlayer.BUFFERS; q++)
	{
	    if(instr[q]!=null)
	    {
		dta+=instr[q].name+"\n";
		dta+=(instr[q].loop==true) ? "1\n" : "0\n";
	    }
	    else
	    {
		dta+="null\n";
		dta+="0\n";
	    }
	}
	return dta;
    }
    
    // ladataan samplet
    static void loadSamples(String dta[], int pos[])
    {
	String tmp="";
	for(int q=0; q<SoundPlayer.BUFFERS; q++)
	{
	    tmp=dta[pos[0]++];
	    if(tmp.equals("null")==true)
	    {
		pos[0]++; // hypp�� yli
		continue;
	    }
	    
	    instr[q]=new Instru();
	    instr[q].name=tmp;
	    instr[q].loop = (Integer.parseInt(dta[pos[0]++])==0 ? false : true );
	    
	    if(instr[q]!=null)
		if(instr[q].name.equals("")==false)
		{
		    System.out.println("load: "+instr[q].name);
		    SoundPlayer.load(instr[q].name, q, instr[q].loop);
		}
	}
	
    }
    static public void copyFile(File in, File out) throws Exception
    {
	FileInputStream fis  = new FileInputStream(in);
	FileOutputStream fos = new FileOutputStream(out);
	byte[] buf = new byte[1024];
	int i = 0;
	while((i=fis.read(buf))!=-1)
	{
	    fos.write(buf, 0, i);
	}
	fis.close();
	fos.close();
    }
    
    
    void openDialog()
    {
	final Frame owner = this;
	
	// avataan dialogi jossa voidaan valita taustakuva
	JFileChooser chooser = new JFileChooser(curDir);
	int returnVal = chooser.showOpenDialog(owner);
	
	if (returnVal == JFileChooser.APPROVE_OPTION)
	{
	    // ota uusi hakemisto talteen
	    curDir=chooser.getSelectedFile().getPath();
	    jTextField1.setText( curDir );
	    instr[Tracker.curCol].name=curDir;
	}
	
    }
    
    /** Creates new form SetInstrument */
    public SetInstrument()
    {
	initComponents();
	setTitle("Set Instrument : "+name);
	
	if(instr[Tracker.curCol]!=null)
	{
	    // pist� asetukset n�kyviin
	    jTextField1.setText( instr[Tracker.curCol].name );
	    jCheckBox1.setSelected( instr[Tracker.curCol].loop );
	    repaint();
	}
	else
	    instr[Tracker.curCol]=new Instru();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Set Instrument");
        jButton1.setText("Load sample");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                jTextField1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                jTextField1KeyTyped(evt);
            }
        });

        jCheckBox1.setText("Looping");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jButton2.setText("OK");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Cancel");
        jButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(194, 194, 194)
                        .add(jButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton3))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jButton1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
                            .add(jCheckBox1))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(16, 16, 16)
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 34, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton3)
                    .add(jButton2))
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextField1KeyReleased
    {//GEN-HEADEREND:event_jTextField1KeyReleased
	// tiedostonimi field
	instr[Tracker.curCol].name = jTextField1.getText();
	
    }//GEN-LAST:event_jTextField1KeyReleased
    
    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextField1KeyTyped
    {//GEN-HEADEREND:event_jTextField1KeyTyped
	// tiedostonimi field
	instr[Tracker.curCol].name = jTextField1.getText();
    }//GEN-LAST:event_jTextField1KeyTyped
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton3ActionPerformed
    {//GEN-HEADEREND:event_jButton3ActionPerformed
	// cancel
	dispose();
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
	// OK
	// ladataan samplet
	for(int q=0; q<SoundPlayer.BUFFERS; q++)
	{
	    if(instr[q]!=null)
		if(instr[q].name.equals("")==false)
		{
		System.out.println("load: "+instr[q].name);
		
		SoundPlayer.load(instr[q].name, q, instr[q].loop);
		}
	}
	
	dispose();
	
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox1ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox1ActionPerformed
	// looppaako ?
	instr[Tracker.curCol].loop = jCheckBox1.isSelected();
	
    }//GEN-LAST:event_jCheckBox1ActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
	// load sample
	// avaa open dialogi
	
	openDialog();
	
    }//GEN-LAST:event_jButton1ActionPerformed
    
    public static void create(String trackName)
    {
	name=trackName;
	
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		SetInstrument i=new SetInstrument();
		i.setVisible(true);
	    }
	});
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
    
}
