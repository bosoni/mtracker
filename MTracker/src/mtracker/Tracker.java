/*
 * Tracker.java
 * by mjt, 2007
 * mixut@hotmail.com
 *
 */
package mtracker;

import javax.swing.JOptionPane;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Receiver;
import java.util.Vector;

public class Tracker extends Thread
{
    public static Tracker tracker=new Tracker();
    
    static Track curTrack=new Track();
    static Vector<Track> tracks=new Vector<Track>();
    
    public static int curRow=0, curCol=0, curPage=0, step=1;
    public static boolean playing=false, recording=false, recordAndPlay=false, changePage=false;
    public static int bpm=120;
    
    
    static String saveTracks(String dta)
    {
	for(int q=0; q<tracks.size(); q++)
	{
	    for(int row=0; row<64; row++)
	    {
		// page_num, row, col,   key, keynumber, pressure, pitch
		for(int ch=0; ch<SoundPlayer.BUFFERS; ch++)
		{
		    if(Tracker.tracks.get(q).notes[row][ch]!=null) // tallenna vain nuotit
		    {
			dta+=q+"\n";
			dta+=row+"\n";
			dta+=ch+"\n";
			
			Note tn=Tracker.tracks.get(q).notes[row][ch];
			dta+=tn.key+"\n";
			dta+=tn.keyNumber+"\n";
			dta+=tn.pressure+"\n";
			dta+=tn.pitch+"\n";
		    }
		}
	    }
	}
	return dta;
    }
    
    // ladataan samplet
    static void loadTracks(String dta[], int pos[])
    {
	String tmp="";
	
	int i=pos[0];
	int lastPage=0;
	
	while(true)
	{
	    if(i==dta.length) break;
	    
	    // page_num, row, col,   key, keynumber, pressure, pitch
	    int pg=Integer.parseInt(dta[i++]);
	    int row=Integer.parseInt(dta[i++]);
	    int col=Integer.parseInt(dta[i++]);
	    String key=dta[i++];
	    int keyNumber=Integer.parseInt(dta[i++]);
	    int pressure=Integer.parseInt(dta[i++]);
	    int pitch=Integer.parseInt(dta[i++]);
	    if(lastPage!=pg)
	    {
		curTrack=new Track();
		tracks.add(curTrack);
	    }
	    
	    curTrack.notes[row][col]=new Note();
	    curTrack.notes[row][col].key=key;
	    curTrack.notes[row][col].keyNumber=keyNumber;
	    curTrack.notes[row][col].pressure=pressure;
	    curTrack.notes[row][col].pitch=pitch;
	    
	    lastPage=pg;
	}

	curTrack=tracks.get(0);
	changePage(0, true);
		
    }
    
    /**
     * soittaa yhden vaakarivin
     */
    void playRow(int row)
    {
	// joka kanava läpi
	for(int ch=0; ch<SoundPlayer.BUFFERS; ch++)
	{
	    if(curTrack.notes[curRow][ch]!=null)
		SoundPlayer.play(ch, curTrack.notes[curRow][ch]);
	    
	    
	}
	
    }
    
    static boolean OK=true;
    
    // eri threadissa oleva looppi
    public void run()
    {
	float delay=0;
	int time=0, ctimes=0, cc=0;
	
	tracks.add(curTrack);
	curTrack=tracks.get(0);
	
	while(true)
	{
	    if(OK)
		if(playing || recording)
		{
		if(playing || recordAndPlay)
		{
		    playRow(curRow);
		}
		
		update();
		}
	    
	    try
	    {
		/*
		 * suoritetaan odotus 50ms osissa, tehdään se 50 kertaa ja
		 * sit ok=true jolloin voidaan päivittää muuta ohjelmaa.
		 *
		 * tämä siksi että jos sleeppiin pääsee turhan iso luku kun
		 * muuttelee bpm:ää, ohjelma vaan jää jumiin. nyt ei koska
		 * ok=true aina kun muutetaan bpm tai painetaan stop.
		 */
		
		
		// laske bpm
		if(OK)
		{
		    delay=60.0f / (float)bpm;
		    time=(int) (delay*1000.0f); // odotusaika
		    
		    ctimes=time/50;
		    
		    OK=false;
		}
		
		if(cc>=ctimes)
		{
		    OK=true;
		    cc=0;
		}
		cc++;
		Thread.sleep(50);
		
	    }
	    catch(InterruptedException e)
	    {
	    }
	}
    }
    
    /**
     * vaihda sivu. jos curPage ei ole vektorissa, laitetaan se sinne.
     * jos uusi sivu jo vektorissa, otetaan se muokattavaksi,
     * muuten luodaan uusi.
     *
     * prev==true jos otetaan edellinen (silloin ei luoda uutta tracksia)
     */
    static void changePage(int page, boolean prev)
    {
	if(prev==false) // eteenpäin
	{
	    if(tracks.size() <= curPage+1)
	    {
		curTrack=new Track(); // luodaan uusi
		
		tracks.add(curTrack); // tracksit talteen
	    }
	    else curTrack=tracks.get(page);
	}
	else // taaksepäin
	{
	    curTrack=tracks.get(page);
	}
	
	
	for(int q=0; q<64; q++)
	    for(int w=0; w<SoundPlayer.BUFFERS; w++)
	    {
	    if(curTrack.notes[q][w]!=null)
		MTrackerGUI.gui.getTable().setValueAt(curTrack.notes[q][w].key, q, w);
	    else
		MTrackerGUI.gui.getTable().setValueAt("", q, w);
	    }
	
	curPage=page;
	MTrackerGUI.gui.getTable().changeSelection(curRow, curCol, false, false);
	
    }
    
    public void update()
    {
	if(playing==true) curRow++; else curRow += step;
	
	if(curRow>=64) // seuraavalle sivulle?
	{
	    if(playing==true)
	    {
		curRow=0;
		if(curPage+1 >= tracks.size()) curPage=-1; // lopusta alkuun
		
		changePage(curPage+1, false);
	    }
	    else
	    {
		curRow-=64;
		if(changePage) // vaihdetaan sivu automaattisesti?
		{
		    changePage(curPage+1, false);

		}
	    }
	    
	}
	
	MTrackerGUI.gui.posText();
	
	MTrackerGUI.gui.getTable().changeSelection(curRow, curCol, false, false);
    }
    
    
    public void play()
    {
	MTrackerGUI.gui.updateRowCol();
	playing=true;
    }
    
    public void stopAll()
    {
	playing=false;
	recording=false;
    }
    
    public void record()
    {
	MTrackerGUI.gui.updateRowCol();
	recording=true;
    }
    
    public void add(Note note, int event)
    {
	MTrackerGUI.gui.updateRowCol();
	
	if(playing) return;
	
	if(curCol<0) curCol=0;
	
	if(event==0x90) // note on
	{
	    if(note.velocity>0) // note on
	    {
		MTrackerGUI.gui.getTable().setValueAt(note.key, curRow, curCol);
		curTrack.notes[curRow][curCol]=note;
		
		SoundPlayer.play(curCol, note);
	    }
	    
	}
    }
    
    
    /*
    case 0x80: // note off
    case 0x90: // note on
    case 0xd0: // key pressure
    case 0xe0: // pitch wheel change
     */
}



class Track
{
    Note notes[][] = new Note [64][SoundPlayer.BUFFERS];
    
}

class Note
{
    long time=0;
    String key="";
    int keyNumber=-1;
    int velocity=0;
    int pressure=0;
    int pitch=0;
    
    byte pressed=0; // 1==painettuna
    
    byte data=0;
    byte value=0;
    
    public void info()
    {
	System.out.println(""+time+" "+key+" "+velocity+" "+pressed+" "+data+" "+value+" "+pressure+" "+pitch);
    }
}

