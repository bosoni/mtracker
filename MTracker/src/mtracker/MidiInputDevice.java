/*
 * MidiInputDevice.java
 * mixut@hotmail.com
 *
 * midi laitteen käsittely -luokka
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

class MidiInputDevice implements Receiver
{
    static MidiInputDevice keyb=null;
    
    // laitteiden tiedot
    MidiDevice.Info[] mdi = MidiSystem.getMidiDeviceInfo();
    MidiDevice inputDevice=null;
    
    public void create(int in)
    {
	try
	{
	    MidiDevice.Info info = mdi[in];
	    if(info == null)
	    {
		MessageBox("Device not found: " + in, "Error");
		return;
	    }
	    inputDevice = MidiSystem.getMidiDevice(info);
	    if(inputDevice==null) return;
	    
	    inputDevice.open();
	    
	    Receiver r = new MidiInputDevice();
	    Transmitter	t = inputDevice.getTransmitter();
	    t.setReceiver(r);
	    
	}
	catch(Exception e)
	{
	    close();
	    e.printStackTrace();
	}
	
    }
    
    public void close()
    {
	try
	{
	    if(inputDevice!=null) inputDevice.close();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
    
    
    static void MessageBox(String msg, String title)
    {
	if (msg.equals("")) return;
	JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
   
    //-----------------------------
    static final String[] keyNames = {"C ", "C#", "D ", "D#", "E ", "F ", "F#", "G ", "G#", "A ", "A#", "H "};
    private static long startTime=0;
    
    public void send(MidiMessage msg, long timeStamp)
    {
	Note note=new Note();
	
	if(startTime==0) startTime=timeStamp;
	
	note.time=timeStamp-startTime;
	
	ShortMessage message=(ShortMessage)msg;
	switch(message.getCommand())
	{
	    case 0x80: // note off
		note.key=getKeyName(message.getData1());
		note.keyNumber=message.getData1();
		note.pressed=0;
		break;
		
	    case 0x90: // note on
		note.key=getKeyName(message.getData1());
		note.keyNumber=message.getData1();
		note.velocity= message.getData2();
		note.pressed=1;
		
		break;
		
	    case 0xd0: // key pressure
		note.key=getKeyName(message.getData1());
		note.keyNumber=message.getData1();
		note.pressure = message.getData2();
		break;
		
	    case 0xe0: // pitch wheel change
		note.pitch = get14bitValue(message.getData1(), message.getData2());
		break;
		
	}
	
	Tracker.tracker.add(note, message.getCommand());
    }
    
    public static int get14bitValue(int nLowerPart, int nHigherPart)
    {
	return (nLowerPart & 0x7F) | ((nHigherPart & 0x7F) << 7);
    }
    
    public static String getKeyName(int nKeyNumber)
    {
	return keyNames[nKeyNumber % 12] + ((nKeyNumber / 12)- 1); // nuotti + oktaavi
    }
    
}
