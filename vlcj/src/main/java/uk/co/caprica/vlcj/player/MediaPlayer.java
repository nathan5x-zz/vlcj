/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2009, 2010 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.player;

import java.awt.Canvas;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_callback_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_event_e;
import uk.co.caprica.vlcj.binding.internal.libvlc_event_manager_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_event_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_player_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_meta_t;
import uk.co.caprica.vlcj.binding.internal.media_duration_changed;
import uk.co.caprica.vlcj.binding.internal.media_player_length_changed;
import uk.co.caprica.vlcj.binding.internal.media_player_position_changed;
import uk.co.caprica.vlcj.binding.internal.media_player_time_changed;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Simple media player implementation.
 * <p>
 * A more useful implementation will implement chapter and volume controls and 
 * so on.
 */
public abstract class MediaPlayer {

  private static final int VOUT_WAIT_PERIOD = 1000;
  
  protected final LibVlc libvlc = LibVlc.SYNC_INSTANCE;

  private final List<MediaPlayerEventListener> eventListenerList = new ArrayList<MediaPlayerEventListener>();

  private final ExecutorService listenersService = Executors.newSingleThreadExecutor();

  private final ExecutorService metaService = Executors.newSingleThreadExecutor();
  
  private final String[] args;

  private libvlc_instance_t instance;
  private libvlc_media_player_t mediaPlayerInstance;
  private libvlc_event_manager_t mediaPlayerEventManager;
  private libvlc_callback_t callback;

  private Canvas videoSurface;

  private volatile boolean released;

  /**
   * Create a new media player.
   * 
   * @param args arguments to pass to the native player
   */
  public MediaPlayer(String[] args) {
    this.args = args;
    createInstance();
  }

  public void addMediaPlayerEventListener(MediaPlayerEventListener listener) {
    eventListenerList.add(listener);
  }

  public void removeMediaPlayerEventListener(MediaPlayerEventListener listener) {
    eventListenerList.remove(listener);
  }

  public void setVideoSurface(Canvas videoSurface) {
    this.videoSurface = videoSurface;
  }

  public void playMedia(String media) {
    if(videoSurface == null) {
      throw new IllegalStateException("Must set a video surface");
    }

    // It is now OS-dependant as to how the video surface should be set
    nativeSetVideoSurface(mediaPlayerInstance, videoSurface);

    setMedia(media);

    play();
  }

  public long getLength() {
    long result = libvlc.libvlc_media_player_get_length(mediaPlayerInstance);
    if(result != -1) {
      
    }
    return result;
  }
  
  public long getTime() {
    long result = libvlc.libvlc_media_player_get_time(mediaPlayerInstance);
    if(result != -1) {
      
    }
    return result;
  }
  
  // === Playback Controls ====================================================
  
  public void play() {
    int result = libvlc.libvlc_media_player_play(mediaPlayerInstance);
    if(result != 0) {
      
    }
  }

  public void stop() {
    libvlc.libvlc_media_player_stop(mediaPlayerInstance);
  }

  public void pause() {
    libvlc.libvlc_media_player_pause(mediaPlayerInstance);
  }

  // === Audio Controls =======================================================

  public void mute() {
    libvlc.libvlc_audio_toggle_mute(mediaPlayerInstance);
  }
  
  public void mute(boolean mute) {
    libvlc.libvlc_audio_set_mute(mediaPlayerInstance, mute ? 1 : 0);
  }
  
  public boolean isMute() {
    int result = libvlc.libvlc_audio_get_mute(mediaPlayerInstance);
    return result != 0;
  }
  
  public int getVolume() {
    int result = libvlc.libvlc_audio_get_volume(mediaPlayerInstance);
    return result;
  }
  
  public void setVolume(int volume) {
    int result = libvlc.libvlc_audio_set_volume(mediaPlayerInstance, volume);
    if(result != 0) {
      
    }
  }
  
  // === Chapter Controls =====================================================

  public int getChapterCount() {
    int result = libvlc.libvlc_media_player_get_chapter_count(mediaPlayerInstance);
    return result;
  }
  
  public int getChapter() {
    int result = libvlc.libvlc_media_player_get_chapter(mediaPlayerInstance);
    return result;
  }
  
  public void setChapter(int chapterNumber) {
    libvlc.libvlc_media_player_set_chapter(mediaPlayerInstance, chapterNumber);
  }
  
  public void nextChapter() {
    libvlc.libvlc_media_player_next_chapter(mediaPlayerInstance);
  }
  
  public void previousChapter() {
    libvlc.libvlc_media_player_previous_chapter(mediaPlayerInstance);
  }
  
  // === Sub-Picture/Sub-Title Controls =======================================
  
  public int getSpuCount() {
    int result = libvlc.libvlc_video_get_spu_count(mediaPlayerInstance);
    return result;
  }
  
  public int getSpu() {
    int result = libvlc.libvlc_video_get_spu(mediaPlayerInstance);
    if(result != -1) {
    }
    return result;
  }
  
  public void setSpu(int spu) {
    int result = libvlc.libvlc_video_set_spu(mediaPlayerInstance, spu);
    if(result != 0) {
    }
  }
  
  // === Snapshot Controls ====================================================
  
  public void saveSnapshot() {
    File snapshotDirectory = new File(System.getProperty("user.home"));
    File snapshotFile = new File(snapshotDirectory, "vlcj-snapshot-" + System.currentTimeMillis() + ".png");
    int result = libvlc.libvlc_video_take_snapshot(mediaPlayerInstance, 0, snapshotFile.getAbsolutePath(), 0, 0);
    if(result != 0) {
      
    }
  }
  
  /**
   * Create and prepare the native media player resources.
   */
  private void createInstance() {
    instance = libvlc.libvlc_new(args.length, args);

    mediaPlayerInstance = libvlc.libvlc_media_player_new(instance);

    mediaPlayerEventManager = libvlc.libvlc_media_player_event_manager(mediaPlayerInstance);

    registerEventListener();
    
    eventListenerList.add(new MetaDataEventHandler());
  }

  /**
   * Clean up the native media player resources.
   */
  private void destroyInstance() {
    deregisterEventListener();

    eventListenerList.clear();
    
    if(mediaPlayerEventManager != null) {
      mediaPlayerEventManager = null;
    }

    if(mediaPlayerInstance != null) {
      libvlc.libvlc_media_player_release(mediaPlayerInstance);
      mediaPlayerInstance = null;
    }

    if(instance != null) {
      libvlc.libvlc_release(instance);
      instance = null;
    }
    
    listenersService.shutdown();
    
    metaService.shutdown();
  }

  private void registerEventListener() {
    callback = new VlcVideoPlayerCallback();

    for(libvlc_event_e event : libvlc_event_e.values()) {
      int result = libvlc.libvlc_event_attach(mediaPlayerEventManager, event.intValue(), callback, null);
      if(result == 0) {
      }
      else {
      }
    }
  }

  private void deregisterEventListener() {
    if(callback != null) {
      for(libvlc_event_e event : libvlc_event_e.values()) {
        libvlc.libvlc_event_detach(mediaPlayerEventManager, event.intValue(), callback, null);
      }

      callback = null;
    }
  }

  private void setMedia(String media) {
    libvlc_media_t mediaDescriptor = libvlc.libvlc_media_new_path(instance, media);
    libvlc.libvlc_media_parse(mediaDescriptor);
    
    libvlc_meta_t[] metas = libvlc_meta_t.values();
    
    for(libvlc_meta_t meta : metas) {
      System.out.println("meta=" + libvlc.libvlc_media_get_meta(mediaDescriptor, meta.intValue()));
    }
    
    long duration = libvlc.libvlc_media_get_duration(mediaDescriptor);
    System.out.println("duration=" + duration);
    
    libvlc.libvlc_media_player_set_media(mediaPlayerInstance, mediaDescriptor);
    libvlc.libvlc_media_release(mediaDescriptor);
  }

  private Dimension getVideoDimension() {
    IntByReference px = new IntByReference();
    IntByReference py = new IntByReference();
    int result = libvlc.libvlc_video_get_size(mediaPlayerInstance, 0, px, py);
    // TODO I think libvlc has this backwards!!! so i'll swap
    return new Dimension(py.getValue(), px.getValue());
  }
  
  private boolean hasVideoOut() {
    int hasVideoOut = libvlc.libvlc_media_player_has_vout(mediaPlayerInstance);
    return hasVideoOut != 0;
  }
  
  public void release() {
    destroyInstance();
    released = true;
  }

  @Override
  protected synchronized void finalize() throws Throwable {
    if(!released) {
      release();
    }
  }

  private void notifyListeners(libvlc_event_t event) {
    if(!eventListenerList.isEmpty()) {
      for(int i = eventListenerList.size() - 1; i >= 0; i--) {
        MediaPlayerEventListener listener = eventListenerList.get(i);
        int eventType = event.type;
//        System.out.println("eventType: " + eventType + " -> " + libvlc_event_e.event(eventType));
        
        switch(libvlc_event_e.event(eventType)) {

          case libvlc_MediaDurationChanged:
            long newDuration = ((media_duration_changed)event.u.getTypedValue(media_duration_changed.class)).new_duration;
//            listener.durationChanged(this, newDuration);
            break;
        
          case libvlc_MediaPlayerPlaying:
            listener.playing(this);
            break;
        
          case libvlc_MediaPlayerPaused:
            System.out.println("video dimension: " + getVideoDimension()); 
            listener.paused(this);
            break;
        
          case libvlc_MediaPlayerStopped:
            listener.stopped(this);
            break;
        
          case libvlc_MediaPlayerEndReached:
            listener.finished(this);
            break;
        
          case libvlc_MediaPlayerTimeChanged:
            long newTime = ((media_player_time_changed)event.u.getTypedValue(media_player_time_changed.class)).new_time;
            listener.timeChanged(this, newTime);
            break;

          case libvlc_MediaPlayerPositionChanged:
            float newPosition = ((media_player_position_changed)event.u.getTypedValue(media_player_position_changed.class)).new_position;
            listener.positionChanged(this, newPosition);
            break;
            
          case libvlc_MediaPlayerLengthChanged:
            long newLength = ((media_player_length_changed)event.u.getTypedValue(media_player_length_changed.class)).new_length;
            listener.lengthChanged(this, newLength);
            break;
        }
      }
    }
  }

  private void notifyListeners(VideoMetaData videoMetaData) {
    if(!eventListenerList.isEmpty()) {
      for(int i = eventListenerList.size() - 1; i >= 0; i--) {
        MediaPlayerEventListener listener = eventListenerList.get(i);
        listener.metaDataAvailable(this, videoMetaData);
      }
    }
  }

  private final class VlcVideoPlayerCallback implements libvlc_callback_t {

    public void callback(libvlc_event_t event, Pointer userData) {
      // Notify listeners in a different thread - the other thread is
      // necessary to prevent a potential native library failure if the
      // native library is re-entered
      if(!eventListenerList.isEmpty()) {
        listenersService.submit(new NotifyListenersRunnable(event));
      }
    }
  }

  private final class NotifyListenersRunnable implements Runnable {

    private final libvlc_event_t event;

    private NotifyListenersRunnable(libvlc_event_t event) {
      this.event = event;
    }

    @Override
    public void run() {
      notifyListeners(event);
    }
  }
  
  /**
   * With vlc, the meta data is not available until after the video output has
   * started.
   * <p>
   * Note that simply using the listener and handling the playing event will
   * <strong>not</strong> work.
   * <p>
   * This implementation loops, sleeping and checking, until libvlc reports that
   * video output is available.
   * <p>
   * This seems to be quite reliable but <strong>not</strong> 100% - on some
   * occasions the event seems not to fire. 
   */
  private final class NotifyMetaRunnable implements Runnable {

    @Override
    public void run() {
      for(;;) {
        try {
          Thread.sleep(VOUT_WAIT_PERIOD);

          if(hasVideoOut()) {
            VideoMetaData videoMetaData = new VideoMetaData();
            videoMetaData.setVideoDimension(getVideoDimension());
            videoMetaData.setSpuCount(getSpuCount());
            
            notifyListeners(videoMetaData);
            
            break;
          }
        }
        catch(InterruptedException e) {
        }
      }
    }
  }

  /**
   * Event listener implementation that waits for video "playing" events.
   */
  private final class MetaDataEventHandler extends MediaPlayerEventAdapter {

    @Override
    public void playing(MediaPlayer mediaPlayer) {
      // Kick off an asynchronous task to obtain the video meta data (when
      // available)
      metaService.submit(new NotifyMetaRunnable());
    }
  }
  
  /**
   * Template method.
   * 
   * @param instance media player instance
   * @param videoSurface video surface component
   */
  protected abstract void nativeSetVideoSurface(libvlc_media_player_t instance, Canvas videoSurface);
}