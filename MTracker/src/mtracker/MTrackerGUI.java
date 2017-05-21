/*
 * MTrackerGUI.java
 * by mjt, 2007
 * mixut@hotmail.com
 *
 *
 */
/*
 * save/load, tiedostoformaatti:
 *
 * mtr  : tunniste, string
 * channels : montako kanavaa, int
 * bpm : int
 * ; BUFFERS kpl sampleja
 * name : string
 * looping : 0/1 (false/true)
 *
 * ; music data, joka nuotille:
 * page_num
 * row
 * col
 * key
 * keynumber
 * pressure
 * pitch
 *
 *
 */
package mtracker;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class MTrackerGUI extends javax.swing.JFrame
{
    private static String VERSION="v0.2";
    public static MTrackerGUI gui=null;
    static boolean updateRow=false;
    String curDir=null;
    
    void save(String fileName)
    {
	FileIO file = new FileIO();
	if (file.createFile(fileName) == false) return;
	String dataString = "mtr\n"+SoundPlayer.BUFFERS+"\n"+Tracker.bpm+"\n";
	
	dataString = SetInstrument.saveSamples(dataString);
	dataString = Tracker.saveTracks(dataString);
	
	file.writeFile(dataString); // tallenna datat
    }
    
    void load(String fileName)
    {
	newProject();
	
	FileIO in=new FileIO();
	in.openFile(fileName);
	
	String dataString = "";
	dataString = in.readFile(); // koko tiedosto dataStringiin
	String strs[]=dataString.split("\n"); // pilko
	
	int pos[]=new int [1];
	pos[0]=0;
	
	if(strs[pos[0]++].equals("mtr")==false)
	{
	    MessageBox("Wrong fileformat!");
	    return;
	}
	SoundPlayer.BUFFERS = Integer.parseInt(strs[pos[0]++]);
	Tracker.bpm = Integer.parseInt(strs[pos[0]++]);
	jTextField2.setText(""+Tracker.bpm);
	
	SetInstrument.loadSamples(strs, pos);
	Tracker.loadTracks(strs, pos);
	
    }
    
    void newProject()
    {
	clearTable();
	
	Tracker.curCol=0;
	Tracker.curRow=0;
	
	Tracker.curTrack.notes=null;
	Tracker.curTrack.notes=new Note [64][SoundPlayer.BUFFERS];
	
	Tracker.tracks.clear();
	Tracker.curTrack=new Track();
	Tracker.tracks.add(Tracker.curTrack);
	
	SetInstrument.instr=null;
	SetInstrument.instr=new Instru [SoundPlayer.BUFFERS];
	SoundPlayer.samples=new Sample [SoundPlayer.BUFFERS];
	for(int q=0; q<SoundPlayer.BUFFERS; q++) SoundPlayer.samples[q]=new Sample();
	
	jTextField2.setText("120");
	jLabel5.setText("0/0");
	jLabel6.setText("3"); // oktaavi
	jLabel7.setText("1"); // step
	
	jCheckBox1.setSelected(false);
	jCheckBox2.setSelected(false);
	jCheckBox3.setSelected(false);
    }
    
    void clearTable()
    {
	Object[][] ob=new Object[64][SoundPlayer.BUFFERS];
	String[] txt=new String [SoundPlayer.BUFFERS];
	for(int q=0; q<SoundPlayer.BUFFERS; q++)
	{
	    txt[q]="Trk"+q;
	}
	jTable1.setModel(new javax.swing.table.DefaultTableModel(ob, txt));
	jScrollPane1.setViewportView(jTable1);
	
	MyTableCellEditor editor=new MyTableCellEditor(jTextField2);
	for(int q=0; q<SoundPlayer.BUFFERS; q++)
	{
	    jTable1.getColumnModel().getColumn(q).setCellEditor(editor);
	}
	
    }
    
    void setNote(char key)
    {
	if(key=='+')
	{ Tracker.changePage(Tracker.curPage+1, false); return; }
	if(key=='-' && Tracker.curPage>0)
	{ Tracker.changePage(Tracker.curPage-1, true); return; }
	
	
	String notes="q2w3er5t6y7ui9o0p";
	
	Note note=new Note();
	note.velocity=0xFF;
	
	for(int q=0; q<notes.length(); q++)
	{
	    // poista nuotti
	    if(key==' ' || key==127) // 127==delete
	    {
		MTrackerGUI.gui.getTable().setValueAt("", Tracker.curRow, Tracker.curCol);
		Tracker.curTrack.notes[Tracker.curRow][Tracker.curCol]=null;
	    }
	    
	    if(key==notes.charAt(q))
	    {
		note.key = MidiInputDevice.keyNames[q%12];
		
		int octave=3;
		octave=Integer.parseInt( jLabel6.getText() );
		
		if(q>=12) note.key+=(octave+1);
		else note.key+=octave;
		
		note.keyNumber=q + octave*12;
		
		Tracker.tracker.add(note, 0x90);
		
		
		if(updateRow)
		{
		    Tracker.tracker.update();
		}
		
		break;
	    }
	}
	
    }
    
    void updateRowCol()
    {
	jLabel5.setText( Tracker.curPage + "/"+ (Tracker.tracks.size()-1) );
	
	Tracker.tracker.curRow=jTable1.getSelectedRow();
	Tracker.tracker.curCol=jTable1.getSelectedColumn();
	
	if(Tracker.tracker.curCol<0)
	{
	    Tracker.tracker.curCol=0;
	    jTable1.setEditingColumn(0);
	}
	
	if(Tracker.tracker.curRow<0)
	{
	    Tracker.tracker.curRow=0;
	    jTable1.setEditingRow(0);
	}
	
	jTable1.repaint();
	
	jLabel9.setText(""+Tracker.curRow+":"+Tracker.curCol);
	
    }
    
    void posText()
    {
	jLabel5.setText( Tracker.curPage + "/"+ (Tracker.tracks.size()-1) );
	jLabel9.setText(""+Tracker.curRow+":"+0);
    }
    
    
    static boolean init=false;
    /**
     * alkuasetukset
     */
    void setup()
    {
	if(init==true) return;
	
	MidiInputDevice.keyb=new MidiInputDevice();
	SelectDevice.create();
	
	// luo trackeri toiseen säikeeseen
	Tracker.tracker.start();
	
	//openal käyttöön
	if(SoundPlayer.initAL()==false)
	{
	    MessageBox("initAL() error!");
	    dispose();
	}
	
	try
	{
	    File dir = new File(".");
	    curDir= dir.getCanonicalPath();
	    SetInstrument.curDir = dir.getCanonicalPath();
	}
	catch(IOException e)
	{
	    System.out.println(e);
	}
	
	init=true;
    }
    
    public javax.swing.JTable getTable()
    {
	return jTable1;
    }
    
    /**
     * Creates new form MTrackerGUI
     */
    public MTrackerGUI()
    {
	initComponents();
	setup();
	
	clearTable();
	
        jTable1.changeSelection(Tracker.curRow, Tracker.curCol, false, false);
    }
    
    
    static void MessageBox(String str)
    {
	System.out.println(str);
	JOptionPane.showMessageDialog(null, str, "MTracker", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MTracker");
        setResizable(false);
        jScrollPane1.setFont(new java.awt.Font("Dialog", 0, 10));
        jScrollPane1.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                jScrollPane1KeyPressed(evt);
            }
        });

        jTable1.setBackground(new java.awt.Color(204, 204, 204));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setDragEnabled(true);
        jTable1.setEditingColumn(0);
        jTable1.setEditingRow(0);
        jTable1.setGridColor(new java.awt.Color(51, 51, 51));
        jTable1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
        {
            public void mouseDragged(java.awt.event.MouseEvent evt)
            {
                jTable1MouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt)
            {
                jTable1MouseMoved(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                jTable1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                jTable1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                jTable1KeyTyped(evt);
            }
        });
        jTable1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jTable1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                jTable1MouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                jTable1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                jTable1MouseReleased(evt);
            }
        });

        jScrollPane1.setViewportView(jTable1);

        jButton1.setText("Play");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Record");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Page");

        jButton3.setText("Stop");
        jButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel2.setText("BPM");

        jTextField2.setText("120");
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                jTextField2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                jTextField2KeyReleased(evt);
            }
        });

        jLabel3.setText("Octave");

        jLabel4.setText("Step");

        jCheckBox1.setText("Record and play");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox2.setText("Update row");
        jCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jCheckBox3.setText("Update page");
        jCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBox3ActionPerformed(evt);
            }
        });

        jLabel5.setText("0/0");

        jButton4.setText("-");
        jButton4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("+");
        jButton5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel6.setText("3");

        jLabel7.setText("1");

        jButton9.setText("-");
        jButton9.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("+");
        jButton10.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setText("-");
        jButton11.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("+");
        jButton12.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton12ActionPerformed(evt);
            }
        });

        jLabel8.setText("Position:");

        jLabel9.setText("0:0");

        jMenu1.setText("File");
        jMenuItem4.setText("New");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem4ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem4);

        jMenuItem5.setText("Open");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem5ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem5);

        jMenuItem6.setText("Save");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem6ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem6);

        jMenu1.add(jSeparator1);

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem1ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");
        jMenuItem2.setText("Select input device");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem2ActionPerformed(evt);
            }
        });

        jMenu2.add(jMenuItem2);

        jMenuItem7.setText("Set instrument");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem7ActionPerformed(evt);
            }
        });

        jMenu2.add(jMenuItem7);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");
        jMenuItem8.setText("Help");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem8ActionPerformed(evt);
            }
        });

        jMenu3.add(jMenuItem8);

        jMenuItem3.setText("About");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem3ActionPerformed(evt);
            }
        });

        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jButton1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jCheckBox1)
                                        .add(85, 85, 85))
                                    .add(layout.createSequentialGroup()
                                        .add(jCheckBox2)
                                        .add(112, 112, 112))
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel3)
                                            .add(jLabel1)
                                            .add(jLabel4))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 25, Short.MAX_VALUE)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(jLabel6)
                                                .add(jLabel7)))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                                .add(jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(jButton9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(jButton11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createSequentialGroup()
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(jButton5))
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                                .add(jButton12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(jButton10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                                    .add(layout.createSequentialGroup()
                                        .add(jCheckBox3)
                                        .add(106, 106, 106))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel2)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                                .add(14, 14, 14)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 297, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel9)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButton1)
                            .add(jButton3)
                            .add(jButton2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckBox1)
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel2)
                                    .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel1)
                                    .add(jLabel5))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel3)
                                    .add(jLabel6))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel4)
                                    .add(jLabel7)))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jButton9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jButton10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jButton11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jButton12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckBox2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckBox3)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jLabel9)))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton4ActionPerformed
    {//GEN-HEADEREND:event_jButton4ActionPerformed
	// edellinen sivu
	int page=0;
	String nums[]=jLabel5.getText().split("/");
	
	page=Integer.parseInt( nums[0] );
	if(page>0) Tracker.changePage(page-1, true);
	
	jLabel5.setText( Tracker.curPage + "/"+ (Tracker.tracks.size()-1) );
    }//GEN-LAST:event_jButton4ActionPerformed
    
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton5ActionPerformed
    {//GEN-HEADEREND:event_jButton5ActionPerformed
	// seuraava sivu
	int page=0;
	String nums[]=jLabel5.getText().split("/");
	
	page=Integer.parseInt( nums[0] );
	Tracker.changePage(page+1, false);
	
	jLabel5.setText( Tracker.curPage + "/"+ (Tracker.tracks.size()-1) );
    }//GEN-LAST:event_jButton5ActionPerformed
    
    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton11ActionPerformed
    {//GEN-HEADEREND:event_jButton11ActionPerformed
	// step pienemmäksi
	int step=1;
	step=Integer.parseInt( jLabel7.getText() );
	if(step>1) jLabel7.setText(""+ (step-1) );
	Tracker.step=Integer.parseInt( jLabel7.getText() );
	
    }//GEN-LAST:event_jButton11ActionPerformed
    
    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton12ActionPerformed
    {//GEN-HEADEREND:event_jButton12ActionPerformed
	// step add
	int step=1;
	step=Integer.parseInt( jLabel7.getText() );
	jLabel7.setText(""+ (step+1) );
	Tracker.step=Integer.parseInt( jLabel7.getText() );
	
    }//GEN-LAST:event_jButton12ActionPerformed
    
    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton9ActionPerformed
    {//GEN-HEADEREND:event_jButton9ActionPerformed
	// octave alaspäin
	int octave=3;
	octave=Integer.parseInt( jLabel6.getText() );
	if(octave>0) jLabel6.setText(""+ (octave-1) );
	
    }//GEN-LAST:event_jButton9ActionPerformed
    
    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton10ActionPerformed
    {//GEN-HEADEREND:event_jButton10ActionPerformed
	// octave ylöspäin
	int octave=3;
	octave=Integer.parseInt( jLabel6.getText() );
	jLabel6.setText(""+ (octave+1) );
    }//GEN-LAST:event_jButton10ActionPerformed
    
    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox3ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox3ActionPerformed
	// update page
	// vaihdetaanko sivua automaattisesti?
	Tracker.changePage=jCheckBox3.isSelected();
    }//GEN-LAST:event_jCheckBox3ActionPerformed
    
    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox2ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox2ActionPerformed
	// update row
	// jos ruksattu, niin kun painaa jotain näppäintä, mennään sit step määrä rivejä alaspäin
	updateRow=jCheckBox2.isSelected();
    }//GEN-LAST:event_jCheckBox2ActionPerformed
    
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox1ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox1ActionPerformed
	// record and play
	// jos ruksattu, soitetaan muut kanavat samalla kun tallennetaan
	Tracker.recordAndPlay=jCheckBox1.isSelected();
    }//GEN-LAST:event_jCheckBox1ActionPerformed
    
    private void jScrollPane1KeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jScrollPane1KeyPressed
    {//GEN-HEADEREND:event_jScrollPane1KeyPressed
    }//GEN-LAST:event_jScrollPane1KeyPressed
    
    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem6ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem6ActionPerformed
	// save
	final Frame owner = this;
	
	// avataan dialogi jossa voidaan valita taustakuva
	JFileChooser chooser = new JFileChooser(curDir);
	int returnVal = chooser.showSaveDialog(owner);
	
	if (returnVal == JFileChooser.APPROVE_OPTION)
	{
	    // ota uusi hakemisto talteen
	    curDir=chooser.getSelectedFile().getPath();
	    
	    save(curDir);
	}
	
	
    }//GEN-LAST:event_jMenuItem6ActionPerformed
    
    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem8ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem8ActionPerformed
	MessageBox("Lataa kanaville instrumentit ja ala soittamaan. Voit soittaa koskettimilla tai näppäimistöllä (qwert -> cdefg jne).");
    }//GEN-LAST:event_jMenuItem8ActionPerformed
    
    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem7ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem7ActionPerformed
	// set instrument
	// avataan uusi ikkuna jossa voi ladata instrumentit
	SetInstrument.create("Track"+Tracker.curCol);
	
    }//GEN-LAST:event_jMenuItem7ActionPerformed
    
    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem5ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem5ActionPerformed
	// load
	final Frame owner = this;
	
	// avataan dialogi jossa voidaan valita taustakuva
	JFileChooser chooser = new JFileChooser(curDir);
	int returnVal = chooser.showOpenDialog(owner);
	
	if (returnVal == JFileChooser.APPROVE_OPTION)
	{
	    // ota uusi hakemisto talteen
	    curDir=chooser.getSelectedFile().getPath();
	    
	    load(curDir);
	}
	
    }//GEN-LAST:event_jMenuItem5ActionPerformed
    
    
    
    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem4ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem4ActionPerformed
	// new
	newProject();
    }//GEN-LAST:event_jMenuItem4ActionPerformed
    
    private void jTextField2KeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextField2KeyPressed
    {//GEN-HEADEREND:event_jTextField2KeyPressed
	if(jTextField2.getText().equals("")==false) Tracker.bpm= Integer.parseInt( jTextField2.getText() );
	Tracker.OK=true;
    }//GEN-LAST:event_jTextField2KeyPressed
    
    private void jTable1KeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTable1KeyPressed
    {//GEN-HEADEREND:event_jTable1KeyPressed
	setNote( evt.getKeyChar() );
	
	updateRowCol();
    }//GEN-LAST:event_jTable1KeyPressed
    
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem2ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem2ActionPerformed
	SelectDevice.create();
    }//GEN-LAST:event_jMenuItem2ActionPerformed
    
    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem3ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem3ActionPerformed
	MessageBox("MTracker "+VERSION+" by mjt, 2007\nmixut@hotmail.com");
	
    }//GEN-LAST:event_jMenuItem3ActionPerformed
    
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
	MidiInputDevice.keyb.close();
	System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    
    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextField2KeyReleased
    {//GEN-HEADEREND:event_jTextField2KeyReleased
	if(jTextField2.getText().equals("")==false) Tracker.bpm= Integer.parseInt( jTextField2.getText() );
	Tracker.OK=true;
    }//GEN-LAST:event_jTextField2KeyReleased
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton3ActionPerformed
    {//GEN-HEADEREND:event_jButton3ActionPerformed
	Tracker.tracker.stopAll();
	Tracker.OK=true;
	
	for(int q=0; q<SoundPlayer.BUFFERS; q++)
	{
	    SoundPlayer.stop(q);
	}
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void jTable1MouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MouseEntered
    {//GEN-HEADEREND:event_jTable1MouseEntered
						    }//GEN-LAST:event_jTable1MouseEntered
    
    private void jTable1MouseMoved(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MouseMoved
    {//GEN-HEADEREND:event_jTable1MouseMoved
						    }//GEN-LAST:event_jTable1MouseMoved
    
    private void jTable1KeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTable1KeyReleased
    {//GEN-HEADEREND:event_jTable1KeyReleased
	updateRowCol();
	    }//GEN-LAST:event_jTable1KeyReleased
    
    private void jTable1KeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTable1KeyTyped
    {//GEN-HEADEREND:event_jTable1KeyTyped
		    }//GEN-LAST:event_jTable1KeyTyped
    
    private void jTable1MouseDragged(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MouseDragged
    {//GEN-HEADEREND:event_jTable1MouseDragged
						    }//GEN-LAST:event_jTable1MouseDragged
    
    private void jTable1MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MouseReleased
    {//GEN-HEADEREND:event_jTable1MouseReleased
						    }//GEN-LAST:event_jTable1MouseReleased
    
    private void jTable1MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MousePressed
    {//GEN-HEADEREND:event_jTable1MousePressed
	updateRowCol();
		    }//GEN-LAST:event_jTable1MousePressed
    
    private void jTable1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MouseClicked
    {//GEN-HEADEREND:event_jTable1MouseClicked
	updateRowCol();
		    }//GEN-LAST:event_jTable1MouseClicked
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
	Tracker.tracker.record();
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
	Tracker.tracker.play();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		gui=new MTrackerGUI();
		gui.setVisible(true);
	    }
	});
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}

class MyTableCellEditor extends DefaultCellEditor
{
    public MyTableCellEditor(JTextField textField)
    {
	super(textField);
    }
    public boolean isCellEditable(EventObject e)
    {
	return false;
    }
    public boolean shouldSelectCell()
    {
	return true;
    }
}
