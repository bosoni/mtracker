/*
 * SoundPlayer.java
 * by mjt, 2007
 * mixut@hotmail.com
 *
 */
// http://jeanjacques.dialo.free.fr/frequenc.htm

package mtracker;

import java.nio.ByteBuffer;
import java.util.Random;
import net.java.games.joal.AL;
import net.java.games.joal.ALException;
import net.java.games.joal.ALFactory;
import net.java.games.joal.util.ALut;

public class SoundPlayer
{
    static private AL al;
    
    static int BUFFERS=8; // montako kanavaa on, montako instrumentti‰ voidaan ladata
    
    static Sample[] samples=new Sample [BUFFERS];
    
    // Buffers hold sound data.
    static private int[] buffers = new int[BUFFERS];
    
    // Sources are points emitting sound.
    static private int[] sources = new int[BUFFERS];
    
    // source
    static private float[] sourcePos = { 0.01f, 0.01f, 0.01f }; // jos 0,0,0 niin ‰‰nt‰ ei kuulunut winkussa
    static private float[] sourceVel = { 0.0f, 0.0f, 0.0f };
    
    // listener
    static private float[] listenerPos = { 0.0f, 0.0f, 0.0f };
    static private float[] listenerVel = { 0.0f, 0.0f, 0.0f };
    static private float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
    
    static private boolean initialized = false;
    static boolean initAL()
    {
        if (initialized)
        {
            return true;
        }
        
        // Initialize OpenAL and clear the error bit.
        try
        {
            ALut.alutInit();
            al = ALFactory.getAL();
            al.alGetError();
        }
        catch (ALException e)
        {
            e.printStackTrace();
            return false;
        }
        
        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
        al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
        
        // bind buffers into audio sources
        al.alGenSources(BUFFERS, sources, 0);
        
        al.alGenBuffers(BUFFERS, buffers, 0);
        if (al.alGetError() != AL.AL_NO_ERROR)
        {
            return false;
        }
        
        for(int q=0; q<BUFFERS; q++) samples[q]=new Sample();
        
        initialized = true;
        return true;
    }
    
    static void destroy()
    {
        al.alDeleteBuffers(BUFFERS, buffers, 0);
        al.alDeleteSources(BUFFERS, sources, 0);
    }
    
    static void makeBuffer(int channel)
    {
        al.alBufferData(buffers[channel], samples[channel].format[0], samples[channel].data[0], samples[channel].size[0], samples[channel].freq[0]);
    }
    
    static boolean load(String fileName, int channel, boolean loop)
    {
        al.alSourcei(sources[channel], al.AL_BUFFER, al.AL_NONE);
        
        // load wav data into buffers
        samples[channel].fileName=fileName;
        ALut.alutLoadWAVFile(fileName, samples[channel].format, samples[channel].data, samples[channel].size, samples[channel].freq, samples[channel].loop);
        makeBuffer(channel);
        
        al.alSourcei(sources[channel], AL.AL_BUFFER, buffers[channel]);
        al.alSourcef(sources[channel], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[channel], AL.AL_GAIN, 1.0f);
        al.alSourcefv(sources[channel], AL.AL_POSITION, sourcePos, 0);
        al.alSourcefv(sources[channel], AL.AL_VELOCITY, sourceVel, 0);
        al.alSourcei(sources[channel], AL.AL_LOOPING, (loop==true) ? AL.AL_TRUE : AL.AL_FALSE);
        
        
        // do another error check and return
        if (al.alGetError() != AL.AL_NO_ERROR)
        {
            return false;
        }
        
        return true;
    }
    
    static void play(int channel, Note note)
    {
        if(samples[channel]==null ||
                samples[channel].fileName==null ||
                samples[channel].fileName.equals("")) return; // ei ladattu samplea
        
        // nuottien taajuudet
        float hz[]={ 32.7f, 34.6f, 36.7f, 38.9f, 41.2f, 43.6f, 46.2f, 49.0f, 51.9f, 55.0f, 58.0f, 62.0f, 65f };
        float mul[]={
            1f/16f,
            1f/4f,
            1f/2f,
            1,2,4,8,16,32,64};
        
        int key=note.keyNumber%12;
        int oct=note.keyNumber/12; // oktaavi
        
        int freq =(int) ( (float)hz[key]/(float)32.5f  * (float)samples[channel].freq[0]);
        freq =(int) ( (float)freq * mul[oct] );
        
        stop(channel);
        al.alSourcei(buffers[channel], al.AL_BUFFER, al.AL_NONE);
        al.alSourcei(sources[channel], al.AL_BUFFER, al.AL_NONE);
        
        al.alBufferData(buffers[channel], samples[channel].format[0], samples[channel].data[0], samples[channel].size[0],  freq);
        al.alSourcei(sources[channel], AL.AL_BUFFER, buffers[channel]);
        
        al.alSourcePlay(sources[channel]);
    }
    
    static void play(int channel)
    {
        al.alSourcePlay(sources[channel]);
    }
    
    static void stop(int channel)
    {
        al.alSourceStop(sources[channel]);
    }
    
    static void pause(int channel)
    {
        al.alSourcePause(sources[channel]);
    }
    
    static int channelState(int channel)
    {
        int[] state = new int[1];
        al.alGetSourcei(sources[channel], AL.AL_SOURCE_STATE, state, 0);
        return state[0]; // esim. AL.AL_PLAYING
    }
    
    
}

class Sample
{
    String fileName="";
    
    int[] format = new int[1];
    int[] size = new int[1];
    int[] freq = new int[1];
    int[] loop = new int[1];
    ByteBuffer[] data = new ByteBuffer[1];
    
    
    
}
